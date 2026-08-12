package com.wcdk.process.migration;

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
public class MigrationResult {

    private String processInstanceId;
    private String sourceProcessDefinitionId;
    private String targetProcessDefinitionId;
    private boolean success;
    private String message;
}