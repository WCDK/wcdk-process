package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("SYS_USER")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysUserEntity {

    @Id
    private Long id;

    @Column("DEPT_ID")
    private Long deptId;

    private String username;

    @Column("PASSWORD_HASH")
    private String passwordHash;

    @Column("REAL_NAME")
    private String realName;

    private String mobile;

    private String email;

    private Integer status;

    @Column("LAST_LOGIN_TIME")
    private Instant lastLoginTime;

    @Column("CREATE_TIME")
    private Instant createTime;

    @Column("UPDATE_TIME")
    private Instant updateTime;
}