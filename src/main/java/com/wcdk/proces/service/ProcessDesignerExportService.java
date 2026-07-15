package com.wcdk.proces.service;

import com.wcdk.proces.dto.ProcessDesignerExportRequest;
import com.wcdk.proces.dto.ProcessDesignerExportResponse;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
public interface ProcessDesignerExportService {

    ProcessDesignerExportResponse export(ProcessDesignerExportRequest request);
}
