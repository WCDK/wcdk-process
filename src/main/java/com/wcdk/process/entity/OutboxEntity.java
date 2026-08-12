package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_outbox")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class OutboxEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("aggregate_type")
    private String aggregateType;

    @Column("aggregate_id")
    private String aggregateId;

    @Column("event_type")
    private String eventType;

    private String payload;

    private String status;

    @Column("retry_count")
    private Integer retryCount;

    @Column("max_retries")
    private Integer maxRetries;

    @Column("created_at")
    private Instant createdAt;

    @Column("processed_at")
    private Instant processedAt;
}