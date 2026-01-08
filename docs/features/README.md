# Features Documentation

This folder contains documentation for overall project features spanning both frontend and backend.

## Feature Index

| Feature | Documentation | Status |
|---------|--------------|--------|
| [Authentication](./authentication.md) | Complete auth system | ✅ Complete |
| [Multi-Tenancy](./multi-tenancy.md) | College isolation | ✅ Complete |
| [Testing](../testing.md) | Test infrastructure | ✅ Complete |

## Quick Links

### Backend Docs
- [Authentication](../../backend/docs/authentication.md)
- [Authorization](../../backend/docs/authorization.md)
- [Error Handling](../../backend/docs/error-handling.md)

### Frontend Docs
- [Authentication](../../frontend/docs/authentication.md)
- [Components](../../frontend/docs/components.md)

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
│  │ • Forms         │  │ • Drives API    │  │ • Recommendations           │ │
│  │ • Components    │  │ • RBAC          │  │                             │ │
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
