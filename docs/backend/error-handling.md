# Error Handling

Complete documentation for error handling in the PlacementPro backend.

## Table of Contents

1. [Overview](#overview)
2. [Error Response Format](#error-response-format)
3. [Custom Exceptions](#custom-exceptions)
4. [Global Exception Handler](#global-exception-handler)
5. [HTTP Status Code Mapping](#http-status-code-mapping)
6. [Logging Strategy](#logging-strategy)
7. [Best Practices](#best-practices)

---

## Overview

The error handling system provides:

- **Consistent response format**: All errors follow the same JSON structure
- **Centralized handling**: One place for all exception mapping
- **Proper HTTP status codes**: Semantic status codes for different errors
- **Security-conscious messages**: External errors don't leak internal details
- **Comprehensive logging**: Internal logging with full stack traces

---

## Error Response Format

### Standard Error Response

```json
{
  "success": false,
  "message": "User-friendly error message",
  "errors": null
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters"
  }
}
```

### ApiResponse Class

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private Map<String, String> errors;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .build();
    }
}
```

---

## Custom Exceptions

### AuthenticationException

Thrown when authentication fails (invalid credentials).

```java
package com.campusplacement.auth.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**When to throw:**
- Email not found in database
- Password doesn't match
- Token is invalid or expired

### AccountDeactivatedException

Thrown when a deactivated account attempts to login.

```java
package com.campusplacement.auth.exception;

public class AccountDeactivatedException extends RuntimeException {

    public AccountDeactivatedException(String message) {
        super(message);
    }
}
```

**When to throw:**
- User's `isActive` flag is false
- User has been soft-deleted

### CollegeInactiveException

Thrown when user's college is inactive.

```java
package com.campusplacement.auth.exception;

public class CollegeInactiveException extends RuntimeException {

    public CollegeInactiveException(String message) {
        super(message);
    }
}
```

**When to throw:**
- College's `isActive` flag is false
- User has no associated college (non-SUPER_ADMIN)

---

## Global Exception Handler

### Complete Implementation

```java
package com.campusplacement.common;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 401 - Authentication Failed
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 401 - User Not Found (masked as invalid credentials)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(
            UsernameNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid credentials"));
    }

    // 403 - Account Deactivated
    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDeactivatedException(
            AccountDeactivatedException ex) {
        log.warn("Account deactivated: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 404 - College Inactive
    @ExceptionHandler(CollegeInactiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleCollegeInactiveException(
            CollegeInactiveException ex) {
        log.warn("College inactive: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 403 - Access Denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(
                "Access denied. You don't have permission to access this resource."));
    }

    // 400 - Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
    }

    // 400 - Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    // 500 - Internal Server Error (catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(
                "An unexpected error occurred. Please try again later."));
    }
}
```

---

## HTTP Status Code Mapping

| Exception | HTTP Status | Code | Use Case |
|-----------|-------------|------|----------|
| `AuthenticationException` | UNAUTHORIZED | 401 | Invalid credentials |
| `UsernameNotFoundException` | UNAUTHORIZED | 401 | User not found |
| `AccountDeactivatedException` | FORBIDDEN | 403 | Deactivated account |
| `AccessDeniedException` | FORBIDDEN | 403 | Insufficient permissions |
| `CollegeInactiveException` | NOT_FOUND | 404 | Inactive college |
| `MethodArgumentNotValidException` | BAD_REQUEST | 400 | Validation error |
| `IllegalArgumentException` | BAD_REQUEST | 400 | Bad input |
| `Exception` | INTERNAL_SERVER_ERROR | 500 | Unexpected error |

---

## Logging Strategy

### Log Levels

| Level | Usage | Example |
|-------|-------|---------|
| ERROR | Unexpected exceptions, system failures | Database connection lost |
| WARN | Expected but notable errors | Invalid login attempt |
| INFO | Important business events | User logged in |
| DEBUG | Detailed debugging info | Token validation steps |

### Logging Examples

```java
// WARN - Expected authentication failure
log.warn("Authentication failed for email: {}", email);

// WARN - Access denied attempt
log.warn("Access denied for user {} to resource {}", userId, resource);

// ERROR - Unexpected error with stack trace
log.error("Unexpected error occurred", ex);

// INFO - Important business event
log.info("User {} logged in successfully with role {}", email, role);

// DEBUG - Detailed debugging
log.debug("Token validated successfully for user {}", email);
```

### What NOT to Log

```java
// NEVER log passwords
log.info("Password: {}", password);  // DON'T DO THIS

// NEVER log full tokens
log.info("Token: {}", token);  // DON'T DO THIS

// Log token prefix instead
log.debug("Token validated: {}...", token.substring(0, 20));
```

---

## Best Practices

### 1. Use Specific Exception Types

```java
// GOOD - Specific exception
throw new AuthenticationException("Invalid credentials");

// BAD - Generic exception
throw new RuntimeException("Login failed");
```

### 2. Don't Expose Internal Details

```java
// GOOD - User-friendly message
return ApiResponse.error("An unexpected error occurred");

// BAD - Exposes internal structure
return ApiResponse.error("NullPointerException at UserService.java:45");
```

### 3. Validate Early, Fail Fast

```java
public void createUser(CreateUserDTO dto) {
    // Validate first
    if (dto.getEmail() == null) {
        throw new IllegalArgumentException("Email is required");
    }

    // Then process
    userRepository.save(mapToEntity(dto));
}
```

### 4. Use Validation Annotations

```java
public class LoginRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

### 5. Handle Null Safely

```java
// GOOD - Null-safe check
if (user.getIsActive() == null || !user.getIsActive()) {
    throw new AccountDeactivatedException("Account is deactivated");
}

// BAD - Potential NPE
if (!user.getIsActive()) {  // NPE if isActive is null
    throw new AccountDeactivatedException("...");
}
```

### 6. Audit Security Events

```java
public LoginResponseDTO login(LoginRequestDTO request) {
    try {
        // ... authentication logic
        auditLogin(userId, email, true);
        return response;
    } catch (AuthenticationException ex) {
        auditLogin(userId, email, false);  // Log failed attempt
        throw ex;
    }
}
```

---

## Related Documentation

### Project Documentation
- [Backend Overview](../README.md) - Module architecture with common module
- [API Specifications](../../api-specs.md) - Response format specifications
- [Security Model](../../security.md) - Security event logging

### Feature Documentation
- [Applications](../../functional-specs/applications.md) - Application error scenarios
- [Eligibility](../../functional-specs/eligibility.md) - Eligibility check errors
- [Authentication](auth/authentication.md) - Auth error handling
