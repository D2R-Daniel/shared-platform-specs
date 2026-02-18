# Shared Platform SDK - Claude Instructions

## Project Overview

This is a multi-language SDK providing client libraries for the Shared Platform API in Python, Node.js (TypeScript), and Java.

## Repository Structure

```
shared-platform-sdk/
├── models/                     # YAML model definitions
│   ├── auth/
│   ├── users/
│   ├── roles/
│   ├── teams/
│   ├── invitations/
│   ├── email/
│   ├── settings/
│   ├── webhooks/
│   └── apikeys/
├── openapi/                    # OpenAPI specifications
│   └── {module}/{module}-api.yaml
├── packages/
│   ├── python/                 # Python SDK
│   │   ├── src/shared_platform/
│   │   ├── tests/
│   │   └── pyproject.toml
│   ├── node/                   # Node.js SDK
│   │   ├── src/
│   │   └── package.json
│   └── java/                   # Java SDK
│       ├── src/main/java/com/platform/sdk/
│       ├── src/test/java/
│       └── pom.xml
└── .claude/
    ├── skills/                 # Claude skills
    └── workflows/              # Development workflows
```

## SDK Modules

| Module | Description |
|--------|-------------|
| auth | Authentication (login, tokens, sessions) |
| users | User management CRUD |
| roles | Role and permission management |
| teams | Team management |
| invitations | User invitation system |
| email | Email templates and sending |
| settings | Tenant configuration |
| webhooks | Webhook subscriptions and delivery |
| apikeys | API key management |

## Development Commands

### Python SDK
```bash
cd packages/python

# Install dependencies
poetry install

# Run tests
poetry run pytest

# Run specific test file
poetry run pytest tests/test_users.py -v

# Type checking
poetry run mypy src/
```

### Node.js SDK
```bash
cd packages/node

# Install dependencies
npm install

# Run tests
npm test

# Build
npm run build

# Type checking
npm run typecheck
```

### Java SDK
```bash
cd packages/java

# Set Java 17 (required)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Run tests
mvn test

# Build
mvn package

# Run specific test class
mvn test -Dtest="UserClientTest"
```

## Code Style Guidelines

### Python
- Use type hints for all functions
- Use `from __future__ import annotations` for forward references
- Follow PEP 8 naming (snake_case)
- Use Google-style docstrings
- Prefer dataclasses or Pydantic for models

### TypeScript
- Explicit types for all function parameters and returns
- No `any` types without justification
- Use interfaces for data structures
- Use async/await over raw promises
- Export types from `types.ts`

### Java
- Use Builder pattern for complex objects
- Prefer immutable objects
- Use Optional for nullable returns (not parameters)
- Add JavaDoc for public APIs
- Follow Google Java Style Guide

## Cross-Language Consistency

All SDKs must maintain consistency:

| Concept | Python | TypeScript | Java |
|---------|--------|------------|------|
| List items | `list()` | `list()` | `list()` |
| Get item | `get(id)` | `get(id)` | `get(id)` |
| Create item | `create(request)` | `create(request)` | `create(request)` |
| Update item | `update(id, request)` | `update(id, request)` | `update(id, request)` |
| Delete item | `delete(id)` | `delete(id)` | `delete(id)` |

## Error Handling

HTTP status codes map to exceptions consistently:

| Status | Python | TypeScript | Java |
|--------|--------|------------|------|
| 400 | `ValidationError` | `ValidationError` | `ValidationException` |
| 401 | `AuthenticationError` | `AuthenticationError` | `AuthenticationException` |
| 403 | `AuthorizationError` | `AuthorizationError` | `AuthorizationException` |
| 404 | `NotFoundError` | `NotFoundError` | `NotFoundException` |
| 429 | `RateLimitError` | `RateLimitError` | `RateLimitException` |
| 5xx | `ServerError` | `ServerError` | `ServerException` |

## Development Workflow

### For New Features (complex, multi-file)
```
/feature-dev [description]
```
7-phase guided workflow: Discovery → Codebase Exploration → Clarifying Questions → Architecture Design → Implementation → Quality Review → Summary

### For Iterative Tasks (TDD, refactoring)
```
/ralph-loop "task description"
```

### For Code Review
```
/code-review
```

### For Git
```
/commit                  # Analyze + commit
/commit-push-pr          # Commit + push + create PR
```

## Development Principles

These are **non-negotiable**. All code reviews verify compliance.

1. **Specification-Driven Development** — A written `spec.md` MUST exist before implementation begins. The spec is the single source of truth for acceptance criteria.
2. **Test-First Development (TDD)** — NO production code without a failing test first. Red → Green → Refactor. "I'll add tests later" is rejected.
3. **Evidence-Based Verification** — NO completion claims without fresh verification evidence. Run commands fresh, read complete output, report actual numbers ("47 tests passed, 0 failed").
4. **Systematic Debugging** — NO fixes without root cause investigation first. One small change per hypothesis. Create failing test before implementing fix.
5. **Discovery-First Design** — MUST explore requirements & alternatives before implementation. Propose 2-3 approaches with trade-offs.
6. **Plan-Driven Development** — Multi-step tasks MUST have a written `plan.md` before coding. Plans contain bite-sized tasks with exact file paths, code, commands, and expected output.
7. **Security-First Design** — No secrets in code/logs. HTTPS enforced. All inputs validated. Secure crypto only.
8. **Simplicity & Maintainability** — Single responsibility. No premature abstraction. YAGNI. Delete unused code completely.
9. **Semantic Versioning** — MAJOR.MINOR.PATCH. Breaking changes require migration guide. Deprecation warnings precede removal.

### Feature Directory Convention

```
specs/
└── {###}-{feature-name}/        # e.g. 006-platform-component-expansion
    ├── spec.md                  # MANDATORY: Feature specification
    ├── research.md              # Competitive analysis, alternatives
    ├── plan.md                  # Implementation plan (bite-sized tasks)
    ├── tasks.md                 # Granular task list (T001, T002...)
    ├── data-model.md            # Entity definitions & relationships
    ├── quickstart.md            # Quick start guide
    ├── ux-design.md             # UX flows (if UI project)
    ├── infrastructure.md        # Infra requirements (if needed)
    └── contracts/               # API endpoint definitions (OpenAPI)
```

### Quality Gates (All Must Pass Before Merge)

| Gate | Requirement |
|------|-------------|
| Tests | All tests pass in all 3 SDKs |
| Types | Type checking passes (mypy, tsc, javac) |
| Fresh Evidence | Verification commands run fresh, not cached |
| Security | No known vulnerable dependencies |
| Spec Compliance | Implementation matches spec acceptance criteria |
| Documentation | Public APIs documented |

### When to Skip the Workflow

- **Trivial changes** (typo fix, single-line bug fix, doc update): Skip spec phase, commit directly.
- **Everything else**: Use `/feature-dev`. If it touches multiple files, adds an API, or requires design decisions — you need a spec.

## Adding New Modules

Follow the development workflow above. The implementation phase for SDK modules specifically requires:

1. Create YAML model in `models/{module}/`
2. Create OpenAPI spec in `openapi/{module}/`
3. Implement in Python with tests (TDD)
4. Implement in Node.js with tests (TDD)
5. Implement in Java with tests (TDD)
6. Update exports in all packages

## Skills Available

Review skills in `.claude/skills/`:

- **typescript-reviewer**: TypeScript code review
- **python-reviewer**: Python code review
- **java-reviewer**: Java code review
- **sdk-design-reviewer**: Cross-language SDK design
- **api-client-reviewer**: HTTP client patterns
- **unit-testing-reviewer**: Test quality review
- **integration-testing-reviewer**: Integration test patterns
- **documentation-reviewer**: Documentation quality
- **release-manager**: Version and release management
- **security-reviewer**: Security best practices
- **code-architecture**: Architecture review
- **excel-generator**: Report generation

## Testing Requirements

All changes must pass tests in all three SDKs:

```bash
# Run all tests
cd packages/python && poetry run pytest
cd packages/node && npm test
cd packages/java && mvn test
```

## Commit Guidelines

Use conventional commits:

```
feat(module): description   # New feature
fix(module): description    # Bug fix
docs(module): description   # Documentation
refactor(module): description # Refactoring
test(module): description   # Tests
```

Example:
```
feat(webhooks): add webhook signature verification

- Add HMAC-SHA256 signature generation
- Add signature verification utility
- Add tests for edge cases
```

## Active Technologies
- TypeScript 5+, Node.js 18+ + NextAuth v5-beta (all 5 products already use this), Zod (runtime validation), jose (JWT verification) (007-shared-platform-foundation)
- PostgreSQL (100% alignment across all 5 products), dual ORM support (Prisma 5/6 for Dream Team + HireWise, Drizzle for Payroll + Books + Learn) (007-shared-platform-foundation)

## Recent Changes
- 007-shared-platform-foundation: Added TypeScript 5+, Node.js 18+ + NextAuth v5-beta (all 5 products already use this), Zod (runtime validation), jose (JWT verification)
