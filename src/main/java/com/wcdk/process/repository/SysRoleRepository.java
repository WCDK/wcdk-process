package com.wcdk.process.repository;

import com.wcdk.process.entity.SysRoleEntity;
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
public interface SysRoleRepository extends BaseRepository<SysRoleEntity> {

    Mono<SysRoleEntity> findByRoleCode(String roleCode);

    Flux<SysRoleEntity> findByStatus(Integer status);
}