# PlacementPro Frontend

Next.js React frontend for the AI-Assisted Campus Placement System.

## Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Next.js 16+ (App Router) |
| Language | JavaScript (JSX) |
| Styling | Tailwind CSS |
| Icons | Lucide React |
| HTTP Client | Fetch API |

## Quick Start

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs at: `http://localhost:3000`

## Project Structure

```
frontend/
├── src/
│   ├── app/                              # Next.js App Router pages
│   │   ├── page.jsx                      # Landing page
│   │   ├── login/                        # Login page
│   │   ├── forbidden/                    # Access denied page
│   │   ├── not-found.jsx                 # 404 page
│   │   └── dashboard/                    # Protected dashboard routes
│   │       ├── student/                  # Student dashboard
│   │       ├── coordinator/              # TPO dashboard
│   │       ├── admin/                    # Admin dashboard
│   │       └── superadmin/               # Super admin dashboard
│   ├── components/
│   │   ├── auth/                         # Auth components
│   │   ├── layout/                       # Layout components
│   │   ├── ui/                           # Reusable UI components
│   │   └── forms/                        # Form components
│   ├── context/
│   │   └── AuthProvider.jsx              # Auth state management
│   ├── hooks/
│   │   └── useAuth.js                    # Auth hook
│   ├── services/                         # API services
│   └── utils/                            # Helper functions
├── __tests__/                            # Jest unit tests
├── e2e/                                  # Playwright E2E tests
├── public/                               # Static assets
└── package.json
```

## Authentication

### AuthProvider

The `AuthProvider` component manages authentication state:

```jsx
import { AuthProvider } from '@/context/AuthProvider';

function App({ children }) {
  return (
    <AuthProvider>
      {children}
    </AuthProvider>
  );
}
```

### useAuth Hook

```jsx
import { useAuth } from '@/hooks/useAuth';

function Component() {
  const { user, isLoading, login, logout } = useAuth();

  if (isLoading) return <Loading />;
  if (!user) return <Redirect to="/login" />;

  return <Dashboard user={user} />;
}
```

### Protected Routes

Routes under `/dashboard/*` are protected by role-specific guards:
- `StudentRouteGuard` - Requires STUDENT role
- `CoordinatorRouteGuard` - Requires COORDINATOR role
- `AdminRouteGuard` - Requires ADMIN role
- `SuperAdminRouteGuard` - Requires SUPER_ADMIN role

## Role-Based Routing

| Role | Dashboard Route |
|------|-----------------|
| STUDENT | `/dashboard/student` |
| COORDINATOR | `/dashboard/coordinator` |
| ADMIN | `/dashboard/admin` |
| SUPER_ADMIN | `/dashboard/superadmin` |

## Testing

### Unit Tests (Jest)

```bash
# Run all tests
npm test

# Run with coverage
npm run test:coverage

# Watch mode
npm run test:watch
```

**Test Coverage:**
- `authService.test.js` - 15 tests
- `AuthProvider.test.jsx` - 12 tests
- `LoginPage.test.jsx` - 15 tests
- **Total: 47 tests**

### E2E Tests (Playwright)

```bash
# Install browsers (first time only)
npx playwright install

# Run E2E tests
npm run test:e2e

# Run with UI
npm run test:e2e:ui

# View report
npx playwright show-report
```

**E2E Test Coverage:**
- Login flow tests - 10 tests
- Security tests - 12 tests
- **Total: 22 tests**

### Test Requirements

| Test Type | Requirements |
|-----------|-------------|
| Unit (Jest) | None |
| E2E (Playwright) | Backend running with seeded test users |

## Environment Variables

Create `.env.local`:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run start` | Start production server |
| `npm run lint` | Run ESLint |
| `npm test` | Run Jest tests |
| `npm run test:coverage` | Jest with coverage |
| `npm run test:e2e` | Run Playwright tests |

## UI Design

The frontend uses a modern glassmorphism design with:
- Gradient backgrounds
- Frosted glass cards
- Smooth animations
- Responsive layouts
- Dark mode support (coming soon)

## Error Handling

The frontend handles various error scenarios:
- Network errors with user-friendly messages
- 401 responses trigger automatic logout
- 403 responses redirect to `/forbidden`
- Form validation with field-level errors

## Related Documentation

- [Architecture](../docs/architecture.md)
- [API Specifications](../docs/api-specs.md)
- [Main README](../docs/README.md)
- [Frontend Implementation Details](../docs/frontend/README.md)
- [Functional Specifications](../docs/functional-specs/README.md)
