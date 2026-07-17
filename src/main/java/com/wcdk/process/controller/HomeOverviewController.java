package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.HomeOverviewResponse;
import com.wcdk.process.service.HomeOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
@Tag(name = "首页统计", description = "提供首页概览统计数据")
public class HomeOverviewController {

    private final HomeOverviewService homeOverviewService;

    @GetMapping("/overview")
    @Operation(summary = "查询首页统计", description = "查询流程定义、流程模型、流程单和待办任务数量")
    public ApiResponse<HomeOverviewResponse> getOverview() {
        return ApiResponse.success(homeOverviewService.getOverview());
    }
}
