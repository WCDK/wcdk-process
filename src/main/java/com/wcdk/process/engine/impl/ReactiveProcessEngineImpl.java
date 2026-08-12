package com.wcdk.process.engine.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.engine.ReactiveProcessEngine;
import com.wcdk.process.engine.ReactiveRepositoryService;
import com.wcdk.process.engine.ReactiveRuntimeService;
import com.wcdk.process.engine.ReactiveTaskService;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.ProcessDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ReactiveProcessEngineImpl implements ReactiveProcessEngine {

    private final ReactiveRuntimeService runtimeService;
    private final ReactiveTaskService taskService;
    private final ReactiveRepositoryService repositoryService;
    private final ReactiveCommandExecutorImpl commandExecutor;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ReactiveRuntimeService getRuntimeService() {
        return runtimeService;
    }

    @Override
    public ReactiveTaskService getTaskService() {
        return taskService;
    }

    @Override
    public ReactiveRepositoryService getRepositoryService() {
        return repositoryService;
    }

    @Override
    public ReactiveCommandExecutorImpl getCommandExecutor() {
        return commandExecutor;
    }

    @Override
    public Mono<ProcessGraph> getProcessGraph(String processDefinitionId) {
        return processDefinitionRepository.selectById(processDefinitionId)
                .flatMap(def -> {
                    if (def.getGraphJson() == null) {
                        return Mono.empty();
                    }
                    try {
                        ProcessGraph graph = objectMapper.readValue(def.getGraphJson(), ProcessGraph.class);
                        graph.setProcessDefinitionId(processDefinitionId);
                        return Mono.just(graph);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }
}