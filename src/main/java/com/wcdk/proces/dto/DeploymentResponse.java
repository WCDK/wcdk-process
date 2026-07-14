package com.wcdk.proces.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Data
@Builder
public class DeploymentResponse {

    private String deploymentId;

    private String deploymentName;

    private String category;

    private Date deployTime;
}
