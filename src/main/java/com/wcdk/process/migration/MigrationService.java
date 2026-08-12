package com.wcdk.process.migration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface MigrationService {

    Mono<MigrationResult> migrateProcessInstance(MigrationRequest request);

    Flux<MigrationResult> migrateProcessInstances(Flux<MigrationRequest> requests);

    Mono<Void> suspendProcessDefinition(String processDefinitionId);

    Mono<Void> activateProcessDefinition(String processDefinitionId);

    Mono<Void> suspendExecution(String executionId);

    Mono<Void> activateExecution(String executionId);

    Mono<Void> suspendTask(String taskId);

    Mono<Void> activateTask(String taskId);
}