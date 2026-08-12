package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_process_definition")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessDefinitionEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    private String key;

    private String name;

    private Integer version;

    private String category;

    private String description;

    @Column("deployment_id")
    private String deploymentId;

    @Column("resource_name")
    private String resourceName;

    @Column("diagram_resource_name")
    private String diagramResourceName;

    @Column("graph_json")
    private String graphJson;

    private Integer suspended;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}