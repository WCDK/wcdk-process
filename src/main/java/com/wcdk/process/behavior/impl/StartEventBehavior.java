package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * 流程开始事件行为实现。
 * <p>处理 BPMN 流程定义中的开始事件（Start Event）节点。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>将当前执行状态设置为活跃（ACTIVE）</li>
 *   <li>记录活动开始的历史事件</li>
 *   <li>获取所有出向序列流并依次触发，推动流程向下执行</li>
 *   <li>若无出向序列流则直接结束流程</li>
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
public class StartEventBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final HistoryEventRepository historyEventRepository;
    private final ReactiveAgenda agenda;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();

        execution.setState(ExecutionState.ACTIVE.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    // Record history event
                    return historyEventRepository.insert(createHistoryEvent(context));
                }))
                .then(Mono.defer(() -> {
                    // Take outgoing sequence flows
                    List<String> outgoingEdges = graph.getOutgoingEdges(execution.getNodeId())
                            .stream()
                            .map(e -> e.getId())
                            .toList();
                    if (outgoingEdges.isEmpty()) {
                        agenda.planEndProcess(execution.getId());
                        return Mono.empty();
                    }
                    for (String edgeId : outgoingEdges) {
                        agenda.planTakeSequenceFlow(execution.getId(), edgeId);
                    }
                    return Mono.empty();
                }))
                .then();
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