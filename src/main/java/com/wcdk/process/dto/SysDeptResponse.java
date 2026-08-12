package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysDeptResponse {
    private Long id;
    private Long parentId;
    private String deptCode;
    private String deptName;
    private String parentDeptName;
    private Integer sortNo;
    private Integer status;
    private String remark;
    private Instant createTime;
    private Instant updateTime;
}