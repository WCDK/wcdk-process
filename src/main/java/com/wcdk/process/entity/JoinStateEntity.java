package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_join_state")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class JoinStateEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("process_instance_id")
    private String processInstanceId;

    @Column("scope_execution_id")
    private String scopeExecutionId;

    @Column("gateway_id")
    private String gatewayId;

    @Column("cycle_key")
    private String cycleKey;

    @Column("expected_count")
    private Integer expectedCount;

    @Column("arrived_count")
    private Integer arrivedCount;

    private String status;

    private Long revision;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}