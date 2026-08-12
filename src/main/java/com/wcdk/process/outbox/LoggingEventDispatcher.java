package com.wcdk.process.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class LoggingEventDispatcher implements OutboxEventDispatcher {

    @Override
    public Mono<Void> dispatch(String aggregateType, String aggregateId, String eventType, String payload) {
        log.info("Dispatching event: aggregateType={}, aggregateId={}, eventType={}",
                aggregateType, aggregateId, eventType);
        log.debug("Event payload: {}", payload);
        return Mono.empty();
    }
}