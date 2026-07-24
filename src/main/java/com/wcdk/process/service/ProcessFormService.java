package com.wcdk.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.ProcessFormBindingSaveRequest;
import com.wcdk.process.dto.ProcessFormResponse;
import com.wcdk.process.dto.ProcessFormSaveRequest;
import com.wcdk.process.entity.WcdkProcessForm;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
public interface ProcessFormService extends IService<WcdkProcessForm> {

    PageResponse<ProcessFormResponse> listForm(long pageNum, long pageSize, String formName, String formKey,
                                               Boolean boundProcess, String processNode);

    ProcessFormResponse saveForm(ProcessFormSaveRequest request);

    ProcessFormResponse getFormByKey(String formKey);

    List<ProcessFormResponse> listFormBinding(String processDefinitionId);

    void saveFormBinding(ProcessFormBindingSaveRequest request);

    void deleteForm(Long id);
}
