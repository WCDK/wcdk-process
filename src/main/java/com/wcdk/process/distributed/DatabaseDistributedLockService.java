package com.wcdk.process.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
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
public class DatabaseDistributedLockService implements DistributedLockService {

    private final DistributedLockRepository lockRepository;

    @Override
    public Mono<LockToken> acquireLock(String lockKey, String owner, Duration leaseDuration) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(leaseDuration);

        return lockRepository.findByLockKey(lockKey)
                .flatMap(existing -> {
                    if (existing.getExpiresAt().isAfter(Instant.now())) {
                        if (owner.equals(existing.getOwner())) {
                            existing.setToken(token);
                            existing.setExpiresAt(expiresAt);
                            return lockRepository.updateById(existing)
                                    .then(Mono.just(buildLockToken(lockKey, token, owner, expiresAt)));
                        }
                        log.debug("Lock already held by another owner: lockKey={}, owner={}",
                                lockKey, existing.getOwner());
                        return Mono.empty();
                    }
                    return deleteAndAcquire(existing, lockKey, token, owner, expiresAt);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    DistributedLockEntity entity = new DistributedLockEntity();
                    entity.setId(UUID.randomUUID().toString());
                    entity.setLockKey(lockKey);
                    entity.setToken(token);
                    entity.setOwner(owner);
                    entity.setExpiresAt(expiresAt);
                    entity.setCreatedAt(Instant.now());

                    return lockRepository.insert(entity)
                            .then(Mono.just(buildLockToken(lockKey, token, owner, expiresAt)));
                }));
    }

    private Mono<LockToken> deleteAndAcquire(DistributedLockEntity existing, String lockKey,
                                              String token, String owner, Instant expiresAt) {
        return lockRepository.deleteById(existing.getId())
                .then(Mono.defer(() -> {
                    DistributedLockEntity entity = new DistributedLockEntity();
                    entity.setId(UUID.randomUUID().toString());
                    entity.setLockKey(lockKey);
                    entity.setToken(token);
                    entity.setOwner(owner);
                    entity.setExpiresAt(expiresAt);
                    entity.setCreatedAt(Instant.now());

                    return lockRepository.insert(entity)
                            .then(Mono.just(buildLockToken(lockKey, token, owner, expiresAt)));
                }));
    }

    @Override
    public Mono<Boolean> releaseLock(String lockKey, String lockToken, String owner) {
        return lockRepository.findByLockKeyAndToken(lockKey, lockToken)
                .flatMap(entity -> {
                    if (owner.equals(entity.getOwner())) {
                        return lockRepository.deleteById(entity.getId()).then(Mono.just(true));
                    }
                    return Mono.just(false);
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> isLocked(String lockKey) {
        return lockRepository.findByLockKey(lockKey)
                .map(entity -> entity.getExpiresAt().isAfter(Instant.now()))
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> renewLock(String lockKey, String lockToken, String owner, Duration leaseDuration) {
        return lockRepository.findByLockKeyAndToken(lockKey, lockToken)
                .flatMap(entity -> {
                    if (owner.equals(entity.getOwner())) {
                        entity.setExpiresAt(Instant.now().plus(leaseDuration));
                        return lockRepository.updateById(entity).then(Mono.just(true));
                    }
                    return Mono.just(false);
                })
                .defaultIfEmpty(false);
    }

    private LockToken buildLockToken(String lockKey, String token, String owner, Instant expiresAt) {
        return LockToken.builder()
                .lockKey(lockKey)
                .token(token)
                .owner(owner)
                .expiresAt(expiresAt)
                .build();
    }
}