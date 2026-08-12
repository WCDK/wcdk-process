package com.wcdk.process.repository;

import com.wcdk.process.entity.IdentityLinkEntity;
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
public interface IdentityLinkRepository extends BaseRepository<IdentityLinkEntity> {

    Flux<IdentityLinkEntity> findByTaskId(String taskId);

    Flux<IdentityLinkEntity> findByProcessInstanceId(String processInstanceId);

    Flux<IdentityLinkEntity> findByUserId(String userId);

    Flux<IdentityLinkEntity> findByGroupId(String groupId);

    Flux<IdentityLinkEntity> findByTaskIdAndLinkType(String taskId, String linkType);
}