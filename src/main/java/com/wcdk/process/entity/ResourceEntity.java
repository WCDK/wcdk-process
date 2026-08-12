package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_resource")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ResourceEntity {

    @Id
    private String id;

    @Column("deployment_id")
    private String deploymentId;

    @Column("tenant_id")
    private String tenantId;

    private String name;

    @Column("resource_type")
    private String resourceType;

    private String content;

    @Column("content_bytes")
    private byte[] contentBytes;

    @Column("created_at")
    private Instant createdAt;
}