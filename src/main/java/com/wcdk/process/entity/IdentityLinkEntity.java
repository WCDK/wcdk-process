package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_identity_link")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class IdentityLinkEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("task_id")
    private String taskId;

    @Column("process_instance_id")
    private String processInstanceId;

    @Column("link_type")
    private String linkType;

    @Column("user_id")
    private String userId;

    @Column("group_id")
    private String groupId;

    @Column("created_at")
    private Instant createdAt;
}