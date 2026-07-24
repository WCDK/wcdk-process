package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProcessDiagramNodeResponse {

    private String elementId;

    private String elementName;

    private String elementType;

    private String parentId;

    private String documentation;

    private String defaultFlowId;

    private String approvalResult;

    private String approvalResultText;

    private String approvalAssignee;

    private String approvalComment;

    private String formKey;

    private List<String> boundFormKeys;

    private List<Map<String, Object>> boundForms;

    private Double x;

    private Double y;

    private Double width;

    private Double height;

    private Integer incomingCount;

    private Integer outgoingCount;
}
