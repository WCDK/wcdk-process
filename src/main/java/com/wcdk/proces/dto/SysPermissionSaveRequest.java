package com.wcdk.proces.dto;

import lombok.Data;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
public class SysPermissionSaveRequest {

    private Long parentId;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private String routePath;

    private Integer sortNo;

    private Integer status;

    private String remark;
}
