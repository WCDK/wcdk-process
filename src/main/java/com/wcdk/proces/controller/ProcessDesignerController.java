package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.dto.ProcessDesignerExportRequest;
import com.wcdk.proces.dto.ProcessDesignerExportResponse;
import com.wcdk.proces.service.ProcessDesignerExportService;
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
@Tag(name = "流程设计器", description = "提供流程设计器导出能力")
public class ProcessDesignerController {

    private final ProcessDesignerExportService processDesignerExportService;

    @PostMapping("/export")
    @Operation(summary = "导出流程设计器内容", description = "根据前端画布节点和连线数据导出 BPMN、BPMN.XML 或 PNG 文件")
    public ApiResponse<ProcessDesignerExportResponse> export(@RequestBody ProcessDesignerExportRequest request) {
        return ApiResponse.success("流程设计导出成功", processDesignerExportService.export(request));
    }
}
