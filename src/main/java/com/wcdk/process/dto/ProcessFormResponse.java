package com.wcdk.process.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@Data
@Builder
public class ProcessFormResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String formKey;

    private String formName;

    private Integer formVersion;

    private Integer fieldCount;

    private Object schema;

    private Boolean boundProcess;

    private String processDefinitionId;

    private String processNodeId;

    private String processNodeName;

    private String processNode;

    private String resourceName;

    private String tenantId;

    private Integer status;

    private String remark;

    private String createUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
