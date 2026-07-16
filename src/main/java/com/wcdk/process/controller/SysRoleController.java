package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.dto.SysRoleResponse;
import com.wcdk.process.dto.SysRoleSaveRequest;
import com.wcdk.process.service.SysRoleService;
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
@Tag(name = "��ɫ����", description = "�ṩ��ɫ��ҳ�����������º�ɾ������")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("/list")
    @Operation(summary = "��ҳ��ѯ��ɫ", description = "����ɫ���ƺ�״̬��ҳ��ѯ��ɫ��Ϣ")
    public ApiResponse<PageResponse<SysRoleResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                           @RequestParam(defaultValue = "10") Long pageSize,
                                                           @RequestParam(required = false) String roleName,
                                                           @RequestParam(required = false) Integer status) {
        AuthContextHolder.requirePermission("sys:role:view");
        return ApiResponse.success(sysRoleService.listRole(pageNum, pageSize, roleName, status));
    }

    @PostMapping
    @Operation(summary = "������ɫ", description = "����һ����ɫ�����ؽ�ɫ��Ϣ")
    public ApiResponse<SysRoleResponse> create(@RequestBody SysRoleSaveRequest request) {
        AuthContextHolder.requirePermission("sys:role:add");
        return ApiResponse.success("������ɫ�ɹ�", sysRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "�޸Ľ�ɫ", description = "���ݽ�ɫID�޸Ľ�ɫ��Ϣ")
    public ApiResponse<SysRoleResponse> update(@PathVariable Long id, @RequestBody SysRoleSaveRequest request) {
        AuthContextHolder.requirePermission("sys:role:edit");
        return ApiResponse.success("�޸Ľ�ɫ�ɹ�", sysRoleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ɾ����ɫ", description = "���ݽ�ɫIDɾ����ɫ��Ϣ")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:role:delete");
        sysRoleService.deleteRole(id);
        return ApiResponse.success("ɾ����ɫ�ɹ�", null);
    }
}
