package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.context.AuthContextHolder;
import com.wcdk.proces.dto.SysUserResponse;
import com.wcdk.proces.dto.SysUserSaveRequest;
import com.wcdk.proces.service.SysUserService;
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
@RequestMapping("/sys/user")
@Tag(name = "用户管理", description = "提供用户分页、创建、更新和删除能力")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户", description = "按用户名、姓名、部门和状态分页查询用户信息")
    public ApiResponse<PageResponse<SysUserResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                           @RequestParam(defaultValue = "10") Long pageSize,
                                                           @RequestParam(required = false) String username,
                                                           @RequestParam(required = false) String realName,
                                                           @RequestParam(required = false) Long deptId,
                                                           @RequestParam(required = false) Integer status) {
        AuthContextHolder.requirePermission("sys:user:view");
        return ApiResponse.success(sysUserService.listUser(pageNum, pageSize, username, realName, deptId, status));
    }

    @PostMapping
    @Operation(summary = "新增用户", description = "新增一个用户并绑定角色")
    public ApiResponse<SysUserResponse> create(@RequestBody SysUserSaveRequest request) {
        AuthContextHolder.requirePermission("sys:user:add");
        return ApiResponse.success("新增用户成功", sysUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户", description = "根据用户ID修改用户信息和角色")
    public ApiResponse<SysUserResponse> update(@PathVariable Long id, @RequestBody SysUserSaveRequest request) {
        AuthContextHolder.requirePermission("sys:user:edit");
        return ApiResponse.success("修改用户成功", sysUserService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户信息")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:user:delete");
        sysUserService.deleteUser(id);
        return ApiResponse.success("删除用户成功", null);
    }
}
