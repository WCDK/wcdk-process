package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcessClientRegisterRequest {

    private String clientId;

    private String clientName;

    private String callbackUrl;

    private String authFlg;

    private Map<String, String> requestHeaderParams;

    private Set<String> processBeanNames;
}
