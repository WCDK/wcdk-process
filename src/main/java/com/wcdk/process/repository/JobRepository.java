package com.wcdk.process.repository;

import com.wcdk.process.entity.JobEntity;
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
public interface JobRepository extends BaseRepository<JobEntity> {

    Flux<JobEntity> findByStatus(String status);

    Flux<JobEntity> findByProcessInstanceId(String processInstanceId);

    Flux<JobEntity> findByLockOwner(String lockOwner);

    Mono<JobEntity> findByNodeIdAndProcessInstanceId(String nodeId, String processInstanceId);

}