package com.wcdk.process.distributed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_distributed_lock")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class DistributedLockEntity {

    @Id
    private String id;

    @Column("lock_key")
    private String lockKey;

    private String token;

    private String owner;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("created_at")
    private Instant createdAt;
}