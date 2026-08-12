package com.wcdk.process.outbox;

import com.wcdk.process.entity.OutboxEntity;
import com.wcdk.process.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class OutboxWorker {

    private final OutboxRepository outboxRepository;
    private final OutboxEventDispatcher dispatcher;

    private static final int BATCH_SIZE = 50;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(5);

    public Flux<Void> startPolling() {
        return Flux.interval(POLL_INTERVAL)
                .flatMap(tick -> pollAndProcess().then())
                .onErrorResume(e -> {
                    log.error("OutboxWorker polling error", e);
                    return Mono.empty();
                });
    }

    public Mono<Integer> pollAndProcess() {
        return outboxRepository.findByStatus("PENDING")
                .filter(entity -> entity.getRetryCount() < 3)
                .take(BATCH_SIZE)
                .concatMap(this::processEvent)
                .count()
                .map(Long::intValue);
    }

    private Mono<Void> processEvent(OutboxEntity entity) {
        return dispatcher.dispatch(
                        entity.getAggregateType(),
                        entity.getAggregateId(),
                        entity.getEventType(),
                        entity.getPayload())
                .then(Mono.defer(() -> {
                    entity.setStatus("PROCESSED");
                    entity.setProcessedAt(Instant.now());
                    return outboxRepository.updateById(entity);
                }))
                .then()
                .onErrorResume(e -> {
                    log.warn("Failed to dispatch outbox event: id={}, error={}",
                            entity.getId(), e.getMessage());
                    return handleError(entity, e);
                });
    }

    private Mono<Void> handleError(OutboxEntity entity, Throwable error) {
        int newRetryCount = entity.getRetryCount() + 1;
        entity.setRetryCount(newRetryCount);

        if (newRetryCount >= entity.getMaxRetries()) {
            entity.setStatus("FAILED");
            log.error("Outbox event permanently failed: id={}, aggregateType={}, eventType={}",
                    entity.getId(), entity.getAggregateType(), entity.getEventType());
        } else {
            entity.setStatus("RETRY");
        }

        return outboxRepository.updateById(entity).then();
    }
}