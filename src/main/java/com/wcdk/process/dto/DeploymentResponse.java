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
public class DeploymentResponse {

    private String id;
    private String tenantId;
    private String name;
    private String category;
    private String description;
    private Instant deploymentTime;
    private String sourceSystem;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;
}