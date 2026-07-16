package com.wcdk.process.controller;

import com.wcdk.process.common.ApiResponse;
import com.wcdk.process.dto.DeploymentResponse;
import com.wcdk.process.dto.ProcessDefinitionDetailResponse;
import com.wcdk.process.dto.ProcessDefinitionResponse;
import com.wcdk.process.service.FlowableDeployService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
@RestController
@RequestMapping("/flowable/deploy")
@RequiredArgsConstructor
@Tag(name = "���̲������", description = "�ṩ���̶��岿�𡢲����б��ѯ�����̶����ѯ�Ͳ���ɾ���ӿ�")
public class FlowableDeployController {

    private final FlowableDeployService flowableDeployService;

    @PostMapping("/process")
    @Operation(summary = "�������̶����ļ�", description = "�ϴ����̶����ļ����������̲���ͬʱ�� processBean �󶨿ͻ���")
    public ApiResponse<DeploymentResponse> deployProcess(@RequestParam String deploymentName,
                                                         @RequestParam(required = false) String category,
                                                         @RequestParam String processBeanName,
                                                         @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("���̲���ɹ�", flowableDeployService.deployProcess(deploymentName, category, processBeanName, file));
    }

    @GetMapping("/list")
    @Operation(summary = "��ѯ�����б�", description = "��ѯ��ǰϵͳ�е����̲����¼�б�")
    public ApiResponse<List<DeploymentResponse>> listDeployment() {
        return ApiResponse.success(flowableDeployService.listDeployment());
    }

    @GetMapping("/definition/list")
    @Operation(summary = "��ѯ���̶����б�", description = "��ѯ�Ѳ���ɹ������̶����б�")
    public ApiResponse<List<ProcessDefinitionResponse>> listProcessDefinition() {
        return ApiResponse.success(flowableDeployService.listProcessDefinition());
    }

    @GetMapping("/definition/{processDefinitionId}")
    @Operation(summary = "��ѯ���̶�������", description = "�������̶����Ų�ѯ���̶�������������ͼ�ṹ")
    public ApiResponse<ProcessDefinitionDetailResponse> getProcessDefinitionDetail(@PathVariable String processDefinitionId) {
        return ApiResponse.success(flowableDeployService.getProcessDefinitionDetail(processDefinitionId));
    }

    @DeleteMapping
    @Operation(summary = "ɾ�����̲���", description = "���ݲ�����ɾ�����̲��𣬿�ѡ�Ƿ���ɾ����������ʵ��")
    public ApiResponse<Void> deleteDeployment(@RequestParam String deploymentId,
                                              @RequestParam(defaultValue = "true") Boolean cascade) {
        flowableDeployService.deleteDeployment(deploymentId, cascade);
        return ApiResponse.success("ɾ������ɹ�", null);
    }
}
