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
 * @date 2026/7/16
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("WCDK_PROCESS_CLIENT")
public class WcdkProcessClient {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String clientId;

    private String clientName;

    private String callbackUrl;

    private String authFlg;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

