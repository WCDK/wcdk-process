package com.wcdk.process.service;

import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.SysUserResponse;
import com.wcdk.process.dto.SysUserSaveRequest;
import com.wcdk.process.entity.SysUser;
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
