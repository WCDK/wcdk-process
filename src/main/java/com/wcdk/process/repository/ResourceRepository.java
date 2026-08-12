package com.wcdk.process.repository;

import com.wcdk.process.entity.ResourceEntity;
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
public interface ResourceRepository extends BaseRepository<ResourceEntity> {

    Flux<ResourceEntity> findByDeploymentId(String deploymentId);

    Mono<ResourceEntity> findByDeploymentIdAndName(String deploymentId, String name);

    Flux<ResourceEntity> findByTenantId(String tenantId);
}