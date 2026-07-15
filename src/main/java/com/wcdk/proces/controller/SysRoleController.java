package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.context.AuthContextHolder;
import com.wcdk.proces.dto.SysRoleResponse;
import com.wcdk.proces.dto.SysRoleSaveRequest;
import com.wcdk.proces.service.SysRoleService;
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
@RequestMapping("/sys/role")
@Tag(name = "角色管理", description = "提供角色分页、创建、更新和删除能力")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询角色", description = "按角色名称和状态分页查询角色信息")
    public ApiResponse<PageResponse<SysRoleResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                           @RequestParam(defaultValue = "10") Long pageSize,
                                                           @RequestParam(required = false) String roleName,
                                                           @RequestParam(required = false) Integer status) {
        AuthContextHolder.requirePermission("sys:role:view");
        return ApiResponse.success(sysRoleService.listRole(pageNum, pageSize, roleName, status));
    }

    @PostMapping
    @Operation(summary = "新增角色", description = "新增一个角色并返回角色信息")
    public ApiResponse<SysRoleResponse> create(@RequestBody SysRoleSaveRequest request) {
        AuthContextHolder.requirePermission("sys:role:add");
        return ApiResponse.success("新增角色成功", sysRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改角色", description = "根据角色ID修改角色信息")
    public ApiResponse<SysRoleResponse> update(@PathVariable Long id, @RequestBody SysRoleSaveRequest request) {
        AuthContextHolder.requirePermission("sys:role:edit");
        return ApiResponse.success("修改角色成功", sysRoleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "根据角色ID删除角色信息")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:role:delete");
        sysRoleService.deleteRole(id);
        return ApiResponse.success("删除角色成功", null);
    }
}
