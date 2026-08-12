package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.enums.ProcessStatus;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 流程结束事件行为实现。
 * <p>处理 BPMN 流程定义中的结束事件（End Event）节点。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>将当前执行状态设置为已完成（COMPLETED）</li>
 *   <li>记录活动完成的历史事件</li>
 *   <li>若当前执行是根执行（主流程结束），则将流程实例状态更新为已完成，并计算流程持续时间</li>
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
public class EndEventBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final HistoryEventRepository historyEventRepository;
    private final ReactiveAgenda agenda;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();

        execution.setState(ExecutionState.COMPLETED.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createHistoryEvent(context));
                }))
                .then(Mono.defer(() -> {
                    // Check if this is the root execution (main process end)
                    if (execution.getId().equals(execution.getRootExecutionId())) {
                        return processInstanceRepository.selectById(execution.getProcessInstanceId())
                                .flatMap(pi -> {
                                    pi.setStatus(ProcessStatus.COMPLETED.name());
                                    pi.setEndTime(Instant.now());
                                    if (pi.getStartTime() != null) {
                                        pi.setDurationMs(java.time.Duration.between(pi.getStartTime(), pi.getEndTime()).toMillis());
                                    }
                                    pi.setUpdatedAt(Instant.now());
                                    return processInstanceRepository.updateById(pi);
                                });
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
        event.setEventType(HistoryEventType.ACTIVITY_COMPLETED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }
}