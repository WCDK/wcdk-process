package com.wcdk.process.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

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
public class MigrationRequest {

    private String processInstanceId;
    private String targetProcessDefinitionId;
    private Map<String, Object> variables;
    private boolean skipCustomListeners;
    private boolean skipIoMapping;
}