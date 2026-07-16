package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
@Builder
public class SysDeptResponse {

    private Long id;

    private Long parentId;

    private String parentDeptName;

    private String deptCode;

    private String deptName;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;
}
