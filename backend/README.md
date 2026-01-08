# PlacementPro Backend

Java Spring Boot backend for the AI-Assisted Campus Placement System.

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21+ |
| Framework | Spring Boot 3.2.x |
| Build Tool | Maven 3.9+ |
| Database | PostgreSQL 15+ |
| Security | Spring Security + JWT |
| ORM | Hibernate / Spring Data JPA |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+ (or use Supabase)

### Configuration

Create a `.env` file in the backend directory:

```properties
DATABASE_URL=jdbc:postgresql://localhost:5432/campus_placement
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION_MS=86400000
```

### Run Development Server

```bash
# Build and run
mvn clean compile spring-boot:run

# Or just run after initial build
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`

## Project Structure

```
backend/
├── src/main/java/com/campusplacement/
│   ├── CampusPlacementApplication.java   # Main entry point
│   ├── auth/                             # Authentication module
│   │   ├── AuthController.java           # Login/logout endpoints
│   │   ├── AuthService.java              # Auth business logic
│   │   ├── dto/                          # Request/response DTOs
│   │   └── exception/                    # Auth-specific exceptions
│   ├── security/                         # Security configuration
│   │   ├── JwtTokenProvider.java         # JWT generation/validation
│   │   ├── JwtAuthenticationFilter.java  # Request filter
│   │   ├── SecurityConfig.java           # Security configuration
│   │   └── CustomUserDetailsService.java # User loading
│   ├── users/                            # User management
│   ├── students/                         # Student profiles
│   ├── companies/                        # Company management
│   ├── drives/                           # Placement drives
│   ├── applications/                     # Job applications
│   ├── eligibility/                      # Eligibility scoring
│   ├── colleges/                         # Multi-college support
│   ├── config/                           # App configuration
│   │   └── TestDataSeeder.java           # Dev/test data seeding
│   └── common/                           # Shared utilities
│       ├── ApiResponse.java              # Standard response wrapper
│       └── GlobalExceptionHandler.java   # Centralized error handling
├── src/main/resources/
│   ├── application.yml                   # Main configuration
│   └── schema.sql                        # Database schema
└── src/test/java/com/campusplacement/   # Test classes
```

## API Endpoints

All endpoints use the `/api/v1` prefix.

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/logout` | User logout | Yes |
| GET | `/api/v1/auth/me` | Current user info | Yes |

### Protected Resources

| Module | Base Path | Access |
|--------|-----------|--------|
| Students | `/api/v1/students` | STUDENT, COORDINATOR, ADMIN |
| Drives | `/api/v1/drives` | All authenticated |
| Applications | `/api/v1/applications` | STUDENT, COORDINATOR |
| Companies | `/api/v1/companies` | COORDINATOR, ADMIN |
| Analytics | `/api/v1/analytics` | COORDINATOR, ADMIN |
| Users | `/api/v1/users` | ADMIN, SUPER_ADMIN |

## Authentication & Authorization

### JWT Token Structure

```json
{
  "sub": "user@email.com",
  "uid": 123,
  "role": "STUDENT",
  "cid": 1,
  "ver": 1,
  "iat": 1704067200,
  "exp": 1704153600
}
```

### Role-Based Access Control (RBAC)

| Role | Description |
|------|-------------|
| STUDENT | View drives, apply to jobs, manage profile |
| COORDINATOR | Manage drives, view applicants, analytics |
| ADMIN | College administration, user management |
| SUPER_ADMIN | Platform-wide administration |

## Error Handling

The backend uses centralized error handling via `GlobalExceptionHandler`:

| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| `AuthenticationException` | 401 | Invalid credentials |
| `AccountDeactivatedException` | 403 | Disabled account |
| `CollegeInactiveException` | 404 | Inactive college |
| `AccessDeniedException` | 403 | Insufficient permissions |
| `MethodArgumentNotValidException` | 400 | Validation errors |
| `Exception` | 500 | Unexpected errors |

### Standard Response Format

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

```json
{
  "success": false,
  "message": "Error message here",
  "errors": { "field": "error" }
}
```

## Testing

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests (Requires Docker)

```bash
# Run integration tests
mvn test -Dtest=*IntegrationTest

# Testcontainers automatically spins up PostgreSQL
```

### Test Data Seeding

For development/testing, the `TestDataSeeder` automatically creates test users when running with `dev` or `test` profile:

| Email | Role | Password |
|-------|------|----------|
| student@test.edu | STUDENT | password123 |
| coordinator@test.edu | COORDINATOR | password123 |
| admin@test.edu | ADMIN | password123 |
| superadmin@platform.com | SUPER_ADMIN | password123 |

## Database

### Supported Databases

- PostgreSQL 15+ (recommended)
- Supabase (cloud PostgreSQL)

### Schema Management

The `schema.sql` file contains the idempotent database schema that runs on startup.

## Security Features

- JWT-based stateless authentication
- BCrypt password hashing
- Role-based endpoint protection
- CORS configuration for frontend
- Login audit logging
- IP address tracking

## Development

### Hot Reload

```bash
# With Spring Boot DevTools (included)
mvn spring-boot:run
```

### Debug Mode

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## Related Documentation

- [Architecture](../docs/architecture.md)
- [API Specifications](../docs/api-specs.md)
- [Main README](../docs/README.md)
