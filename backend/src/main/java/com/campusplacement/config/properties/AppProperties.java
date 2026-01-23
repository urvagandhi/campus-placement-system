package com.campusplacement.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt;
    private Audit audit;
    private Cookie cookie;
    private Auth auth;

    @Data
    public static class Jwt {
        private String secret;
        private String issuer;
        private String audience;
        private Long accessExpirationMs;
        private Integer refreshExpirationDays;
    }

    @Data
    public static class Audit {
        private Integer retentionDays;
        private Boolean cleanupEnabled;
        private String cleanupCron;
    }

    @Data
    public static class Cookie {
        private Boolean secure;
        private String domain;
    }

    @Data
    public static class Auth {
        private Session session;

        @Data
        public static class Session {
            private Integer maxConcurrentSessions;
            private Integer idleTimeoutMinutes;
        }
    }
}
