package com.wcdk.process.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessAuditReport {

    private String processInstanceId;
    private String processDefinitionKey;
    private String businessKey;
    private String starter;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private List<AuditEntry> entries;
    private AuditSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    public static class AuditEntry {
        private String eventType;
        private String nodeId;
        private String userId;
        private Instant timestamp;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    /**
     * WCDK 流程模块类型。
     *
     * @author WCDK
     * @version 1.0
     */
    public static class AuditSummary {
        private int totalActivities;
        private int completedActivities;
        private int totalTasks;
        private int completedTasks;
        private int totalVariables;
        private long totalDurationMs;
    }
}