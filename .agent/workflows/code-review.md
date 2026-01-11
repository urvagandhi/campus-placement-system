---
description: Code review workflow - ensures all changes comply with agent rules
---

# Code Review Workflow

Before any diff, implementation, bug fix, feature, or improvement, execute the following steps:

## 1. Reference Agent Rules

Read and internalize the rules defined in `.agent/rules/placement-rules.md`. Every change must comply with these rules.

## 2. Pre-Implementation Checklist

Before writing any code:

- [ ] Understand the requirement fully; ask clarifying questions if ambiguous
- [ ] Identify the root cause (for bug fixes)
- [ ] Design the solution end-to-end (data → logic → API → UI)
- [ ] Consider security implications
- [ ] Plan test coverage

### Backend-Specific Pre-Checks

- [ ] Which module does this belong to? (auth, drives, applications, etc.)
- [ ] Does it need a new entity or modify existing?
- [ ] What scope enforcement is needed? (college_id, department_id)
- [ ] Which roles can access this? (STUDENT, COORDINATOR, ADMIN, SUPER_ADMIN)
- [ ] Does it need AI service integration?

### Frontend-Specific Pre-Checks

- [ ] Which route/page is affected?
- [ ] Is authentication required? (wrap in ProtectedRoute)
- [ ] What role-based UI variations are needed?
- [ ] What loading/error/empty states are needed?

## 3. Implementation Standards

During implementation:

### Backend Implementation

- Follow Service Interface Pattern (interface + implementation)
- Apply proper scope filtering in repository queries
- Use `@PreAuthorize` for role checks
- Return `ApiResponse<T>` from all controllers
- Log important operations with SLF4J
- Validate inputs with Jakarta Bean Validation

### Frontend Implementation

- Use existing components from `components/ui/`
- Call APIs through `services/api.js`
- Get auth state from `useAuth()` hook
- Handle all async states (loading, error, success, empty)
- Apply Tailwind CSS consistently

### AI Service Implementation

- Define Pydantic schemas for request/response
- Implement in `services/` directory
- Add endpoint in `app.py`
- Include fallback/default behavior
- Test with sample data

## 4. Post-Implementation Verification

After implementation:

- [ ] Root cause addressed, not symptoms
- [ ] End-to-end implementation complete
- [ ] Error handling implemented
- [ ] Input validation in place
- [ ] Security considerations addressed
- [ ] Tests written and passing
- [ ] Documentation updated
- [ ] Consistent with existing patterns
- [ ] No dead code or TODOs left behind

### Multi-Tenancy Verification

- [ ] All database queries filter by appropriate scope
- [ ] No cross-college data leakage possible
- [ ] ScopeContext properly obtained and used
- [ ] Repository methods include scope parameters

### Security Verification

- [ ] Authentication required where appropriate
- [ ] Authorization checks at controller level
- [ ] No sensitive data in logs
- [ ] Inputs validated and sanitized

## 5. Stop & Escalate

If any of the following occur, STOP and escalate:

- Requirements conflict with architecture
- Security would be compromised
- Implementation would violate SOLID principles
- Technical debt would be introduced without justification
- Multi-tenancy boundaries would be violated
- Scope enforcement is unclear or missing

## 6. Documentation Requirements

For significant changes, update:

- [ ] Feature documentation in `docs/features/`
- [ ] API documentation if endpoints changed
- [ ] README if setup steps affected
- [ ] ER diagram if schema changed (`docs/database/ER-DIAGRAM.md`)
- [ ] Workflow diagrams if business flow changed

## Reference

All rules defined in `.agent/rules/placement-rules.md` are mandatory and non-negotiable.
