package com.wcdk.process.controller;

import com.wcdk.process.dto.ApiResponse;
import com.wcdk.process.dto.PageResponse;
import com.wcdk.process.dto.SysRoleRequest;
import com.wcdk.process.dto.SysRoleResponse;
import com.wcdk.process.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 角色管理接口。
 *
 * @auther WCDK
 * @date 2026/08/10
 * @version 1.0
 */
@RestController
@RequestMapping({"/api/sys/role", "/sys/role"})
@RequiredArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping("/list")
    public Mono<ApiResponse<PageResponse<SysRoleResponse>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        return sysRoleService.list(pageNum, pageSize, roleName, status)
                .map(ApiResponse::success);
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<SysRoleResponse>> getById(@PathVariable Long id) {
        return sysRoleService.getById(id)
                .map(ApiResponse::success);
    }

    @PostMapping
    public Mono<ApiResponse<SysRoleResponse>> create(@RequestBody SysRoleRequest request) {
        return sysRoleService.create(request)
                .map(role -> ApiResponse.success("创建成功", role));
    }

    @PutMapping("/{id}")
    public Mono<ApiResponse<SysRoleResponse>> update(@PathVariable Long id, @RequestBody SysRoleRequest request) {
        return sysRoleService.update(id, request)
                .map(role -> ApiResponse.success("更新成功", role));
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<Void>> delete(@PathVariable Long id) {
        return sysRoleService.delete(id)
                .thenReturn(ApiResponse.success("删除成功", null));
    }
}