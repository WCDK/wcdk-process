package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.context.AuthContextHolder;
import com.wcdk.proces.dto.SysDeptResponse;
import com.wcdk.proces.dto.SysDeptSaveRequest;
import com.wcdk.proces.service.SysDeptService;
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
@Tag(name = "部门管理", description = "提供部门分页、创建、更新和删除能力")
public class SysDeptController {

    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询部门", description = "按部门名称和状态分页查询部门信息")
    public ApiResponse<PageResponse<SysDeptResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                           @RequestParam(defaultValue = "10") Long pageSize,
                                                           @RequestParam(required = false) String deptName,
                                                           @RequestParam(required = false) Integer status) {
        AuthContextHolder.requirePermission("sys:dept:view");
        return ApiResponse.success(sysDeptService.listDept(pageNum, pageSize, deptName, status));
    }

    @PostMapping
    @Operation(summary = "新增部门", description = "新增一个部门并返回部门信息")
    public ApiResponse<SysDeptResponse> create(@RequestBody SysDeptSaveRequest request) {
        AuthContextHolder.requirePermission("sys:dept:add");
        return ApiResponse.success("新增部门成功", sysDeptService.createDept(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改部门", description = "根据部门ID修改部门信息")
    public ApiResponse<SysDeptResponse> update(@PathVariable Long id, @RequestBody SysDeptSaveRequest request) {
        AuthContextHolder.requirePermission("sys:dept:edit");
        return ApiResponse.success("修改部门成功", sysDeptService.updateDept(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "根据部门ID删除部门信息")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthContextHolder.requirePermission("sys:dept:delete");
        sysDeptService.deleteDept(id);
        return ApiResponse.success("删除部门成功", null);
    }
}
