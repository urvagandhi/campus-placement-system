# Testing Documentation

Comprehensive testing guide for the PlacementPro Campus Placement System.

## Overview

The project includes multiple layers of testing:

| Layer | Framework | Location | Tests |
|-------|-----------|----------|-------|
| Backend Unit | JUnit 5 | `backend/src/test/java` | 34 |
| Backend Integration | Testcontainers | `backend/src/test/java` | 50+ |
| Frontend Unit | Jest | `frontend/__tests__` | 47 |
| Frontend E2E | Playwright | `frontend/e2e` | 22 |

## Backend Testing

### Unit Tests

Run without any external dependencies:

```bash
cd backend
mvn test -Dtest=AuthServiceTest,JwtTokenProviderTest
```

**Test Files:**
- `AuthServiceTest.java` - Authentication service tests
- `JwtTokenProviderTest.java` - JWT token generation/validation

### Integration Tests

Requires Docker for Testcontainers:

```bash
cd backend
mvn test -Dtest=*IntegrationTest
```

**Test Files:**
- `AuthControllerIntegrationTest.java` - Full API tests
- `UserRepositoryIntegrationTest.java` - Repository tests
- `LoginAuditRepositoryIntegrationTest.java` - Audit tests
- `SecurityIntegrationTest.java` - RBAC tests

### Test Coverage

```bash
mvn test jacoco:report
# Report at: target/site/jacoco/index.html
```

## Frontend Testing

### Jest Unit Tests

```bash
cd frontend

# Run all tests
npm test

# With coverage
npm run test:coverage

# Watch mode
npm run test:watch
```

**Test Files:**
- `__tests__/services/authService.test.js` - API service tests
- `__tests__/context/AuthProvider.test.jsx` - Auth context tests
- `__tests__/app/login/LoginPage.test.jsx` - Login component tests

### Playwright E2E Tests

```bash
cd frontend

# Install browsers (first time)
npx playwright install

# Run tests
npm run test:e2e

# Run with UI
npm run test:e2e:ui

# View report
npx playwright show-report
```

**Test Files:**
- `e2e/auth/login.spec.ts` - Login flow tests
- `e2e/auth/security.spec.ts` - Security tests

### E2E Test Requirements

1. Backend must be running:
   ```bash
   cd backend && mvn spring-boot:run
   ```

2. Test users must be seeded (automatic with dev profile)

## Test Users

The `TestDataSeeder` automatically creates these users in dev/test profiles:

| Email | Role | Password |
|-------|------|----------|
| student@test.edu | STUDENT | password123 |
| coordinator@test.edu | COORDINATOR | password123 |
| admin@test.edu | ADMIN | password123 |
| superadmin@platform.com | SUPER_ADMIN | password123 |

## Configuration

### Backend Test Configuration

`src/test/resources/application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15-alpine:///test
```

### Frontend Test Configuration

`jest.config.js`:
```javascript
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1'
  },
  testPathIgnorePatterns: ['/node_modules/', '/e2e/']
};
```

`playwright.config.ts`:
```typescript
export default defineConfig({
  testDir: './e2e',
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI
  }
});
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
      - run: cd backend && mvn test

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '18'
      - run: cd frontend && npm ci && npm test

  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
      - uses: actions/setup-java@v4
      - run: cd backend && mvn spring-boot:run &
      - run: cd frontend && npx playwright install --with-deps
      - run: cd frontend && npm run test:e2e
```

## Writing New Tests

### Backend Test Template

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock private MyRepository myRepository;
    @InjectMocks private MyService myService;

    @Test
    void should_do_something() {
        // Given
        when(myRepository.findById(1L)).thenReturn(Optional.of(entity));

        // When
        var result = myService.findById(1L);

        // Then
        assertThat(result).isNotNull();
    }
}
```

### Frontend Test Template

```jsx
import { render, screen, fireEvent } from '@testing-library/react';
import MyComponent from '@/components/MyComponent';

describe('MyComponent', () => {
    it('should render correctly', () => {
        render(<MyComponent />);
        expect(screen.getByText('Hello')).toBeInTheDocument();
    });
});
```

### E2E Test Template

```typescript
import { test, expect } from '@playwright/test';

test.describe('Feature', () => {
    test('should do something', async ({ page }) => {
        await page.goto('/some-page');
        await expect(page.getByText('Hello')).toBeVisible();
    });
});
```
