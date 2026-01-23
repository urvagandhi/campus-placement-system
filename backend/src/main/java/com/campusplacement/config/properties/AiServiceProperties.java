package com.campusplacement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "ai-service")
public class AiServiceProperties {
    private String baseUrl;
    private Integer timeout;
}
