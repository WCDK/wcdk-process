package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartProcessRequest {

    /**
     * ���̶����ʶ��Ĭ��ʹ��ʾ������ leave-process
     */
    private String processDefinitionKey;

    /**
     * ҵ������
     */
    private String businessKey;

    /**
     * ���̷�����
     */
    private String starter;

    /**
     * ���̻ص�bean����
     */
    private String processBeanName;

    /**
     * ���̱���
     */
    private Map<String, Object> variables;
}
