package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class TaskResponse {

    private String id;
    private String tenantId;
    private String processInstanceId;
    private String executionId;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private String name;
    private String state;
    private String assignee;
    private String owner;
    private Integer priority;
    private Instant dueTime;
    private Instant createTime;
    private Instant claimTime;
    private Instant completeTime;
    private String description;
    private Long revision;
    private Instant createdAt;
    private Instant updatedAt;
}