# AGENTS.md - Coding Guidelines for AI Agents

## Project Overview

`crab_station` is a Spring Boot 3.x + React 18 internal operations system with JWT authentication.

Backend:

- Java 17
- Spring Boot 3.2+
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- EasyExcel
- Lombok

Frontend:

- React 18
- TypeScript
- Vite 5
- Ant Design 5
- Zustand
- Axios
- React Router v6

## Business Modules

| Module | Description |
| --- | --- |
| Auth | Login, register, logout, JWT token, current user |
| Users | Admin-only user management |
| Finance | Income and expense record management |
| Purchase | Purchase master records and purchase items |
| PurchaseSpec | Internal purchase specification management |
| PlatformSpec | External platform specification management |
| SpecificationMapping | Mapping between purchase specs and platform specs |
| Express | Excel import and dynamic express analysis |
| ApiConfig | Admin-only external platform API configuration |
| ScheduledTask | Admin-only scheduled task configuration |

Important boundaries:

- `ApiConfig` is currently configuration storage only. It does not call third-party APIs yet.
- `ScheduledTask` is currently configuration storage only. It does not execute scheduled jobs yet.
- `Express` is the highest-risk module because it stores dynamic Excel columns in JSON and still needs standardized fields and import batches.

## Documentation Rules

- `crab_station.md`: product and business requirements only.
- `plan.md`: development plan, repair plan, priority, and verification matrix.
- `README.md`: project entrypoint, setup, commands, and module overview.
- `DEVELOPMENT_MEMORY.md`: per-round discussion notes, decisions, changes, and verification results.
- `AGENTS.md`: agent/collaborator engineering instructions only.

Do not mix repair plans into `crab_station.md`. Do not use `README.md` as a long audit report.

## Build/Test Commands

Backend:

```bash
cd backend
mvn clean install
mvn spring-boot:run
mvn test
mvn test -Dtest=ClassName
mvn clean package
```

Frontend:

```bash
cd frontend
npm install
npm run dev
npm run build
npm run preview
```

Notes:

- The frontend currently has no ESLint, Prettier, or test framework configured.
- The backend currently has no Maven Wrapper. If Maven is unavailable, add a wrapper or run in an environment with Maven installed.

## Java Guidelines

Naming:

- Classes: PascalCase.
- Methods and variables: camelCase.
- Constants: UPPER_SNAKE_CASE.
- Packages: lowercase.

Code style:

- Prefer constructor injection with `final` fields and Lombok `@RequiredArgsConstructor`.
- Use DTOs for request/response boundaries.
- Use Bean Validation on request DTOs.
- Put business logic in services.
- Use `@Transactional` on service methods that mutate data.
- Throw `BusinessException` for expected business failures.
- Avoid returning JPA entities directly from controllers.

Security:

- Admin-only APIs should use `@PreAuthorize("hasRole('ADMIN')")`.
- Do not expose raw passwords, API keys, or secrets in DTOs.
- Keep last active admin protection intact.
- Do not rely on the current admin fallback in `CustomUserDetailsService` as a long-term production strategy.

Pagination:

- Use `PageRequestUtils` for pageable endpoints.
- Keep sort field whitelists explicit.
- Keep maximum page size bounded.

## TypeScript/React Guidelines

Naming:

- Components: PascalCase.
- Functions and variables: camelCase.
- Interfaces: PascalCase.
- Component files: PascalCase where consistent with the existing page structure.
- Utility/API files: camelCase.

Import order:

1. React.
2. Third-party libraries.
3. Local components/pages.
4. API, stores, utilities.

Code style:

- Use function components and hooks.
- Keep TypeScript strict.
- Prefer `interface` for object shapes.
- Keep API types aligned with backend DTOs.
- Avoid adding a second token storage mechanism; authentication currently uses Zustand persist `auth-storage`.

Frontend UX:

- Follow existing Ant Design patterns.
- Keep business pages as usable tables/forms, not marketing screens.
- Avoid nested cards and decorative-heavy layouts.
- Keep mobile layout functional.
- Remove production-useless debug logs.

## Current Technical Risks To Respect

When modifying the project, account for these known issues:

- Express sent-date filtering is currently not implemented correctly.
- Express address filtering currently uses JSON string `LIKE`.
- Express column and fee statistics currently perform full-table scans.
- Excel import currently stores each sheet in memory before saving.
- API secrets are only response-masked, not encrypted in database.
- JWT has a default local secret.
- Some UI/backend messages and historical docs contain mojibake.
- Database migrations are not yet managed by Flyway/Liquibase.
- Tests are minimal or absent.

Do not expand new feature scope before checking whether the task touches these risks.

## Editing Rules

- Keep changes scoped to the requested task.
- Do not rewrite unrelated files.
- Do not delete user changes.
- Use the existing architecture and naming patterns.
- Prefer small, verifiable changes.
- Update `DEVELOPMENT_MEMORY.md` after meaningful changes.
- If a change alters requirements, update `crab_station.md`.
- If a change alters planned work, update `plan.md`.
- If a change alters setup, update `README.md`.

## Verification Expectations

For backend changes, prefer:

```bash
cd backend
mvn test
```

or at minimum:

```bash
cd backend
mvn -DskipTests package
```

For frontend changes, prefer:

```bash
cd frontend
npm run build
```

If full build is unavailable, run:

```bash
cd frontend
npx tsc --noEmit
```

Always state what was and was not verified.
