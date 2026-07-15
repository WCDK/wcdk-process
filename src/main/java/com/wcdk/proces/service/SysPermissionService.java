package com.wcdk.proces.service;

import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.SysPermissionResponse;
import com.wcdk.proces.dto.SysPermissionSaveRequest;

import java.util.List;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface SysPermissionService {

    PageResponse<SysPermissionResponse> listPermission(long pageNum,
                                                       long pageSize,
                                                       String permissionName,
                                                       String permissionType,
                                                       Long parentId,
                                                       Integer status);

    SysPermissionResponse createPermission(SysPermissionSaveRequest request);

    SysPermissionResponse updatePermission(Long id, SysPermissionSaveRequest request);

    void deletePermission(Long id);

    List<Long> listPermissionIdsByRoleId(Long roleId);

    List<SysPermissionResponse> listByIds(List<Long> permissionIds);

    Set<String> listPermissionCodesByUserId(Long userId);
}
