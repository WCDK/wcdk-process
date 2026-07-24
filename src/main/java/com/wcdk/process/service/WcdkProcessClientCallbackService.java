package com.wcdk.process.service;

import com.wcdk.process.dto.WcdkProcessClientRegisterRequest;
import com.wcdk.process.dto.WcdkProcessConnectionEvent;
import com.wcdk.process.dto.WcdkProcessRpcCallbackResponse;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public interface WcdkProcessClientCallbackService {

    void callback(WcdkProcessConnectionEvent event);

    List<WcdkProcessRpcCallbackResponse> rpcCallback(WcdkProcessConnectionEvent event);

    void notifyRegisterSuccess(WcdkProcessClientRegisterRequest request);
}
