# crab_station Development Memory

> Purpose: record long-term development continuity, including current status, latest completed work, key decisions, blockers, and next steps.
> Maintenance rule: update this file before ending each meaningful development round. Keep it concise, actionable, and traceable.

## 1. Document Role

This file does not replace the other project documents:

- `crab_station.md`: product and business requirements.
- `plan.md`: repair plan, development plan, priorities, and verification matrix.
- `README.md`: project entrypoint, setup, commands, environment variables, and module overview.
- `AGENTS.md`: engineering and collaboration rules for AI agents and maintainers.

This file only preserves development continuity and answers three questions:

1. What was completed in the previous round?
2. What state is the project currently in?
3. Where should the next round continue?

## 2. Current Project Overview

- Project name: `crab_station`.
- Product type: internal operations management system.
- Backend: Java 17, Spring Boot 3.2, Spring Security, JPA, MySQL, JWT, EasyExcel.
- Frontend: React 18, TypeScript, Vite 5, Ant Design 5, Zustand, Axios.
- Architecture: frontend/backend separation.
- Authentication: JWT Bearer Token.
- Main business route:
  - User authentication and admin management.
  - Purchase records with purchase items.
  - Purchase specs, platform specs, and spec mappings.
  - Express Excel import and dynamic express analysis.
  - Finance income/expense records.
  - API configuration storage.
  - Scheduled task configuration storage.

## 3. Current Status

Completed:

- Backend and frontend main business modules exist.
- JWT authentication exists.
- Admin role field and several admin-only APIs exist.
- User, finance, purchase, specs, express, API config, and scheduled task pages exist.
- Documentation was reorganized into dedicated files:
  - requirements
  - development plan
  - README
  - agent guidelines
  - development memory

Not yet completed or still risky:

- Backend Maven build has not been verified in the current environment.
- No Maven Wrapper exists yet.
- Express sent-date filtering is not truly implemented.
- Express statistics still rely on full-table JSON scanning in places.
- Express import lacks batch tracking and file de-duplication.
- Excel import still has memory risk for large sheets.
- API secrets are response-masked but still stored in plaintext.
- JWT default secret is still a local fixed value.
- Some UI/backend source text still contains mojibake.
- Database migrations are not managed by Flyway/Liquibase.
- Automated tests are minimal or absent.

## 4. Latest Round Record

### 2026-05-03

- User request:
  - Review existing Markdown files.
  - Keep `crab_station.md` as a requirements-only document.
  - Move all repair/development plans into `plan.md`.
  - Delete `research.md`.
  - Keep and review/update `AGENTS.md`.
  - Update `README.md`.
  - Consider whether a separate file is needed to record each maintenance round and conversation decisions.

- Judgment:
  - A long-term project memory file is useful and should be kept separate from requirements and planning documents.
  - `research.md` was redundant after the requirements and plan split.

- Actions completed:
  - Rewrote `crab_station.md` as a requirements-only document.
  - Rewrote `plan.md` as the repair, development, and verification plan.
  - Rewrote `README.md` as a concise project entrypoint.
  - Rewrote `AGENTS.md` to reflect current modules and collaboration rules.
  - Deleted `research.md`.
  - Added `DEVELOPMENT_MEMORY.md`.
  - Reworked `DEVELOPMENT_MEMORY.md` to follow the structure of `D:\work\HarmonyOs\SmartAccounting\DEVELOPMENT_MEMORY.md`.

- Files changed:
  - `crab_station.md`
  - `plan.md`
  - `README.md`
  - `AGENTS.md`
  - `DEVELOPMENT_MEMORY.md`
  - `research.md`

- Verification:
  - Documentation-only round.
  - No backend or frontend source code changed.
  - No build/test command was required.

## 5. Next Tasks

Recommended next round:

1. Establish backend build verification.
   - Add Maven Wrapper or confirm Maven availability.
   - Run `mvn -DskipTests package`.
   - Run `mvn test` if possible.
2. Fix express sent-date filtering.
   - Remove placeholder `dynamic_fields IS NOT NULL` filtering.
   - Use real extracted date values or introduce standard fields.
3. Start cleaning mojibake in user-facing frontend text and backend messages.
4. Strengthen JWT secret handling for non-development environments.
5. Design API secret encryption storage.

## 6. Current Key Decisions

- `crab_station.md` must stay focused on product/business requirements.
- `plan.md` is the only place for repair priorities and implementation plans.
- `README.md` should stay concise and serve as the onboarding entrypoint.
- `DEVELOPMENT_MEMORY.md` is the continuity log and should be updated every meaningful round.
- `AGENTS.md` should describe durable engineering rules, not one-off conversation history.
- `ApiConfig` is currently configuration storage only.
- `ScheduledTask` is currently configuration storage only.
- Express analysis is the core high-risk module and should be stabilized before major feature expansion.
- Do not expand large new features before closing P0/P1 correctness and security issues in `plan.md`.

## 7. Open Questions

- Should the project add Maven Wrapper now, or rely on a system Maven installation?
- Should production deployment require a named Spring profile such as `prod`?
- Should API `apiKey` also be encrypted, or only `secret`?
- Should express import duplicates be rejected or allowed with warnings?
- Should express standard fields be added directly to `express_analysis`, or should a separate normalized table be introduced?
- Should the frontend keep JWT in localStorage short term, or migrate to HttpOnly Cookie earlier?
- Should mojibake cleanup prioritize frontend UI, backend messages, or documentation first?

## 8. Per-Round Update Template

Copy the template below into "Latest Round Record" at the top or under the corresponding date:

```text
### YYYY-MM-DD

- Round goal:
- Completed:
- Files changed:
- Key decisions:
- Problems encountered:
- Verification:
- Suggested next round:
```

## 9. Collaboration Rules

- Before each development round, read:
  - `DEVELOPMENT_MEMORY.md`
  - `plan.md`
  - The relevant section of `crab_station.md`
  - `AGENTS.md`
- Before ending each meaningful development round, update:
  - latest round record
  - current status
  - next tasks
  - key decisions or open questions if changed
- If implementation differs from the previous plan, treat code and this file as the freshest operational memory, then update `plan.md` or `crab_station.md` when the difference affects plans or requirements.
- Keep this file factual. Do not use it as a detailed audit report or as a replacement for commits.
