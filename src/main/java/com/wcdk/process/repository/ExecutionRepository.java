package com.wcdk.process.repository;

import com.wcdk.process.entity.ExecutionEntity;
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
public interface ExecutionRepository extends BaseRepository<ExecutionEntity> {

    Flux<ExecutionEntity> findByProcessInstanceId(String processInstanceId);

    Flux<ExecutionEntity> findByParentId(String parentId);

    Flux<ExecutionEntity> findByScopeExecutionId(String scopeExecutionId);

    Flux<ExecutionEntity> findByNodeId(String nodeId);

    Flux<ExecutionEntity> findByProcessInstanceIdAndState(String processInstanceId, String state);

    Mono<ExecutionEntity> findByProcessInstanceIdAndNodeId(String processInstanceId, String nodeId);

    Flux<ExecutionEntity> findByProcessDefinitionId(String processDefinitionId);
}