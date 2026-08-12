package com.wcdk.process.engine.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wcdk.process.engine.ReactiveRepositoryService;
import com.wcdk.process.entity.*;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ReactiveRepositoryServiceImpl implements ReactiveRepositoryService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ExecutionRepository executionRepository;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<ProcessDefinitionEntity> getProcessDefinitionById(String id) {
        return processDefinitionRepository.selectById(id);
    }

    @Override
    public Flux<ProcessDefinitionEntity> getProcessDefinitionsByKey(String key) {
        return processDefinitionRepository.findByTenantIdAndKey("default", key);
    }

    @Override
    public Mono<ProcessDefinitionEntity> getLatestProcessDefinitionByKey(String tenantId, String key) {
        return processDefinitionRepository.findByTenantIdAndKeyAndVersion(tenantId, key, null)
                .switchIfEmpty(Mono.defer(() ->
                        processDefinitionRepository.findByTenantIdAndKey(tenantId, key)
                                .sort((a, b) -> Integer.compare(b.getVersion(), a.getVersion()))
                                .next()
                ));
    }

    @Override
    public Mono<ProcessInstanceEntity> getProcessInstanceById(String id) {
        return processInstanceRepository.selectById(id);
    }

    @Override
    public Flux<ProcessInstanceEntity> getProcessInstancesByDefinitionId(String processDefinitionId) {
        return processInstanceRepository.findByProcessDefinitionId(processDefinitionId);
    }

    @Override
    public Mono<ExecutionEntity> getExecutionById(String id) {
        return executionRepository.selectById(id);
    }

    @Override
    public Flux<ExecutionEntity> getExecutionsByProcessInstanceId(String processInstanceId) {
        return executionRepository.findByProcessInstanceId(processInstanceId);
    }

    @Override
    public Mono<TaskEntity> getTaskById(String id) {
        return taskRepository.selectById(id);
    }

    @Override
    public Flux<TaskEntity> getTasksByProcessInstanceId(String processInstanceId) {
        return taskRepository.findByProcessInstanceId(processInstanceId);
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