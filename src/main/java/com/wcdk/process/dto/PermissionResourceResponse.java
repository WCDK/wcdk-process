package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/17
 * @version 1.0
 **/
@Data
@Builder
public class PermissionResourceResponse {

    private Long id;

    private Long parentId;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private String routePath;

    private String icon;

    private Integer sortNo;

    private String remark;

    private List<PermissionResourceResponse> children;
}
