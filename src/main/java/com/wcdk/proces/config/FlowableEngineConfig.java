package com.wcdk.proces.config;

import org.flowable.common.engine.impl.util.DbUtil;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @auther WCDK
 * @date 2026/7/10
 * @version 1.0
 **/
@Configuration
public class FlowableEngineConfig {

    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {
        return this::configureProcessEngine;
    }

    private void configureProcessEngine(SpringProcessEngineConfiguration configuration) {
        configuration.setDatabaseType(DbUtil.DATABASE_TYPE_ORACLE);
    }
}
