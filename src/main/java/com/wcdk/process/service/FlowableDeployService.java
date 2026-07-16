package com.wcdk.process.service;

import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
public interface FlowableDeployService {

    DeploymentResponse deployProcess(String deploymentName, String category, String clientId, String processBeanName, MultipartFile file);

    List<DeploymentResponse> listDeployment(String deploymentName, String category);

    List<ProcessDefinitionResponse> listProcessDefinition();

    ProcessDefinitionDetailResponse getProcessDefinitionDetail(String processDefinitionId);

    void deleteDeployment(String deploymentId, Boolean cascade);
}
