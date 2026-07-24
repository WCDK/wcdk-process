package com.wcdk.process.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @auther WCDK
 * @date 2026/7/23
 * @version 1.0
 **/
@Data
@Component
@ConfigurationProperties(prefix = "wcdk.process.nacos")
public class WcdkProcessNacosProperties {

    private Boolean enabled = false;
}
