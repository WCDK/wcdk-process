package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_variable")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class VariableEntity {

    @Id
    private String id;

    @Column("tenant_id")
    private String tenantId;

    @Column("process_instance_id")
    private String processInstanceId;

    @Column("scope_type")
    private String scopeType;

    @Column("scope_id")
    private String scopeId;

    private String name;

    @Column("value_type")
    private String valueType;

    @Column("value_text")
    private String valueText;

    @Column("value_long")
    private Long valueLong;

    @Column("value_double")
    private Double valueDouble;

    @Column("value_boolean")
    private Integer valueBoolean;

    private Long revision;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}