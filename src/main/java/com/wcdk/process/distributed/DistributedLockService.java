package com.wcdk.process.distributed;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface DistributedLockService {

    Mono<LockToken> acquireLock(String lockKey, String owner, Duration leaseDuration);

    Mono<Boolean> releaseLock(String lockKey, String lockToken, String owner);

    Mono<Boolean> isLocked(String lockKey);

    Mono<Boolean> renewLock(String lockKey, String lockToken, String owner, Duration leaseDuration);
}