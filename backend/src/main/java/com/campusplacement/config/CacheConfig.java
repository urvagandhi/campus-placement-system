package com.campusplacement.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Cache configuration for performance optimization.
 *
 * <p>
 * <strong>Caching Strategy:</strong>
 * </p>
 * <ul>
 * <li><strong>Primary:</strong> Request-scoped via
 * {@link com.campusplacement.organizations.ScopeContextHolder}</li>
 * <li><strong>Secondary:</strong> Cross-request via Caffeine (optional, for
 * high-load scenarios)</li>
 * </ul>
 *
 * <p>
 * <strong>Cache Configuration:</strong>
 * </p>
 * <ul>
 * <li><strong>allowedDepartments:</strong> 5-minute TTL, max 10,000
 * entries</li>
 * <li>Automatically evicted when user assignments change</li>
 * <li>Uses Caffeine for high-performance in-memory caching</li>
 * </ul>
 *
 * <p>
 * <strong>Security Notes:</strong>
 * </p>
 * <ul>
 * <li>Cache keys MUST include userId for proper isolation</li>
 * <li>Never cache across users or colleges</li>
 * <li>TTL kept short to ensure scope changes propagate quickly</li>
 * </ul>
 *
 * @see com.campusplacement.organizations.OrganizationScopeService
 * @see com.campusplacement.organizations.ScopeContextHolder
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures Caffeine cache manager for scope-related caches.
     *
     * <p>
     * <strong>Performance Characteristics:</strong>
     * </p>
     * <ul>
     * <li>Caffeine provides ~O(1) access time</li>
     * <li>Automatic eviction based on TTL and size</li>
     * <li>Thread-safe for concurrent access</li>
     * </ul>
     *
     * @return CacheManager configured with Caffeine
     */
    @SuppressWarnings("null")
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("allowedDepartments");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Builds Caffeine cache with performance and security settings.
     *
     * <p>
     * <strong>Configuration Rationale:</strong>
     * </p>
     * <ul>
     * <li><strong>5-minute TTL:</strong> Balance between performance and data
     * freshness</li>
     * <li><strong>10,000 max entries:</strong> Supports ~10K concurrent users</li>
     * <li><strong>Size-based eviction:</strong> Prevents memory exhaustion</li>
     * </ul>
     *
     * @return Caffeine builder with configured settings
     */
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(10_000)
                .recordStats(); // Enable cache hit/miss metrics
    }
}
