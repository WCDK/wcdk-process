package com.wcdk.process.dto;

import lombok.Data;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@Data
public class ProcessFormSaveRequest {

    private String formKey;

    private String formName;

    private Object schema;
}
