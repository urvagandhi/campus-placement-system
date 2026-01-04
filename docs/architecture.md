# Architecture Documentation

## System Architecture

The AI-Assisted Campus Placement System follows a **Modular Monolithic** architecture with a separate AI microservice for decision support.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              USERS                                       │
│                 Students │ TPO │ Admin │ Super Admin                    │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                               │
│                         Next.js Frontend                                 │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐            │
│  │  Student  │  │   TPO     │  │   Admin   │  │  Common   │            │
│  │ Dashboard │  │ Dashboard │  │ Dashboard │  │Components │            │
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘            │
│                         API Service Layer                                │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ REST API (JSON)
                                    │ Authorization: Bearer <JWT>
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                                │
│                    Java Spring Boot Backend                              │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                     Security Filter Chain                         │   │
│  │              JWT Authentication + Role Authorization              │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │   Auth   │ │ Students │ │Companies │ │  Drives  │ │Analytics │     │
│  │Controller│ │Controller│ │Controller│ │Controller│ │Controller│     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘     │
│       │            │            │            │            │             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │   Auth   │ │ Student  │ │ Company  │ │  Drive   │ │Analytics │     │
│  │ Service  │ │ Service  │ │ Service  │ │ Service* │ │ Service  │     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘     │
│       │            │            │            │                          │
│  ┌─────────────────────────────────────────────┐       │               │
│  │              Repository Layer               │       │               │
│  │    JPA Repositories (Spring Data)           │       │               │
│  └─────────────────────────────────────────────┘       │               │
│                          │                              │               │
│                          │                       ┌──────────────┐       │
│                          │                       │   AI Client  │───────┤
│                          │                       └──────────────┘       │
└──────────────────────────┼──────────────────────────────────────────────┘
                           │                              │
                           │                              │ HTTP (Internal)
                           ▼                              ▼
┌──────────────────────────────────┐    ┌─────────────────────────────────┐
│          DATA LAYER              │    │       AI SERVICE LAYER          │
│         MySQL Database           │    │     Python FastAPI              │
│  ┌─────────────────────────────┐ │    │  ┌────────────────────────────┐ │
│  │        Tables               │ │    │  │   Eligibility Service      │ │
│  │  • users                    │ │    │  │   Skill Gap Service        │ │
│  │  • student_profiles         │ │    │  │   Career Insights (TODO)   │ │
│  │  • companies                │ │    │  └────────────────────────────┘ │
│  │  • placement_drives         │ │    │                                  │
│  │  • applications             │ │    │  * Decision Support Only        │
│  │  • eligibility_results      │ │    │  * No Autonomous Decisions      │
│  └─────────────────────────────┘ │    └─────────────────────────────────┘
└──────────────────────────────────┘

* Service Interface + Implementation Pattern
```

## Design Principles

| Principle | Implementation |
|-----------|----------------|
| **Modular Monolith** | Feature-based package structure, not microservices |
| **Separation of Concerns** | Controller → Service → Repository pattern |
| **Loose Coupling** | Interface-based design, DTOs for data transfer |
| **Single Responsibility** | Each module handles one domain area |
| **Open/Closed** | Services implement interfaces for extensibility |

## Module Responsibilities

### Backend Modules

| Module | Responsibility |
|--------|----------------|
| `auth` | User authentication, JWT token management |
| `users` | User CRUD operations, role management |
| `students` | Student profile management |
| `companies` | Company/recruiter data management |
| `drives` | Placement drive lifecycle management |
| `applications` | Job application tracking |
| `eligibility` | Eligibility scoring (via AI service) |
| `analytics` | Placement statistics and reporting |
| `ai` | AI service client for decision support |
| `config` | Security, CORS, application configuration |
| `common` | Shared utilities, base classes, constants |

### AI Service Modules

| Module | Responsibility |
|--------|----------------|
| `eligibility_service` | Rule-based eligibility scoring |
| `skill_gap_service` | Skill gap analysis and recommendations |
| `models` | Pydantic data models |
| `schemas` | API request/response schemas |

## Data Flow

### Eligibility Check Flow

```
1. Frontend → GET /api/v1/eligibility/check?studentId=X&driveId=Y
2. Backend EligibilityController receives request
3. EligibilityService orchestrates the check
4. AIClient calls Python service POST /api/v1/eligibility/score
5. Python EligibilityService calculates rule-based score
6. Result returned through the chain
7. Frontend displays eligibility status
```

## Security Architecture

- **Authentication**: JWT tokens with role claims
- **Authorization**: Spring Security @PreAuthorize annotations
- **RBAC**: Four roles with hierarchical permissions
- **AI Service**: Internal-only (not exposed to frontend)

## Scalability Considerations

- Modular design allows independent scaling
- AI service can be scaled separately if needed
- Stateless architecture supports horizontal scaling
- Database connection pooling for efficiency

## Frontend Routing Architecture

### Routing Strategy

> The application uses `/dashboard` as a protected workspace namespace rather than a business feature. All role-based interfaces are accessed under `/dashboard/{role}`, which clearly separates public routes (login, registration) from authenticated role-specific workspaces and simplifies role-based access control and backend authorization mapping.

> Although "dashboard" typically refers to a feature, in this system it represents a protected role-based workspace boundary, not a single screen.

### Role-Based Routing Table

| Role | Route | Layout |
|------|-------|--------|
| Student | `/dashboard/student` | `StudentLayout` |
| Placement Coordinator | `/dashboard/coordinator` | `CoordinatorLayout` |
| Admin | `/dashboard/admin` | `AdminLayout` |
| Super Admin | `/dashboard/super-admin` | `SuperAdminLayout` |

### Layout Hierarchy

| File | Responsibility |
|------|---------------|
| `app/layout.js` | Global layout (fonts, metadata) |
| `app/dashboard/layout.jsx` | Shared dashboard shell |
| `app/dashboard/{role}/layout.js` | Role guard + role-specific sidebar |
