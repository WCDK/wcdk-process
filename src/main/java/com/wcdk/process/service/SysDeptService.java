package com.wcdk.process.service;

import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.SysDeptResponse;
import com.wcdk.process.dto.SysDeptSaveRequest;
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
