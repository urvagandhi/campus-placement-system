package com.campusplacement.organizations;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped cache for {@link ScopeContext}.
 *
 * <p>
 * <strong>Purpose:</strong> Avoids redundant scope resolution within a single
 * HTTP request.
 * Scope resolution involves recursive queries, so caching per-request is a
 * significant
 * performance optimization.
 * </p>
 *
 * <p>
 * <strong>Why @RequestScope, not Redis/global cache?</strong>
 * </p>
 * <ul>
 * <li>Scope can change between requests (admin updates assignments)</li>
 * <li>Request-scoped cache is inherently safe - no invalidation needed</li>
 * <li>Global cache would require complex invalidation on assignment
 * changes</li>
 * </ul>
 *
 * <p>
 * <strong>Usage:</strong>
 * </p>
 * 
 * <pre>
 * {@code
 * @Autowired
 * private ScopeContextHolder scopeContextHolder;
 *
 * public ScopeContext resolveScope(Long userId) {
 *     return scopeContextHolder.get()
 *             .filter(ctx -> ctx.userId().equals(userId))
 *             .orElseGet(() -> {
 *                 ScopeContext computed = computeScope(userId);
 *                 scopeContextHolder.set(computed);
 *                 return computed;
 *             });
 * }
 * }
 * </pre>
 */
@Component
@RequestScope
public class ScopeContextHolder {

    private ScopeContext cachedContext;

    /**
     * Gets the cached scope context if available.
     *
     * @return Optional containing cached context, or empty if not cached
     */
    public Optional<ScopeContext> get() {
        return Optional.ofNullable(cachedContext);
    }

    /**
     * Caches the scope context for this request.
     *
     * @param context The resolved scope context
     */
    public void set(ScopeContext context) {
        this.cachedContext = context;
    }

    /**
     * Clears the cached context.
     * Useful if scope might change mid-request (rare edge case).
     */
    public void clear() {
        this.cachedContext = null;
    }

    /**
     * Checks if a context is currently cached.
     *
     * @return true if context is cached
     */
    public boolean isCached() {
        return cachedContext != null;
    }
}
