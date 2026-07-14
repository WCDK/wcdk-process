package com.wcdk.proces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcdkProcesClientRegisterRequest {

    private String clientId;

    private String clientName;

    private Set<String> processBeanNames;
}
