package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.EdgeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.execution.ReactiveVariableManager;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * 排他网关行为实现。
 * <p>处理 BPMN 流程定义中的排他网关（Exclusive Gateway / XOR Gateway）节点。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>评估所有出向序列流的条件表达式</li>
 *   <li>选择第一个满足条件的出线执行（排他性，仅选择一条）</li>
 *   <li>若无条件匹配则选择默认出线（Default Flow）</li>
 * </ul>
 * @author wcdk
 */
@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ExclusiveGatewayBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final HistoryEventRepository historyEventRepository;
    private final ReactiveVariableManager variableManager;
    private final ReactiveAgenda agenda;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();

        execution.setState(ExecutionState.ACTIVE.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createHistoryEvent(context));
                }))
                .then(Mono.defer(() -> {
                    // Evaluate outgoing conditions
                    List<EdgeDefinition> outgoingEdges = graph.getOutgoingEdges(execution.getNodeId());
                    return evaluateConditions(context, outgoingEdges)
                            .flatMap(matchedEdge -> {
                                if (matchedEdge != null) {
                                    agenda.planTakeSequenceFlow(execution.getId(), matchedEdge.getId());
                                }
                                return Mono.empty();
                            });
                }))
                .then();
    }

    private Mono<EdgeDefinition> evaluateConditions(ExecutionContext context, List<EdgeDefinition> edges) {
        if (edges.isEmpty()) {
            return Mono.empty();
        }

        // Find default edge
        EdgeDefinition defaultEdge = edges.stream()
                .filter(EdgeDefinition::isDefault)
                .findFirst()
                .orElse(null);

        return Flux.fromIterable(edges)
                .filter(edge -> !edge.isDefault() && edge.getConditionExpression() != null)
                .concatMap(edge -> evaluateExpression(context, edge.getConditionExpression())
                        .filter(Boolean::booleanValue)
                        .map(ignored -> edge))
                .next()
                .switchIfEmpty(Mono.justOrEmpty(defaultEdge));
    }

    private Mono<Boolean> evaluateExpression(ExecutionContext context, String expression) {
        if (expression == null || expression.isBlank()) {
            return Mono.just(true);
        }
        // Simple variable comparison: ${variableName == 'value'}
        // For now, implement basic expression evaluation
        String trimmed = expression.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            String inner = trimmed.substring(2, trimmed.length() - 1);
            if (inner.contains("==")) {
                String[] parts = inner.split("==", 2);
                String varName = parts[0].trim();
                String expected = parts[1].trim().replace("'", "").replace("\"", "");
                return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                        .map(actual -> expected.equals(String.valueOf(actual)))
                        .defaultIfEmpty(false);
            }
        }
        return Mono.just(true);
    }

    private com.wcdk.process.entity.HistoryEventEntity createHistoryEvent(ExecutionContext context) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(context.getProcessInstanceId());
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(context.getExecutionId());
        event.setEventType(HistoryEventType.ACTIVITY_STARTED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }
}