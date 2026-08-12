package com.wcdk.process.engine.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveTaskService;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.enums.TaskState;
import com.wcdk.process.exception.ProcessEngineException;
import com.wcdk.process.repository.IdentityLinkRepository;
import com.wcdk.process.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ReactiveTaskServiceImpl implements ReactiveTaskService {

    private final TaskRepository taskRepository;
    private final IdentityLinkRepository identityLinkRepository;
    private final ReactiveAgenda agenda;
    private final ReactiveCommandExecutorImpl commandExecutor;

    @Override
    public Mono<TaskEntity> completeTask(String taskId, String userId, Map<String, Object> variables) {
        return taskRepository.selectById(taskId)
                .switchIfEmpty(Mono.error(new ProcessEngineException("Task not found: " + taskId)))
                .flatMap(task -> {
                    if (!TaskState.CLAIMED.name().equals(task.getState())) {
                        return Mono.error(new ProcessEngineException("Task must be claimed before completion. Current state: " + task.getState()));
                    }
                    if (userId != null && !userId.equals(task.getAssignee())) {
                        return Mono.error(new ProcessEngineException("Task is not assigned to user: " + userId));
                    }
                    task.setState(TaskState.COMPLETED.name());
                    task.setCompleteTime(Instant.now());
                    task.setUpdatedAt(Instant.now());
                    return taskRepository.updateById(task)
                            .then(Mono.defer(() -> {
                                agenda.planCompleteTask(task.getExecutionId(), task.getId());
                                return commandExecutor.flushAgenda();
                            }))
                            .thenReturn(task);
                });
    }

    @Override
    public Mono<TaskEntity> claimTask(String taskId, String userId) {
        return taskRepository.selectById(taskId)
                .switchIfEmpty(Mono.error(new ProcessEngineException("Task not found: " + taskId)))
                .flatMap(task -> {
                    if (!TaskState.CREATED.name().equals(task.getState())) {
                        return Mono.error(new ProcessEngineException("Task can only be claimed in CREATED state. Current state: " + task.getState()));
                    }
                    task.setAssignee(userId);
                    task.setState(TaskState.CLAIMED.name());
                    task.setClaimTime(Instant.now());
                    task.setUpdatedAt(Instant.now());
                    return taskRepository.updateById(task).thenReturn(task);
                });
    }

    @Override
    public Mono<TaskEntity> unclaimTask(String taskId) {
        return taskRepository.selectById(taskId)
                .switchIfEmpty(Mono.error(new ProcessEngineException("Task not found: " + taskId)))
                .flatMap(task -> {
                    if (!TaskState.CLAIMED.name().equals(task.getState())) {
                        return Mono.error(new ProcessEngineException("Task can only be unclaimed in CLAIMED state. Current state: " + task.getState()));
                    }
                    task.setAssignee(null);
                    task.setState(TaskState.CREATED.name());
                    task.setClaimTime(null);
                    task.setUpdatedAt(Instant.now());
                    return taskRepository.updateById(task).thenReturn(task);
                });
    }

    @Override
    public Mono<TaskEntity> getTaskById(String taskId) {
        return taskRepository.selectById(taskId);
    }

    @Override
    public Flux<TaskEntity> getTasksByAssignee(String assignee) {
        return taskRepository.findByAssignee(assignee);
    }

    @Override
    public Flux<TaskEntity> getTasksByProcessInstanceId(String processInstanceId) {
        return taskRepository.findByProcessInstanceId(processInstanceId);
    }
}