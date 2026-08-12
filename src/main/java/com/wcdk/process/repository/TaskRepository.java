package com.wcdk.process.repository;

import com.wcdk.process.entity.TaskEntity;
import com.wcdk.r2dbc.BaseRepository;
import com.wcdk.r2dbc.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface TaskRepository extends BaseRepository<TaskEntity> {

    Flux<TaskEntity> findByProcessInstanceId(String processInstanceId);

    Flux<TaskEntity> findByExecutionId(String executionId);

    Flux<TaskEntity> findByAssignee(String assignee);

    Flux<TaskEntity> findByState(String state);

    Flux<TaskEntity> findByTaskDefinitionKey(String taskDefinitionKey);

    Mono<TaskEntity> findByProcessInstanceIdAndTaskDefinitionKey(String processInstanceId, String taskDefinitionKey);
}