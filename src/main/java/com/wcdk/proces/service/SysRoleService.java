package com.wcdk.proces.service;

import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.SysRoleResponse;
import com.wcdk.proces.dto.SysRoleSaveRequest;

import java.util.List;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface SysRoleService {

    PageResponse<SysRoleResponse> listRole(long pageNum, long pageSize, String roleName, Integer status);

    SysRoleResponse createRole(SysRoleSaveRequest request);

    SysRoleResponse updateRole(Long id, SysRoleSaveRequest request);

    void deleteRole(Long id);

    List<Long> listRoleIdsByUserId(Long userId);

    List<SysRoleResponse> listByIds(List<Long> roleIds);

    List<String> listRoleNamesByUserId(Long userId);
}
