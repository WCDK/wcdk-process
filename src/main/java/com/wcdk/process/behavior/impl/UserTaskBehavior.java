package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.entity.IdentityLinkEntity;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.enums.IdentityLinkType;
import com.wcdk.process.enums.TaskState;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import com.wcdk.process.repository.IdentityLinkRepository;
import com.wcdk.process.repository.TaskRepository;
import com.wcdk.process.enums.IdentityLinkType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * 用户任务行为实现。
 * <p>处理 BPMN 流程定义中的用户任务（User Task）节点。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>创建待办任务（Task）并持久化到数据库</li>
 *   <li>将当前执行状态设置为等待（WAITING），暂停流程等待人工处理</li>
 *   <li>记录任务创建的历史事件</li>
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
public class UserTaskBehavior implements ReactiveNodeBehavior {

    private final TaskRepository taskRepository;
    private final IdentityLinkRepository identityLinkRepository;
    private final ExecutionRepository executionRepository;
    private final HistoryEventRepository historyEventRepository;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        String assignee = context.getExecution().getNodeType().equals("USER_TASK")
                ? null : null;

        TaskEntity task = new TaskEntity();
        task.setTenantId(context.getProcessInstance().getTenantId());
        task.setProcessInstanceId(context.getProcessInstanceId());
        task.setExecutionId(execution.getId());
        task.setProcessDefinitionId(context.getProcessDefinitionId());
        task.setTaskDefinitionKey(context.getCurrentNodeId());
        task.setName(context.getCurrentNodeId());
        task.setState(TaskState.CREATED.name());
        task.setPriority(50);
        task.setCreateTime(Instant.now());
        task.setRevision(1L);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        execution.setState(ExecutionState.WAITING.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(taskRepository.insert(task))
                .flatMap(savedTask -> {
                    // Record task created history
                    return historyEventRepository.insert(createTaskCreatedEvent(context, savedTask))
                            .then(Mono.just(savedTask));
                })
                .then();
    }

    private com.wcdk.process.entity.HistoryEventEntity createTaskCreatedEvent(ExecutionContext context, TaskEntity task) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(context.getProcessInstanceId());
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(context.getExecutionId());
        event.setTaskId(task.getId());
        event.setEventType(HistoryEventType.TASK_CREATED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }
}