package com.wcdk.process.repository;

import com.wcdk.process.entity.VariableEntity;
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
public interface VariableRepository extends BaseRepository<VariableEntity> {

    Flux<VariableEntity> findByProcessInstanceId(String processInstanceId);

    Flux<VariableEntity> findByScopeTypeAndScopeId(String scopeType, String scopeId);

    Mono<VariableEntity> findByScopeTypeAndScopeIdAndName(String scopeType, String scopeId, String name);

    Flux<VariableEntity> findByProcessInstanceIdAndName(String processInstanceId, String name);
}