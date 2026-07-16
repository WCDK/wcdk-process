package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.ProcessDesignerExportRequest;
import com.wcdk.process.dto.ProcessDesignerExportResponse;
import com.wcdk.process.service.ProcessDesignerExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@RestController
@RequestMapping("/flowable/designer")
@RequiredArgsConstructor
@Tag(name = "���������", description = "�ṩ�����������������")
public class ProcessDesignerController {

    private final ProcessDesignerExportService processDesignerExportService;

    @PostMapping("/export")
    @Operation(summary = "�����������������", description = "����ǰ�˻����ڵ���������ݵ��� BPMN��BPMN.XML �� PNG �ļ�")
    public ApiResponse<ProcessDesignerExportResponse> export(@RequestBody ProcessDesignerExportRequest request) {
        return ApiResponse.success("������Ƶ����ɹ�", processDesignerExportService.export(request));
    }
}
