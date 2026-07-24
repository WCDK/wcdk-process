package com.wcdk.process.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @auther WCDK
 * @date 2026/7/22
 * @version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessFormBindingItemRequest {

    private String taskDefinitionKey;

    private List<Long> formIds;
}
