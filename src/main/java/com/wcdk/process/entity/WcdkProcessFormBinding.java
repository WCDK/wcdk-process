package com.wcdk.process.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("WCDK_PROCESS_FORM_BINDING")
public class WcdkProcessFormBinding {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long formId;

    private String processDefinitionId;

    private String processDefinitionKey;

    private Integer processDefinitionVersion;

    private String deploymentId;

    private String tenantId;

    private String bindScope;

    private String taskDefinitionKey;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
