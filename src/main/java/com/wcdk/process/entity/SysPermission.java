package com.wcdk.process.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SYS_PERMISSION")
public class SysPermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long parentId;

    private String permissionCode;

    private String permissionName;

    private String permissionType;

    private String routePath;

    private String icon;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
