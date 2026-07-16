package com.wcdk.process.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {

    private Long userId;

    private String username;

    private String realName;

    private Long deptId;

    private String deptName;

    private String token;

    private List<Long> roleIds;

    private List<String> roleNames;

    private Set<String> permissionCodes;
}
