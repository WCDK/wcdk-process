package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_task")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class TaskEntity {

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

    @Column("task_definition_key")
    private String taskDefinitionKey;

    private String name;

    private String state;

    private String assignee;

    private String owner;

    private Integer priority;

    @Column("due_time")
    private Instant dueTime;

    @Column("create_time")
    private Instant createTime;

    @Column("claim_time")
    private Instant claimTime;

    @Column("complete_time")
    private Instant completeTime;

    @Column("form_data")
    private String formData;

    private String description;

    private Long revision;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}