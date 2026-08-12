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
public class SysRoleResponse {
    private Long id;
    private String roleCode;
    private String roleName;
    private Integer sortNo;
    private Integer status;
    private String remark;
    private Instant createTime;
    private Instant updateTime;
    private List<Long> permissionIds;
    private List<String> permissionNames;
}