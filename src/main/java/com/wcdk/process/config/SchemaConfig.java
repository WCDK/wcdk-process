package com.wcdk.process.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wcdk.process.schema")
/**
 * WCDK 流程模块类型。
 *
 * @author WCDK
 * @version 1.0
 */
public class SchemaConfig {

    private boolean autoInit = false;
    private String dialect = "postgresql";
    private boolean versionCheck = true;
}