package com.wcdk.process.repository;

import com.wcdk.process.entity.EventSubscriptionEntity;
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
public interface EventSubscriptionRepository extends BaseRepository<EventSubscriptionEntity> {

    Flux<EventSubscriptionEntity> findByProcessInstanceId(String processInstanceId);

    Flux<EventSubscriptionEntity> findByEventTypeAndEventName(String eventType, String eventName);

    Mono<EventSubscriptionEntity> findByEventTypeAndEventNameAndProcessInstanceId(String eventType, String eventName, String processInstanceId);

    Flux<EventSubscriptionEntity> findByExecutionId(String executionId);

    Flux<EventSubscriptionEntity> findByState(String state);
}