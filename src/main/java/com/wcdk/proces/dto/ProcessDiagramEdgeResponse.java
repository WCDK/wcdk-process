package com.wcdk.proces.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProcessDiagramEdgeResponse {

    private String elementId;

    private String elementName;

    private String sourceRef;

    private String targetRef;

    private String conditionExpression;

    private List<ProcessDiagramWaypointResponse> waypoints;
}
