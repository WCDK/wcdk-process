package com.wcdk.process.engine;

import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.execution.ProcessGraph;
import reactor.core.publisher.Mono;

/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public interface ReactiveProcessEngine {

    ReactiveRuntimeService getRuntimeService();

    ReactiveTaskService getTaskService();

    ReactiveRepositoryService getRepositoryService();

    ReactiveCommandExecutor getCommandExecutor();

    Mono<ProcessGraph> getProcessGraph(String processDefinitionId);
}