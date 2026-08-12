package com.wcdk.process.distributed;

import com.wcdk.r2dbc.BaseRepository;
import com.wcdk.r2dbc.Repository;
import reactor.core.publisher.Mono;

@Repository
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface DistributedLockRepository extends BaseRepository<DistributedLockEntity> {

    Mono<DistributedLockEntity> findByLockKey(String lockKey);

    Mono<DistributedLockEntity> findByLockKeyAndToken(String lockKey, String token);

}