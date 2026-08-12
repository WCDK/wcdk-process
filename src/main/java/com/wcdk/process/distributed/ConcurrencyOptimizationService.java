package com.wcdk.process.distributed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ConcurrencyOptimizationService {

    private final DistributedLockService lockService;
    private final ConcurrentHashMap<String, AtomicInteger> activeExecutions = new ConcurrentHashMap<>();

    private static final Duration LOCK_LEASE_DURATION = Duration.ofSeconds(30);

    public ConcurrencyOptimizationService(DistributedLockService lockService) {
        this.lockService = lockService;
    }

    public <T> Mono<T> executeWithLock(String lockKey, String owner, Mono<T> operation) {
        return lockService.acquireLock(lockKey, owner, LOCK_LEASE_DURATION)
                .flatMap(token -> {
                    if (token == null) {
                        return Mono.error(new RuntimeException("Failed to acquire lock: " + lockKey));
                    }
                    activeExecutions.computeIfAbsent(lockKey, k -> new AtomicInteger(0))
                            .incrementAndGet();

                    return operation
                            .doFinally(signal -> {
                                activeExecutions.computeIfPresent(lockKey, (k, v) -> {
                                    int count = v.decrementAndGet();
                                    return count <= 0 ? null : v;
                                });
                                lockService.releaseLock(lockKey, token.getToken(), owner)
                                        .subscribe();
                            });
                });
    }

    public Mono<Boolean> tryExecuteWithLock(String lockKey, String owner, Runnable operation) {
        return lockService.acquireLock(lockKey, owner, LOCK_LEASE_DURATION)
                .flatMap(token -> {
                    if (token == null) {
                        return Mono.just(false);
                    }
                    try {
                        operation.run();
                        return lockService.releaseLock(lockKey, token.getToken(), owner)
                                .then(Mono.just(true));
                    } catch (Exception e) {
                        log.error("Error executing operation with lock: {}", lockKey, e);
                        return lockService.releaseLock(lockKey, token.getToken(), owner)
                                .then(Mono.just(false));
                    }
                });
    }

    public int getActiveExecutionCount(String lockKey) {
        AtomicInteger count = activeExecutions.get(lockKey);
        return count != null ? count.get() : 0;
    }

    public int getTotalActiveExecutions() {
        return activeExecutions.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }
}