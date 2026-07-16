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
@Tag(name = "Ȩ�޹���", description = "�ṩȨ�޷�ҳ�����������º�ɾ������")
public class SysPermissionController {

    private final SysPermissionService sysPermissionService;

    public SysPermissionController(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    @GetMapping("/list")
    @Operation(summary = "��ҳ��ѯȨ��", description = "��Ȩ�����ơ����͡�����Ȩ�޺�״̬��ҳ��ѯȨ����Ϣ")
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
    @Operation(summary = "����Ȩ��", description = "����һ��Ȩ�޲�����Ȩ����Ϣ")
    public ApiResponse<SysPermissionResponse> create(@RequestBody SysPermissionSaveRequest request) {
        AuthContextHolder.requirePermission("sys:permission:add");
        return ApiResponse.success("����Ȩ�޳ɹ�", sysPermissionService.createPermission(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "�޸�Ȩ��", description = "����Ȩ��ID�޸�Ȩ����Ϣ")
    public ApiResponse<SysPermissionResponse> update(@PathVariable Long id, @RequestBody SysPermissionSaveRequest request) {
        AuthContextHolder.requirePermission("sys:permission:edit");
        return ApiResponse.success("�޸�Ȩ�޳ɹ�", sysPermissionService.updatePermission(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ɾ��Ȩ��", description = "����Ȩ��IDɾ��Ȩ����Ϣ")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:permission:delete");
        sysPermissionService.deletePermission(id);
        return ApiResponse.success("ɾ��Ȩ�޳ɹ�", null);
    }
}
