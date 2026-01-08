# Authorization Feature (RBAC)

Complete documentation for role-based access control in PlacementPro backend.

## Table of Contents

1. [Overview](#overview)
2. [User Roles](#user-roles)
3. [Role Hierarchy](#role-hierarchy)
4. [Endpoint Protection](#endpoint-protection)
5. [Implementation](#implementation)
6. [Code Examples](#code-examples)
7. [Testing](#testing)

---

## Overview

The authorization system implements Role-Based Access Control (RBAC) with:

- **Four distinct roles**: STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN
- **Method-level security**: Using Spring Security annotations
- **URL-based rules**: Configured in SecurityConfig
- **College-scoped access**: Multi-tenant isolation

### Key Principle

> **Users NEVER select their role.** Roles are assigned by administrators through the system.

---

## User Roles

### Role Matrix

| Feature | STUDENT | COORDINATOR | ADMIN | SUPER_ADMIN |
|---------|:-------:|:-----------:|:-----:|:-----------:|
| View own profile | ✅ | ✅ | ✅ | ✅ |
| View drives | ✅ | ✅ | ✅ | ✅ |
| Apply to drives | ✅ | ❌ | ❌ | ❌ |
| Manage drives | ❌ | ✅ | ✅ | ✅ |
| View all students | ❌ | ✅ | ✅ | ✅ |
| Manage users | ❌ | ❌ | ✅ | ✅ |
| College settings | ❌ | ❌ | ✅ | ✅ |
| View all colleges | ❌ | ❌ | ❌ | ✅ |
| System settings | ❌ | ❌ | ❌ | ✅ |

### Role Descriptions

#### STUDENT
- View and update own profile
- Browse available placement drives
- Apply to eligible drives
- View application status
- View eligibility scores

#### COORDINATOR (TPO)
- All STUDENT permissions
- Create and manage drives
- View all student profiles (in their scope)
- Shortlist candidates
- View analytics and reports
- Manage company relationships

#### ADMIN (College Admin)
- All COORDINATOR permissions
- Manage users within college
- Configure departments
- College-wide settings
- View audit logs

#### SUPER_ADMIN (Platform Admin)
- All ADMIN permissions
- Manage all colleges
- Platform configuration
- System-wide analytics
- Global audit logs

---

## Role Hierarchy

```
SUPER_ADMIN
    │
    └── ADMIN
          │
          └── COORDINATOR
                │
                └── STUDENT
```

**Note**: Higher roles DO NOT automatically inherit permissions of lower roles. Each role has explicitly defined permissions.

---

## Endpoint Protection

### URL Pattern Rules

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        // Public endpoints
        .requestMatchers("/api/v1/auth/login").permitAll()
        .requestMatchers("/swagger-ui/**").permitAll()

        // Role-specific endpoints
        .requestMatchers("/api/v1/admin/**")
            .hasAnyRole("ADMIN", "SUPER_ADMIN")
        .requestMatchers("/api/v1/coordinator/**")
            .hasAnyRole("COORDINATOR", "ADMIN", "SUPER_ADMIN")
        .requestMatchers("/api/v1/superadmin/**")
            .hasRole("SUPER_ADMIN")

        // Authenticated endpoints
        .requestMatchers("/api/v1/**").authenticated()
    );

    return http.build();
}
```

### Endpoint Access Matrix

| Endpoint Pattern | Access |
|------------------|--------|
| `/api/v1/auth/login` | Public |
| `/api/v1/auth/me` | Authenticated |
| `/api/v1/students/**` | STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN |
| `/api/v1/drives/**` | All authenticated |
| `/api/v1/applications/**` | STUDENT, COORDINATOR |
| `/api/v1/coordinator/**` | COORDINATOR, ADMIN, SUPER_ADMIN |
| `/api/v1/admin/**` | ADMIN, SUPER_ADMIN |
| `/api/v1/superadmin/**` | SUPER_ADMIN only |

---

## Implementation

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(this::configureAuthorization)
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler));

        return http.build();
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>
            .AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            .requestMatchers("/api/v1/auth/login").permitAll()
            .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
            .requestMatchers("/api/v1/superadmin/**").hasRole("SUPER_ADMIN")
            .anyRequest().authenticated();
    }
}
```

### Method-Level Security

```java
@Service
public class UserService {

    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public void deleteUser(Long userId) {
        // Only ADMIN and SUPER_ADMIN can delete users
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteCollege(Long collegeId) {
        // Only SUPER_ADMIN can delete colleges
    }

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    public User getUser(Long userId) {
        // Users can view themselves, admins can view anyone
    }
}
```

### Custom User Details

```java
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final UserRole role;
    private final Long collegeId;
    private final boolean isActive;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
```

---

## Code Examples

### Protecting Controller Methods

```java
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        // Accessible by ADMIN and SUPER_ADMIN
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Only SUPER_ADMIN can delete users
    }
}
```

### Checking Roles Programmatically

```java
@Service
public class DriveService {

    public List<Drive> getDrives() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return driveRepository.findAll();
        } else {
            return driveRepository.findByCollegeId(user.getCollegeId());
        }
    }
}
```

### Multi-Tenant Isolation

```java
@Service
public class StudentService {

    @PreAuthorize("hasAnyRole('COORDINATOR', 'ADMIN')")
    public List<Student> getStudents() {
        CustomUserDetails user = getCurrentUser();

        // SUPER_ADMIN sees all, others see only their college
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return studentRepository.findAll();
        }

        return studentRepository.findByCollegeId(user.getCollegeId());
    }
}
```

---

## Testing

### Unit Test for Authorization

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void student_cannot_access_admin_endpoints() throws Exception {
        String token = createTokenForRole(UserRole.STUDENT);

        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_access_admin_endpoints() throws Exception {
        String token = createTokenForRole(UserRole.ADMIN);

        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void coordinator_cannot_access_superadmin_endpoints() throws Exception {
        String token = createTokenForRole(UserRole.COORDINATOR);

        mockMvc.perform(get("/api/v1/superadmin/colleges")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }
}
```
