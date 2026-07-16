package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessDiagramNodeResponse {

    private String elementId;

    private String elementName;

    private String elementType;

    private String documentation;

    private Double x;

    private Double y;

    private Double width;

    private Double height;

    private Integer incomingCount;

    private Integer outgoingCount;
}
