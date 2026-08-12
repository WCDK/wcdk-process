package com.wcdk.process.behavior.impl;

import com.wcdk.process.behavior.ServiceTaskDelegate;
import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.exception.ProcessEngineException;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.ExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 服务任务行为，支持 Spring Bean 委托与 delegateExpression。
 *
 * @author WCDK
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class ServiceTaskBehavior implements ReactiveNodeBehavior {
    private final ExecutionRepository executionRepository;
    private final ReactiveAgenda agenda;
    private final ApplicationContext applicationContext;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();
        var node = graph.getNode(execution.getNodeId());
        String delegateName = node.getStringProperty("delegateExpression", null);
        if (delegateName == null) delegateName = node.getStringProperty("class", null);
        if (delegateName == null) delegateName = node.getStringProperty("delegate", null);
        if (delegateName == null) {
            return Mono.error(new ProcessEngineException("Service Task delegate not configured: " + node.getId()));
        }
        if (delegateName.startsWith("${") && delegateName.endsWith("}")) {
            delegateName = delegateName.substring(2, delegateName.length() - 1).trim();
        }
        final String beanName = delegateName;
        execution.setState(ExecutionState.ACTIVE.name());
        execution.setUpdatedAt(Instant.now());
        return executionRepository.updateById(execution)
                .then(resolveDelegate(beanName)
                        .flatMap(delegate -> delegate.execute(context)))
                .then(Mono.defer(() -> continueFlow(context)));
    }

    private Mono<ServiceTaskDelegate> resolveDelegate(String beanName) {
        try {
            return Mono.just((ServiceTaskDelegate) applicationContext.getBean(beanName));
        } catch (Exception ignored) {
            try {
                return Mono.just((ServiceTaskDelegate) applicationContext.getBean(Class.forName(beanName)));
            } catch (Exception error) {
                return Mono.error(new ProcessEngineException("Service Task delegate not found: " + beanName, error));
            }
        }
    }

    private Mono<Void> continueFlow(ExecutionContext context) {
        var edges = context.getProcessGraph().getOutgoingEdges(context.getExecutionId());
        if (edges.isEmpty()) agenda.planEndProcess(context.getExecutionId());
        else edges.forEach(edge -> agenda.planTakeSequenceFlow(context.getExecutionId(), edge.getId()));
        return Mono.empty();
    }
}
