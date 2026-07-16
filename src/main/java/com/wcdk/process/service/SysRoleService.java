package com.wcdk.process.service;

import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.SysRoleResponse;
import com.wcdk.process.dto.SysRoleSaveRequest;

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
