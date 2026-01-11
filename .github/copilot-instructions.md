# PlacementPro AI Coding Agent Instructions

## System Architecture

**PlacementPro** is a modular placement management system:

- **Backend**: Java 21, Spring Boot 3.2.x, PostgreSQL 15+, Maven
- **Frontend**: Next.js 14+, React 18, Tailwind CSS 3.x, Node 18+
- **AI Service**: Python 3.10+, FastAPI 0.100+ (decision-support only, not autonomous)
- **Pattern**: Controllers → Services (interface+implementation) → Repositories → Database
- **Auth**: JWT + Refresh Tokens with role-based access control (STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN)

## Backend Modules & Responsibilities

| Module           | Purpose                                                               |
| ---------------- | --------------------------------------------------------------------- |
| `auth/`          | JWT authentication, token generation/validation, refresh tokens       |
| `security/`      | Spring Security config, JWT filters, user details, scope security     |
| `users/`         | User entity and management                                            |
| `students/`      | Student profile management (dual-ownership model)                     |
| `companies/`     | Company profiles and management                                       |
| `drives/`        | Placement drive lifecycle (DriveService interface + DriveServiceImpl) |
| `applications/`  | Job applications with scope-aware RBAC queries                        |
| `organizations/` | OrganizationUnit hierarchy, UserAssignment, ScopeContext              |
| `eligibility/`   | Eligibility scoring (rule-first, AI-second approach)                  |
| `analytics/`     | Placement statistics & reporting                                      |
| `ai/`            | AIClient with Circuit Breaker + Retry patterns                        |
| `colleges/`      | College entity (top-level tenant)                                     |
| `common/`        | ApiResponse wrapper, GlobalExceptionHandler, utilities                |
| `config/`        | Application configuration beans                                       |

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

### Documentation

- **Architecture**: [docs/architecture.md](docs/architecture.md)
- **Security model**: [docs/security.md](docs/security.md)
- **Testing strategy**: [docs/testing.md](docs/testing.md)
- **API Specifications**: [docs/api-specs.md](docs/api-specs.md)

### Feature Documentation

- **Applications**: [docs/features/applications.md](docs/features/applications.md)
- **Placement Drives**: [docs/features/drives.md](docs/features/drives.md)
- **Eligibility**: [docs/features/eligibility.md](docs/features/eligibility.md)
- **Students**: [docs/features/students.md](docs/features/students.md)
- **Companies**: [docs/features/companies.md](docs/features/companies.md)
- **Analytics**: [docs/features/analytics.md](docs/features/analytics.md)
- **Multi-Tenancy**: [docs/features/multi-tenancy.md](docs/features/multi-tenancy.md)
- **Authentication**: [docs/features/authentication.md](docs/features/authentication.md)

### Technical Documentation

- **ER Diagram**: [docs/database/ER-DIAGRAM.md](docs/database/ER-DIAGRAM.md)
- **Business Workflows**: [docs/workflows/BUSINESS-WORKFLOWS.md](docs/workflows/BUSINESS-WORKFLOWS.md)
- **Backend Overview**: [backend/docs/BACKEND-OVERVIEW.md](backend/docs/BACKEND-OVERVIEW.md)
- **AI Service**: [docs/ai-service/README.md](docs/ai-service/README.md)

### READMEs

- **Backend**: [backend/README.md](backend/README.md)
- **Frontend**: [frontend/README.md](frontend/README.md)

### Agent Rules

- **Placement Rules**: [.agent/rules/placement-rules.md](.agent/rules/placement-rules.md)
- **Code Review Workflow**: [.agent/workflows/code-review.md](.agent/workflows/code-review.md)
- **Feature Implementation**: [.agent/workflows/feature-implementation.md](.agent/workflows/feature-implementation.md)

## When Stuck

1. **System design questions**: → `docs/architecture.md`
2. **Service implementation pattern**: → Review `DriveServiceImpl` (standard interface+impl pattern)
3. **RBAC/queries**: → Study `ApplicationRepository` (excellent scope-aware query examples)
4. **Frontend auth**: → `AuthProvider.jsx` → `AuthContext.jsx` → `useAuth()` hook
5. **Adding new role**: Update `Role` enum → create `@PreAuthorize` rules → update UI dashboards
6. **Multi-tenancy**: → `OrganizationScopeService` for scope resolution
7. **AI Integration**: → `AIClient` with Circuit Breaker pattern
8. **Feature workflows**: → `.agent/workflows/feature-implementation.md`

---

**Last Updated**: January 11, 2026
**Stack Versions**: Java 21, Spring Boot 3.2.x, Node 18+, Python 3.10+, PostgreSQL 15+
