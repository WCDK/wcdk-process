package com.wcdk.process.repository;

import com.wcdk.process.entity.OutboxEntity;
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
public interface OutboxRepository extends BaseRepository<OutboxEntity> {

    Flux<OutboxEntity> findByStatus(String status);

    Flux<OutboxEntity> findByAggregateTypeAndAggregateId(String aggregateType, String aggregateId);


    Mono<Long> countByStatus(String status);
}