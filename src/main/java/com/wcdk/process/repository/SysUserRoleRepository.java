package com.wcdk.process.repository;

import com.wcdk.process.entity.SysUserRoleEntity;
import com.wcdk.r2dbc.BaseRepository;
import com.wcdk.r2dbc.Repository;
import reactor.core.publisher.Flux;

@Repository
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface SysUserRoleRepository extends BaseRepository<SysUserRoleEntity> {

    Flux<SysUserRoleEntity> findByUserId(Long userId);

    Flux<SysUserRoleEntity> findByRoleId(Long roleId);
}