package com.wcdk.process.repository;

import com.wcdk.process.entity.ProcessInstanceEntity;
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
public interface ProcessInstanceRepository extends BaseRepository<ProcessInstanceEntity> {

    Flux<ProcessInstanceEntity> findByProcessDefinitionId(String processDefinitionId);

    Flux<ProcessInstanceEntity> findByTenantIdAndBusinessKey(String tenantId, String businessKey);

    Flux<ProcessInstanceEntity> findByStarter(String starter);

    Flux<ProcessInstanceEntity> findByParentProcessInstanceId(String parentProcessInstanceId);

    Flux<ProcessInstanceEntity> findByStatus(String status);
}