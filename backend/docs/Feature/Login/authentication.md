# Authentication Feature

Complete documentation for the authentication system in PlacementPro backend.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Components](#components)
4. [API Endpoints](#api-endpoints)
5. [JWT Token Structure](#jwt-token-structure)
6. [Authentication Flow](#authentication-flow)
7. [Code Examples](#code-examples)
8. [Error Handling](#error-handling)
9. [Security Considerations](#security-considerations)
10. [Testing](#testing)
11. [Configuration](#configuration)

---

## Overview

The authentication system provides secure user authentication using JWT (JSON Web Tokens). It supports:

- **Stateless Authentication**: No server-side session storage
- **Role-Based Access**: Four distinct user roles
- **Multi-Tenant Support**: College-scoped authentication
- **Audit Logging**: Complete login attempt tracking
- **Secure Password Storage**: BCrypt hashing with salt

### Key Principles

1. **Users NEVER select their role** - Roles are assigned by administrators
2. **Tokens are stateless** - No blacklist, version-based invalidation
3. **All credentials validated server-side** - Never trust client input
4. **Failed attempts are logged** - For security monitoring

> **Note on Registration**: Public registration is NOT supported. User creation is a privileged governance action handled by specific roles (Coordinator, Admin, Super Admin) via dedicated endpoints. See `UserGovernance.md` for details.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT REQUEST                                  │
│                    (Email + Password OR JWT Token)                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         JwtAuthenticationFilter                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 1. Extract token from Authorization header                          │   │
│  │ 2. Validate token signature and expiration                          │   │
│  │ 3. Load user details from database                                  │   │
│  │ 4. Set SecurityContext authentication                               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AuthController                                  │
│  ┌──────────────────┐ ┌──────────────────────────┐                          │
│  │ POST /login      │ │ POST /logout             │                          │
│  │ POST /me         │ │                          │                          │
│  └──────────────────┘ └──────────────────────────┘                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               AuthService                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ • Validate credentials                                               │   │
│  │ • Check user active status                                          │   │
│  │ • Check college active status                                       │   │
│  │ • Generate JWT token                                                │   │
│  │ • Audit login attempts                                              │   │
│  │ • Update last login timestamp                                       │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            JwtTokenProvider                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ • Generate tokens with claims                                        │   │
│  │ • Validate token signatures                                         │   │
│  │ • Extract claims (email, role, userId, collegeId)                   │   │
│  │ • Check token expiration                                            │   │
│  │ • Version-based invalidation support                                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Components

### 1. AuthController

**Location**: `com.campusplacement.auth.AuthController`

REST controller handling authentication endpoints.

```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
        @Valid @RequestBody LoginRequestDTO request);

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout();

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> getCurrentUser();
}
```

### 2. AuthService

**Location**: `com.campusplacement.auth.AuthService`

Core authentication business logic.

**Responsibilities:**
- Credential validation
- User status verification
- College status verification
- Token generation
- Audit logging
- Last login tracking

### 3. JwtTokenProvider

**Location**: `com.campusplacement.security.JwtTokenProvider`

JWT token generation and validation.

**Token Claims:**
| Claim | Description | Example |
|-------|-------------|---------|
| `sub` | Subject (email) | user@test.edu |
| `uid` | User ID | 123 |
| `role` | User role | STUDENT |
| `cid` | College ID | 1 |
| `ver` | Token version | 1 |
| `oid` | Org unit ID (optional) | 5 |
| `iat` | Issued at | 1704067200 |
| `exp` | Expiration | 1704153600 |

### 4. JwtAuthenticationFilter

**Location**: `com.campusplacement.security.JwtAuthenticationFilter`

Spring Security filter that intercepts requests and validates JWT tokens.

**Filter Chain Position**: Before `UsernamePasswordAuthenticationFilter`

### 5. CustomUserDetailsService

**Location**: `com.campusplacement.security.CustomUserDetailsService`

Loads user details from database for Spring Security.

### 6. CustomUserDetails

**Location**: `com.campusplacement.security.CustomUserDetails`

Custom implementation of `UserDetails` with additional fields.

---

## API Endpoints

### POST /api/v1/auth/login

Authenticates a user and returns a JWT token.

**Request:**
```json
{
  "email": "student@test.edu",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "userId": 123,
    "role": "STUDENT",
    "collegeId": 1,
    "redirectUrl": "/dashboard/student"
  },
  "message": "Login successful"
}
```

**Error Responses:**

| Status | Condition | Response |
|--------|-----------|----------|
| 401 | Invalid credentials | `{"success": false, "message": "Invalid credentials"}` |
| 403 | Account deactivated | `{"success": false, "message": "Account is deactivated..."}` |
| 404 | College inactive | `{"success": false, "message": "College is not active..."}` |
| 400 | Validation error | `{"success": false, "message": "Validation failed", "errors": {...}}` |

### POST /api/v1/auth/logout

Logs out the current user.

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Logout successful"
}
```

### GET /api/v1/auth/me

Gets the current authenticated user's information.

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "role": "STUDENT",
    "collegeId": 1,
    "redirectUrl": "/dashboard/student"
  }
}
```

---

## JWT Token Structure

### Header
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "student@test.edu",
  "uid": 123,
  "role": "STUDENT",
  "cid": 1,
  "ver": 1,
  "iat": 1704067200,
  "exp": 1704153600
}
```

### Token Lifetime

| Environment | Duration |
|-------------|----------|
| Development | 24 hours |
| Production | 24 hours (configurable) |

### Token Validation Rules

1. **Signature Valid**: Token signed with secret key
2. **Not Expired**: Current time < expiration time
3. **Version Match**: Token version >= required version
4. **User Exists**: User still exists in database
5. **User Active**: User account is active
6. **College Active**: User's college is active (if applicable)

---

## Authentication Flow

### Login Flow

```
┌──────────┐          ┌──────────────┐          ┌─────────────┐
│  Client  │          │ AuthController│          │ AuthService │
└────┬─────┘          └──────┬───────┘          └──────┬──────┘
     │                       │                         │
     │  POST /login          │                         │
     │  {email, password}    │                         │
     │──────────────────────>│                         │
     │                       │                         │
     │                       │  login(request)         │
     │                       │────────────────────────>│
     │                       │                         │
     │                       │        ┌────────────────┴───────────────────┐
     │                       │        │ 1. Find user by email              │
     │                       │        │ 2. Verify password (BCrypt)        │
     │                       │        │ 3. Check user.isActive             │
     │                       │        │ 4. Check college.isActive          │
     │                       │        │ 5. Generate JWT token              │
     │                       │        │ 6. Build redirect URL              │
     │                       │        │ 7. Update lastLogin                │
     │                       │        │ 8. Audit login attempt             │
     │                       │        └────────────────┬───────────────────┘
     │                       │                         │
     │                       │  LoginResponseDTO       │
     │                       │<────────────────────────│
     │                       │                         │
     │  200 OK               │                         │
     │  {token, userId, ...} │                         │
     │<──────────────────────│                         │
     │                       │                         │
```

### Request Authentication Flow

```
┌──────────┐     ┌─────────────────────┐     ┌─────────────────┐     ┌────────────┐
│  Client  │     │JwtAuthenticationFilter│   │CustomUserDetails│     │ Controller │
└────┬─────┘     └──────────┬──────────┘     └────────┬────────┘     └─────┬──────┘
     │                      │                         │                    │
     │  GET /api/v1/resource│                         │                    │
     │  Authorization: Bearer <token>                 │                    │
     │─────────────────────>│                         │                    │
     │                      │                         │                    │
     │         ┌────────────┴───────────────┐         │                    │
     │         │ 1. Extract token from header│        │                    │
     │         │ 2. Validate token           │        │                    │
     │         └────────────┬───────────────┘         │                    │
     │                      │                         │                    │
     │                      │  loadUserByUsername     │                    │
     │                      │────────────────────────>│                    │
     │                      │                         │                    │
     │                      │  UserDetails            │                    │
     │                      │<────────────────────────│                    │
     │                      │                         │                    │
     │         ┌────────────┴───────────────┐         │                    │
     │         │ Set SecurityContext        │         │                    │
     │         └────────────┬───────────────┘         │                    │
     │                      │                         │                    │
     │                      │  Continue filter chain  │                    │
     │                      │─────────────────────────────────────────────>│
     │                      │                         │                    │
     │  200 OK              │                         │                    │
     │  {response data}     │                         │                    │
     │<────────────────────────────────────────────────────────────────────│
```

---

## Code Examples

### Generating a Token

```java
@Service
public class JwtTokenProvider {

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("uid", user.getId())
            .claim("role", user.getRole().name())
            .claim("cid", user.getCollege() != null ? user.getCollege().getId() : null)
            .claim("ver", getCurrentTokenVersion())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
}
```

### Validating a Token

```java
public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        log.warn("Invalid JWT token: {}", e.getMessage());
        return false;
    }
}
```

### Extracting Claims

```java
public String getEmailFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
    return claims.getSubject();
}

public Long getUserIdFromToken(String token) {
    Claims claims = getClaims(token);
    return claims.get("uid", Long.class);
}

public String getRoleFromToken(String token) {
    Claims claims = getClaims(token);
    return claims.get("role", String.class);
}
```

### Login Implementation

```java
@Transactional
public LoginResponseDTO login(LoginRequestDTO request) {
    String email = request.getEmail().toLowerCase().trim();

    // 1. Find user
    User user = userRepository.findByEmailWithCollege(email)
        .orElseThrow(() -> {
            auditLogin(null, email, false);
            return new AuthenticationException("Invalid credentials");
        });

    // 2. Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        auditLogin(user.getId(), email, false);
        throw new AuthenticationException("Invalid credentials");
    }

    // 3. Check user active
    if (user.getIsActive() == null || !user.getIsActive()) {
        auditLogin(user.getId(), email, false);
        throw new AccountDeactivatedException("Account is deactivated");
    }

    // 4. Check college active (non-super-admin)
    if (user.getRole() != UserRole.SUPER_ADMIN) {
        College college = user.getCollege();
        if (college == null || !college.getIsActive()) {
            auditLogin(user.getId(), email, false);
            throw new CollegeInactiveException("College is not active");
        }
    }

    // 5. Generate token and respond
    String token = jwtTokenProvider.generateToken(user);
    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);
    auditLogin(user.getId(), email, true);

    return LoginResponseDTO.builder()
        .token(token)
        .userId(user.getId())
        .role(user.getRole().name())
        .collegeId(user.getCollege() != null ? user.getCollege().getId() : null)
        .redirectUrl(buildRedirectUrl(user.getRole()))
        .build();
}
```

---

## Error Handling

### Custom Exceptions

| Exception | HTTP Status | When Thrown |
|-----------|-------------|-------------|
| `AuthenticationException` | 401 | Invalid email/password |
| `AccountDeactivatedException` | 403 | Account disabled |
| `CollegeInactiveException` | 404 | College not active |

### Exception Definitions

```java
// AuthenticationException.java
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

// AccountDeactivatedException.java
public class AccountDeactivatedException extends RuntimeException {
    public AccountDeactivatedException(String message) {
        super(message);
    }
}

// CollegeInactiveException.java
public class CollegeInactiveException extends RuntimeException {
    public CollegeInactiveException(String message) {
        super(message);
    }
}
```

### Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDeactivatedException(
            AccountDeactivatedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
    }
}
```

---

## Security Considerations

### Password Security

- **Algorithm**: BCrypt with default strength (10 rounds)
- **Salt**: Automatically generated per password
- **Storage**: Only hash stored, never plaintext

```java
// Encoding
String hash = passwordEncoder.encode(plainPassword);

// Verification
boolean matches = passwordEncoder.matches(plainPassword, hash);
```

### Token Security

- **Algorithm**: HMAC-SHA512
- **Secret Key**: 256-bit minimum (configurable)
- **No Blacklist**: Version-based invalidation instead
- **Short Lifetime**: 24 hours default

### Audit Logging

Every login attempt is logged with:
- User ID (if known)
- Email attempted
- Timestamp
- IP address (X-Forwarded-For aware)
- User agent
- Success/failure status

```java
@Entity
@Table(name = "login_audit")
public class LoginAudit {
    @Id @GeneratedValue
    private Long id;
    private Long userId;
    private String email;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private Boolean success;
}
```

### Rate Limiting

> **Note**: Rate limiting is not yet implemented. Recommended implementation:
> - 5 failed attempts per email per 15 minutes
> - Progressive lockout (1min, 5min, 15min, 1hr)

---

## Testing

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returns_token() {
        // Given
        User user = createTestUser();
        when(userRepository.findByEmailWithCollege("test@test.edu"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPasswordHash()))
            .thenReturn(true);
        when(jwtTokenProvider.generateToken(user))
            .thenReturn("test-token");

        // When
        LoginResponseDTO result = authService.login(
            new LoginRequestDTO("test@test.edu", "password"));

        // Then
        assertThat(result.getToken()).isEqualTo("test-token");
        assertThat(result.getRole()).isEqualTo("STUDENT");
    }

    @Test
    void login_wrong_password_throws() {
        // Given
        User user = createTestUser();
        when(userRepository.findByEmailWithCollege("test@test.edu"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash()))
            .thenReturn(false);

        // When/Then
        assertThrows(AuthenticationException.class, () ->
            authService.login(new LoginRequestDTO("test@test.edu", "wrong")));
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_success_returns_token() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "student@test.edu", "password": "password123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").exists())
            .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }
}
```

---

## Configuration

### Application Properties

```yaml
# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-minimum}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000}  # 24 hours

# Security Configuration
spring:
  security:
    enabled: true
```

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `JWT_SECRET` | Secret key for signing JWTs | Yes |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds | No (default: 24h) |

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```
