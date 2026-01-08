# Frontend Features Documentation

This folder contains detailed documentation for all frontend features.

## Feature Index

| Feature | Documentation | Status |
|---------|--------------|--------|
| [Authentication](./authentication.md) | Login, context, hooks | ✅ Complete |
| [Components](./components.md) | UI components library | ✅ Complete |
| [Routing](./routing.md) | App router, guards | ✅ Complete |

## Quick Reference

### API Base URL
```javascript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
```

### Auth Token Storage
```javascript
localStorage.getItem('authToken');  // Get token
localStorage.setItem('authToken', token);  // Set token
localStorage.removeItem('authToken');  // Clear token
```

### Role-Based Redirects
| Role | Dashboard |
|------|-----------|
| STUDENT | `/dashboard/student` |
| COORDINATOR | `/dashboard/coordinator` |
| ADMIN | `/dashboard/admin` |
| SUPER_ADMIN | `/dashboard/superadmin` |
