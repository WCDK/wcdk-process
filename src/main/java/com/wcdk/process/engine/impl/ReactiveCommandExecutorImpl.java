package com.wcdk.process.engine.impl;

import com.wcdk.process.engine.ReactiveCommand;
import com.wcdk.process.engine.ReactiveCommandExecutor;
import com.wcdk.process.engine.ReactiveAgenda;
import com.wcdk.process.engine.ReactiveNodeBehavior;
import com.wcdk.process.engine.ReactiveRepositoryService;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.enums.NodeType;
import com.wcdk.process.exception.ProcessEngineException;
import com.wcdk.process.execution.ExecutionContext;
import com.wcdk.process.execution.NodeDefinition;
import com.wcdk.process.execution.ProcessGraph;
import com.wcdk.process.execution.ReactiveVariableManager;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
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
public class ReactiveCommandExecutorImpl implements ReactiveCommandExecutor {

    private final ReactiveRepositoryService repositoryService;
    private final ReactiveAgenda agenda;
    private final ReactiveVariableManager variableManager;
    private final ExecutionRepository executionRepository;
    private final ApplicationContext applicationContext;
    private final ProcessInstanceRepository processInstanceRepository;

    @Override
    public <T> Mono<T> execute(ReactiveCommand<T> command) {
        return Mono.deferContextual(ctx -> {
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.setVariableManager(variableManager);
            return command.execute(executionContext)
                    .flatMap(result -> processAgendaOperations().then(Mono.just(result)));
        });
    }

    public Mono<Void> flushAgenda() {
        return processAgendaOperations();
    }

    private Mono<Void> processAgendaOperations() {
        return Mono.defer(() -> {
            if (!agenda.hasOperations()) {
                return Mono.empty();
            }
            ReactiveAgendaImpl.Operation operation = agenda.nextOperation();
            return processOperation(operation)
                    .then(Mono.defer(this::processAgendaOperations));
        });
    }

    private Mono<Void> processOperation(ReactiveAgendaImpl.Operation operation) {
        return switch (operation.getType()) {
            case "CONTINUE_PROCESS" -> continueProcessInNode(operation.getExecutionId(), operation.getTargetId());
            case "TAKE_SEQUENCE_FLOW" -> takeSequenceFlow(operation.getExecutionId(), operation.getTargetId());
            case "END_PROCESS" -> endProcess(operation.getExecutionId());
            case "COMPLETE_TASK" -> completeTask(operation.getExecutionId(), operation.getTargetId());
            default -> Mono.error(new ProcessEngineException("Unknown operation type: " + operation.getType()));
        };
    }

    private Mono<Void> continueProcessInNode(String executionId, String nodeId) {
        return repositoryService.getExecutionById(executionId)
                .flatMap(execution -> repositoryService.getProcessInstanceById(execution.getProcessInstanceId())
                        .zipWith(repositoryService.getProcessDefinitionById(execution.getProcessDefinitionId()))
                        .flatMap(tuple -> {
                            ProcessInstanceEntity pi = tuple.getT1();
                            ProcessDefinitionEntity pd = tuple.getT2();
                            return repositoryService.getProcessGraph(pd.getId())
                                    .switchIfEmpty(Mono.error(new ProcessEngineException("Process graph not found for: " + pd.getId())))
                                    .flatMap(graph -> {
                                        NodeDefinition node = graph.getNode(nodeId);
                                        if (node == null) {
                                            return Mono.error(new ProcessEngineException("Node not found: " + nodeId));
                                        }
                                        ExecutionContext context = new ExecutionContext();
                                        context.setProcessInstance(pi);
                                        context.setProcessDefinition(pd);
                                        context.setExecution(execution);
                                        context.setProcessGraph(graph);
                                        context.setVariableManager(variableManager);
                                        return executeNodeBehavior(node, context);
                                    });
                        }));
    }

    private Mono<Void> executeNodeBehavior(NodeDefinition node, ExecutionContext context) {
        String behaviorClassName = node.getStringProperty("behaviorClass", null);
        if (behaviorClassName == null) {
            behaviorClassName = getDefaultBehaviorClass(node.getNodeType());
        }
        if (behaviorClassName == null) {
            return Mono.error(new ProcessEngineException("No behavior class for node type: " + node.getNodeType()));
        }
        try {
            Class<?> clazz = Class.forName(behaviorClassName);
            ReactiveNodeBehavior behavior = applicationContext.getBean((Class<? extends ReactiveNodeBehavior>) clazz);
            return behavior.execute(context);
        } catch (ClassNotFoundException e) {
            return Mono.error(new ProcessEngineException("Behavior class not found: " + behaviorClassName, e));
        } catch (Exception e) {
            return Mono.error(new ProcessEngineException("Failed to instantiate behavior: " + behaviorClassName, e));
        }
    }

    private String getDefaultBehaviorClass(NodeType nodeType) {
        return switch (nodeType) {
            case START_EVENT -> "com.wcdk.process.behavior.impl.StartEventBehavior";
            case END_EVENT -> "com.wcdk.process.behavior.impl.EndEventBehavior";
            case USER_TASK -> "com.wcdk.process.behavior.impl.UserTaskBehavior";
            case EXCLUSIVE_GATEWAY -> "com.wcdk.process.behavior.impl.ExclusiveGatewayBehavior";
            case SUB_PROCESS -> "com.wcdk.process.behavior.impl.EmbeddedSubProcessBehavior";
            case BOUNDARY_EVENT -> "com.wcdk.process.behavior.impl.BoundaryEventBehavior";
            case SERVICE_TASK -> "com.wcdk.process.behavior.impl.MultiInstanceBehavior";
            case SCRIPT_TASK -> "com.wcdk.process.behavior.impl.MultiInstanceBehavior";
            case CALL_ACTIVITY -> "com.wcdk.process.behavior.impl.CallActivityBehavior";
            case INCLUSIVE_GATEWAY -> "com.wcdk.process.behavior.impl.InclusiveGatewayBehavior";
            case PARALLEL_GATEWAY -> "com.wcdk.process.behavior.impl.ParallelGatewayBehavior";
            default -> null;
        };
    }

    private Mono<Void> takeSequenceFlow(String executionId, String edgeId) {
        return repositoryService.getExecutionById(executionId)
                .flatMap(execution -> repositoryService.getProcessInstanceById(execution.getProcessInstanceId())
                        .zipWith(repositoryService.getProcessDefinitionById(execution.getProcessDefinitionId()))
                        .flatMap(tuple -> repositoryService.getProcessGraph(tuple.getT2().getId())
                                .switchIfEmpty(Mono.error(new ProcessEngineException("Process graph not found")))
                                .flatMap(graph -> {
                                    var edge = graph.getEdge(edgeId);
                                    if (edge == null) {
                                        return Mono.error(new ProcessEngineException("Edge not found: " + edgeId));
                                    }
                                    execution.setNodeId(edge.getTargetNodeId());
                                    execution.setUpdatedAt(java.time.Instant.now());
                                    return executionRepository.updateById(execution)
                                            .then(Mono.defer(() -> {
                                                agenda.planContinueProcessInNode(executionId, edge.getTargetNodeId());
                                                return Mono.empty();
                                            }));
                                })));
    }

    private Mono<Void> processInstanceRepositoryUpdate(ProcessInstanceEntity pi) {
        pi.setStatus("COMPLETED");
        pi.setEndTime(java.time.Instant.now());
        pi.setUpdatedAt(java.time.Instant.now());
        if (pi.getStartTime() != null) {
            pi.setDurationMs(java.time.Duration.between(pi.getStartTime(), pi.getEndTime()).toMillis());
        }
        return processInstanceRepository.updateById(pi).then();
    }
    private Mono<Void> completeTask(String executionId, String taskId) {
        return repositoryService.getTaskById(taskId)
                .switchIfEmpty(Mono.error(new ProcessEngineException("Task not found: " + taskId)))
                .flatMap(task -> {
                    if (!"COMPLETED".equals(task.getState())) {
                        return Mono.error(new ProcessEngineException("Task is not completed: " + taskId));
                    }
                    return repositoryService.getExecutionById(executionId)
                            .switchIfEmpty(Mono.error(new ProcessEngineException("Execution not found: " + executionId)))
                            .flatMap(execution -> {
                                execution.setState(com.wcdk.process.enums.ExecutionState.ACTIVE.name());
                                execution.setUpdatedAt(java.time.Instant.now());
                                return executionRepository.updateById(execution)
                                        .then(repositoryService.getProcessDefinitionById(execution.getProcessDefinitionId()))
                                        .flatMap(pd -> repositoryService.getProcessGraph(pd.getId()))
                                        .flatMap(graph -> {
                                            var outgoing = graph.getOutgoingEdges(execution.getNodeId());
                                            if (outgoing.isEmpty()) agenda.planEndProcess(executionId);
                                            else outgoing.forEach(edge -> agenda.planTakeSequenceFlow(executionId, edge.getId()));
                                            return Mono.empty();
                                        });
                            });
                });
    }
    private Mono<Void> endProcess(String executionId) {
        return repositoryService.getExecutionById(executionId)
                .flatMap(execution -> repositoryService.getProcessInstanceById(execution.getProcessInstanceId())
                        .flatMap(pi -> {
                            execution.setState(com.wcdk.process.enums.ExecutionState.COMPLETED.name());
                            execution.setUpdatedAt(java.time.Instant.now());
                            return executionRepository.updateById(execution)
                                    .then(processInstanceRepositoryUpdate(pi));
                        }));
}
}