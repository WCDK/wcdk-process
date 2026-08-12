package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_job")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class JobEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("process_instance_id")
    private String processInstanceId;

    @Column("execution_id")
    private String executionId;

    @Column("process_definition_id")
    private String processDefinitionId;

    @Column("node_id")
    private String nodeId;

    @Column("job_type")
    private String jobType;

    private String status;

    @Column("due_at")
    private Instant dueAt;

    @Column("retry_count")
    private Integer retryCount;

    @Column("max_retries")
    private Integer maxRetries;

    @Column("lock_owner")
    private String lockOwner;

    @Column("lock_token")
    private String lockToken;

    @Column("lock_until")
    private Instant lockUntil;

    private String payload;

    @Column("last_error")
    private String lastError;

    private Long revision;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}