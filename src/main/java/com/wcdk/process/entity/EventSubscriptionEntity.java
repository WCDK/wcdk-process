package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_event_subscription")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class EventSubscriptionEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("process_instance_id")
    private String processInstanceId;

    @Column("execution_id")
    private String executionId;

    @Column("event_type")
    private String eventType;

    @Column("event_name")
    private String eventName;

    @Column("node_id")
    private String nodeId;

    private String state;

    @Column("created_at")
    private Instant createdAt;
}