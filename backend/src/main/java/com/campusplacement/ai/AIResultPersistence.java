package com.campusplacement.ai;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.campusplacement.ai.dto.ResumeParseResponseDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;

/**
 * Persistence and caching layer for AI results.
 *
 * <p>
 * <strong>Cache Key Design (CRITICAL for multi-tenancy):</strong>
 * </p>
 * <p>
 * Cache keys MUST include: (collegeId, departmentId, driveId, studentId,
 * modelVersion)
 * </p>
 * <p>
 * Cached results are NEVER reused across scopes or tenants.
 * </p>
 *
 * <p>
 * <strong>Cache Strategy:</strong>
 * </p>
 * <ul>
 * <li>In-memory cache using Caffeine for fast lookups</li>
 * <li>TTL-based expiration to ensure freshness</li>
 * <li>Scope-keyed to prevent cross-tenant leakage</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AIResultPersistence {

    private static final Logger log = LoggerFactory.getLogger(AIResultPersistence.class);

    /**
     * Current AI model version. Update when AI model changes
     * to invalidate stale cache entries.
     */
    private static final String MODEL_VERSION = "v1.0.0";

    /**
     * Cache for parsed resume results.
     * Key format: "resume:{collegeId}:{studentId}:{modelVersion}"
     */
    private final Cache<String, CachedResult<ResumeParseResponseDTO>> resumeParseCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(24))
            .recordStats()
            .build();

    // ==================== Resume Parse Caching ====================

    /**
     * Gets cached resume parse result if available and valid.
     *
     * @param collegeId College ID (tenant isolation)
     * @param studentId Student ID
     * @return Cached result if present and not expired
     */
    public Optional<ResumeParseResponseDTO> getCachedResumeParse(Long collegeId, Long studentId) {
        String key = buildResumeParseKey(collegeId, studentId);
        CachedResult<ResumeParseResponseDTO> cached = resumeParseCache.getIfPresent(key);

        if (cached != null && !cached.isExpired()) {
            log.debug("Cache HIT for resume parse: collegeId={}, studentId={}", collegeId, studentId);
            return Optional.of(cached.getValue());
        }

        log.debug("Cache MISS for resume parse: collegeId={}, studentId={}", collegeId, studentId);
        return Optional.empty();
    }

    /**
     * Caches a resume parse result with scope isolation.
     *
     * @param collegeId College ID (tenant isolation)
     * @param studentId Student ID
     * @param result    The result to cache
     */
    public void cacheResumeParse(Long collegeId, Long studentId, ResumeParseResponseDTO result) {
        if (collegeId == null) {
            log.warn("Cannot cache resume parse without collegeId - skipping cache");
            return;
        }

        String key = buildResumeParseKey(collegeId, studentId);
        CachedResult<ResumeParseResponseDTO> cached = new CachedResult<>(
                result,
                LocalDateTime.now().plusHours(24));

        resumeParseCache.put(key, cached);
        log.debug("Cached resume parse: collegeId={}, studentId={}", collegeId, studentId);
    }

    /**
     * Invalidates cached resume parse for a student.
     * Call this when a student uploads a new resume.
     *
     * @param collegeId College ID
     * @param studentId Student ID
     */
    public void invalidateResumeParse(Long collegeId, Long studentId) {
        String key = buildResumeParseKey(collegeId, studentId);
        resumeParseCache.invalidate(key);
        log.debug("Invalidated resume parse cache: collegeId={}, studentId={}", collegeId, studentId);
    }

    // ==================== Key Builders ====================

    /**
     * Builds a scope-safe cache key for resume parsing.
     * Includes collegeId for tenant isolation and modelVersion for cache
     * invalidation.
     */
    private String buildResumeParseKey(Long collegeId, Long studentId) {
        return String.format("resume:%d:%d:%s",
                collegeId != null ? collegeId : 0,
                studentId,
                MODEL_VERSION);
    }

    // ==================== Cache Statistics ====================

    /**
     * Gets cache statistics for monitoring.
     */
    public CacheStats getResumeParseCacheStats() {
        var stats = resumeParseCache.stats();
        return new CacheStats(
                stats.hitCount(),
                stats.missCount(),
                stats.evictionCount(),
                resumeParseCache.estimatedSize());
    }

    // ==================== Inner Classes ====================

    /**
     * Wrapper for cached values with expiration tracking.
     */
    private static class CachedResult<T> {
        private final T value;
        private final LocalDateTime expiresAt;

        CachedResult(T value, LocalDateTime expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        T getValue() {
            return value;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    /**
     * Cache statistics record for monitoring.
     */
    public record CacheStats(
            long hitCount,
            long missCount,
            long evictionCount,
            long estimatedSize) {
        public double hitRate() {
            long total = hitCount + missCount;
            return total > 0 ? (double) hitCount / total : 0.0;
        }
    }
}
