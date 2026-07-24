package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestApproveRequest {

    private String taskId;

    private Boolean approved;

    private String approvalAction;

    private String comment;

    private Map<String, Object> formData;
}
