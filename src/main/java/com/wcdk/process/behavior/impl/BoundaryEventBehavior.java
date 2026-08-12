package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.engine.ReactiveRepositoryService;
import com.wcdk.process.entity.EventSubscriptionEntity;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.EventSubscriptionRepository;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 边界事件行为实现。
 * <p>处理 BPMN 流程定义中的边界事件（Boundary Event）节点，
 * 附着在活动节点上用于捕获特定事件。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>注册事件订阅（Event Subscription），监听指定类型的事件</li>
 *   <li>中断型边界事件（Interrupting）：触发时终止被附着的活动执行</li>
 *   <li>非中断型边界事件（Non-Interrupting）：触发时不终止被附着的活动，仅激活事件出线</li>
 *   <li>支持事件订阅的取消操作</li>
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
public class BoundaryEventBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final HistoryEventRepository historyEventRepository;
    private final EventSubscriptionRepository eventSubscriptionRepository;
    private final ReactiveAgenda agenda;
    private final ReactiveRepositoryService repositoryService;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();
        NodeDefinition boundaryNode = graph.getNode(execution.getNodeId());

        String attachedToNodeId = (String) boundaryNode.getProperty("attachedToRef");
        boolean isInterrupting = getIsInterrupting(boundaryNode);

        execution.setState(ExecutionState.ACTIVE.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createActivityStartedEvent(context));
                }))
                .then(Mono.defer(() -> {
                    return registerEventSubscription(context, boundaryNode, attachedToNodeId);
                }))
                .then(Mono.defer(() -> {
                    if (isInterrupting) {
                        return handleInterruptingBoundary(context, attachedToNodeId);
                    } else {
                        return handleNonInterruptingBoundary(context, attachedToNodeId);
                    }
                }))
                .then();
    }

    private boolean getIsInterrupting(NodeDefinition boundaryNode) {
        Object value = boundaryNode.getProperty("isInterrupting");
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return true;
    }

    private Mono<Void> registerEventSubscription(ExecutionContext context, NodeDefinition boundaryNode, String attachedToNodeId) {
        String eventType = (String) boundaryNode.getProperty("eventType");
        String eventName = (String) boundaryNode.getProperty("eventName");

        if (eventType == null || eventName == null) {
            return Mono.empty();
        }

        EventSubscriptionEntity subscription = new EventSubscriptionEntity();
        subscription.setId(UUID.randomUUID().toString());
        subscription.setTenantId(context.getProcessInstance().getTenantId());
        subscription.setProcessInstanceId(context.getProcessInstanceId());
        subscription.setExecutionId(context.getExecutionId());
        subscription.setEventType(eventType);
        subscription.setEventName(eventName);
        subscription.setNodeId(context.getCurrentNodeId());
        subscription.setState("ACTIVE");
        subscription.setCreatedAt(Instant.now());

        return eventSubscriptionRepository.insert(subscription).then();
    }

    private Mono<Void> handleInterruptingBoundary(ExecutionContext context, String attachedToNodeId) {
        return executionRepository.findByProcessInstanceId(context.getProcessInstanceId())
                .filter(execution -> attachedToNodeId.equals(execution.getNodeId()))
                .filter(execution -> ExecutionState.ACTIVE.name().equals(execution.getState())
                        || ExecutionState.WAITING.name().equals(execution.getState()))
                .flatMap(execution -> {
                    execution.setState(ExecutionState.TERMINATED.name());
                    execution.setUpdatedAt(Instant.now());
                    return executionRepository.updateById(execution);
                })
                .then();
    }

    private Mono<Void> handleNonInterruptingBoundary(ExecutionContext context, String attachedToNodeId) {
        return Mono.empty();
    }

    public Mono<Void> triggerBoundaryEvent(String processInstanceId, String boundaryNodeId, String eventType, String eventName) {
        return eventSubscriptionRepository.findByEventTypeAndEventName(eventType, eventName)
                .filter(sub -> processInstanceId.equals(sub.getProcessInstanceId()))
                .filter(sub -> boundaryNodeId.equals(sub.getNodeId()))
                .filter(sub -> "ACTIVE".equals(sub.getState()))
                .next()
                .flatMap(subscription -> {
                    return repositoryService.getExecutionById(subscription.getExecutionId())
                            .flatMap(execution -> {
                                return repositoryService.getProcessDefinitionById(execution.getProcessDefinitionId())
                                        .zipWith(repositoryService.getProcessInstanceById(execution.getProcessInstanceId()))
                                        .flatMap(tuple -> {
                                            var pd = tuple.getT1();
                                            var pi = tuple.getT2();
                                            return repositoryService.getProcessGraph(pd.getId())
                                                    .flatMap(graph -> {
                                                        ExecutionContext context = new ExecutionContext();
                                                        context.setExecution(execution);
                                                        context.setProcessDefinition(pd);
                                                        context.setProcessInstance(pi);
                                                        context.setProcessGraph(graph);
                                                        return eventSubscriptionRepository.updateById(subscription).then(triggerEventFlow(context, subscription, graph));
                                                    });
                                        });
                            });
                })
                .then();
    }

    private Mono<Void> triggerEventFlow(ExecutionContext context, EventSubscriptionEntity subscription, ProcessGraph graph) {
        if (graph == null) {
            return Mono.empty();
        }

        NodeDefinition boundaryNode = graph.getNode(subscription.getNodeId());
        if (boundaryNode == null) {
            return Mono.empty();
        }

        List<String> outgoingEdges = boundaryNode.getOutgoingEdges();
        if (outgoingEdges.isEmpty()) {
            return Mono.empty();
        }

        for (String edgeId : outgoingEdges) {
            agenda.planTakeSequenceFlow(context.getExecutionId(), edgeId);
        }

        return Mono.empty();
    }

    public Mono<Void> cancelEventSubscription(String processInstanceId, String eventType, String eventName) {
        return eventSubscriptionRepository.findByEventTypeAndEventName(eventType, eventName)
                .filter(sub -> processInstanceId.equals(sub.getProcessInstanceId()))
                .filter(sub -> "ACTIVE".equals(sub.getState()))
                .flatMap(sub -> {
                    sub.setState("DELETED");
                    return eventSubscriptionRepository.updateById(sub);
                })
                .then();
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