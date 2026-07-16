package com.wcdk.process.dto;

import lombok.Data;

import java.util.List;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
public class SysRoleSaveRequest {

    private String roleCode;

    private String roleName;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private List<Long> permissionIds;
}
