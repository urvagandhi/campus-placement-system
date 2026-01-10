package com.campusplacement.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic paginated response wrapper.
 *
 * <p>
 * Provides standardized pagination metadata for list responses.
 * Designed for reuse across all paginated endpoints.
 * </p>
 *
 * <p>
 * <strong>Future Considerations:</strong>
 * </p>
 * <ul>
 * <li>Support for cursor-based pagination (for large datasets)</li>
 * <li>Support for search/filter criteria</li>
 * <li>Support for sorting metadata</li>
 * </ul>
 *
 * @param <T> Type of content items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    /**
     * List of items in the current page.
     */
    private List<T> content;

    /**
     * Current page number (0-indexed internally, 1-indexed in API).
     */
    private int page;

    /**
     * Number of items per page.
     */
    private int size;

    /**
     * Total number of items across all pages.
     */
    private long totalElements;

    /**
     * Total number of pages.
     */
    private int totalPages;

    /**
     * Whether this is the first page.
     */
    private boolean first;

    /**
     * Whether this is the last page.
     */
    private boolean last;

    /**
     * Whether there is a next page.
     */
    private boolean hasNext;

    /**
     * Whether there is a previous page.
     */
    private boolean hasPrevious;

    /**
     * Creates a PagedResponse from Spring's Page object.
     *
     * @param page    Spring Page object
     * @param content Transformed content (may differ from page.getContent() if
     *                mapped)
     * @param <T>     Type of content items
     * @return PagedResponse instance
     */
    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<?> page, List<T> content) {
        return PagedResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Creates a PagedResponse directly from Spring's Page with same content type.
     *
     * @param page Spring Page object
     * @param <T>  Type of content items
     * @return PagedResponse instance
     */
    public static <T> PagedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return of(page, page.getContent());
    }
}
