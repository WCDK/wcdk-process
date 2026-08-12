package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_execution")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ExecutionEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("process_instance_id")
    private String processInstanceId;

    @Column("process_definition_id")
    private String processDefinitionId;

    @Column("parent_id")
    private String parentId;

    @Column("scope_execution_id")
    private String scopeExecutionId;

    @Column("root_execution_id")
    private String rootExecutionId;

    @Column("node_id")
    private String nodeId;

    @Column("node_type")
    private String nodeType;

    private String state;

    @Column("is_scope")
    private Integer isScope;

    @Column("is_concurrent")
    private Integer isConcurrent;

    @Column("is_event_scope")
    private Integer isEventScope;

    @Column("is_multi_instance")
    private Integer isMultiInstance;

    @Column("multi_instance_index")
    private Integer multiInstanceIndex;

    @Column("multi_instance_total")
    private Integer multiInstanceTotal;

    @Column("suspension_state")
    private Integer suspensionState;

    private Long revision;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}