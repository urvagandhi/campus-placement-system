# Features Documentation

This folder contains comprehensive documentation for all PlacementPro features spanning both frontend and backend.

## Feature Index

### Core Features
| Feature | Documentation | Description |
|---------|--------------|-------------|
| [Authentication](authentication.md) | JWT + Refresh Token Flow | Complete auth system with token rotation |
| [Multi-Tenancy](multi-tenancy.md) | College Isolation | Scope-based data access control |

### Business Features
| Feature | Documentation | Description |
|---------|--------------|-------------|
| [Applications](applications.md) | Job Application Lifecycle | PENDING → SHORTLISTED → SELECTED/REJECTED |
| [Placement Drives](drives.md) | Drive Management | DRAFT → UPCOMING → ONGOING → COMPLETED |
| [Eligibility](eligibility.md) | Scoring Algorithm | CGPA (40%) + Skills (35%) + Experience (25%) |
| [Students](students.md) | Profile Management | Dual-ownership model (institution/student) |
| [Companies](companies.md) | Company Profiles | Company management and statistics |
| [Analytics](analytics.md) | Reporting & Insights | Dashboard, trends, AI-powered insights |

## Quick Links

### Backend Documentation
- [Backend Overview](../backend/README.md) - Module architecture
- [Authentication Implementation](../backend/README.md#auth-module) - JWT implementation
- [Authorization Implementation](../backend/README.md#security-module) - RBAC implementation
- [Error Handling](../backend/error-handling.md) - Exception handling

### Technical Documentation
- [ER Diagram](../database/ER-DIAGRAM.md) - Complete database schema
- [Business Workflows](../workflows/BUSINESS-WORKFLOWS.md) - Sequence diagrams
- [AI Service](../ai-service/README.md) - Python FastAPI documentation
- [Testing Strategy](../testing.md) - Test coverage guide

### Core Documentation
- [Architecture](../architecture.md) - System architecture
- [Security Model](../security.md) - RBAC and data ownership
- [API Specifications](../api-specs.md) - REST API contracts

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PlacementPro System                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────────┐ │
│  │    Frontend     │  │    Backend      │  │       AI Service            │ │
│  │   (Next.js)     │  │ (Spring Boot)   │  │       (FastAPI)             │ │
│  │                 │  │                 │  │                             │ │
│  │ • Login UI      │  │ • Auth API      │  │ • Eligibility Scoring       │ │
│  │ • Dashboards    │  │ • User Mgmt     │  │ • Skill Gap Analysis        │ │
│  │ • Forms         │  │ • Drives API    │  │ • Student Ranking           │ │
│  │ • Components    │  │ • RBAC          │  │ • Resume Parsing            │ │
│  └────────┬────────┘  └────────┬────────┘  └──────────────┬──────────────┘ │
│           │                    │                          │                 │
│           └────────────────────┼──────────────────────────┘                 │
│                                │                                            │
│                    ┌───────────┴───────────┐                                │
│                    │     PostgreSQL        │                                │
│                    │     Database          │                                │
│                    └───────────────────────┘                                │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```
