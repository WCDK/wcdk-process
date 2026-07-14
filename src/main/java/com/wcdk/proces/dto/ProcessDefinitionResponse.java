package com.wcdk.proces.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
public class ProcessDefinitionResponse {

    private String processDefinitionId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String category;

    private Integer version;

    private String deploymentId;

    private String resourceName;

    private Boolean suspended;
}
