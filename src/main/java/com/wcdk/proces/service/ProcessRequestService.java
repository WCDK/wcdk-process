package com.wcdk.proces.service;

import com.wcdk.proces.common.PageResponse;
import com.wcdk.proces.dto.ProcessDefinitionDetailResponse;
import com.wcdk.proces.dto.ProcessRequestApproveRequest;
import com.wcdk.proces.dto.ProcessRequestCreateRequest;
import com.wcdk.proces.dto.ProcessRequestResponse;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
public interface ProcessRequestService {

    ProcessRequestResponse createProcessRequest(ProcessRequestCreateRequest request);

    ProcessRequestResponse submitProcessRequest(Long id);

    ProcessRequestResponse getProcessRequest(Long id);

    ProcessDefinitionDetailResponse getProcessRequestDiagramDetail(Long id);

    PageResponse<ProcessRequestResponse> listProcessRequest(long pageNum,
                                                            long pageSize,
                                                            String processNo,
                                                            String starter,
                                                            String businessTitle,
                                                            String category,
                                                            String processDefinitionKey,
                                                            String status);

    void approveProcessRequest(ProcessRequestApproveRequest request);

    void deleteProcessRequest(Long id, String deleteReason);
}
