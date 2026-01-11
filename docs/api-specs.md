# API Specifications

All API endpoints use the `/api/v1` prefix for versioning.

## Authentication

All protected endpoints require the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

## Response Format

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "errors": null,
  "timestamp": "2024-01-04T10:30:00"
}
```

---

## Auth Endpoints

### POST `/api/v1/auth/login`

Login and receive JWT token.

**Request:**
```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "student@example.com",
      "name": "John Doe",
      "role": "STUDENT"
    }
  }
}
```

### GET `/api/v1/auth/me`

Get current authenticated user.

### POST `/api/v1/auth/refresh`

Refresh access token using a valid refresh token.

**Request:**
```json
{
  "refreshToken": "uuid-refresh-token-string"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "new-uuid-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

---

## User Governance Endpoints

### POST `/api/v1/coordinator/students`
Create new student (Coordinator only). Requires valid `organizationUnitId`.

### POST `/api/v1/admin/coordinators`
Create new coordinator (Admin only).

### POST `/api/v1/superadmin/admins`
Create new admin (Super Admin only).

### POST `/api/v1/superadmin/colleges`
Create new college (Super Admin only).

---

## Student Endpoints

### GET `/api/v1/students`
Get all students (TPO/Admin only)

### GET `/api/v1/students/{id}`
Get student by ID

### GET `/api/v1/students/me`
Get current student's profile

### POST `/api/v1/students`
Create/update student profile

**Request:**
```json
{
  "enrollmentNo": "2021CSE001",
  "department": "CSE",
  "cgpa": 8.5,
  "skills": ["Java", "Python", "SQL"],
  "resumeUrl": "https://drive.google.com/...",
  "batchYear": 2025,
  "projectsCount": 5,
  "internshipMonths": 3
}
```

### PATCH `/api/v1/students/{id}/skills`
Update student skills

---

## Company Endpoints

### GET `/api/v1/companies`
Get all companies

### GET `/api/v1/companies/{id}`
Get company by ID

### POST `/api/v1/companies`
Create company (TPO/Admin)

### PUT `/api/v1/companies/{id}`
Update company (TPO/Admin)

### DELETE `/api/v1/companies/{id}`
Delete company (Admin)

---

## Drive Endpoints

### GET `/api/v1/drives`
Get all placement drives

### GET `/api/v1/drives/upcoming`
Get upcoming drives

### GET `/api/v1/drives/{id}`
Get drive by ID

### POST `/api/v1/drives`
Create drive (TPO/Admin)

**Request:**
```json
{
  "companyId": 1,
  "title": "Software Developer",
  "jobRole": "Full Stack Developer",
  "description": "...",
  "packageLpa": 12.5,
  "driveDate": "2024-02-15",
  "registrationDeadline": "2024-02-10",
  "minCgpa": 7.0,
  "eligibleDepartments": ["CSE", "IT", "ECE"],
  "requiredSkills": ["Java", "Spring Boot"],
  "location": "Bangalore"
}
```

### PATCH `/api/v1/drives/{id}/status`
Update drive status

---

## Application Endpoints

### GET `/api/v1/applications/my`
Get current student's applications

### GET `/api/v1/applications/drive/{driveId}`
Get applications for a drive (TPO/Admin)

### POST `/api/v1/applications/apply`
Apply to a drive

**Request:**
```json
{
  "driveId": 1,
  "notes": "Interested in this role"
}
```

### PATCH `/api/v1/applications/{id}/status`
Update application status (TPO/Admin)

Allowed statuses (validated server-side): PENDING, SHORTLISTED, REJECTED, SELECTED, WITHDRAWN.
Valid transitions:
- PENDING → SHORTLISTED | REJECTED | WITHDRAWN
- SHORTLISTED → SELECTED | REJECTED | WITHDRAWN
- SELECTED/REJECTED/WITHDRAWN → no further transitions (idempotent updates only)

### DELETE `/api/v1/applications/{id}/withdraw`
Withdraw application

---

## Eligibility Endpoints

### GET `/api/v1/eligibility/check`
Check eligibility for student and drive

**Query Parameters:**
- `studentId`: Student ID
- `driveId`: Drive ID

**Response:**
```json
{
  "success": true,
  "data": {
    "score": 75.5,
    "isEligible": true,
    "reasons": ["Student meets all eligibility criteria"],
    "cgpaScore": 85.0,
    "skillsScore": 70.0,
    "experienceScore": 80.0,
    "skillGaps": ["Machine Learning"]
  }
}
```

### GET `/api/v1/eligibility/my/{driveId}`
Get current student's eligibility for drive

---

## Analytics Endpoints

### GET `/api/v1/analytics/overview`
Get overall placement statistics

**Response:**
```json
{
  "success": true,
  "data": {
    "totalStudents": 500,
    "placedStudents": 350,
    "placementPercentage": 70.0,
    "averagePackage": 8.5,
    "highestPackage": 25.0,
    "totalDrives": 45,
    "completedDrives": 30
  }
}
```

### GET `/api/v1/analytics/departments`
Get department-wise statistics

---

## AI Service Endpoints

Base URL: `http://localhost:8000`

### POST `/api/v1/eligibility/score`
Calculate eligibility score

### POST `/api/v1/skills/gap-analysis`
Analyze skill gaps

### GET `/health`
Health check

---

## Error Codes

| Code | Description |
|------|-------------|
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error |
