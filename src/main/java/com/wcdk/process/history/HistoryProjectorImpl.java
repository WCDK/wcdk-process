package com.wcdk.process.history;

import com.wcdk.process.entity.HistoryEventEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.entity.TaskEntity;
import com.wcdk.process.enums.HistoryEventType;
import com.wcdk.process.repository.HistoryEventRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import com.wcdk.process.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class HistoryProjectorImpl implements HistoryProjector {

    private final HistoryEventRepository historyEventRepository;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;

    @Override
    public Mono<ProcessInstanceHistory> getProcessInstanceHistory(String processInstanceId) {
        return processInstanceRepository.selectById(processInstanceId)
                .flatMap(pi -> Mono.zip(
                                Mono.just(pi),
                                getActivityHistory(processInstanceId).collectList(),
                                getTaskHistory(processInstanceId).collectList(),
                                getVariableHistory(processInstanceId).collectList()
                        )
                        .map(tuple -> {
                            ProcessInstanceEntity entity = tuple.getT1();
                            List<ActivityHistory> activities = tuple.getT2();
                            List<TaskHistory> tasks = tuple.getT3();
                            List<VariableHistory> variables = tuple.getT4();

                            Duration duration = null;
                            if (entity.getStartTime() != null && entity.getEndTime() != null) {
                                duration = Duration.between(entity.getStartTime(), entity.getEndTime());
                            }

                            return ProcessInstanceHistory.builder()
                                    .processInstanceId(entity.getId())
                                    .processDefinitionId(entity.getProcessDefinitionId())
                                    .processDefinitionKey(entity.getProcessDefinitionKey())
                                    .processDefinitionVersion(entity.getProcessDefinitionVersion())
                                    .businessKey(entity.getBusinessKey())
                                    .starter(entity.getStarter())
                                    .status(entity.getStatus())
                                    .startTime(entity.getStartTime())
                                    .endTime(entity.getEndTime())
                                    .duration(duration)
                                    .activities(activities)
                                    .tasks(tasks)
                                    .variables(variables)
                                    .build();
                        }));
    }

    @Override
    public Flux<ActivityHistory> getActivityHistory(String processInstanceId) {
        return historyEventRepository.findByProcessInstanceId(processInstanceId)
                .filter(e -> isActivityEvent(e.getEventType()))
                .collectList()
                .flatMapMany(events -> {
                    Map<String, List<HistoryEventEntity>> grouped = events.stream()
                            .filter(e -> e.getNodeId() != null)
                            .collect(Collectors.groupingBy(HistoryEventEntity::getNodeId));

                    return Flux.fromIterable(grouped.entrySet())
                            .map(entry -> {
                                List<HistoryEventEntity> nodeEvents = entry.getValue();
                                HistoryEventEntity started = nodeEvents.stream()
                                        .filter(e -> HistoryEventType.ACTIVITY_STARTED.name().equals(e.getEventType()))
                                        .min(Comparator.comparing(HistoryEventEntity::getCreatedAt))
                                        .orElse(null);

                                HistoryEventEntity completed = nodeEvents.stream()
                                        .filter(e -> HistoryEventType.ACTIVITY_COMPLETED.name().equals(e.getEventType()))
                                        .max(Comparator.comparing(HistoryEventEntity::getCreatedAt))
                                        .orElse(null);

                                Duration duration = null;
                                if (started != null && started.getCreatedAt() != null
                                        && completed != null && completed.getCreatedAt() != null) {
                                    duration = Duration.between(started.getCreatedAt(), completed.getCreatedAt());
                                }

                                return ActivityHistory.builder()
                                        .nodeId(entry.getKey())
                                        .eventType(started != null ? started.getEventType() : null)
                                        .executionId(started != null ? started.getExecutionId() : null)
                                        .startedAt(started != null ? started.getCreatedAt() : null)
                                        .completedAt(completed != null ? completed.getCreatedAt() : null)
                                        .duration(duration)
                                        .build();
                            });
                });
    }

    @Override
    public Flux<TaskHistory> getTaskHistory(String processInstanceId) {
        return taskRepository.findByProcessInstanceId(processInstanceId)
                .map(task -> {
                    Duration claimDuration = null;
                    if (task.getCreateTime() != null && task.getClaimTime() != null) {
                        claimDuration = Duration.between(task.getCreateTime(), task.getClaimTime());
                    }

                    Duration completeDuration = null;
                    if (task.getClaimTime() != null && task.getCompleteTime() != null) {
                        completeDuration = Duration.between(task.getClaimTime(), task.getCompleteTime());
                    }

                    return TaskHistory.builder()
                            .taskId(task.getId())
                            .taskDefinitionKey(task.getTaskDefinitionKey())
                            .name(task.getName())
                            .assignee(task.getAssignee())
                            .state(task.getState())
                            .createdAt(task.getCreateTime())
                            .claimedAt(task.getClaimTime())
                            .completedAt(task.getCompleteTime())
                            .claimDuration(claimDuration)
                            .completeDuration(completeDuration)
                            .build();
                });
    }

    @Override
    public Flux<VariableHistory> getVariableHistory(String processInstanceId) {
        return historyEventRepository.findByProcessInstanceId(processInstanceId)
                .filter(e -> isVariableEvent(e.getEventType()))
                .map(event -> VariableHistory.builder()
                        .variableId(event.getId())
                        .name(event.getEventName())
                        .value(event.getPayload())
                        .eventType(event.getEventType())
                        .timestamp(event.getCreatedAt())
                        .build());
    }

    @Override
    public Flux<ProcessInstanceHistory> getProcessInstancesByDefinitionKey(String processDefinitionKey) {
        return processInstanceRepository.findByStatus("RUNNING")
                .filter(pi -> processDefinitionKey.equals(pi.getProcessDefinitionKey()))
                .flatMap(pi -> getProcessInstanceHistory(pi.getId()));
    }

    private boolean isActivityEvent(String eventType) {
        return HistoryEventType.ACTIVITY_STARTED.name().equals(eventType)
                || HistoryEventType.ACTIVITY_COMPLETED.name().equals(eventType);
    }

    private boolean isVariableEvent(String eventType) {
        return HistoryEventType.VARIABLE_CREATED.name().equals(eventType)
                || HistoryEventType.VARIABLE_UPDATED.name().equals(eventType)
                || HistoryEventType.VARIABLE_DELETED.name().equals(eventType);
    }
}