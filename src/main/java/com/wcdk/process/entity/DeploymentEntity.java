package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_deployment")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class DeploymentEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    private String name;

    private String category;

    @Column("deployment_time")
    private Instant deploymentTime;

    @Column("source_system")
    private String sourceSystem;

    private String description;

    private Integer version;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}