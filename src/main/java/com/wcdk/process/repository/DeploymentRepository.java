package com.wcdk.process.repository;

import com.wcdk.process.entity.DeploymentEntity;
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
public interface DeploymentRepository extends BaseRepository<DeploymentEntity> {

    Flux<DeploymentEntity> findByTenantId(String tenantId);

    Flux<DeploymentEntity> findByTenantIdAndName(String tenantId, String name);

    Mono<Long> countByTenantId(String tenantId);
}