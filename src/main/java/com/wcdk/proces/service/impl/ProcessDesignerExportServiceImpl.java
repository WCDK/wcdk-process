package com.wcdk.proces.service.impl;

import com.wcdk.proces.common.ProcessDesignerExportSupport;
import com.wcdk.proces.dto.ProcessDesignerExportRequest;
import com.wcdk.proces.dto.ProcessDesignerExportResponse;
import com.wcdk.proces.service.ProcessDesignerExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Service
@RequiredArgsConstructor
public class ProcessDesignerExportServiceImpl implements ProcessDesignerExportService {

    private final ProcessDesignerExportSupport processDesignerExportSupport;

    @Override
    public ProcessDesignerExportResponse export(ProcessDesignerExportRequest request) {
        return processDesignerExportSupport.export(request);
    }
}
