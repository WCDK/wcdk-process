package com.wcdk.process.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("SYS_PERMISSION")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysPermissionEntity {

    @Id
    private Long id;

    @Column("PARENT_ID")
    private Long parentId;

    @Column("PERMISSION_CODE")
    private String permissionCode;

    @Column("PERMISSION_NAME")
    private String permissionName;

    @Column("PERMISSION_TYPE")
    private String permissionType;

    @Column("ROUTE_PATH")
    private String routePath;

    @Column("SORT_NO")
    private Integer sortNo;

    private Integer status;

    private String remark;

    @Column("CREATE_TIME")
    private Instant createTime;

    @Column("UPDATE_TIME")
    private Instant updateTime;

    private String icon;
}