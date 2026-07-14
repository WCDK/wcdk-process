package com.wcdk.proces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SDK HTTP 回调事件。
 *
 * @author WCDK
 * @date 2026/7/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcesConnectionEvent {

    private String connectionId;

    private String clientId;

    private String clientName;

    private String processInstanceId;

    private String processDefinitionKey;

    private String businessKey;

    private String processBeanName;

    private String eventType;

    private String message;

    private LocalDateTime eventTime;

    private Map<String, Object> payload;

    private String errorMessage;
}
