package com.wcdk.proces.service;

import com.wcdk.proces.dto.DeploymentResponse;
import com.wcdk.proces.dto.ProcessDefinitionDetailResponse;
import com.wcdk.proces.dto.ProcessDefinitionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
public interface FlowableDeployService {

    DeploymentResponse deployProcess(String deploymentName, String category, MultipartFile file);

    List<DeploymentResponse> listDeployment();

    List<ProcessDefinitionResponse> listProcessDefinition();

    ProcessDefinitionDetailResponse getProcessDefinitionDetail(String processDefinitionId);

    void deleteDeployment(String deploymentId, Boolean cascade);
}
