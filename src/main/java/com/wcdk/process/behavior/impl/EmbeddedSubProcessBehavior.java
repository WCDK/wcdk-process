package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.engine.ReactiveRepositoryService;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.enums.NodeType;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.execution.ReactiveVariableManager;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 嵌入式子流程行为实现。
 * <p>处理 BPMN 流程定义中的嵌入式子流程（Embedded Sub-Process）节点。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>解析子流程的流程图定义（ProcessGraph）</li>
 *   <li>创建子执行实例并启动子流程</li>
 *   <li>将父流程的变量复制到子流程作用域</li>
 *   <li>子流程完成后恢复父流程执行</li>
 * </ul>
 * @author wcdk
 */
@Component
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class EmbeddedSubProcessBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final HistoryEventRepository historyEventRepository;
    private final ReactiveVariableManager variableManager;
    private final ReactiveAgenda agenda;
    private final ReactiveRepositoryService repositoryService;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();
        NodeDefinition subProcessNode = graph.getNode(execution.getNodeId());

        execution.setState(ExecutionState.ACTIVE.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createActivityStartedEvent(context));
                }))
                .then(Mono.defer(() -> {
                    return startSubProcess(context, execution, graph, subProcessNode);
                }))
                .then();
    }

    private Mono<Void> startSubProcess(ExecutionContext context, ExecutionEntity parentExecution,
                                       ProcessGraph parentGraph, NodeDefinition subProcessNode) {
        ProcessGraph subGraph = getSubProcessGraph(subProcessNode);
        if (subGraph == null) {
            return Mono.error(new com.wcdk.process.exception.ProcessEngineException(
                    "SubProcess graph not found for node: " + subProcessNode.getId()));
        }

        String subProcessExecutionId = UUID.randomUUID().toString();
        String subProcessScopeExecutionId = UUID.randomUUID().toString();

        ExecutionEntity subExecution = new ExecutionEntity();
        subExecution.setId(subProcessExecutionId);
        subExecution.setTenantId(parentExecution.getTenantId());
        subExecution.setProcessInstanceId(parentExecution.getProcessInstanceId());
        subExecution.setProcessDefinitionId(parentExecution.getProcessDefinitionId());
        subExecution.setParentId(parentExecution.getId());
        subExecution.setScopeExecutionId(subProcessScopeExecutionId);
        subExecution.setRootExecutionId(parentExecution.getRootExecutionId());
        subExecution.setNodeId(subGraph.getStartNodeId());
        subExecution.setNodeType(NodeType.START_EVENT.name());
        subExecution.setState(ExecutionState.ACTIVE.name());
        subExecution.setIsScope(1);
        subExecution.setIsConcurrent(0);
        subExecution.setIsEventScope(0);
        subExecution.setIsMultiInstance(0);
        subExecution.setSuspensionState(1);
        subExecution.setRevision(1L);
        subExecution.setCreatedAt(Instant.now());
        subExecution.setUpdatedAt(Instant.now());

        return executionRepository.insert(subExecution)
                .then(Mono.defer(() -> {
                    return copyParentVariablesToSubProcess(context.getProcessInstanceId(),
                            parentExecution.getId(), subProcessExecutionId);
                }))
                .then(Mono.defer(() -> {
                    agenda.planContinueProcessInNode(subProcessExecutionId, subGraph.getStartNodeId());
                    return Mono.empty();
                }));
    }

    private ProcessGraph getSubProcessGraph(NodeDefinition subProcessNode) {
        String subGraphJson = (String) subProcessNode.getProperty("subGraphJson");
        if (subGraphJson == null) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            ProcessGraph subGraph = mapper.readValue(subGraphJson, ProcessGraph.class);
            return subGraph;
        } catch (Exception e) {
            return null;
        }
    }

    private Mono<Void> copyParentVariablesToSubProcess(String processInstanceId,
                                                       String parentExecutionId,
                                                       String subProcessExecutionId) {
        return variableManager.getAllExecutionVariables(parentExecutionId)
                .flatMap(entry -> variableManager.setExecutionVariable(
                        subProcessExecutionId, entry.name(), entry.value()))
                .then();
    }

    public Mono<Void> completeSubProcess(ExecutionContext context, String subProcessExecutionId) {
        return executionRepository.selectById(subProcessExecutionId)
                .flatMap(subExecution -> {
                    subExecution.setState(ExecutionState.COMPLETED.name());
                    subExecution.setUpdatedAt(Instant.now());
                    return executionRepository.updateById(subExecution);
                })
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createActivityCompletedEvent(context, subProcessExecutionId));
                }))
                .then(Mono.defer(() -> {
                    return executionRepository.selectById(subProcessExecutionId);
                }))
                .flatMap(subExecution -> {
                    return executionRepository.selectById(subExecution.getParentId())
                            .flatMap(parentExecution -> {
                                parentExecution.setState(ExecutionState.ACTIVE.name());
                                parentExecution.setUpdatedAt(Instant.now());
                                return executionRepository.updateById(parentExecution)
                                        .then(Mono.defer(() -> {
                                            agenda.planContinueProcessInNode(
                                                    parentExecution.getId(),
                                                    parentExecution.getNodeId());
                                            return Mono.empty();
                                        }));
                            });
                })
                .then();
    }

    private com.wcdk.process.entity.HistoryEventEntity createActivityStartedEvent(ExecutionContext context) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(context.getProcessInstanceId());
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(context.getExecutionId());
        event.setEventType(HistoryEventType.ACTIVITY_STARTED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }

    private com.wcdk.process.entity.HistoryEventEntity createActivityCompletedEvent(ExecutionContext context, String executionId) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(context.getProcessInstanceId());
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(executionId);
        event.setEventType(HistoryEventType.ACTIVITY_COMPLETED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }
}