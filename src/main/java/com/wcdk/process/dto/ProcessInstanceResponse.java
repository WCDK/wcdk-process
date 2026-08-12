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
public class ProcessInstanceResponse {

    private String id;
    private String tenantId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private Integer processDefinitionVersion;
    private String businessKey;
    private String starter;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private String status;
    private Integer suspensionState;
    private Long revision;
    private Instant createdAt;
    private Instant updatedAt;
}