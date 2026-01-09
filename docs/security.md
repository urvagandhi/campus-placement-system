# Security Documentation

## Overview

This document outlines the security model for the Campus Placement System, including role-based access control, data ownership boundaries, and audit logging policies.

---

## Role-Based Access Control (RBAC)

### Supported Roles

| Role | Description | Scope |
|------|-------------|-------|
| **STUDENT** | End user applying to placement drives | Own profile, eligible drives |
| **COORDINATOR** | Placement operations (TPO, faculty coordinators) | Assigned departments, all drives |
| **ADMIN** | College governance | All users, departments in college |
| **SUPER_ADMIN** | Platform owner | All colleges, system-wide |

### Role-Permission Matrix

| Resource | STUDENT | COORDINATOR | ADMIN | SUPER_ADMIN |
|----------|---------|-------------|-------|-------------|
| **Own Profile (Career)** | Edit | Edit | Edit | Edit |
| **Own Profile (Academic)** | Read | Edit | Edit | Edit |
| **Other Profiles** | - | Read | Read | Read |
| **Drives (View)** | Yes | Yes | Yes | Yes |
| **Drives (Create/Edit)** | - | Yes | Yes | Yes |
| **Applications (Own)** | Manage | - | - | - |
| **Applications (All)** | - | Manage | View | View |
| **Organization Units** | - | - | Manage | Manage |
| **Users** | - | - | Manage | Manage |
| **Colleges** | - | - | - | Manage |

---

## Student Data Ownership Model

### Core Principle

> Roles define **WHAT** a user can do.  
> Organizational assignments define **WHERE** they can do it.

### Field Classifications

#### Institution-Owned (Immutable by Student)

These fields are controlled by the institution and cannot be modified by students:

| Field | Table | Reason |
|-------|-------|--------|
| `email` | `users` | Login identity, assigned by institution |
| `role` | `users` | System-determined |
| `college_id` | `users` | Institutional affiliation |
| `is_active` | `users` | Account status |
| `enrollment_no` | `student_profiles` | Official registration |
| `department_id` | `student_profiles` | Academic assignment |
| `cgpa` | `student_profiles` | Academic record |
| `backlogs` | `student_profiles` | Academic record |
| `batch_year` | `student_profiles` | Admission cohort |
| `semester` | `student_profiles` | Current academic standing |

#### Student-Owned (Editable)

These fields are owned and editable by the student:

| Field | Table | Purpose |
|-------|-------|---------|
| `name` | `users` | Display name preference |
| `phone_number` | `users` | Contact information |
| `skills` | `student_profiles` | Technical competencies |
| `resume_url` | `student_profiles` | Resume document link |
| `projects_count` | `student_profiles` | Experience metric |
| `internship_months` | `student_profiles` | Experience metric |
| `certifications` | `student_profiles` | Professional credentials |
| `linkedin_url` | `student_profiles` | Professional profile |
| `github_url` | `student_profiles` | Code portfolio |
| `career_interests` | `student_profiles` | Job preferences |

---

## Security Enforcement

### Backend Enforcement

1. **DTO Filtering**: `StudentProfileUpdateDTO` only contains career-layer fields
2. **Service Validation**: `StudentService` ignores academic fields from student requests
3. **Audit Logging**: All restricted field modification attempts are logged
4. **Role Checking**: `@PreAuthorize` annotations on all endpoints

### Frontend Enforcement

1. **Read-Only Display**: Academic fields rendered with disabled styling
2. **Input Restrictions**: No input elements for institution-owned fields
3. **No Dropdown Selection**: Department cannot be selected by students

### Audit Logging

All security-relevant events are logged to `SECURITY_AUDIT` logger:

- Restricted field modification attempts
- Cross-profile access attempts
- Unauthorized endpoint access
- Failed authentication attempts

---

## API Endpoint Security

### Student-Accessible Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/students/me` | Get own profile |
| `PATCH` | `/api/v1/students/me/profile` | Update career fields |
| `GET` | `/api/v1/drives` | List all drives |
| `POST` | `/api/v1/applications/apply` | Apply to drive |
| `GET` | `/api/v1/applications/my` | Get own applications |
| `DELETE` | `/api/v1/applications/{id}/withdraw` | Withdraw application |

### Forbidden Endpoints for Students (Return 403)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/users` | Create users |
| `POST` | `/api/v1/drives` | Create drives |
| `PATCH` | `/api/v1/organization-units` | Modify org units |
| `GET` | `/api/v1/students` | List all students |
| `GET` | `/api/v1/students/{id}` | View other profiles |

---

## Authentication

- **Method**: JWT (JSON Web Token)
- **Token Location**: `Authorization: Bearer <token>` header
- **Token Contents**: User ID, role, college ID (claims)
- **Expiration**: Configurable (default 24 hours)
- **Refresh**: Not implemented (re-login required)

### Token Security

- Tokens are stateless (no server-side storage)
- Role is verified from database, not token claims
- Scope is always derived from database, never from client

---

## Error Handling

| Scenario | HTTP Status | Response |
|----------|-------------|----------|
| Invalid token | 401 | Unauthorized |
| Expired token | 401 | Unauthorized |
| Insufficient role | 403 | Forbidden |
| Cross-profile access | 403 | Forbidden |
| Apply after deadline | 400 | Bad Request |
| Duplicate application | 409 | Conflict |
