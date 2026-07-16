package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
public class SysUserResponse {

    private Long id;

    private Long deptId;

    private String deptName;

    private String username;

    private String realName;

    private String mobile;

    private String email;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private List<Long> roleIds;

    private List<String> roleNames;
}
