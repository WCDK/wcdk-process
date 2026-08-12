package com.wcdk.process.engine;

import com.wcdk.process.entity.ProcessInstanceEntity;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveRuntimeService {

    Mono<ProcessInstanceEntity> startProcessByKey(String processDefinitionKey, String businessKey, String starter, Map<String, Object> variables);

    Mono<ProcessInstanceEntity> startProcessByKey(String tenantId, String processDefinitionKey, String businessKey, String starter, Map<String, Object> variables);

    Mono<ProcessInstanceEntity> getProcessInstanceById(String processInstanceId);

    Mono<Void> suspendProcessInstance(String processInstanceId);

    Mono<Void> activateProcessInstance(String processInstanceId);

    Mono<Void> terminateProcessInstance(String processInstanceId);

    Mono<Void> suspendExecution(String executionId);

    Mono<Void> activateExecution(String executionId);

    Mono<Void> suspendProcessDefinition(String processDefinitionId);

    Mono<Void> activateProcessDefinition(String processDefinitionId);
}