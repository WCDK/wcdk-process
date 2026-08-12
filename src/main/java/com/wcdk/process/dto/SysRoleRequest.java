package com.wcdk.process.dto;

import lombok.Data;

import java.util.List;

@Data
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysRoleRequest {
    private Long id;
    private String roleCode;
    private String roleName;
    private Integer sortNo;
    private Integer status;
    private String remark;
    private List<Long> permissionIds;
}