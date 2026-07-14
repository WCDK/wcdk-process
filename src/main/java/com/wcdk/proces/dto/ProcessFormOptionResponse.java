package com.wcdk.proces.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessFormOptionResponse {

    private String label;

    private String value;
}
