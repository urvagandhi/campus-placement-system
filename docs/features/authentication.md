# Authentication System

Complete end-to-end authentication documentation for PlacementPro.

## Overview

The authentication system spans both frontend and backend to provide:

- Secure JWT-based authentication
- Role-based access control (RBAC)
- Multi-tenant college isolation
- Comprehensive audit logging
- Session management

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            AUTHENTICATION FLOW                               │
└─────────────────────────────────────────────────────────────────────────────┘

     ┌──────────────────┐                     ┌──────────────────────────────┐
     │     FRONTEND     │                     │          BACKEND             │
     │    (Next.js)     │                     │       (Spring Boot)          │
     └────────┬─────────┘                     └──────────────┬───────────────┘
              │                                              │
              │                                              │
    ┌─────────┴──────────┐                      ┌────────────┴────────────────┐
    │                    │                      │                             │
    │  ┌──────────────┐  │   POST /login        │  ┌───────────────────────┐  │
    │  │  Login Page  │──┼──────────────────────┼─▶│   AuthController      │  │
    │  └──────────────┘  │   {email, password}  │  └───────────┬───────────┘  │
    │                    │                      │              │              │
    │                    │                      │              ▼              │
    │                    │                      │  ┌───────────────────────┐  │
    │                    │                      │  │     AuthService       │  │
    │                    │                      │  │  • Find user          │  │
    │                    │                      │  │  • Verify password    │  │
    │                    │                      │  │  • Check active       │  │
    │                    │                      │  │  • Check college      │  │
    │                    │                      │  └───────────┬───────────┘  │
    │                    │                      │              │              │
    │                    │                      │              ▼              │
    │                    │                      │  ┌───────────────────────┐  │
    │  ┌──────────────┐  │   200 OK             │  │   JwtTokenProvider    │  │
    │  │ AuthProvider │◀─┼──────────────────────┼──│  • Generate token     │  │
    │  │ • Store token│  │   {token, role, ...} │  │  • Sign with HS512    │  │
    │  │ • Set user   │  │                      │  └───────────────────────┘  │
    │  └──────┬───────┘  │                      │                             │
    │         │          │                      │                             │
    │         ▼          │                      │                             │
    │  ┌──────────────┐  │                      │                             │
    │  │  Dashboard   │  │                      │                             │
    │  │ (Role-based) │  │                      │                             │
    │  └──────────────┘  │                      │                             │
    │                    │                      │                             │
    └────────────────────┘                      └─────────────────────────────┘
```

## User Roles

| Role | Description | Dashboard |
|------|-------------|-----------|
| STUDENT | End user applying to placements | `/dashboard/student` |
| COORDINATOR | Placement officer managing drives | `/dashboard/coordinator` |
| ADMIN | College administrator | `/dashboard/admin` |
| SUPER_ADMIN | Platform administrator | `/dashboard/superadmin` |

## JWT Token

### Structure

```
Header.Payload.Signature
```

### Payload Claims

```json
{
  "sub": "student@test.edu",  // Email (subject)
  "uid": 123,                 // User ID
  "role": "STUDENT",          // User role
  "cid": 1,                   // College ID
  "ver": 1,                   // Token version
  "iat": 1704067200,          // Issued at
  "exp": 1704153600           // Expiration
}
```

### Lifetime

- **Default**: 24 hours
- **Configurable**: Via `JWT_EXPIRATION_MS` env var

## Security Features

### Password Security
- BCrypt hashing with 12 rounds
- No plaintext storage
- Server-side validation only

### Token Security
- HMAC-SHA512 signing
- 256-bit minimum secret key
- Version-based invalidation support
- Short-lived tokens
- Refresh token rotation

### Security Headers
| Header | Purpose |
|--------|---------|
| Content-Security-Policy | Mitigates XSS attacks |
| X-Frame-Options (DENY) | Prevents clickjacking |
| X-Content-Type-Options (nosniff) | Prevents MIME sniffing |
| Strict-Transport-Security | Enforces HTTPS |

### Audit Logging
All security events are logged including:
- Login attempts (success/failure)
- Token refresh and rotation
- Access denied (403) events
- Authentication failures (401)
- Profile updates
- Token reuse detection
- Unauthorized device detection

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/refresh` | Refresh access token | No (uses refresh token) |
| POST | `/api/v1/auth/logout` | Logout | Yes |
| GET | `/api/v1/auth/me` | Current user | Yes |

## Frontend Integration

### AuthProvider Setup

```jsx
// app/layout.jsx
import { AuthProvider } from '@/context/AuthProvider';

export default function RootLayout({ children }) {
  return (
    <html>
      <body>
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  );
}
```

### Using Auth Hook

```jsx
import { useAuth } from '@/hooks/useAuth';

function Component() {
  const { user, isLoading, login, logout } = useAuth();

  if (isLoading) return <Loading />;

  return user ? (
    <Dashboard user={user} onLogout={logout} />
  ) : (
    <Login onLogin={login} />
  );
}
```

## Backend Headers

### Required Header

```
Authorization: Bearer <jwt_token>
```

### CORS Configuration

```yaml
Allowed Origins: http://localhost:3000
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type
```

## Error Handling

### Authentication Errors

| Error | HTTP Status | Message |
|-------|-------------|---------|
| Invalid credentials | 401 | "Invalid credentials" |
| Account deactivated | 403 | "Account is deactivated..." |
| College inactive | 404 | "College is not active..." |
| Token expired | 401 | "Token has expired" |
| Invalid token | 401 | "Invalid token" |

### Frontend Error Handling

```jsx
try {
  await login(email, password);
} catch (error) {
  if (error.message.includes('Invalid')) {
    setError('Incorrect email or password');
  } else if (error.message.includes('deactivated')) {
    setError('Your account has been disabled');
  } else {
    setError('Login failed. Please try again.');
  }
}
```

## Testing

### Test Accounts

| Email | Password | Role |
|-------|----------|------|
| student@test.edu | password123 | STUDENT |
| coordinator@test.edu | password123 | COORDINATOR |
| admin@test.edu | password123 | ADMIN |
| superadmin@platform.com | password123 | SUPER_ADMIN |

### Test Commands

```bash
# Backend unit tests
cd backend && mvn test -Dtest=AuthServiceTest

# Frontend unit tests
cd frontend && npm test

# E2E tests
cd frontend && npm run test:e2e
```

## Related Documentation

- [Backend Auth](../../backend/docs/authentication.md) - Detailed backend implementation
- [Frontend Auth](../../frontend/docs/authentication.md) - Detailed frontend implementation
- [Authorization](../../backend/docs/authorization.md) - RBAC documentation
