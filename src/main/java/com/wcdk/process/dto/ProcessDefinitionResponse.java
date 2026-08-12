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
public class ProcessDefinitionResponse {

    private String id;
    private String tenantId;
    private String key;
    private String name;
    private Integer version;
    private String category;
    private String description;
    private String deploymentId;
    private String resourceName;
    private String diagramResourceName;
    private Integer suspended;
    private Instant createdAt;
    private Instant updatedAt;
}