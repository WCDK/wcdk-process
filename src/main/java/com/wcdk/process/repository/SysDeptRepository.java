package com.wcdk.process.repository;

import com.wcdk.process.entity.SysDeptEntity;
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
public interface SysDeptRepository extends BaseRepository<SysDeptEntity> {

    Flux<SysDeptEntity> findByParentId(Long parentId);

    Mono<SysDeptEntity> findByDeptCode(String deptCode);

    Flux<SysDeptEntity> findByStatus(Integer status);
}