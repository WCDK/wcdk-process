package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessFormBindingSaveRequest {

    private String processDefinitionId;

    private String processDefinitionKey;

    private Integer processDefinitionVersion;

    private String deploymentId;

    private List<ProcessFormBindingItemRequest> bindings;
}
