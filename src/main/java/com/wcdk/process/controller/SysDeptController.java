package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.dto.SysDeptResponse;
import com.wcdk.process.dto.SysDeptSaveRequest;
import com.wcdk.process.service.SysDeptService;
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
@RequestMapping("/sys/dept")
@Tag(name = "���Ź���", description = "�ṩ���ŷ�ҳ�����������º�ɾ������")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @GetMapping("/list")
    @Operation(summary = "��ҳ��ѯ����", description = "���������ƺ�״̬��ҳ��ѯ������Ϣ")
    public ApiResponse<PageResponse<SysDeptResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                           @RequestParam(defaultValue = "10") Long pageSize,
                                                           @RequestParam(required = false) String deptName,
                                                           @RequestParam(required = false) Integer status) {
        AuthContextHolder.requirePermission("sys:dept:view");
        return ApiResponse.success(sysDeptService.listDept(pageNum, pageSize, deptName, status));
    }

    @PostMapping
    @Operation(summary = "��������", description = "����һ�����Ų����ز�����Ϣ")
    public ApiResponse<SysDeptResponse> create(@RequestBody SysDeptSaveRequest request) {
        AuthContextHolder.requirePermission("sys:dept:add");
        return ApiResponse.success("�������ųɹ�", sysDeptService.createDept(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "�޸Ĳ���", description = "���ݲ���ID�޸Ĳ�����Ϣ")
    public ApiResponse<SysDeptResponse> update(@PathVariable Long id, @RequestBody SysDeptSaveRequest request) {
        AuthContextHolder.requirePermission("sys:dept:edit");
        return ApiResponse.success("�޸Ĳ��ųɹ�", sysDeptService.updateDept(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "ɾ������", description = "���ݲ���IDɾ��������Ϣ")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:dept:delete");
        sysDeptService.deleteDept(id);
        return ApiResponse.success("ɾ�����ųɹ�", null);
    }
}
