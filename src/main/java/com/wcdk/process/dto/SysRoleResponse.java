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
public class SysRoleResponse {

    private Long id;

    private String roleCode;

    private String roleName;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private List<Long> permissionIds;

    private List<String> permissionNames;

    private LocalDateTime createTime;
}
