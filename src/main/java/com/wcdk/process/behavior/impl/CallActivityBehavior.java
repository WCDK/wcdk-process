package com.wcdk.process.behavior.impl;

import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.engine.ReactiveRepositoryService;
import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.enums.ExecutionState;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.enums.ProcessStatus;
import com.wcdk.process.exception.ProcessEngineException;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.execution.ReactiveVariableManager;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.HistoryEventRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 调用活动行为实现。
 * <p>处理 BPMN 流程定义中的调用活动（Call Activity）节点，用于调用外部流程。</p>
 * <p>主要职责：</p>
 * <ul>
 *   <li>根据调用元素键（calledElement）查找目标流程定义</li>
 *   <li>创建子流程实例并启动被调用流程</li>
 *   <li>支持入参变量映射（inVariableMapping），将父流程变量传入子流程</li>
 *   <li>支持出参变量映射（outVariableMapping），子流程完成后将变量回传父流程</li>
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
public class CallActivityBehavior implements ReactiveNodeBehavior {

    private final ExecutionRepository executionRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final HistoryEventRepository historyEventRepository;
    private final ReactiveVariableManager variableManager;
    private final ReactiveAgenda agenda;
    private final ReactiveRepositoryService repositoryService;

    @Override
    public Mono<Void> execute(ExecutionContext context) {
        ExecutionEntity execution = context.getExecution();
        ProcessGraph graph = context.getProcessGraph();
        NodeDefinition callActivityNode = graph.getNode(execution.getNodeId());

        String calledElementKey = (String) callActivityNode.getProperty("calledElement");
        String inVariableMapping = (String) callActivityNode.getProperty("inVariableMapping");
        String outVariableMapping = (String) callActivityNode.getProperty("outVariableMapping");

        execution.setState(ExecutionState.ACTIVE.name());
        execution.setUpdatedAt(Instant.now());

        return executionRepository.updateById(execution)
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(createActivityStartedEvent(context));
                }))
                .then(Mono.defer(() -> {
                    return startCalledProcess(context, execution, calledElementKey,
                            inVariableMapping, outVariableMapping);
                }))
                .then();
    }

    private Mono<Void> startCalledProcess(ExecutionContext context, ExecutionEntity parentExecution,
                                           String calledElementKey, String inVariableMapping,
                                           String outVariableMapping) {
        return repositoryService.getLatestProcessDefinitionByKey(
                context.getProcessInstance().getTenantId(), calledElementKey)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Process definition not found: " + calledElementKey)))
                .flatMap(calledDef -> {
                    ProcessInstanceEntity childPi = new ProcessInstanceEntity();
                    childPi.setId(UUID.randomUUID().toString());
                    childPi.setTenantId(context.getProcessInstance().getTenantId());
                    childPi.setProcessDefinitionId(calledDef.getId());
                    childPi.setProcessDefinitionKey(calledDef.getKey());
                    childPi.setProcessDefinitionVersion(calledDef.getVersion());
                    childPi.setParentProcessInstanceId(context.getProcessInstanceId());
                    childPi.setRootProcessInstanceId(context.getProcessInstance().getRootProcessInstanceId());
                    childPi.setStarter(context.getProcessInstance().getStarter());
                    childPi.setStartTime(Instant.now());
                    childPi.setStatus(ProcessStatus.RUNNING.name());
                    childPi.setSuspensionState(1);
                    childPi.setRevision(1L);
                    childPi.setCreatedAt(Instant.now());
                    childPi.setUpdatedAt(Instant.now());

                    return processInstanceRepository.insert(childPi)
                            .then(Mono.defer(() -> {
                                return copyInVariables(context, parentExecution.getId(),
                                        childPi.getId(), inVariableMapping);
                            }))
                            .then(Mono.defer(() -> {
                                return repositoryService.getProcessGraph(calledDef.getId());
                            }))
                            .flatMap(calledGraph -> {
                                if (calledGraph == null) {
                                    return Mono.error(new ProcessEngineException(
                                            "Process graph not found for: " + calledDef.getId()));
                                }

                                ExecutionEntity childExecution = new ExecutionEntity();
                                childExecution.setId(UUID.randomUUID().toString());
                                childExecution.setTenantId(childPi.getTenantId());
                                childExecution.setProcessInstanceId(childPi.getId());
                                childExecution.setProcessDefinitionId(calledDef.getId());
                                childExecution.setParentId(parentExecution.getId());
                                childExecution.setRootExecutionId(parentExecution.getRootExecutionId());
                                childExecution.setNodeId(calledGraph.getStartNodeId());
                                childExecution.setNodeType("START_EVENT");
                                childExecution.setState(ExecutionState.ACTIVE.name());
                                childExecution.setIsScope(1);
                                childExecution.setIsConcurrent(0);
                                childExecution.setIsEventScope(0);
                                childExecution.setIsMultiInstance(0);
                                childExecution.setSuspensionState(1);
                                childExecution.setRevision(1L);
                                childExecution.setCreatedAt(Instant.now());
                                childExecution.setUpdatedAt(Instant.now());

                                return executionRepository.insert(childExecution)
                                        .then(Mono.defer(() -> {
                                            childPi.setRootProcessInstanceId(childPi.getId());
                                            childExecution.setRootExecutionId(childExecution.getId());
                                            childExecution.setScopeExecutionId(childExecution.getId());
                                            return processInstanceRepository.updateById(childPi)
                                                    .then(executionRepository.updateById(childExecution));
                                        }))
                                        .then(Mono.defer(() -> {
                                            agenda.planContinueProcessInNode(
                                                    childExecution.getId(), calledGraph.getStartNodeId());
                                            return Mono.empty();
                                        }));
                            });
                })
                .then();
    }

    private Mono<Void> copyInVariables(ExecutionContext context, String parentExecutionId,
                                        String childProcessInstanceId, String inVariableMapping) {
        if (inVariableMapping == null || inVariableMapping.isBlank()) {
            return variableManager.getAllExecutionVariables(parentExecutionId)
                    .flatMap(entry -> variableManager.setProcessVariable(
                            childProcessInstanceId, entry.name(), entry.value()))
                    .then();
        }

        return Mono.empty();
    }

    public Mono<Void> completeCalledProcess(ExecutionContext context, String childProcessInstanceId,
                                            String outVariableMapping) {
        return processInstanceRepository.selectById(childProcessInstanceId)
                .flatMap(childPi -> {
                    childPi.setStatus(ProcessStatus.COMPLETED.name());
                    childPi.setEndTime(Instant.now());
                    if (childPi.getStartTime() != null) {
                        childPi.setDurationMs(java.time.Duration.between(
                                childPi.getStartTime(), childPi.getEndTime()).toMillis());
                    }
                    childPi.setUpdatedAt(Instant.now());
                    return processInstanceRepository.updateById(childPi);
                })
                .then(Mono.defer(() -> {
                    return historyEventRepository.insert(
                            createActivityCompletedEvent(context, childProcessInstanceId));
                }))
                .then(Mono.defer(() -> {
                    return copyOutVariables(context, childProcessInstanceId,
                            context.getProcessInstanceId(), outVariableMapping);
                }))
                .then(Mono.defer(() -> {
                    return executionRepository.selectById(context.getExecutionId());
                }))
                .flatMap(execution -> {
                    agenda.planContinueProcessInNode(execution.getId(), execution.getNodeId());
                    return Mono.empty();
                })
                .then();
    }

    private Mono<Void> copyOutVariables(ExecutionContext context, String childProcessInstanceId,
                                         String parentProcessInstanceId, String outVariableMapping) {
        if (outVariableMapping == null || outVariableMapping.isBlank()) {
            return variableManager.getAllVariables("PROCESS", childProcessInstanceId)
                    .filter(entry -> !"rootProcessInstanceId".equals(entry.name()))
                    .filter(entry -> !"parentProcessInstanceId".equals(entry.name()))
                    .flatMap(entry -> variableManager.setProcessVariable(
                            parentProcessInstanceId, entry.name(), entry.value()))
                    .then();
        }

        return Mono.empty();
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

    private com.wcdk.process.entity.HistoryEventEntity createActivityCompletedEvent(ExecutionContext context,
                                                                                      String processInstanceId) {
        com.wcdk.process.entity.HistoryEventEntity event = new com.wcdk.process.entity.HistoryEventEntity();
        event.setTenantId(context.getProcessInstance().getTenantId());
        event.setProcessInstanceId(processInstanceId);
        event.setProcessDefinitionId(context.getProcessDefinitionId());
        event.setExecutionId(context.getExecutionId());
        event.setEventType(HistoryEventType.ACTIVITY_COMPLETED.name());
        event.setNodeId(context.getCurrentNodeId());
        event.setCreatedAt(Instant.now());
        return event;
    }
}