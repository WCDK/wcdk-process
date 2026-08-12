package com.wcdk.process.migration;

import com.wcdk.process.entity.ExecutionEntity;
import com.wcdk.process.entity.ProcessDefinitionEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.exception.ProcessEngineException;
import com.wcdk.process.repository.ExecutionRepository;
import com.wcdk.process.repository.ProcessDefinitionRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import com.wcdk.process.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class MigrationServiceImpl implements MigrationService {

    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessDefinitionRepository processDefinitionRepository;
    private final ExecutionRepository executionRepository;
    private final TaskRepository taskRepository;

    @Override
    public Mono<MigrationResult> migrateProcessInstance(MigrationRequest request) {
        return processInstanceRepository.selectById(request.getProcessInstanceId())
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Process instance not found: " + request.getProcessInstanceId())))
                .flatMap(pi -> processDefinitionRepository.selectById(request.getTargetProcessDefinitionId())
                        .switchIfEmpty(Mono.error(new ProcessEngineException(
                                "Target process definition not found: " + request.getTargetProcessDefinitionId())))
                        .flatMap(targetPd -> {
                            if (targetPd.getSuspended() != null && targetPd.getSuspended() == 1) {
                                return Mono.error(new ProcessEngineException(
                                        "Target process definition is suspended: " + request.getTargetProcessDefinitionId()));
                            }

                            String sourceDefinitionId = pi.getProcessDefinitionId();

                            pi.setProcessDefinitionId(targetPd.getId());
                            pi.setProcessDefinitionKey(targetPd.getKey());
                            pi.setProcessDefinitionVersion(targetPd.getVersion());
                            pi.setUpdatedAt(Instant.now());

                            return processInstanceRepository.updateById(pi)
                                    .then(Mono.defer(() -> {
                                        return executionRepository.findByProcessInstanceId(pi.getId())
                                                .flatMap(execution -> {
                                                    execution.setProcessDefinitionId(targetPd.getId());
                                                    execution.setUpdatedAt(Instant.now());
                                                    return executionRepository.updateById(execution);
                                                })
                                                .then();
                                    }))
                                    .then(Mono.defer(() -> {
                                        return taskRepository.findByProcessInstanceId(pi.getId())
                                                .flatMap(task -> {
                                                    task.setProcessDefinitionId(targetPd.getId());
                                                    task.setUpdatedAt(Instant.now());
                                                    return taskRepository.updateById(task);
                                                })
                                                .then();
                                    }))
                                    .then(Mono.just(MigrationResult.builder()
                                            .processInstanceId(pi.getId())
                                            .sourceProcessDefinitionId(sourceDefinitionId)
                                            .targetProcessDefinitionId(targetPd.getId())
                                            .success(true)
                                            .message("Migration completed successfully")
                                            .build()));
                        }));
    }

    @Override
    public Flux<MigrationResult> migrateProcessInstances(Flux<MigrationRequest> requests) {
        return requests.concatMap(this::migrateProcessInstance);
    }

    @Override
    public Mono<Void> suspendProcessDefinition(String processDefinitionId) {
        return processDefinitionRepository.selectById(processDefinitionId)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Process definition not found: " + processDefinitionId)))
                .flatMap(pd -> {
                    pd.setSuspended(1);
                    pd.setUpdatedAt(Instant.now());
                    return processDefinitionRepository.updateById(pd);
                })
                .then();
    }

    @Override
    public Mono<Void> activateProcessDefinition(String processDefinitionId) {
        return processDefinitionRepository.selectById(processDefinitionId)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Process definition not found: " + processDefinitionId)))
                .flatMap(pd -> {
                    pd.setSuspended(0);
                    pd.setUpdatedAt(Instant.now());
                    return processDefinitionRepository.updateById(pd);
                })
                .then();
    }

    @Override
    public Mono<Void> suspendExecution(String executionId) {
        return executionRepository.selectById(executionId)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Execution not found: " + executionId)))
                .flatMap(execution -> {
                    execution.setSuspensionState(2);
                    execution.setUpdatedAt(Instant.now());
                    return executionRepository.updateById(execution);
                })
                .then();
    }

    @Override
    public Mono<Void> activateExecution(String executionId) {
        return executionRepository.selectById(executionId)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Execution not found: " + executionId)))
                .flatMap(execution -> {
                    execution.setSuspensionState(1);
                    execution.setUpdatedAt(Instant.now());
                    return executionRepository.updateById(execution);
                })
                .then();
    }

    @Override
    public Mono<Void> suspendTask(String taskId) {
        return taskRepository.selectById(taskId)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Task not found: " + taskId)))
                .flatMap(task -> {
                    task.setState("SUSPENDED");
                    task.setUpdatedAt(Instant.now());
                    return taskRepository.updateById(task);
                })
                .then();
    }

    @Override
    public Mono<Void> activateTask(String taskId) {
        return taskRepository.selectById(taskId)
                .switchIfEmpty(Mono.error(new ProcessEngineException(
                        "Task not found: " + taskId)))
                .flatMap(task -> {
                    task.setState("CREATED");
                    task.setUpdatedAt(Instant.now());
                    return taskRepository.updateById(task);
                })
                .then();
    }
}