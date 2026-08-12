package com.wcdk.process.dto;

import lombok.Data;

@Data
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SysDeptRequest {
    private Long id;
    private Long parentId;
    private String deptCode;
    private String deptName;
    private Integer sortNo;
    private Integer status;
    private String remark;
}