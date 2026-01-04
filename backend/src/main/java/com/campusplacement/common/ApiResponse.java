package com.campusplacement.common;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for consistent response format.
 *
 * <p>
 * All REST endpoints should return responses wrapped in this class
 * to maintain consistency across the API.
 * </p>
 *
 * <p>
 * <strong>Response Structure:</strong>
 * </p>
 * 
 * <pre>
 * {
 *   "success": true,
 *   "message": "Operation completed successfully",
 *   "data": { ... },
 *   "errors": null,
 *   "timestamp": "2024-01-04T10:30:00"
 * }
 * </pre>
 *
 * @param <T> the type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the operation was successful.
     */
    private boolean success;

    /**
     * Human-readable message describing the result.
     */
    private String message;

    /**
     * The data payload (if any).
     */
    private T data;

    /**
     * List of errors (if any).
     */
    private List<String> errors;

    /**
     * Timestamp of the response.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Creates a successful response with data.
     *
     * @param data    the data payload
     * @param message success message
     * @param <T>     type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with data and default message.
     *
     * @param data the data payload
     * @param <T>  type of data
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Creates an error response.
     *
     * @param message error message
     * @param errors  list of detailed errors
     * @param <T>     type of data (will be null)
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with a single message.
     *
     * @param message error message
     * @param <T>     type of data (will be null)
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}
