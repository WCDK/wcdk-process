package com.wcdk.proces.dto;

import lombok.Data;

import java.util.List;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
public class SysUserSaveRequest {

    private Long deptId;

    private String username;

    private String password;

    private String realName;

    private String mobile;

    private String email;

    private Integer status;

    private List<Long> roleIds;
}
