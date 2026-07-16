package com.wcdk.process.service;

import com.wcdk.process.dto.WcdkProcessConnectionEvent;

/**
 * SDK HTTP 鍥炶皟澶勭悊鏈嶅姟銆? *
 * @author WCDK
 * @date 2026/7/13
 */
public interface WcdkProcessCallbackService {

    /**
     * 澶勭悊 SDK HTTP 鍥炶皟浜嬩欢銆?     *
     * @param request SDK 鍥炶皟浜嬩欢
     */
    void callback(WcdkProcessConnectionEvent request);
}

