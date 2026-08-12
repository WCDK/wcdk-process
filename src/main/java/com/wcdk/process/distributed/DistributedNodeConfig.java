package com.wcdk.process.distributed;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wcdk.process.distributed")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class DistributedNodeConfig {

    private String nodeId;
    private String nodeName;
    private boolean enabled = true;
    private long lockLeaseSeconds = 30;
    private long cleanupIntervalSeconds = 60;
    private int maxConcurrentExecutions = 100;

    public String getNodeId() {
        if (nodeId == null || nodeId.isBlank()) {
            nodeId = generateNodeId();
        }
        return nodeId;
    }

    private String generateNodeId() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName()
                    + "-" + ProcessHandle.current().pid();
        } catch (Exception e) {
            return "node-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
    }
}