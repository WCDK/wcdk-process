package com.wcdk.process.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class ActivityHistory {

    private String nodeId;
    private String eventType;
    private String executionId;
    private Instant startedAt;
    private Instant completedAt;
    private Duration duration;
}