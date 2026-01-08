# Backend Features Documentation

This folder contains detailed documentation for all backend features.

## Feature Index

| Feature | Documentation | Status |
|---------|--------------|--------|
| [Authentication](./authentication.md) | Login, JWT, sessions | ✅ Complete |
| [Authorization](./authorization.md) | RBAC, permissions | ✅ Complete |
| [User Management](./user-management.md) | CRUD, profiles | ✅ Complete |
| [Error Handling](./error-handling.md) | Exceptions, responses | ✅ Complete |
| [Security](./security.md) | Encryption, auditing | ✅ Complete |

## Quick Reference

### API Base URL
```
http://localhost:8080/api/v1
```

### Authentication Header
```
Authorization: Bearer <jwt_token>
```

### Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Success message"
}
```
