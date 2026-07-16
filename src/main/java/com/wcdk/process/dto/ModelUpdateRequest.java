package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelUpdateRequest {

    /**
     * ģ������
     */
    private String modelName;

    /**
     * ģ�ͷ���
     */
    private String category;

    /**
     * BPMN XML ����
     */
    private String bpmnXml;
}
