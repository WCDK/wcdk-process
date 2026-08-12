package com.wcdk.process.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.entity.OutboxEntity;
import com.wcdk.process.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class OutboxService implements OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_MAX_RETRIES = 3;

    @Override
    public Mono<Void> publish(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxEntity entity = new OutboxEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setAggregateType(aggregateType);
        entity.setAggregateId(aggregateId);
        entity.setEventType(eventType);
        entity.setPayload(payload);
        entity.setStatus("PENDING");
        entity.setRetryCount(0);
        entity.setMaxRetries(DEFAULT_MAX_RETRIES);
        entity.setCreatedAt(Instant.now());

        log.debug("Outbox event created: aggregateType={}, aggregateId={}, eventType={}",
                aggregateType, aggregateId, eventType);

        return outboxRepository.insert(entity).then();
    }

    public Mono<Void> publishEvent(OutboxEvent event) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return Mono.error(e);
        }
        return publish(event.getAggregateType(), event.getAggregateId(), event.getEventType(), payload);
    }
}