package com.wcdk.process.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessActionButtonResponse {

    private String actionKey;

    private String label;

    private String buttonType;

    private Boolean submit;

    private Integer sortOrder;
}
