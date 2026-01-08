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

## 3. Implementation Standards

During implementation:

- Follow existing project conventions
- Apply proper error handling at every layer
- Validate all inputs
- Use appropriate logging
- Write clean, modular code

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

## 5. Stop & Escalate

If any of the following occur, STOP and escalate:

- Requirements conflict with architecture
- Security would be compromised
- Implementation would violate SOLID principles
- Technical debt would be introduced without justification

## Reference

All rules defined in `.agent/rules/placement-rules.md` are mandatory and non-negotiable.
