# Frontend Documentation

Detailed documentation for PlacementPro frontend (Next.js 14+, React 18).

## Documentation Index

| Feature | Documentation | Description |
|---------|--------------|-------------|
| [Authentication](features/login/authentication.md) | Login, context, hooks | ✅ Complete |
| [Components](features/components.md) | UI components library | ✅ Complete |

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

## Related Documentation

- [Architecture](../architecture.md) - System architecture
- [Security](../security.md) - Security model
- [Testing](../testing.md) - Test strategy
- [Backend Auth](../backend/README.md#auth-module) - Backend auth implementation
