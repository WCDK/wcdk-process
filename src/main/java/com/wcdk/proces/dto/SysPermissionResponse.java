package com.wcdk.proces.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
public class SysPermissionResponse {

    private Long id;

    private Long parentId;

    private String parentPermissionName;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private String routePath;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
