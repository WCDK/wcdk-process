package com.wcdk.process.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * @auther WCDK
 * @date 2026/7/15
 * @version 1.0
 **/
@ConfigurationProperties(prefix = "wcdk.process.swagger")
public class Knife4jProperties {

    private String title = "��������ӿ��ĵ�";

    private String description = "�������� ����ӿ��ĵ�";

    private String version = "1.0";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
