package com.wcdk.proces.controller;

import com.wcdk.proces.common.ApiResponse;
import com.wcdk.proces.dto.WcdkProcesClientRegisterRequest;
import com.wcdk.proces.dto.WcdkProcesConnectionEvent;
import com.wcdk.proces.service.WcdkProcesCallbackService;
import com.wcdk.proces.service.WcdkProcesClientRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sdk/wcdkproces")
@RequiredArgsConstructor
@Tag(name = "客户端回调管理", description = "提供客户端注册和回调处理接口")
public class WcdkProcesCallbackController {

    private final WcdkProcesCallbackService wcdkProcesCallbackService;

    private final WcdkProcesClientRegistryService wcdkProcesClientRegistryService;

    @PostMapping("/clients/register")
    @Operation(summary = "注册客户端回调处理器", description = "客户端启动时上报流程回调处理器名称列表，供服务端记录")
    public ApiResponse<Void> registerClient(@RequestBody WcdkProcesClientRegisterRequest request) {
        wcdkProcesClientRegistryService.register(request);
        return ApiResponse.success("客户端回调处理器注册成功", null);
    }

    @PostMapping("/callback")
    @Operation(summary = "接收客户端回调", description = "接收客户端推送的连接事件并分发到本地流程回调处理器")
    public ApiResponse<Void> callback(@RequestBody WcdkProcesConnectionEvent request) {
        wcdkProcesCallbackService.callback(request);
        return ApiResponse.success("客户端回调处理成功", null);
    }
}
