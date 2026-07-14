package com.wcdk.proces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRequestApproveRequest {

    private String taskId;

    private Boolean approved;

    private String comment;
}
