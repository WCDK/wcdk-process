package com.wcdk.process.repository;

import com.wcdk.process.entity.ProcessDefinitionEntity;
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
public interface ProcessDefinitionRepository extends BaseRepository<ProcessDefinitionEntity> {

    Flux<ProcessDefinitionEntity> findByTenantIdAndKey(String tenantId, String key);

    Mono<ProcessDefinitionEntity> findByTenantIdAndKeyAndVersion(String tenantId, String key, Integer version);

    Mono<Long> countByTenantIdAndKey(String tenantId, String key);
}