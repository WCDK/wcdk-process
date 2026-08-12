package com.wcdk.process.dto;

import lombok.Data;

@Data
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysPermissionRequest {
    private Long id;
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private String permissionType;
    private String routePath;
    private Integer sortNo;
    private Integer status;
    private String remark;
    private String icon;
}