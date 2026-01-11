# Business Workflows

## Overview

This document details the key business workflows in PlacementPro, including sequence diagrams and state transitions.

---

## 1. User Authentication Workflow

### Login Flow

```mermaid
sequenceDiagram
    autonumber
    participant Browser
    participant Frontend
    participant AuthController
    participant AuthService
    participant JwtTokenProvider
    participant UserRepository
    participant RefreshTokenService
    participant LoginAuditRepo

    Browser->>Frontend: Enter credentials
    Frontend->>AuthController: POST /api/v1/auth/login
    Note over AuthController: {email, password}

    AuthController->>AuthService: login(request)

    %% Honeypot Check
    AuthService->>AuthService: Check honeypot field
    Note over AuthService: If filled → BOT detected

    %% User Lookup
    AuthService->>UserRepository: findByEmailWithCollege(email)

    alt User not found
        AuthService->>LoginAuditRepo: Log failed attempt
        AuthService-->>AuthController: AuthenticationException
        AuthController-->>Frontend: 401 Unauthorized
    end

    %% Password Verification
    AuthService->>AuthService: BCrypt.matches(password, hash)

    alt Invalid password
        AuthService->>LoginAuditRepo: Log failed attempt
        AuthService-->>AuthController: AuthenticationException
        AuthController-->>Frontend: 401 Unauthorized
    end

    %% Status Checks
    AuthService->>AuthService: Check user.isActive
    alt User deactivated
        AuthService-->>AuthController: AccountDeactivatedException
        AuthController-->>Frontend: 403 Forbidden
    end

    AuthService->>AuthService: Check college.isActive
    alt College inactive
        AuthService-->>AuthController: CollegeInactiveException
        AuthController-->>Frontend: 403 Forbidden
    end

    %% Token Generation
    AuthService->>JwtTokenProvider: generateToken(user)
    JwtTokenProvider-->>AuthService: JWT access token

    AuthService->>RefreshTokenService: createRefreshToken(user)
    RefreshTokenService-->>AuthService: Refresh token

    %% Audit & Update
    AuthService->>UserRepository: Update lastLogin
    AuthService->>LoginAuditRepo: Log successful login

    AuthService-->>AuthController: LoginResponseDTO

    AuthController->>AuthController: Set httpOnly cookies
    AuthController-->>Frontend: 200 OK + tokens

    Frontend->>Frontend: Store in context
    Frontend->>Browser: Redirect to dashboard
```

### Token Refresh Flow

```mermaid
sequenceDiagram
    autonumber
    participant Frontend
    participant AuthController
    participant RefreshTokenService
    participant JwtTokenProvider
    participant Database

    Frontend->>AuthController: POST /api/v1/auth/refresh
    Note over AuthController: {refreshToken} or from cookie

    AuthController->>RefreshTokenService: validateAndRotate(token)

    RefreshTokenService->>Database: Find token by value

    alt Token not found
        RefreshTokenService-->>AuthController: RefreshTokenException
        AuthController-->>Frontend: 401 Unauthorized
    end

    RefreshTokenService->>RefreshTokenService: Check expiration

    alt Token expired
        RefreshTokenService->>Database: Revoke entire token family
        RefreshTokenService-->>AuthController: RefreshTokenException
        AuthController-->>Frontend: 401 Unauthorized
    end

    RefreshTokenService->>RefreshTokenService: Check is_revoked

    alt Token already revoked (REUSE DETECTED)
        RefreshTokenService->>Database: Revoke entire token family
        RefreshTokenService->>Database: Create SecurityAlert
        RefreshTokenService-->>AuthController: TokenReuseDetectedException
        AuthController-->>Frontend: 401 Unauthorized
    end

    %% Rotate Token
    RefreshTokenService->>Database: Revoke current token
    RefreshTokenService->>Database: Create new token (same family_id)

    RefreshTokenService->>JwtTokenProvider: generateToken(user)
    JwtTokenProvider-->>RefreshTokenService: New access token

    RefreshTokenService-->>AuthController: TokenRefreshResponseDTO
    AuthController-->>Frontend: 200 OK + new tokens
```

---

## 2. Placement Drive Workflow

### Drive Creation Flow

```mermaid
sequenceDiagram
    autonumber
    participant Coordinator
    participant DriveController
    participant DriveServiceImpl
    participant ScopeService
    participant DriveRepository
    participant CompanyRepository

    Coordinator->>DriveController: POST /api/v1/drives
    Note over DriveController: CreateDriveRequestDTO

    DriveController->>DriveController: @PreAuthorize("hasRole('COORDINATOR')")

    DriveController->>DriveServiceImpl: createDrive(request)

    DriveServiceImpl->>ScopeService: resolveScope(userId)
    ScopeService-->>DriveServiceImpl: ScopeContext

    alt Not COORDINATOR/ADMIN/SUPER_ADMIN
        DriveServiceImpl-->>DriveController: AccessDeniedException
        DriveController-->>Coordinator: 403 Forbidden
    end

    %% College Validation
    DriveServiceImpl->>DriveServiceImpl: Validate college access

    alt Request different college
        DriveServiceImpl-->>DriveController: AccessDeniedException
        DriveController-->>Coordinator: 403 Forbidden
    end

    %% Department Validation
    DriveServiceImpl->>DriveServiceImpl: Validate eligible departments

    loop Each department
        DriveServiceImpl->>DriveServiceImpl: Check dept belongs to college
        alt Department mismatch
            DriveServiceImpl-->>DriveController: IllegalArgumentException
        end
    end

    %% Create Drive
    DriveServiceImpl->>DriveRepository: save(drive)
    DriveRepository-->>DriveServiceImpl: Saved drive

    DriveServiceImpl-->>DriveController: DriveDTO
    DriveController-->>Coordinator: 201 Created
```

### Drive Status Lifecycle

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Coordinator creates

    DRAFT --> UPCOMING: Coordinator publishes
    DRAFT --> CANCELLED: Coordinator cancels

    UPCOMING --> ONGOING: Drive date reached
    UPCOMING --> CANCELLED: Coordinator cancels

    ONGOING --> COMPLETED: Selections finalized

    CANCELLED --> [*]
    COMPLETED --> [*]

    note right of DRAFT
        - Editable
        - Not visible to students
    end note

    note right of UPCOMING
        - Visible to students
        - Applications open
    end note

    note right of ONGOING
        - Applications closed
        - Shortlisting active
    end note

    note right of COMPLETED
        - Read-only
        - Analytics available
    end note
```

---

## 3. Application Workflow

### Student Application Flow

```mermaid
sequenceDiagram
    autonumber
    participant Student
    participant AppController
    participant AppServiceImpl
    participant EligibilityService
    participant ScopeService
    participant AppRepository
    participant DriveRepository
    participant StudentRepository

    Student->>AppController: POST /api/v1/applications/apply
    Note over AppController: {driveId, resumeUrl, coverLetter}

    AppController->>AppController: @PreAuthorize("hasRole('STUDENT')")

    AppController->>AppServiceImpl: applyToDrive(request)

    %% Get Student Profile
    AppServiceImpl->>StudentRepository: findByUserId(currentUserId)
    StudentRepository-->>AppServiceImpl: StudentProfile

    %% Get Drive
    AppServiceImpl->>DriveRepository: findById(driveId)
    DriveRepository-->>AppServiceImpl: PlacementDrive

    %% Check Duplicate
    AppServiceImpl->>AppRepository: existsByStudentIdAndDriveId()

    alt Already applied
        AppServiceImpl-->>AppController: IllegalStateException
        AppController-->>Student: 409 Conflict
    end

    %% Check College Match
    AppServiceImpl->>AppServiceImpl: Validate student.college == drive.college

    alt College mismatch
        AppServiceImpl-->>AppController: AccessDeniedException
        AppController-->>Student: 403 Forbidden
    end

    %% Check Eligibility
    AppServiceImpl->>EligibilityService: validateApplication(studentId, driveId)

    alt Not eligible
        EligibilityService-->>AppController: EligibilityException
        AppController-->>Student: 400 Bad Request
    end

    %% Create Application
    AppServiceImpl->>AppServiceImpl: Build Application entity
    AppServiceImpl->>AppRepository: save(application)
    AppRepository-->>AppServiceImpl: Saved application

    AppServiceImpl-->>AppController: ApplicationDTO
    AppController-->>Student: 201 Created
```

### Application Status Workflow

```mermaid
stateDiagram-v2
    [*] --> PENDING: Student applies

    PENDING --> SHORTLISTED: Coordinator shortlists
    PENDING --> REJECTED: Coordinator rejects
    PENDING --> WITHDRAWN: Student withdraws

    SHORTLISTED --> SELECTED: Coordinator selects
    SHORTLISTED --> REJECTED: Coordinator rejects
    SHORTLISTED --> WITHDRAWN: Student withdraws

    SELECTED --> [*]
    REJECTED --> [*]
    WITHDRAWN --> [*]

    note right of PENDING
        - Awaiting review
        - Student can withdraw
    end note

    note right of SHORTLISTED
        - Passed initial screening
        - May have interview rounds
    end note

    note right of SELECTED
        - Final selection
        - Offer extended
    end note
```

### Status Update Flow

```mermaid
sequenceDiagram
    autonumber
    participant Coordinator
    participant AppController
    participant AppServiceImpl
    participant ScopeService
    participant AppRepository
    participant DriveRepository

    Coordinator->>AppController: PATCH /api/v1/applications/{id}/status
    Note over AppController: {status: "SHORTLISTED"}

    AppController->>AppController: @PreAuthorize("hasRole('COORDINATOR')")

    AppController->>AppServiceImpl: updateApplicationStatus(id, status)

    %% Scope Check
    AppServiceImpl->>ScopeService: resolveScope(userId)
    ScopeService-->>AppServiceImpl: ScopeContext

    %% Get Application
    AppServiceImpl->>AppRepository: findById(id)
    AppRepository-->>AppServiceImpl: Application

    %% Get Drive for college check
    AppServiceImpl->>DriveRepository: findById(driveId)
    DriveRepository-->>AppServiceImpl: PlacementDrive

    %% Validate Access
    AppServiceImpl->>AppServiceImpl: Check college match
    AppServiceImpl->>AppServiceImpl: Check department scope

    alt No access
        AppServiceImpl-->>AppController: AccessDeniedException
        AppController-->>Coordinator: 403 Forbidden
    end

    %% Validate Transition
    AppServiceImpl->>AppServiceImpl: Check current status allows transition

    alt Invalid transition
        AppServiceImpl-->>AppController: IllegalStateException
        AppController-->>Coordinator: 400 Bad Request
    end

    %% Update Status
    AppServiceImpl->>AppServiceImpl: Set status + timestamp
    AppServiceImpl->>AppRepository: save(application)

    AppServiceImpl-->>AppController: ApplicationDTO
    AppController-->>Coordinator: 200 OK
```

---

## 4. Eligibility Check Workflow

### Eligibility Calculation Flow

```mermaid
flowchart TD
    A[Eligibility Check Request] --> B{Cached Result?}
    B -->|Yes, < 1 hour| C[Return Cached Result]
    B -->|No or Stale| D[Load Student Profile]

    D --> E[Load Drive Requirements]
    E --> F{Hard Rules Check}

    F --> G{CGPA >= min_cgpa?}
    G -->|No| H[Not Eligible: CGPA]
    G -->|Yes| I{Backlogs <= max_backlogs?}

    I -->|No| J[Not Eligible: Backlogs]
    I -->|Yes| K{Department in eligible list?}

    K -->|No| L[Not Eligible: Department]
    K -->|Yes| M[Calculate Scores]

    M --> N[CGPA Score - 40%]
    M --> O[Skills Score - 35%]
    M --> P[Experience Score - 25%]

    N --> Q[Total Score]
    O --> Q
    P --> Q

    Q --> R{Score >= 50?}
    R -->|Yes| S[Eligible]
    R -->|No| T[Not Eligible: Low Score]

    S --> U[Save to eligibility_results]
    T --> U
    H --> U
    J --> U
    L --> U

    U --> V[Return EligibilityResultDTO]
```

### AI Enhancement Flow (Optional)

```mermaid
sequenceDiagram
    autonumber
    participant EligibilityService
    participant AIClient
    participant PythonAI
    participant GeminiService

    EligibilityService->>EligibilityService: Calculate algorithmic score

    alt Gemini API key configured
        EligibilityService->>GeminiService: enhanceScore(student, drive)
        GeminiService->>GeminiService: Prepare prompt
        GeminiService->>GeminiService: Call Gemini API
        GeminiService-->>EligibilityService: AI insights

        EligibilityService->>EligibilityService: Blend scores
        Note over EligibilityService: 70% algorithmic + 30% AI
    else No Gemini key
        EligibilityService->>AIClient: calculateEligibilityScore(request)

        alt AI service available
            AIClient->>PythonAI: POST /api/v1/eligibility/score
            PythonAI-->>AIClient: Score response
            AIClient-->>EligibilityService: Score + breakdown
        else AI service down
            AIClient->>AIClient: Fallback response
            AIClient-->>EligibilityService: Default score
        end
    end
```

---

## 5. Scope Resolution Workflow

### Scope Resolution Algorithm

```mermaid
flowchart TD
    A[resolveScope userId] --> B{Request Cache Hit?}
    B -->|Yes| C[Return Cached ScopeContext]
    B -->|No| D[Load User with College]

    D --> E{Role == SUPER_ADMIN?}
    E -->|Yes| F[Return: No restrictions]
    E -->|No| G[Load Active Assignments]

    G --> H{Has Assignments?}
    H -->|No| I[Return: Own profile only]
    H -->|Yes| J[Process Each Assignment]

    J --> K{College Match?}
    K -->|No| L[LOG SECURITY ALERT]
    K -->|Yes| M{Scope Level?}

    M -->|SELF| N[Add assigned unit]
    M -->|CHILDREN| O[Add unit + direct children]
    M -->|SUBTREE| P[Add unit + all descendants]

    N --> Q[Collect Departments]
    O --> Q
    P --> Q

    L --> Q

    Q --> R{Any UNIVERSITY with SUBTREE?}
    R -->|Yes| S[Set isUniversityScope = true]
    R -->|No| T[Set isUniversityScope = false]

    S --> U[Build ScopeContext]
    T --> U

    U --> V[Cache in ScopeContextHolder]
    V --> W[Return ScopeContext]
```

### Scope-Based Data Access

```mermaid
flowchart TD
    A[Data Access Request] --> B[Resolve User Scope]
    B --> C{User Role?}

    C -->|SUPER_ADMIN| D[Return ALL Data]
    C -->|ADMIN| E[Return College Data]
    C -->|COORDINATOR| F{Scope Type?}
    C -->|STUDENT| G[Return Own Data]

    F -->|University SUBTREE| E
    F -->|Limited Scope| H[Return Department-Filtered Data]

    D --> I[Execute Query]
    E --> I
    H --> I
    G --> I
```

---

## 6. Password Reset Workflow

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant PasswordResetController
    participant PasswordResetService
    participant UserRepository
    participant TokenRepository
    participant EmailService

    User->>PasswordResetController: POST /api/v1/auth/forgot-password
    Note over PasswordResetController: {email}

    PasswordResetController->>PasswordResetService: initiateReset(email)

    PasswordResetService->>UserRepository: findByEmail(email)

    alt User not found
        PasswordResetService-->>PasswordResetController: Success (no leak)
        Note over PasswordResetController: Always return success to prevent enumeration
    end

    PasswordResetService->>PasswordResetService: Generate secure token
    PasswordResetService->>TokenRepository: Save PasswordResetToken
    PasswordResetService->>EmailService: Send reset email

    PasswordResetService-->>PasswordResetController: Success
    PasswordResetController-->>User: Check your email

    %% Reset Phase
    User->>PasswordResetController: POST /api/v1/auth/reset-password
    Note over PasswordResetController: {token, newPassword}

    PasswordResetController->>PasswordResetService: resetPassword(token, newPassword)

    PasswordResetService->>TokenRepository: findByToken(token)

    alt Token not found
        PasswordResetService-->>PasswordResetController: InvalidTokenException
    end

    PasswordResetService->>PasswordResetService: Check expiration
    PasswordResetService->>PasswordResetService: Check not used

    PasswordResetService->>UserRepository: Update password hash
    PasswordResetService->>TokenRepository: Mark token as used
    PasswordResetService->>TokenRepository: Invalidate all user refresh tokens

    PasswordResetService-->>PasswordResetController: Success
    PasswordResetController-->>User: Password reset successful
```

---

## Related Documentation

- [Authentication Feature](../backend/README.md#auth-module)
- [Authorization Feature](../backend/README.md#security-module)
- [API Specifications](../api-specs.md)
- [Database Schema](database/ER-DIAGRAM.md)
