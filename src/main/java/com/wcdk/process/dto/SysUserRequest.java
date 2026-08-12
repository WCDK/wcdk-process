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
public class SysUserRequest {
    private Long id;
    private Long deptId;
    private String username;
    private String password;
    private String realName;
    private String mobile;
    private String email;
    private Integer status;
    private List<Long> roleIds;
}