package com.wcdk.process.engine;

import com.wcdk.process.entity.*;
import com.wcdk.process.execution.ProcessGraph;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveRepositoryService {

    Mono<ProcessDefinitionEntity> getProcessDefinitionById(String id);

    Flux<ProcessDefinitionEntity> getProcessDefinitionsByKey(String key);

    Mono<ProcessDefinitionEntity> getLatestProcessDefinitionByKey(String tenantId, String key);

    Mono<ProcessInstanceEntity> getProcessInstanceById(String id);

    Flux<ProcessInstanceEntity> getProcessInstancesByDefinitionId(String processDefinitionId);

    Mono<ExecutionEntity> getExecutionById(String id);

    Flux<ExecutionEntity> getExecutionsByProcessInstanceId(String processInstanceId);

    Mono<TaskEntity> getTaskById(String id);

    Flux<TaskEntity> getTasksByProcessInstanceId(String processInstanceId);

    Mono<ProcessGraph> getProcessGraph(String processDefinitionId);
}