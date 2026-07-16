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
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("WCDK_PROCESS_REQUEST")
public class ProcessRequest {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String processNo;

    private String starter;

    private String taskName;

    private String businessTitle;

    private String formDataJson;

    private String status;

    private String processInstanceId;

    private String currentTaskId;

    private String currentTaskName;

    private String processDefinitionKey;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
