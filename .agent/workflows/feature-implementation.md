---
description: Feature implementation workflow - step-by-step guide for adding new features
---

# Feature Implementation Workflow

Use this workflow when implementing new features in the PlacementPro system.

## Phase 1: Requirements Analysis

### 1.1 Understand the Feature

- [ ] What is the business purpose?
- [ ] Who are the users? (Students, Coordinators, Admins)
- [ ] What are the success criteria?
- [ ] What are the edge cases?

### 1.2 Identify Affected Layers

- [ ] Database changes needed?
- [ ] Backend API changes?
- [ ] Frontend UI changes?
- [ ] AI service integration?

### 1.3 Multi-Tenancy Impact

- [ ] Is this college-scoped?
- [ ] Is this department-scoped?
- [ ] Does it need scope resolution?

---

## Phase 2: Database Design

### 2.1 Schema Changes

If new tables needed:

```sql
-- Template
CREATE TABLE new_entity (
    id                  BIGSERIAL PRIMARY KEY,
    college_id          BIGINT NOT NULL REFERENCES colleges(id),  -- Multi-tenancy
    -- Entity fields
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX idx_new_entity_college ON new_entity(college_id);
```

### 2.2 Update Documentation

- [ ] Add to `docs/database/ER-DIAGRAM.md`
- [ ] Add migration script if needed

---

## Phase 3: Backend Implementation

### 3.1 Create Entity

Location: `backend/src/main/java/com/placement/<module>/`

```java
@Entity
@Table(name = "new_entities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEntity extends BaseEntity {
    // Fields matching schema
}
```

### 3.2 Create DTOs

Location: Same module

```java
// Request DTO
public record CreateNewEntityRequest(
    @NotBlank String field1,
    @NotNull Long relatedId
) {}

// Response DTO
public record NewEntityDTO(
    Long id,
    String field1,
    LocalDateTime createdAt
) {}
```

### 3.3 Create Repository

```java
@Repository
public interface NewEntityRepository extends JpaRepository<NewEntity, Long> {

    // ALWAYS include scope filtering
    @Query("SELECT e FROM NewEntity e WHERE e.college.id = :collegeId")
    Page<NewEntity> findByCollegeId(@Param("collegeId") Long collegeId, Pageable pageable);
}
```

### 3.4 Create Service Interface

```java
public interface NewEntityService {
    NewEntityDTO create(CreateNewEntityRequest request);
    NewEntityDTO getById(Long id);
    Page<NewEntityDTO> getAll(Pageable pageable);
    NewEntityDTO update(Long id, UpdateNewEntityRequest request);
    void delete(Long id);
}
```

### 3.5 Create Service Implementation

```java
@Service
@RequiredArgsConstructor
@Transactional
public class NewEntityServiceImpl implements NewEntityService {

    private final NewEntityRepository repository;
    private final OrganizationScopeService scopeService;

    @Override
    public NewEntityDTO create(CreateNewEntityRequest request) {
        ScopeContext scope = scopeService.getCurrentUserScope();
        // Implementation with scope enforcement
    }
}
```

### 3.6 Create Controller

```java
@RestController
@RequestMapping("/api/v1/new-entities")
@RequiredArgsConstructor
public class NewEntityController {

    private final NewEntityService service;

    @PostMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ApiResponse<NewEntityDTO>> create(
            @Valid @RequestBody CreateNewEntityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(service.create(request)));
    }
}
```

---

## Phase 4: Frontend Implementation

### 4.1 Create API Service

Location: `frontend/src/services/newEntityService.js`

```javascript
import api from './api';

export const newEntityService = {
  getAll: (params) => api.get('/new-entities', { params }),
  getById: (id) => api.get(`/new-entities/${id}`),
  create: (data) => api.post('/new-entities', data),
  update: (id, data) => api.put(`/new-entities/${id}`, data),
  delete: (id) => api.delete(`/new-entities/${id}`)
};
```

### 4.2 Create Page Component

Location: `frontend/src/app/new-entities/page.jsx`

```jsx
'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/hooks/useAuth';
import ProtectedRoute from '@/components/auth/ProtectedRoute';

export default function NewEntitiesPage() {
  const { user } = useAuth();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Handle loading, error, empty states
}
```

### 4.3 Add Role-Based Access

```jsx
<ProtectedRoute allowedRoles={['COORDINATOR', 'ADMIN']}>
  <NewEntitiesPage />
</ProtectedRoute>
```

---

## Phase 5: AI Service Integration (If Needed)

### 5.1 Create Schema

Location: `ai-service/schemas/new_feature.py`

```python
from pydantic import BaseModel

class NewFeatureRequest(BaseModel):
    field1: str
    field2: list[str]

class NewFeatureResponse(BaseModel):
    success: bool
    result: dict
    confidence: float
```

### 5.2 Create Service

Location: `ai-service/services/new_feature_service.py`

```python
def process_new_feature(request: NewFeatureRequest) -> NewFeatureResponse:
    # Implementation
    pass
```

### 5.3 Add Endpoint

Location: `ai-service/app.py`

```python
@app.post("/api/v1/new-feature/process")
async def process_new_feature(request: NewFeatureRequest):
    return new_feature_service.process(request)
```

---

## Phase 6: Testing

### 6.1 Backend Tests

```java
@SpringBootTest
class NewEntityServiceTest {

    @Test
    void shouldCreateNewEntity() {
        // Test implementation
    }

    @Test
    void shouldEnforceScopeOnRead() {
        // Verify college isolation
    }
}
```

### 6.2 Frontend Tests

```javascript
describe('NewEntityService', () => {
  it('should fetch all entities', async () => {
    // Test implementation
  });
});
```

---

## Phase 7: Documentation

### 7.1 Update Feature Docs

Create `docs/features/new-feature.md` with:

- Overview
- API Endpoints
- Entity Model
- Workflow Diagrams
- Error Responses

### 7.2 Update ER Diagram

If schema changed, update `docs/database/ER-DIAGRAM.md`

### 7.3 Update API Specs

If new endpoints, update `docs/api-specs.md`

---

## Checklist Summary

Before marking complete:

- [ ] Database schema added/updated
- [ ] Entity class created
- [ ] DTOs created (request/response)
- [ ] Repository with scope filtering
- [ ] Service interface defined
- [ ] Service implementation with scope enforcement
- [ ] Controller with RBAC annotations
- [ ] Frontend service layer
- [ ] Frontend page/component
- [ ] Role-based access control
- [ ] Loading/error/empty states
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Documentation updated
- [ ] ER diagram updated if needed
