package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.execution.ExecutionContext;
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
import java.util.UUID;

/**
 * 多实例行为实现。
 * <p>处理 BPMN 流程定义中的多实例（Multi-Instance）节点，支持并行和串行两种模式。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>解析集合表达式（collection）获取待迭代元素列表</li>
 *   <li>并行模式：为每个元素创建独立的子执行实例并发执行</li>
 *   <li>串行模式：按顺序逐个创建子执行实例依次执行</li>
 *   <li>评估完成条件（completionCondition），判断是否提前终止或全部完成</li>
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
public class MultiInstanceBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final HistoryEventRepository historyEventRepository;
    private final ReactiveVariableManager variableManager;
    private final ReactiveAgenda agenda;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();
        NodeDefinition multiInstanceNode = graph.getNode(execution.getNodeId());

        boolean isParallel = getIsParallel(multiInstanceNode);
        String collectionExpression = (String) multiInstanceNode.getProperty("collection");
        String elementVariable = (String) multiInstanceNode.getProperty("elementVariable");
        String completionCondition = (String) multiInstanceNode.getProperty("completionCondition");

        execution.setState(ExecutionState.ACTIVE.name());
        execution.setIsMultiInstance(1);
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createActivityStartedEvent(context));
                }))
                .then(Mono.defer(() -> {
                    return resolveCollection(context, collectionExpression);
                }))
                .flatMap(collection -> {
                    int totalInstances = collection != null ? collection.size() : 0;
                    if (totalInstances == 0) {
                        return Mono.empty();
                    }

                    execution.setMultiInstanceTotal(totalInstances);
                    execution.setMultiInstanceIndex(0);
                    return executionRepository.updateById(execution)
                            .then(Mono.defer(() -> {
                                if (isParallel) {
                                    return createParallelInstances(context, execution, graph,
                                            multiInstanceNode, collection, elementVariable);
                                } else {
                                    return createSequentialInstance(context, execution, graph,
                                            multiInstanceNode, collection, elementVariable, 0);
                                }
                            }));
                })
                .then();
    }

    private boolean getIsParallel(NodeDefinition node) {
        Object value = node.getProperty("isParallel");
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return true;
    }

    private Mono<List<Object>> resolveCollection(ExecutionContext context, String collectionExpression) {
        if (collectionExpression == null || collectionExpression.isBlank()) {
            return Mono.just(List.of());
        }

        String trimmed = collectionExpression.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            String varName = trimmed.substring(2, trimmed.length() - 1);
            return variableManager.getProcessVariable(context.getProcessInstanceId(), varName)
                    .map(value -> {
                        if (value instanceof List<?> list) {
                            return new ArrayList<Object>(list);
                        }
                        if (value instanceof Object[] arr) {
                            return new ArrayList<Object>(List.of(arr));
                        }
                        return List.of(value);
                    })
                    .defaultIfEmpty(List.of());
        }

        return Mono.just(List.of());
    }

    private Mono<Void> createParallelInstances(ExecutionContext context, ExecutionEntity parentExecution,
                                                ProcessGraph graph, NodeDefinition multiInstanceNode,
                                                List<Object> collection, String elementVariable) {
        String innerNodeClassName = (String) multiInstanceNode.getProperty("innerNodeClass");

        return Flux.range(0, collection.size())
                .flatMap(index -> {
                    Object element = collection.get(index);

                    ExecutionEntity childExecution = createChildExecution(
                            parentExecution, index, collection.size());

                    return executionRepository.insert(childExecution)
                            .then(Mono.defer(() -> {
                                if (elementVariable != null) {
                                    return variableManager.setExecutionVariable(
                                            childExecution.getId(), elementVariable, element);
                                }
                                return Mono.empty();
                            }))
                            .then(Mono.defer(() -> {
                                return historyEventRepository.insert(
                                        createMultiInstanceStartedEvent(context, childExecution, index));
                            }))
                            .then(Mono.defer(() -> {
                                agenda.planContinueProcessInNode(childExecution.getId(),
                                        multiInstanceNode.getId());
                                return Mono.empty();
                            }));
                })
                .then();
    }

    private Mono<Void> createSequentialInstance(ExecutionContext context, ExecutionEntity parentExecution,
                                                 ProcessGraph graph, NodeDefinition multiInstanceNode,
                                                 List<Object> collection, String elementVariable, int index) {
        if (index >= collection.size()) {
            return Mono.empty();
        }

        Object element = collection.get(index);

        ExecutionEntity childExecution = createChildExecution(
                parentExecution, index, collection.size());

        return executionRepository.insert(childExecution)
                .then(Mono.defer(() -> {
                    if (elementVariable != null) {
                        return variableManager.setExecutionVariable(
                                childExecution.getId(), elementVariable, element);
                    }
                    return Mono.empty();
                }))
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(
                            createMultiInstanceStartedEvent(context, childExecution, index));
                }))
                .then(Mono.defer(() -> {
                    agenda.planContinueProcessInNode(childExecution.getId(),
                            multiInstanceNode.getId());
                    return Mono.empty();
                }))
                .then();
    }

    private ExecutionEntity createChildExecution(ExecutionEntity parentExecution, int index, int total) {
        ExecutionEntity child = new ExecutionEntity();
        child.setId(UUID.randomUUID().toString());
        child.setTenantId(parentExecution.getTenantId());
        child.setProcessInstanceId(parentExecution.getProcessInstanceId());
        child.setProcessDefinitionId(parentExecution.getProcessDefinitionId());
        child.setParentId(parentExecution.getId());
        child.setScopeExecutionId(parentExecution.getScopeExecutionId());
        child.setRootExecutionId(parentExecution.getRootExecutionId());
        child.setNodeId(parentExecution.getNodeId());
        child.setNodeType(parentExecution.getNodeType());
        child.setState(ExecutionState.ACTIVE.name());
        child.setIsScope(0);
        child.setIsConcurrent(0);
        child.setIsEventScope(0);
        child.setIsMultiInstance(1);
        child.setMultiInstanceIndex(index);
        child.setMultiInstanceTotal(total);
        child.setSuspensionState(1);
        child.setRevision(1L);
        child.setCreatedAt(Instant.now());
        child.setUpdatedAt(Instant.now());
        return child;
    }

    public Mono<Void> completeInstance(ExecutionContext context, String childExecutionId, String completionCondition) {
        return executionRepository.selectById(childExecutionId)
                .flatMap(childExecution -> {
                    childExecution.setState(ExecutionState.COMPLETED.name());
                    childExecution.setUpdatedAt(Instant.now());
                    return executionRepository.updateById(childExecution);
                })
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(
                            createMultiInstanceCompletedEvent(context, childExecutionId));
                }))
                .then(Mono.defer(() -> {
                    return executionRepository.selectById(childExecutionId);
                }))
                .flatMap(childExecution -> {
                    return executionRepository.selectById(childExecution.getParentId())
                            .flatMap(parentExecution -> {
                                return checkCompletionCondition(context, parentExecution, completionCondition);
                            });
                })
                .then();
    }

    private Mono<Void> checkCompletionCondition(ExecutionContext context, ExecutionEntity parentExecution,
                                                 String completionCondition) {
        return executionRepository.findByProcessInstanceId(parentExecution.getProcessInstanceId())
                .filter(exec -> parentExecution.getId().equals(exec.getParentId()))
                .filter(exec -> exec.getIsMultiInstance() != null && exec.getIsMultiInstance() == 1)
                .collectList()
                .flatMap(children -> {
                    long completedCount = children.stream()
                            .filter(c -> ExecutionState.COMPLETED.name().equals(c.getState()))
                            .count();

                    int total = parentExecution.getMultiInstanceTotal() != null ? parentExecution.getMultiInstanceTotal() : 0;

                    boolean conditionMet = evaluateCompletionCondition(
                            completionCondition, completedCount, total);

                    if (conditionMet || completedCount >= total) {
                        parentExecution.setState(ExecutionState.COMPLETED.name());
                        parentExecution.setUpdatedAt(Instant.now());
                        return executionRepository.updateById(parentExecution)
                                .then(Mono.defer(() -> {
                                    return historyEventRepository.insert(
                                            createMultiInstanceCompletedEvent(context, parentExecution.getId()));
                                }))
                                .then(Mono.defer(() -> {
                                    return executionRepository.selectById(parentExecution.getId());
                                }))
                                .flatMap(exec -> {
                                    agenda.planContinueProcessInNode(exec.getId(), exec.getNodeId());
                                    return Mono.empty();
                                });
                    }
                    return Mono.empty();
                })
                .then();
    }

    private boolean evaluateCompletionCondition(String condition, long completedCount, int total) {
        if (condition == null || condition.isBlank()) {
            return false;
        }

        String trimmed = condition.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            String inner = trimmed.substring(2, trimmed.length() - 1);

            if (inner.contains(">=")) {
                String[] parts = inner.split(">=", 2);
                String varName = parts[0].trim();
                String threshold = parts[1].trim();
                if ("nrOfCompletedInstances".equals(varName)) {
                    return completedCount >= Long.parseLong(threshold);
                }
            }
            if (inner.contains("==")) {
                String[] parts = inner.split("==", 2);
                String varName = parts[0].trim();
                String threshold = parts[1].trim();
                if ("nrOfCompletedInstances".equals(varName)) {
                    return completedCount == Long.parseLong(threshold);
                }
            }
        }

        return false;
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

    private com.wcdk.process.entity.HistoryEventEntity createMultiInstanceStartedEvent(ExecutionContext context,
                                                                                         ExecutionEntity childExecution,
                                                                                         int index) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(context.getProcessInstanceId());
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(childExecution.getId());
        event.setEventType(HistoryEventType.ACTIVITY_STARTED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }

    private com.wcdk.process.entity.HistoryEventEntity createMultiInstanceCompletedEvent(ExecutionContext context,
                                                                                           String executionId) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(context.getProcessInstanceId());
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(executionId);
        event.setEventType(HistoryEventType.ACTIVITY_COMPLETED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }
}