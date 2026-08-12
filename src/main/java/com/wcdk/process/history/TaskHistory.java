package com.wcdk.process.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

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
public class TaskHistory {

    private String taskId;
    private String taskDefinitionKey;
    private String name;
    private String assignee;
    private String state;
    private Instant createdAt;
    private Instant claimedAt;
    private Instant completedAt;
    private Duration claimDuration;
    private Duration completeDuration;
}