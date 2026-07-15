package com.wcdk.proces.service;

import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.SysUserResponse;
import com.wcdk.proces.dto.SysUserSaveRequest;
import com.wcdk.proces.entity.SysUser;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface SysUserService {

    PageResponse<SysUserResponse> listUser(long pageNum, long pageSize, String username, String realName, Long deptId, Integer status);

    SysUserResponse createUser(SysUserSaveRequest request);

    SysUserResponse updateUser(Long id, SysUserSaveRequest request);

    void deleteUser(Long id);

    SysUser getByUsername(String username);

    SysUserResponse getUserResponse(Long id);

    void updateLastLoginTime(Long id);
}
