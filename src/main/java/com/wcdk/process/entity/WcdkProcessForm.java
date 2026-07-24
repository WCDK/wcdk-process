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
@TableName("WCDK_PROCESS_FORM")
public class WcdkProcessForm {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String formKey;

    private String formName;

    private Integer formVersion;

    private String formSchemaJson;

    private String resourceName;

    private String tenantId;

    private Integer status;

    private String remark;

    private String createUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
