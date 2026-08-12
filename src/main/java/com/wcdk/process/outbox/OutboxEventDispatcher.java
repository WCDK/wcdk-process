package com.wcdk.process.outbox;

import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface OutboxEventDispatcher {

    Mono<Void> dispatch(String aggregateType, String aggregateId, String eventType, String payload);
}