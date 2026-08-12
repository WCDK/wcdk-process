package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.EdgeDefinition;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.execution.ReactiveVariableManager;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 包含网关行为实现。
 * <p>处理 BPMN 流程定义中的包含网关（Inclusive Gateway / OR Gateway）节点。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>评估所有出向序列流的条件表达式</li>
 *   <li>同时激活所有满足条件的出线（与排他网关不同，可同时走多条路径）</li>
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
public class InclusiveGatewayBehavior implements ReactiveNodeBehavior {

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
                    return historyEventRepository.insert(createActivityStartedEvent(context));
                }))
                .then(Mono.defer(() -> {
                    return evaluateOutgoingConditions(context, graph);
                }))
                .then();
    }

    private Mono<Void> evaluateOutgoingConditions(ExecutionContext context, ProcessGraph graph) {
        List<EdgeDefinition> outgoingEdges = graph.getOutgoingEdges(context.getCurrentNodeId());

        return Flux.fromIterable(outgoingEdges)
                .filterWhen(edge -> evaluateCondition(context, edge))
                .collectList()
                .flatMap(matchedEdges -> {
                    if (matchedEdges.isEmpty()) {
                        EdgeDefinition defaultEdge = findDefaultEdge(outgoingEdges);
                        if (defaultEdge != null) {
                            matchedEdges = List.of(defaultEdge);
                        }
                    }

                    for (EdgeDefinition edge : matchedEdges) {
                        agenda.planTakeSequenceFlow(context.getExecutionId(), edge.getId());
                    }
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Boolean> evaluateCondition(ExecutionContext context, EdgeDefinition edge) {
        if (edge.isDefault()) {
            return Mono.just(false);
        }

        String conditionExpression = edge.getConditionExpression();
        if (conditionExpression == null || conditionExpression.isBlank()) {
            return Mono.just(true);
        }

        String trimmed = conditionExpression.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            String inner = trimmed.substring(2, trimmed.length() - 1);
            return evaluateExpression(context, inner);
        }

        return Mono.just(true);
    }

    private Mono<Boolean> evaluateExpression(ExecutionContext context, String expression) {
        if (expression.contains("==")) {
            String[] parts = expression.split("==", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim().replace("'", "").replace("\"", "");
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> expected.equals(String.valueOf(actual)))
                    .defaultIfEmpty(false);
        }
        if (expression.contains("!=")) {
            String[] parts = expression.split("!=", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim().replace("'", "").replace("\"", "");
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> !expected.equals(String.valueOf(actual)))
                    .defaultIfEmpty(true);
        }
        if (expression.contains(">")) {
            String[] parts = expression.split(">", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim();
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> {
                        try {
                            return Double.parseDouble(String.valueOf(actual)) > Double.parseDouble(expected);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .defaultIfEmpty(false);
        }
        if (expression.contains("<")) {
            String[] parts = expression.split("<", 2);
            String varName = parts[0].trim();
            String expected = parts[1].trim();
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(actual -> {
                        try {
                            return Double.parseDouble(String.valueOf(actual)) < Double.parseDouble(expected);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .defaultIfEmpty(false);
        }

        return variableManager.getProcessVariable(context.getProcessInstanceId(), expression)
                .map(val -> {
                    if (val instanceof Boolean b) {
                        return b;
                    }
                    return val != null;
                })
                .defaultIfEmpty(false);
    }

    private EdgeDefinition findDefaultEdge(List<EdgeDefinition> edges) {
        return edges.stream()
                .filter(EdgeDefinition::isDefault)
                .findFirst()
                .orElse(null);
    }

    private com.wcdk.process.entity.HistoryEventEntity createActivityStartedEvent(ExecutionContext context) {
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