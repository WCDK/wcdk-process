package com.wcdk.proces.service;

import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.SysDeptResponse;
import com.wcdk.proces.dto.SysDeptSaveRequest;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface SysDeptService {

    PageResponse<SysDeptResponse> listDept(long pageNum, long pageSize, String deptName, Integer status);

    SysDeptResponse createDept(SysDeptSaveRequest request);

    SysDeptResponse updateDept(Long id, SysDeptSaveRequest request);

    void deleteDept(Long id);
}
