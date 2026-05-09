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

### 2026-05-09

- Round goal:
  - Record the recent purchase spec, platform spec, and specification mapping changes.
- Completed:
  - Added purchase spec current price and price history tracking.
  - Added price history API: `GET /api/purchase-specs/{id}/price-history`.
  - Added a purchase spec table action to view price history in a drawer.
  - Kept purchase price display without a currency symbol.
  - Kept backend/API support for price-change reason, but hid the frontend input and history reason column for now.
  - Added `category` to purchase specs and platform specs, with allowed values `公` and `母`.
  - Changed purchase/platform spec uniqueness logic from `name` only to `name + category`, so values such as `3-5 公` and `3-5 母` can coexist.
  - Removed entity-level single-column `name` uniqueness from purchase/platform specs.
  - Updated native update/delete predicates for purchase/platform specs to locate rows by old `name + category`.
  - Added category support to specification mappings.
  - Specification mapping DTOs now return category, and create/update requests carry category.
  - Specification mapping service validates that the selected purchase spec and platform spec both match the requested category.
  - Specification mapping modal now selects category first, then platform spec, then purchase spec.
  - Specification mapping platform/purchase spec options are filtered by selected category.
  - Updated platform spec display wording from "规格名称" to "规格范围(两)" in the table, search placeholder, and modal.
- Files changed:
  - Backend spec modules:
    - `backend/src/main/java/com/example/controller/PlatformSpecController.java`
    - `backend/src/main/java/com/example/controller/PurchaseSpecController.java`
    - `backend/src/main/java/com/example/controller/SpecificationMappingController.java`
    - `backend/src/main/java/com/example/dto/CreatePlatformSpecRequest.java`
    - `backend/src/main/java/com/example/dto/CreatePurchaseSpecRequest.java`
    - `backend/src/main/java/com/example/dto/CreateSpecificationMappingRequest.java`
    - `backend/src/main/java/com/example/dto/PlatformSpecDTO.java`
    - `backend/src/main/java/com/example/dto/PurchaseSpecDTO.java`
    - `backend/src/main/java/com/example/dto/PurchaseSpecPriceHistoryDTO.java`
    - `backend/src/main/java/com/example/dto/SpecificationMappingDTO.java`
    - `backend/src/main/java/com/example/dto/UpdatePlatformSpecRequest.java`
    - `backend/src/main/java/com/example/dto/UpdatePurchaseSpecRequest.java`
    - `backend/src/main/java/com/example/dto/UpdateSpecificationMappingRequest.java`
    - `backend/src/main/java/com/example/entity/PlatformSpec.java`
    - `backend/src/main/java/com/example/entity/PurchaseSpec.java`
    - `backend/src/main/java/com/example/entity/PurchaseSpecPriceHistory.java`
    - `backend/src/main/java/com/example/repository/PlatformSpecRepository.java`
    - `backend/src/main/java/com/example/repository/PurchaseSpecPriceHistoryRepository.java`
    - `backend/src/main/java/com/example/repository/PurchaseSpecRepository.java`
    - `backend/src/main/java/com/example/service/PlatformSpecService.java`
    - `backend/src/main/java/com/example/service/PurchaseSpecService.java`
    - `backend/src/main/java/com/example/service/SpecificationMappingService.java`
  - Frontend spec modules:
    - `frontend/src/api/platformSpec.ts`
    - `frontend/src/api/purchaseSpec.ts`
    - `frontend/src/api/specificationMapping.ts`
    - `frontend/src/pages/PlatformSpec/index.tsx`
    - `frontend/src/pages/PlatformSpec/components/PlatformSpecModal.tsx`
    - `frontend/src/pages/PurchaseSpec/index.tsx`
    - `frontend/src/pages/PurchaseSpec/components/PurchaseSpecModal.tsx`
    - `frontend/src/pages/SpecificationMapping/index.tsx`
    - `frontend/src/pages/SpecificationMapping/components/MappingModal.tsx`
- Key decisions:
  - Specification mappings do not store category separately in their table; category is derived from the linked purchase spec and platform spec.
  - Backend validation is still required even though the frontend filters options by category.
  - Existing databases may still have old single-column unique indexes on `purchase_specs.name` or `platform_specs.name`; if tables are not rebuilt, those indexes must be removed manually.
- Verification:
  - User confirmed backend compilation passed after the price-history changes.
  - `cd frontend && npm run build` passed after the category and mapping workflow updates.
  - Backend was not compiled in this environment because `mvn` is not available here.

### 2026-05-08

- Round goal:
  - Fix home express address distribution API being routed as `/{id}` and failing UUID conversion for `address-distribution`.
  - Fix purchase spec update failing with `Purchase spec does not exist`.
  - Fix specification mapping create failing with `Purchase spec does not exist`.
  - Add purchase spec current price and price history tracking with a table action to view history.
- Completed:
  - Reordered `ExpressController` static express GET routes before the UUID `/{id}` route.
  - Kept UUID regex constraints on GET and DELETE ID routes so non-UUID static paths are not valid ID matches.
  - Moved address distribution endpoint dependency to a dedicated `ExpressAddressDistributionService` after backend compilation continued to report `ExpressAnalysisService.getReceiverAddressDistribution()` as missing.
  - Updated `ExpressController` to call `expressAddressDistributionService.getReceiverAddressDistribution()` instead of the `ExpressAnalysisService` method.
  - Updated purchase spec lookup to compare loaded entity UUIDs in Java, matching the prior platform spec UUID-binding workaround.
  - Updated purchase spec update/delete to use native SQL by unique old name, avoiding JPA `save/delete` failures when direct UUID predicates do not match the database representation.
  - Updated purchase spec delete usage checks to compare loaded purchase item and specification mapping entity UUIDs in Java.
  - Updated specification mapping create/update/get lookup paths to compare loaded entity UUIDs in Java instead of using repository `findById` or derived nested UUID queries.
  - Replaced specification mapping duplicate checks with in-memory entity UUID comparison to avoid UUID parameter binding mismatch.
  - Added `price` to purchase specs and request/response DTOs.
  - Added `PurchaseSpecPriceHistory` entity, DTO, repository, and `GET /api/purchase-specs/{id}/price-history`.
  - Purchase spec create/update now writes a price history row when an initial price is set or the price changes.
  - Purchase spec management UI now shows a price column, supports editing price, and includes a "价格历史" action button with a drawer table.
  - Added `priceChangeReason` to purchase spec create/update requests and the purchase spec modal so price history `changeReason` can be entered.
  - Kept price display without a currency symbol per user adjustment.
  - Hid the frontend price-change reason input and price history reason column while keeping backend/API fields in place.
  - Added `category` to purchase specs and platform specs, constrained in service validation to `公` or `母`.
  - Added category fields to purchase/platform spec DTOs, create/update requests, tables, and modals.
  - Rebuilt platform spec management page/modal text while adding the category selector.
  - Changed purchase/platform spec uniqueness logic from `name` only to `name + category`, allowing pairs such as `3-5 公` and `3-5 母`.
  - Removed entity-level single-column `name` uniqueness from purchase/platform specs and updated native update/delete predicates to use old `name + category`.
  - Added category support to specification mappings.
  - Specification mapping responses now include category, and create/update requests carry category.
  - Specification mapping service validates that selected purchase spec and platform spec both match the requested category.
  - Specification mapping modal now requires selecting category first, then filters purchase spec and platform spec options by that category.
  - Rebuilt specification mapping page/modal text while adding the category workflow.
- Files changed:
  - `backend/src/main/java/com/example/controller/ExpressController.java`
  - `backend/src/main/java/com/example/controller/PurchaseSpecController.java`
  - `backend/src/main/java/com/example/dto/CreatePurchaseSpecRequest.java`
  - `backend/src/main/java/com/example/dto/CreatePlatformSpecRequest.java`
  - `backend/src/main/java/com/example/dto/PurchaseSpecDTO.java`
  - `backend/src/main/java/com/example/dto/PlatformSpecDTO.java`
  - `backend/src/main/java/com/example/dto/PurchaseSpecPriceHistoryDTO.java`
  - `backend/src/main/java/com/example/dto/UpdatePurchaseSpecRequest.java`
  - `backend/src/main/java/com/example/dto/UpdatePlatformSpecRequest.java`
  - `backend/src/main/java/com/example/entity/PlatformSpec.java`
  - `backend/src/main/java/com/example/entity/PurchaseSpec.java`
  - `backend/src/main/java/com/example/entity/PurchaseSpecPriceHistory.java`
  - `backend/src/main/java/com/example/repository/PurchaseSpecPriceHistoryRepository.java`
  - `backend/src/main/java/com/example/repository/PurchaseSpecRepository.java`
  - `backend/src/main/java/com/example/repository/PlatformSpecRepository.java`
  - `backend/src/main/java/com/example/service/PlatformSpecService.java`
  - `backend/src/main/java/com/example/service/PurchaseSpecService.java`
  - `backend/src/main/java/com/example/service/SpecificationMappingService.java`
  - `backend/src/main/java/com/example/dto/CreateSpecificationMappingRequest.java`
  - `backend/src/main/java/com/example/dto/SpecificationMappingDTO.java`
  - `backend/src/main/java/com/example/dto/UpdateSpecificationMappingRequest.java`
  - `backend/src/main/java/com/example/service/ExpressAddressDistributionService.java`
  - `frontend/src/api/purchaseSpec.ts`
  - `frontend/src/api/platformSpec.ts`
  - `frontend/src/api/specificationMapping.ts`
  - `frontend/src/pages/PlatformSpec/index.tsx`
  - `frontend/src/pages/PlatformSpec/components/PlatformSpecModal.tsx`
  - `frontend/src/pages/SpecificationMapping/index.tsx`
  - `frontend/src/pages/SpecificationMapping/components/MappingModal.tsx`
  - `frontend/src/pages/PurchaseSpec/index.tsx`
  - `frontend/src/pages/PurchaseSpec/components/PurchaseSpecModal.tsx`
  - `DEVELOPMENT_MEMORY.md`
- Key decisions:
  - Treat the observed error as a stale or overly broad route mapping problem; static route order now avoids ambiguity and current source still constrains UUID routes.
  - Keep address distribution logic in its own service so controller compilation no longer depends on the updated `ExpressAnalysisService` symbol.
  - Reuse the same UUID-binding workaround for purchase specs that was already applied to platform specs.
  - Reuse the same UUID-binding workaround in specification mapping because create/update depends on purchase spec and platform spec IDs.
  - Use the existing `spring.jpa.hibernate.ddl-auto=update` approach for adding the `purchase_specs.price` column and `purchase_spec_price_history` table.
  - Application-level uniqueness for purchase/platform specs is now `name + category`.
  - Existing databases may still have old single-column unique indexes on `purchase_specs.name` or `platform_specs.name`; those indexes must be removed manually or via migration because `ddl-auto=update` usually will not drop them.
  - Specification mappings do not store category separately; category is derived from the linked purchase spec and platform spec, with service-level validation enforcing consistency.
- Problems encountered:
  - Backend build could not be verified because `mvn` is not available in the current environment.
  - User's backend compiler continued reporting the old missing-symbol error, indicating the compile path was still resolving an `ExpressAnalysisService` without the new method.
- Verification:
  - Source inspection confirmed `/address-distribution` is declared before the UUID GET route.
  - Source inspection confirmed `ExpressController` no longer calls `expressAnalysisService.getReceiverAddressDistribution()`.
  - Source inspection confirmed purchase spec update no longer calls `purchaseSpecRepository.findById(id)` or `purchaseSpecRepository.save(spec)`.
  - Source inspection confirmed specification mapping service no longer uses `findById`, `existsByPurchaseSpec_IdAndPlatformSpec_Id`, or `findByPurchaseSpec_IdAndPlatformSpec_Id`.
  - `cd frontend && npm run build` passed.
  - Confirmed `priceChangeReason` is submitted by the frontend and saved as price history `changeReason`.
  - Re-ran `cd frontend && npm run build` after hiding price-change reason UI; it passed.
  - Re-ran `cd frontend && npm run build` after adding purchase/platform category fields; it passed.
  - Confirmed source no longer uses `existsByName`, `findByName`, single-name update/delete methods, or entity `unique=true` on purchase/platform spec names.
  - Re-ran `cd frontend && npm run build` after adding specification mapping category workflow; it passed.
  - Source inspection confirmed specification mapping create/update validates category consistency.
  - `mvn -DskipTests package` was attempted but failed with `mvn` not found.
- Suggested next round:
  - Rebuild/restart the backend with Maven or add a Maven Wrapper, then retest `GET /api/express/address-distribution`.

### 2026-05-07

- Round goal:
  - Fix platform spec edit failing with `Platform spec does not exist`.
- Completed:
  - Updated platform spec lookup to load specs and compare entity UUIDs in Java.
  - This avoids Hibernate UUID parameter binding issues without changing table schema or triggering DDL.
  - Updated platform spec update/delete to use native SQL by unique old name, avoiding JPA `save/delete` stale-row failures caused by UUID binding mismatch.
  - Prevented managed `PlatformSpec` dirty checking during update by computing new values locally instead of mutating the loaded entity.
  - Enabled automatic persistence-context clearing after native update/delete queries.
  - Replaced UUID-based native update/delete predicates after they still failed to match the existing database representation.
  - Updated delete usage check to compare loaded mapping entity UUIDs in Java.
  - Reverted attempted entity-level `VARCHAR` UUID mapping because Hibernate tried to alter `platform_specs.id` and conflicted with `specification_mappings.platform_spec_id` foreign key type.
  - Avoided native string-id lookup because the existing database may store UUID columns as either `BINARY(16)` or `VARCHAR(36)` depending on when the schema was created.
- Files changed:
  - `backend/src/main/java/com/example/repository/PlatformSpecRepository.java`
  - `backend/src/main/java/com/example/service/PlatformSpecService.java`
  - `DEVELOPMENT_MEMORY.md`
- Verification:
  - Not run locally because Maven is unavailable in this environment.
  - User confirmed platform spec update/delete works normally after switching native update/delete predicates to unique-name matching.

### 2026-05-07

- Round goal:
  - Replace the home page user overview with express address distribution charts.
- Completed:
  - Added `/api/express/address-distribution` to aggregate receiver address province and city statistics.
  - Parsed common receiver address dynamic fields and grouped recognized Chinese province/city values.
  - Rebuilt the home page as an express analysis dashboard with province and city SVG pie charts.
  - Added the frontend Express API type and client method for address distribution.
  - Constrained Express `/{id}` routes to UUID patterns so static routes like `/address-distribution` are not captured as IDs.
- Files changed:
  - `backend/src/main/java/com/example/controller/ExpressController.java`
  - `backend/src/main/java/com/example/service/ExpressAnalysisService.java`
  - `frontend/src/api/express.ts`
  - `frontend/src/pages/Home/index.tsx`
  - `DEVELOPMENT_MEMORY.md`
- Problems encountered:
  - `npm run build` failed inside the sandbox with `esbuild` `spawn EPERM`.
  - Re-running the same build with approved elevated execution succeeded.
  - Backend Maven verification could not run because `mvn` is not installed in this environment.
- Verification:
  - `cd frontend && npm run build` passed.
  - `cd backend && mvn -DskipTests package` was attempted but Maven was unavailable.
  - User confirmed the project runs normally after the route fix and rebuild.

### 2026-05-06

- Round goal:
  - Change the Finance UI currency symbol from dollar to RMB, including the sidebar menu icon.
- Completed:
  - Replaced the Finance balance statistic `DollarOutlined` icon with `¥`.
  - Replaced the Finance menu `DollarOutlined` icon with a `¥` symbol.
- Files changed:
  - `frontend/src/pages/Finance/index.tsx`
  - `frontend/src/components/MainLayout/index.tsx`
  - `DEVELOPMENT_MEMORY.md`
- Problems encountered:
  - `npm run build` failed inside the sandbox with `esbuild` `spawn EPERM`.
  - Re-running the same build with approved elevated execution succeeded.
- Verification:
  - `cd frontend && npm run build` passed.

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
