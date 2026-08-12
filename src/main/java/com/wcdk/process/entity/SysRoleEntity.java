package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("SYS_ROLE")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysRoleEntity {

    @Id
    private Long id;

    @Column("ROLE_CODE")
    private String roleCode;

    @Column("ROLE_NAME")
    private String roleName;

    @Column("SORT_NO")
    private Integer sortNo;

    private Integer status;

    private String remark;

    @Column("CREATE_TIME")
    private Instant createTime;

    @Column("UPDATE_TIME")
    private Instant updateTime;
}