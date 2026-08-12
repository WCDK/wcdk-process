package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_process_instance")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ProcessInstanceEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("process_definition_id")
    private String processDefinitionId;

    @Column("process_definition_key")
    private String processDefinitionKey;

    @Column("process_definition_version")
    private Integer processDefinitionVersion;

    @Column("business_key")
    private String businessKey;

    @Column("parent_process_instance_id")
    private String parentProcessInstanceId;

    @Column("root_process_instance_id")
    private String rootProcessInstanceId;

    private String starter;

    @Column("start_time")
    private Instant startTime;

    @Column("end_time")
    private Instant endTime;

    @Column("duration_ms")
    private Long durationMs;

    private String status;

    @Column("suspension_state")
    private Integer suspensionState;

    @Column("error_message")
    private String errorMessage;

    private Long revision;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}