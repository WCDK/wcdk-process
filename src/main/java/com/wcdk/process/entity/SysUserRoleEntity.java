package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("SYS_USER_ROLE")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysUserRoleEntity {


    @Column("USER_ID")
    private Long userId;

    @Column("ROLE_ID")
    private Long roleId;

    @Column("CREATE_TIME")
    private Instant createTime;
}