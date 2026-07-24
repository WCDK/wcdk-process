package com.wcdk.process.service;

import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import com.wcdk.process.dto.ProcessDefinitionUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
public interface FlowableDeployService {

    DeploymentResponse deployProcess(String deploymentName, String category, String clientId, String processBeanName, MultipartFile file);

    List<DeploymentResponse> listDeployment(String deploymentName, String category, String clientId);

    List<ProcessDefinitionResponse> listProcessDefinition();

    ProcessDefinitionDetailResponse getProcessDefinitionDetail(String processDefinitionId);

    ProcessDefinitionDetailResponse updateProcessDefinition(ProcessDefinitionUpdateRequest request);

    void updateDeploymentBinding(String deploymentId, String clientId, String processBeanName);

    void deleteDeployment(String deploymentId, Boolean cascade);
}
