package com.wcdk.process.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("SYS_ROLE_PERMISSION")
public class SysRolePermission {

    private Long roleId;

    private Long permissionId;

    private LocalDateTime createTime;
}
