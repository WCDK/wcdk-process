package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.common.PageResponse;
import com.wcdk.process.context.AuthContextHolder;
import com.wcdk.process.dto.WcdkProcessClientResponse;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.dto.WcdkProcessRpcCallbackResponse;
import com.wcdk.process.service.WcdkProcessClientCallbackService;
import com.wcdk.process.service.WcdkProcessClientRegistryService;
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

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@RestController
@RequestMapping("/wcdk/process/client")
@RequiredArgsConstructor
@Tag(name = "客户端管理", description = "查看已注册的流程客户端信息")
public class WcdkProcessClientController {

    private final WcdkProcessClientRegistryService wcdkProcessClientRegistryService;

    private final WcdkProcessClientCallbackService wcdkProcessClientCallbackService;

    @GetMapping("/list")
    @Operation(summary = "分页查询客户端", description = "按客户端标识、名称、回调地址和流程处理器分页查询已注册客户端信息")
    public ApiResponse<PageResponse<WcdkProcessClientResponse>> list(@RequestParam(defaultValue = "1") Long pageNum,
                                                                     @RequestParam(defaultValue = "10") Long pageSize,
                                                                     @RequestParam(required = false) String clientId,
                                                                     @RequestParam(required = false) String clientName,
                                                                     @RequestParam(required = false) String callbackUrl,
                                                                     @RequestParam(required = false) String processBeanName,
                                                                     @RequestParam(required = false) String sortProp,
                                                                     @RequestParam(required = false) String sortOrder) {
        AuthContextHolder.requirePermission("client:view");
        return ApiResponse.success(wcdkProcessClientRegistryService.listClient(
                pageNum,
                pageSize,
                clientId,
                clientName,
                callbackUrl,
                processBeanName,
                sortProp,
                sortOrder
        ));
    }

    @PostMapping("/{clientId}/detect")
    @Operation(summary = "检测客户端", description = "向已注册客户端发送存活检测请求")
    public ApiResponse<Boolean> detect(@PathVariable String clientId) {
        AuthContextHolder.requirePermission("client:view");
        boolean alive = wcdkProcessClientRegistryService.detectClient(clientId);
        if (alive) {
            return ApiResponse.success("客户端存活", true);
        }
        return ApiResponse.success("客户端未存活", false);
    }

    @PostMapping("/rpc/callback")
    @Operation(summary = "RPC回调客户端", description = "按流程定义绑定关系同步调用客户端流程处理器并返回客户端处理结果")
    public ApiResponse<List<WcdkProcessRpcCallbackResponse>> rpcCallback(@RequestBody WcdkProcessConnectionEvent request) {
        AuthContextHolder.requirePermission("client:view");
        return ApiResponse.success("RPC回调执行完成", wcdkProcessClientCallbackService.rpcCallback(request));
    }

    @DeleteMapping("/{clientId}")
    @Operation(summary = "移除客户端", description = "移除客户端注册信息及其流程处理器绑定信息")
    public ApiResponse<Void> remove(@PathVariable String clientId) {
        AuthContextHolder.requirePermission("client:delete");
        wcdkProcessClientRegistryService.removeClient(clientId);
        return ApiResponse.success("客户端注册信息移除成功", null);
    }
}
