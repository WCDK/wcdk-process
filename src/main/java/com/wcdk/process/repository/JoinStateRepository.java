package com.wcdk.process.repository;

import com.wcdk.process.entity.JoinStateEntity;
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
public interface JoinStateRepository extends BaseRepository<JoinStateEntity> {

    Flux<JoinStateEntity> findByProcessInstanceId(String processInstanceId);

    Mono<JoinStateEntity> findByProcessInstanceIdAndGatewayIdAndCycleKey(String processInstanceId, String gatewayId, String cycleKey);

    Mono<JoinStateEntity> findByScopeExecutionIdAndGatewayId(String scopeExecutionId, String gatewayId);
}