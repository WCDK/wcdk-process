package com.wcdk.process.dto;

import lombok.Data;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@Data
public class SysDeptSaveRequest {

    private Long parentId;

    private String deptCode;

    private String deptName;

    private Integer sortNo;

    private Integer status;

    private String remark;
}
