package com.wcdk.process.history;

import com.wcdk.process.entity.HistoryEventEntity;
import com.wcdk.process.entity.ProcessInstanceEntity;
import com.wcdk.process.repository.HistoryEventRepository;
import com.wcdk.process.repository.ProcessInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class HistoryAuditService {

    private final HistoryEventRepository historyEventRepository;
    private final ProcessInstanceRepository processInstanceRepository;

    public Mono<ProcessAuditReport> generateAuditReport(String processInstanceId) {
        return processInstanceRepository.selectById(processInstanceId)
                .switchIfEmpty(Mono.error(new RuntimeException("Process instance not found: " + processInstanceId)))
                .flatMap(pi -> historyEventRepository.findByProcessInstanceId(processInstanceId)
                        .collectList()
                        .map(events -> buildAuditReport(pi, events)));
    }

    private ProcessAuditReport buildAuditReport(ProcessInstanceEntity pi, List<HistoryEventEntity> events) {
        List<ProcessAuditReport.AuditEntry> entries = events.stream()
                .map(this::toAuditEntry)
                .sorted(Comparator.comparing(ProcessAuditReport.AuditEntry::getTimestamp))
                .collect(Collectors.toList());

        long totalDurationMs = 0;
        if (pi.getStartTime() != null && pi.getEndTime() != null) {
            totalDurationMs = Duration.between(pi.getStartTime(), pi.getEndTime()).toMillis();
        }

        long totalActivities = events.stream()
                .filter(e -> "ACTIVITY_STARTED".equals(e.getEventType()) || "ACTIVITY_COMPLETED".equals(e.getEventType()))
                .map(HistoryEventEntity::getNodeId)
                .distinct()
                .count();

        long completedActivities = events.stream()
                .filter(e -> "ACTIVITY_COMPLETED".equals(e.getEventType()))
                .map(HistoryEventEntity::getNodeId)
                .distinct()
                .count();

        long totalTasks = events.stream()
                .filter(e -> "TASK_CREATED".equals(e.getEventType()))
                .count();

        long completedTasks = events.stream()
                .filter(e -> "TASK_COMPLETED".equals(e.getEventType()))
                .count();

        long totalVariables = events.stream()
                .filter(e -> e.getEventType() != null && e.getEventType().startsWith("VARIABLE_"))
                .count();

        ProcessAuditReport.AuditSummary summary = ProcessAuditReport.AuditSummary.builder()
                .totalActivities((int) totalActivities)
                .completedActivities((int) completedActivities)
                .totalTasks((int) totalTasks)
                .completedTasks((int) completedTasks)
                .totalVariables((int) totalVariables)
                .totalDurationMs(totalDurationMs)
                .build();

        return ProcessAuditReport.builder()
                .processInstanceId(pi.getId())
                .processDefinitionKey(pi.getProcessDefinitionKey())
                .businessKey(pi.getBusinessKey())
                .starter(pi.getStarter())
                .status(pi.getStatus())
                .startTime(pi.getStartTime())
                .endTime(pi.getEndTime())
                .entries(entries)
                .summary(summary)
                .build();
    }

    private ProcessAuditReport.AuditEntry toAuditEntry(HistoryEventEntity event) {
        String description = buildEventDescription(event);
        return ProcessAuditReport.AuditEntry.builder()
                .eventType(event.getEventType())
                .nodeId(event.getNodeId())
                .userId(event.getUserId())
                .timestamp(event.getCreatedAt())
                .description(description)
                .build();
    }

    private String buildEventDescription(HistoryEventEntity event) {
        if (event.getEventType() == null) {
            return "Unknown event";
        }
        return switch (event.getEventType()) {
            case "PROCESS_STARTED" -> "流程启动";
            case "PROCESS_COMPLETED" -> "流程完成";
            case "PROCESS_TERMINATED" -> "流程终止";
            case "ACTIVITY_STARTED" -> "活动开始: " + event.getNodeId();
            case "ACTIVITY_COMPLETED" -> "活动完成: " + event.getNodeId();
            case "TASK_CREATED" -> "任务创建: " + event.getNodeId();
            case "TASK_CLAIMED" -> "任务认领: " + event.getNodeId();
            case "TASK_COMPLETED" -> "任务完成: " + event.getNodeId();
            case "TASK_CANCELLED" -> "任务取消: " + event.getNodeId();
            case "VARIABLE_CREATED" -> "变量创建: " + event.getEventName();
            case "VARIABLE_UPDATED" -> "变量更新: " + event.getEventName();
            case "VARIABLE_DELETED" -> "变量删除: " + event.getEventName();
            default -> event.getEventType();
        };
    }
}