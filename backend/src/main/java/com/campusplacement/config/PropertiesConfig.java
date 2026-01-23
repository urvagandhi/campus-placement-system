package com.campusplacement.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.campusplacement.config.properties.AiServiceProperties;
import com.campusplacement.config.properties.AppProperties;
import com.campusplacement.config.properties.GeminiProperties;

@Configuration
@EnableConfigurationProperties({
        AppProperties.class,
        GeminiProperties.class,
        AiServiceProperties.class
})
public class PropertiesConfig {
}
