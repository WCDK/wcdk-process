package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysUserResponse {
    private Long id;
    private Long deptId;
    private String username;
    private String realName;
    private String mobile;
    private String email;
    private Integer status;
    private Instant lastLoginTime;
    private Instant createTime;
    private Instant updateTime;
    private String deptName;
    private List<Long> roleIds;
    private List<String> roleNames;
}