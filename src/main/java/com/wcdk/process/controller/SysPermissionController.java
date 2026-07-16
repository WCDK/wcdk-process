package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.dto.SysPermissionResponse;
import com.wcdk.process.dto.SysPermissionSaveRequest;
import com.wcdk.process.service.SysPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@RestController
@RequestMapping("/sys/permission")
@Tag(name = "权限管理", description = "提供权限分页、创建、更新和删除能力")
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    public SysPermissionController(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询权限", description = "按权限名称、类型、父级权限和状态分页查询权限信息")
    public ApiResponse<PageResponse<SysPermissionResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                                 @RequestParam(defaultValue = "10") Long pageSize,
                                                                 @RequestParam(required = false) String permissionName,
                                                                 @RequestParam(required = false) String permissionType,
                                                                 @RequestParam(required = false) Long parentId,
                                                                 @RequestParam(required = false) Integer status) {
        AuthContextHolder.requirePermission("sys:permission:view");
        return ApiResponse.success(sysPermissionService.listPermission(
                pageNum,
                pageSize,
                permissionName,
                permissionType,
                parentId,
                status
        ));
    }

    @PostMapping
    @Operation(summary = "新增权限", description = "新增一个权限并返回权限信息")
    public ApiResponse<SysPermissionResponse> create(@RequestBody SysPermissionSaveRequest request) {
        AuthContextHolder.requirePermission("sys:permission:add");
        return ApiResponse.success("新增权限成功", sysPermissionService.createPermission(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改权限", description = "根据权限ID修改权限信息")
    public ApiResponse<SysPermissionResponse> update(@PathVariable Long id, @RequestBody SysPermissionSaveRequest request) {
        AuthContextHolder.requirePermission("sys:permission:edit");
        return ApiResponse.success("修改权限成功", sysPermissionService.updatePermission(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "根据权限ID删除权限信息")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:permission:delete");
        sysPermissionService.deletePermission(id);
        return ApiResponse.success("删除权限成功", null);
    }
}
