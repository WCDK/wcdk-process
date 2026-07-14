package com.wcdk.proces.service;

import com.wcdk.proces.dto.WcdkProcesConnectionEvent;

/**
 * SDK HTTP 回调处理服务。
 *
 * @author WCDK
 * @date 2026/7/13
 */
public interface WcdkProcesCallbackService {

    /**
     * 处理 SDK HTTP 回调事件。
     *
     * @param request SDK 回调事件
     */
    void callback(WcdkProcesConnectionEvent request);
}
