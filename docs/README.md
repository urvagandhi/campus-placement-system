# PlacementPro: AI-Assisted Smart Campus Placement System

A modular monolithic system for managing campus placements with AI-assisted decision support for eligibility scoring and skill gap analysis.

## 🎯 Project Overview

This project provides a complete skeleton for a campus placement management system with:
- **Java Spring Boot Backend** - Core business logic, REST APIs, authentication
- **Python FastAPI AI Service** - Eligibility scoring, skill gap analysis (decision support only)
- **Next.js Frontend** - Modern React-based UI with role-based dashboards

> **Note:** The AI module acts strictly as a decision-support system and does not autonomously make placement decisions.
>
> **Authentication:** Authentication is centralized and role-driven. User roles are determined by backend authentication logic and never selected by users, ensuring secure and scalable role-based access control.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Frontend (Next.js)                           │
│                 React Pages + API Service Layer                 │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ REST API (JSON, Auth Header)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Java Backend (Spring Boot)                      │
│  REST Controllers → Service Layer → Repository Layer → MySQL   │
│                             │                                   │
│                             │ Internal AI API                   │
│                             ▼                                   │
│                    AI Client (HTTP)                             │
└─────────────────────────────────────────────────────────────────┘
                             │
                             │ Internal AI API (Decision Support Only)
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                 Python AI Service (FastAPI)                     │
│            Eligibility Scoring │ Skill Gap Analysis             │
└─────────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
Placement Management/
├── backend/                    # Java Spring Boot Application
│   ├── src/main/java/com/campusplacement/
│   │   ├── auth/              # Authentication module
│   │   ├── users/             # User management
│   │   ├── students/          # Student profiles
│   │   ├── companies/         # Company/recruiter management
│   │   ├── drives/            # Placement drives
│   │   ├── applications/      # Job applications
│   │   ├── eligibility/       # Eligibility scoring
│   │   ├── analytics/         # Placement analytics
│   │   ├── ai/                # AI service client
│   │   ├── config/            # Security, CORS config
│   │   └── common/            # Shared utilities
│   └── pom.xml
│
├── ai-service/                 # Python FastAPI AI Service
│   ├── app.py                 # Main FastAPI application
│   ├── models/                # Pydantic models
│   ├── services/              # AI logic (eligibility, skill gap)
│   ├── schemas/               # Request/response schemas
│   └── requirements.txt
│
├── frontend/                   # Next.js React Application
│   ├── src/
│   │   ├── app/               # Pages (App Router)
│   │   ├── components/        # Reusable UI components
│   │   ├── services/          # API client
│   │   └── utils/             # Helper functions
│   └── package.json
│
├── docs/                       # Documentation
│   ├── architecture.md
│   └── api-specs.md
│
└── README.md
```

## 🛠️ Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Backend | Java + Spring Boot | 21+ / 3.2.x |
| AI Service | Python + FastAPI | 3.10+ / 0.109.0 |
| Frontend | Next.js (React) | 14+ |
| Database | PostgreSQL | 15+ |
| Build Tool | Maven | 3.9+ |

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Python 3.10+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.9+

### Backend Setup

```bash
cd backend

# Configure database in application.yml
# Update MySQL password

# Build and run
mvn clean compile
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### AI Service Setup

```bash
cd ai-service

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run
uvicorn app:app --reload
```

AI Service runs at: `http://localhost:8000`
API Docs: `http://localhost:8000/docs`

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs at: `http://localhost:3000`

## 👥 Role-Based Access Control (RBAC)

| Role | Access Scope |
|------|--------------|
| STUDENT | Profile, drives, applications, eligibility results |
| TPO | Manage drives, shortlist students, view analytics |
| ADMIN | Manage users, departments, system configuration |
| SUPER_ADMIN | Global system administration, audit logs |

## 🛣️ Frontend Routing Strategy

> The application uses `/dashboard` as a protected workspace namespace rather than a business feature. All role-based interfaces are accessed under `/dashboard/{role}`, which clearly separates public routes (login, registration) from authenticated role-specific workspaces and simplifies role-based access control and backend authorization mapping.

> Although "dashboard" typically refers to a feature, in this system it represents a protected role-based workspace boundary, not a single screen.

### Role-Based Routing Table

| Role | Route |
|------|-------|
| Student | `/dashboard/student` |
| Placement Coordinator (TPO) | `/dashboard/coordinator` |
| Admin | `/dashboard/admin` |
| Super Admin | `/dashboard/superadmin` |

## 📡 API Endpoints

All backend APIs use the `/api/v1` prefix.

| Module | Endpoints |
|--------|-----------|
| Auth | `/api/v1/auth/login`, `/api/v1/auth/register` |
| Students | `/api/v1/students/**` |
| Companies | `/api/v1/companies/**` |
| Drives | `/api/v1/drives/**` |
| Applications | `/api/v1/applications/**` |
| Eligibility | `/api/v1/eligibility/**` |
| Analytics | `/api/v1/analytics/**` |

See [docs/api-specs.md](docs/api-specs.md) for detailed API documentation.

## 🧪 Testing

### Backend Tests

```bash
cd backend

# Unit tests
mvn test -Dtest=AuthServiceTest,JwtTokenProviderTest

# Integration tests (requires Docker)
mvn test -Dtest=*IntegrationTest
```

### Frontend Tests

```bash
cd frontend

# Unit tests (Jest)
npm test

# With coverage
npm run test:coverage

# E2E tests (Playwright)
npm run test:e2e
```

### Test Summary

| Category | Tests | Status |
|----------|-------|--------|
| Backend Unit | 34 | ✅ Passing |
| Frontend Unit | 47 | ✅ Passing |
| Frontend E2E | 22 | ✅ Passing |
| **Total** | **103** | ✅ |

### Test Users (Dev/Test)

| Email | Role | Password |
|-------|------|----------|
| student@test.edu | STUDENT | password123 |
| coordinator@test.edu | COORDINATOR | password123 |
| admin@test.edu | ADMIN | password123 |
| superadmin@platform.com | SUPER_ADMIN | password123 |

## 🔮 Future Extensions

- [x] JWT authentication implementation
- [x] Multi-college support
- [x] Unit and integration tests
- [ ] Database relationships and constraints
- [ ] Actual AI scoring algorithms
- [ ] State management (Zustand/Redux)
- [ ] Docker containerization
- [ ] CI/CD pipeline

## 📚 Related Documentation

- [Architecture Documentation](docs/architecture.md)
- [API Specifications](docs/api-specs.md)
- [Implementation Plan](IMPLEMENTATION_PLAN.md)

## 📄 License

This project is created for educational purposes as part of the Software Engineering course.

---

Built with ❤️ using Spring Boot, FastAPI, and Next.js
