package com.wcdk.process.service;

import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;

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
