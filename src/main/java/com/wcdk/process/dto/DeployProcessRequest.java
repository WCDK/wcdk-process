package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class DeployProcessRequest {

    private String name;

    private String category;

    private String description;

    private String tenantId;

    private String bpmnXml;

    private String processDefinitionKey;
}