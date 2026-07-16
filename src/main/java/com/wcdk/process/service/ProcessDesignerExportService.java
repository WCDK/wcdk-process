package com.wcdk.process.service;

import com.wcdk.process.dto.ProcessDesignerExportRequest;
import com.wcdk.process.dto.ProcessDesignerExportResponse;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface ProcessDesignerExportService {

    ProcessDesignerExportResponse export(ProcessDesignerExportRequest request);
}
