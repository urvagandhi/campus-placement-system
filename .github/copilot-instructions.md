# PlacementPro AI Coding Agent Instructions

## System Architecture

**PlacementPro** is a modular placement management system:

- **Backend**: Java 21, Spring Boot 3.2.x, PostgreSQL 15+, Maven
- **Frontend**: Next.js, React, TypeScript, Tailwind CSS, Node 18+
- **AI Service**: Python 3.10+, FastAPI (decision-support only, not autonomous)
- **Pattern**: Controllers → Services (interface+implementation) → Repositories → Database
- **Auth**: JWT tokens with role-based access control (STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN)

## Backend Modules & Responsibilities

| Module                              | Purpose                                                               |
| ----------------------------------- | --------------------------------------------------------------------- |
| `auth/`                             | JWT authentication, token generation/validation                       |
| `security/`                         | Spring Security config, JWT filters, user details                     |
| `users/`, `students/`, `companies/` | Entity lifecycle management                                           |
| `drives/`                           | Placement drive lifecycle (DriveService interface + DriveServiceImpl) |
| `applications/`                     | Job applications with scope-aware RBAC queries                        |
| `eligibility/`                      | Eligibility scoring (calls AI service, caches results)                |
| `analytics/`                        | Placement statistics & reporting                                      |
| `ai/`                               | HTTP client (AIClient) for Python AI service                          |
| `common/`                           | ApiResponse wrapper, GlobalExceptionHandler, utilities                |

## Critical Implementation Patterns

### 1. Service Interface Pattern

```java
// Example: DriveService (interface) + DriveServiceImpl
public interface DriveService {
    DriveDTO createDrive(CreateDriveRequest request);
    DriveDTO getDriveById(Long id);
}

public class DriveServiceImpl implements DriveService { ... }
```

**Why**: Dependency inversion, testability, clear contracts.

### 2. DTO at API Boundaries

Controllers use DTOs (not JPA entities directly). Services map between DTOs and entities.

### 3. Scope-Aware Repository Queries

```java
// ApplicationRepository example
@Query("SELECT a FROM Application a WHERE a.student.college.id = :collegeId")
List<Application> findByCollege(@Param("collegeId") Long collegeId);
```

**Purpose**: RBAC enforcement—every query filters by college_id, department_id, or user context.

### 4. Centralized Error Handling

GlobalExceptionHandler + ApiResponse wrapper:

```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
}
```

All endpoints return this structure.

### 5. AI Service Integration

- Backend calls Python service via `AIClient.callEligibilityScore(request)`
- Results cached in `EligibilityResult` table
- No frontend → AI direct calls
- Health check: GET `http://localhost:8000/health`

## Data Ownership Model

| Category              | Source                                                                                   | Mutable |
| --------------------- | ---------------------------------------------------------------------------------------- | ------- |
| **Institution-owned** | SSO/Email → Email, role, college_id, enrollment_no, CGPA, backlogs, department_id        | No      |
| **Student-owned**     | Student input → name, phone_number, skills, resume_url, certifications, career_interests | Yes     |

Enforced at service layer via `StudentProfile` fields.

## Build & Run Commands

**Backend (Java/Maven)**

```bash
mvn clean compile              # Compile
mvn spring-boot:run            # Run (localhost:8080)
mvn test                       # Unit tests
mvn test -Dtest=*IntegrationTest  # Integration tests
mvn test jacoco:report         # Coverage → target/site/jacoco/index.html
```

**Frontend (Next.js)**

```bash
npm install                    # Install deps
npm run dev                    # Dev server (localhost:3000)
npm run build && npm start     # Production
npm test                       # Jest tests
npm run test:coverage          # Coverage report
npm run test:e2e               # Playwright E2E (requires: npx playwright install)
```

**AI Service (Python/FastAPI)**

```bash
pip install -r requirements.txt
uvicorn app:app --reload      # Run (localhost:8000)
curl http://localhost:8000/health  # Health check
# Swagger docs: http://localhost:8000/docs
```

## Key Conventions

1. **API Versioning**: All endpoints use `/api/v1` prefix
2. **Authentication**: JWT via `Authorization: Bearer <token>` header
3. **CORS**: Backend allows `localhost:3000` and `localhost:8080`
4. **Logging**: SLF4J with `logback-spring.xml`
5. **Roles**: Hierarchical (SUPER_ADMIN > ADMIN > COORDINATOR > STUDENT)

## Common Development Tasks

**Adding a new endpoint:**

1. Create controller method with `@GetMapping`/`@PostMapping`
2. Implement in Service (interface + implementation class)
3. Add Repository method if needed (with `@Query` for RBAC filtering)
4. Map DTOs in service layer
5. Test with unit tests + integration tests

**Adding RBAC check:**

1. Use `@PreAuthorize("hasRole('COORDINATOR')")` on controller
2. Add scope-filtered `@Query` to repository (filter by college_id, etc.)
3. Example: `ApplicationRepository.findByCollege(Long collegeId)`

**Integrating with AI service:**

1. Create request object in `schemas/` (Pydantic model)
2. Call `AIClient.callEligibilityScore(request)` in `EligibilityServiceImpl`
3. Store result in `EligibilityResult` table
4. Return DTO to frontend

**Frontend authentication:**

1. Wrap app in `AuthProvider`
2. Use `useAuth()` hook to access token, user, roles
3. Protect routes with `ProtectedRoute` component
4. Call API with `Authorization` header via `authService.js`

## Important Files & References

- **Architecture**: [docs/architecture.md](docs/architecture.md)
- **Security model**: [docs/security.md](docs/security.md)
- **Testing strategy**: [docs/testing.md](docs/testing.md)
- **Backend README**: [backend/README.md](backend/README.md)
- **Frontend README**: [frontend/README.md](frontend/README.md)
- **AI Service**: [ai-service/app.py](ai-service/app.py)

## When Stuck

1. **System design questions**: → `docs/architecture.md`
2. **Service implementation pattern**: → Review `DriveServiceImpl` (standard interface+impl pattern)
3. **RBAC/queries**: → Study `ApplicationRepository` (excellent scope-aware query examples)
4. **Frontend auth**: → `AuthProvider.jsx` → `AuthContext.jsx` → `useAuth()` hook
5. **Adding new role**: Update `Role` enum → create `@PreAuthorize` rules → update UI dashboards

---

**Last Updated**: January 10, 2026
**Stack Versions**: Java 21, Spring Boot 3.2.x, Node 18+, Python 3.10+, PostgreSQL 15+
