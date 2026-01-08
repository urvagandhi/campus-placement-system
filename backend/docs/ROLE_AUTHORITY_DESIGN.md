# Role Authority & Departmental Influence Design

**Principle:** *Departments inform, Admin records, T&P decides — the UI reflects this flow without adding new roles or dashboards.*

---

## Core RBAC Model (FINAL)

| Role | Authority | Scope |
|---|---|---|
| **SUPER_ADMIN** | Platform owner | All colleges |
| **ADMIN** | College operations | Single college (SUBTREE) |
| **COORDINATOR** | Placement operations | Institute/Department |
| **STUDENT** | Self-service | Own profile |

> ❌ **NO "Department Admin" role or dashboard**
> Departments influence via *data*, not *decisions*.

---

## Departmental Influence Flow

```
┌─────────────────────────────────────────────────────────┐
│ Department provides academic calendar dates (offline)   │
│                      ↓                                  │
│ ADMIN enters dates in "Academic Calendar" section       │
│                      ↓                                  │
│ Data stored: org_unit_id (DEPT) + date_range + type     │
│                      ↓                                  │
│ COORDINATOR creates placement drive                     │
│                      ↓                                  │
│ System detects conflict (calendar ↔ drive dates)        │
│                      ↓                                  │
│ Warning shown to COORDINATOR (banner/badge)             │
│                      ↓                                  │
│ COORDINATOR decides: reschedule OR proceed              │
└─────────────────────────────────────────────────────────┘
```

**Key points:**
- Departments do NOT block anything
- Departments do NOT approve anything
- T&P (Coordinator) has final authority

---

## Frontend Dashboard Changes (FUTURE)

### Admin Dashboard
- [ ] Add "Academic Calendar" section
  - Calendar entries linked to `organization_unit_id` (DEPARTMENT)
  - Fields: date range, type (Mid-sem, End-sem, Event)

### Coordinator Dashboard
- [ ] Conflict warning banner on drive creation
- [ ] Notifications panel (optional, Phase-3)

### ❌ NOT Adding
- Department Admin dashboard
- Separate Department login
- Department-level approval screens

---

## Faculty Coordinators

Faculty coordinators are:
- **Role:** `COORDINATOR`
- **Assignment:** Department-level org unit
- **Dashboard:** Same Coordinator dashboard
- **Scope:** Limited by `user_assignments.scope_level`

They can:
- See conflicts for their department
- Add remarks (future)
- ❌ NOT override placement decisions
