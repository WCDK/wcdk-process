package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/21
 * @version 1.0
 **/
@Data
@Builder
public class ProcessFormTableCellResponse {

    private Integer row;

    private Integer column;

    private List<ProcessFormFieldResponse> fields;
}
