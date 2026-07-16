package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
public class CurrentUserResponse {

    private Long userId;

    private String username;

    private String realName;

    private Long deptId;

    private String deptName;

    private List<Long> roleIds;

    private List<String> roleNames;

    private Set<String> permissionCodes;
}
