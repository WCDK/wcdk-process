package com.wcdk.process.repository;

import com.wcdk.process.entity.SysPermissionEntity;
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
public interface SysPermissionRepository extends BaseRepository<SysPermissionEntity> {

    Flux<SysPermissionEntity> findByParentId(Long parentId);

    Mono<SysPermissionEntity> findByPermissionCode(String permissionCode);

    Flux<SysPermissionEntity> findByPermissionType(String permissionType);

    Flux<SysPermissionEntity> findByStatus(Integer status);
}