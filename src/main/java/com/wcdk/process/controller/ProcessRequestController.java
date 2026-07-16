package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessRequestApproveRequest;
import com.wcdk.process.dto.ProcessRequestCreateRequest;
import com.wcdk.process.dto.ProcessRequestResponse;
import com.wcdk.process.service.ProcessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/process/request")
@RequiredArgsConstructor
@Tag(name = "��������",description = "�������ύ����ѯ��������ɾ����������")
public class ProcessRequestController {

    private final ProcessRequestService processRequestService;

    @PostMapping
    @Operation(
            summary = "������������",
            description = "���ݶ�̬������ݴ�����������"
    )
    public ApiResponse<ProcessRequestResponse> createProcessRequest(@RequestBody ProcessRequestCreateRequest request) {
        return ApiResponse.success("�������봴���ɹ�", processRequestService.createProcessRequest(request));
    }

    @PostMapping("/{id}/submit")
    @Operation(
            summary = "�ύ��������",
            description = "���� ID �ύ���̲ݸ�"
    )
    public ApiResponse<ProcessRequestResponse> submitProcessRequest(@PathVariable Long id) {
        return ApiResponse.success("���������ύ�ɹ�", processRequestService.submitProcessRequest(id));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "��ѯ������������",
            description = "���� ID ��ѯ������������"
    )
    public ApiResponse<ProcessRequestResponse> getProcessRequest(@PathVariable Long id) {
        return ApiResponse.success(processRequestService.getProcessRequest(id));
    }

    @GetMapping("/{id}/diagram")
    @Operation(
            summary = "��ѯ������������ͼ����",
            description = "������������ ID ��ѯ��Ӧ����ͼ�ṹ����ǰ����ڵ�"
    )
    public ApiResponse<ProcessDefinitionDetailResponse> getProcessRequestDiagramDetail(@PathVariable Long id) {
        return ApiResponse.success(processRequestService.getProcessRequestDiagramDetail(id));
    }

    @GetMapping("/list")
    @Operation(
            summary = "��ѯ���������б�",
            description = "������ʱ�䵹���ҳ��ѯ���������б�"
    )
    public ApiResponse<PageResponse<ProcessRequestResponse>> listProcessRequest(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String processNo,
            @RequestParam(required = false) String starter,
            @RequestParam(required = false) String businessTitle,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String processDefinitionKey,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(processRequestService.listProcessRequest(
                pageNum,
                pageSize,
                processNo,
                starter,
                businessTitle,
                category,
                processDefinitionKey,
                status
        ));
    }

    @PostMapping("/approve")
    @Operation(
            summary = "������������",
            description = "�������������Ӧ����������"
    )
    public ApiResponse<Void> approveProcessRequest(@RequestBody ProcessRequestApproveRequest request) {
        processRequestService.approveProcessRequest(request);
        return ApiResponse.success("����������������ɹ�", null);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "ɾ����������",
            description = "ɾ���������뼰����ص�����ʱ����"
    )
    public ApiResponse<Void> deleteProcessRequest(@PathVariable Long id,
                                                  @RequestParam(required = false) String deleteReason) {
        processRequestService.deleteProcessRequest(id, deleteReason);
        return ApiResponse.success("��������ɾ���ɹ�", null);
    }
}
