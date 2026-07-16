package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.dto.SysUserResponse;
import com.wcdk.process.dto.SysUserSaveRequest;
import com.wcdk.process.service.SysUserService;
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
@Tag(name = "�û�����", description = "�ṩ�û���ҳ�����������º�ɾ������")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping("/list")
    @Operation(summary = "��ҳ��ѯ�û�", description = "���û��������������ź�״̬��ҳ��ѯ�û���Ϣ")
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
    @Operation(summary = "�����û�", description = "����һ���û����󶨽�ɫ")
    public ApiResponse<SysUserResponse> create(@RequestBody SysUserSaveRequest request) {
        AuthContextHolder.requirePermission("sys:user:add");
        return ApiResponse.success("�����û��ɹ�", sysUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "�޸��û�", description = "�����û�ID�޸��û���Ϣ�ͽ�ɫ")
    public ApiResponse<SysUserResponse> update(@PathVariable Long id, @RequestBody SysUserSaveRequest request) {
        AuthContextHolder.requirePermission("sys:user:edit");
        return ApiResponse.success("�޸��û��ɹ�", sysUserService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ɾ���û�", description = "�����û�IDɾ���û���Ϣ")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:user:delete");
        sysUserService.deleteUser(id);
        return ApiResponse.success("ɾ���û��ɹ�", null);
    }
}
