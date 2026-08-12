package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.entity.JoinStateEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.execution.EdgeDefinition;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.JoinStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * 并行网关行为，负责并行分流与持久化汇流计数。
 *
 * @author WCDK
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ParallelGatewayBehavior implements ReactiveNodeBehavior {
    private final ExecutionRepository executionRepository;
    private final JoinStateRepository joinStateRepository;
    private final ReactiveAgenda agenda;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();
        var node = graph.getNode(execution.getNodeId());
        var incoming = node.getIncomingEdges().size();
        var outgoing = graph.getOutgoingEdges(node.getId());
        if (incoming > 1) return join(context, incoming, outgoing);
        if (outgoing.isEmpty()) return Mono.empty();
        execution.setState(ExecutionState.WAITING.name());
        return executionRepository.updateById(execution)
                .thenMany(reactor.core.publisher.Flux.fromIterable(outgoing))
                .flatMap(edge -> {
                    ExecutionEntity branch = copy(execution, edge.getTargetNodeId());
                    return executionRepository.insert(branch).doOnSuccess(saved ->
                            agenda.planContinueProcessInNode(saved.getId(), saved.getNodeId()));
                }).then();
    }

    private Mono<Void> join(ExecutionContext context, int expected, java.util.List<EdgeDefinition> outgoing) {
        ExecutionEntity execution = context.getExecution();
        execution.setState(ExecutionState.COMPLETED.name());
        execution.setUpdatedAt(Instant.now());
        return executionRepository.updateById(execution)
                .then(joinStateRepository.findByProcessInstanceIdAndGatewayIdAndCycleKey(
                        execution.getProcessInstanceId(), execution.getNodeId(), "default"))
                .switchIfEmpty(Mono.defer(() -> {
                    JoinStateEntity state = new JoinStateEntity();
                    state.setId(UUID.randomUUID().toString());
                    state.setTenantId(execution.getTenantId());
                    state.setProcessInstanceId(execution.getProcessInstanceId());
                    state.setScopeExecutionId(execution.getScopeExecutionId());
                    state.setGatewayId(execution.getNodeId());
                    state.setCycleKey("default");
                    state.setExpectedCount(expected);
                    state.setArrivedCount(0);
                    state.setStatus("WAITING");
                    state.setRevision(1L);
                    state.setCreatedAt(Instant.now());
                    state.setUpdatedAt(Instant.now());
                    return joinStateRepository.insert(state);
                }))
                .flatMap(state -> {
                    state.setArrivedCount(state.getArrivedCount() + 1);
                    state.setUpdatedAt(Instant.now());
                    if (state.getArrivedCount() < state.getExpectedCount()) return joinStateRepository.updateById(state).then();
                    state.setStatus("COMPLETED");
                    return joinStateRepository.updateById(state).thenMany(reactor.core.publisher.Flux.fromIterable(outgoing))
                            .doOnNext(edge -> agenda.planTakeSequenceFlow(execution.getId(), edge.getId())).then();
                });
    }

    private ExecutionEntity copy(ExecutionEntity source, String nodeId) {
        ExecutionEntity e = new ExecutionEntity();
        e.setId(UUID.randomUUID().toString()); e.setTenantId(source.getTenantId());
        e.setProcessInstanceId(source.getProcessInstanceId()); e.setProcessDefinitionId(source.getProcessDefinitionId());
        e.setParentId(source.getId()); e.setScopeExecutionId(source.getScopeExecutionId()); e.setRootExecutionId(source.getRootExecutionId());
        e.setNodeId(nodeId); e.setState(ExecutionState.ACTIVE.name()); e.setIsConcurrent(1); e.setIsScope(0); e.setSuspensionState(1);
        e.setRevision(1L); e.setCreatedAt(Instant.now()); e.setUpdatedAt(Instant.now()); return e;
    }
}