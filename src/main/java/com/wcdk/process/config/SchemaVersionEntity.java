package com.wcdk.process.config;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("wcdk_schema_version")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SchemaVersionEntity {

    @Id
    private Long id;

    @Column("version")
    private String version;

    @Column("description")
    private String description;

    @Column("applied_at")
    private Instant appliedAt;

    @Column("checksum")
    private String checksum;
}