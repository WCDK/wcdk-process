package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.service.WcdkProcessCallbackService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
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
@RequestMapping("/sdk/wcdkprocess")
@RequiredArgsConstructor
@Tag(name = "客户端回调管理", description = "提供客户端注册和回调处理接口")
public class WcdkProcessCallbackController {

    private final WcdkProcessCallbackService wcdkProcessCallbackService;

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    @PostMapping("/clients/register")
    @Operation(summary = "注册客户端回调处理器", description = "客户端启动时上报流程回调处理器名称列表，供服务端记录")
    public ApiResponse<String> registerClient(@RequestBody WcdkProcessClientRegisterRequest request) {
        wcdkProcessClientRegistryService.register(request);
        return ApiResponse.success("客户端回调处理器注册成功", null);
    }

    @PostMapping("/callback")
    @Operation(summary = "接收客户端回调", description = "接收客户端推送的连接事件并分发到本地流程回调处理程序")
    public ApiResponse<Void> callback(@RequestBody WcdkProcessConnectionEvent request) {
        wcdkProcessCallbackService.callback(request);
        return ApiResponse.success("客户端回调处理成功", null);
    }
}


