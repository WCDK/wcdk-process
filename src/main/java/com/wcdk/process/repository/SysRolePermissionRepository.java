package com.wcdk.process.repository;

import com.wcdk.process.entity.SysRolePermissionEntity;
import com.wcdk.r2dbc.BaseRepository;
import com.wcdk.r2dbc.Repository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface SysRolePermissionRepository extends BaseRepository<SysRolePermissionEntity> {

    Flux<SysRolePermissionEntity> findByRoleId(Long roleId);

    Mono<Long> deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

    Flux<SysRolePermissionEntity> findByPermissionId(Long permissionId);
}