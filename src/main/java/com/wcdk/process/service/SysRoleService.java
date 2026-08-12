package com.wcdk.process.service;

import com.wcdk.process.dto.PageResponse;
import com.wcdk.process.dto.SysRoleRequest;
import com.wcdk.process.dto.SysRoleResponse;
import reactor.core.publisher.Mono;

/**
 * 角色管理服务。
 *
 * @auther WCDK
 * @date 2026/08/10
 * @version 1.0
 */
public interface SysRoleService {

    Mono<PageResponse<SysRoleResponse>> list(Integer pageNum, Integer pageSize, String roleName, Integer status);

    Mono<SysRoleResponse> getById(Long id);

    Mono<SysRoleResponse> create(SysRoleRequest request);

    Mono<SysRoleResponse> update(Long id, SysRoleRequest request);

    Mono<Void> delete(Long id);
}