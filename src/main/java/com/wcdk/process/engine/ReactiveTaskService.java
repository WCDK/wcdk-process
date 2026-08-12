package com.wcdk.process.engine;

import com.wcdk.process.entity.TaskEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveTaskService {

    Mono<TaskEntity> completeTask(String taskId, String userId, java.util.Map<String, Object> variables);

    Mono<TaskEntity> claimTask(String taskId, String userId);

    Mono<TaskEntity> unclaimTask(String taskId);

    Mono<TaskEntity> getTaskById(String taskId);

    Flux<TaskEntity> getTasksByAssignee(String assignee);

    Flux<TaskEntity> getTasksByProcessInstanceId(String processInstanceId);
}