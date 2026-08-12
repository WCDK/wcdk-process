package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("SYS_ROLE_PERMISSION")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysRolePermissionEntity {

    @Column("ROLE_ID")
    private Long roleId;

    @Column("PERMISSION_ID")
    private Long permissionId;

    @Column("CREATE_TIME")
    private Instant createTime;
}