# crab_station

`crab_station` is a Spring Boot + React internal operations system for purchase management, finance records, express Excel analysis, specification mapping, API configuration, and scheduled task configuration.

## Tech Stack

Backend:

- Java 17
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- EasyExcel
- Maven

Frontend:

- React 18
- TypeScript
- Vite 5
- Ant Design 5
- Zustand
- Axios
- React Router v6

## Modules

- Auth: login, register, JWT authentication, current user.
- Users: admin-only user management.
- Finance: income and expense records.
- Purchase: purchase records with purchase items.
- PurchaseSpec: internal purchase specification management.
- PlatformSpec: external platform specification management.
- SpecificationMapping: mapping between purchase specs and platform specs.
- Express: Excel import and dynamic express data analysis.
- ApiConfig: admin-only external platform API configuration.
- ScheduledTask: admin-only scheduled task configuration.

## Project Structure

```text
backend/
  src/main/java/com/example/
    config/
    controller/
    dto/
    entity/
    exception/
    listener/
    repository/
    security/
    service/
    util/
  src/main/resources/application.yml

frontend/
  src/
    api/
    components/
    pages/
    stores/
    utils/
  vite.config.ts
  package.json
```

## Requirements

- Java 17+
- Node.js 18+
- MySQL 8+
- Maven 3.8+ or Maven Wrapper if added later

## Database

Create the database:

```sql
CREATE DATABASE crab_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Default local configuration:

- Database: `crab_db`
- Username: `root`
- Password: `123456`
- Port: `3306`

## Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend service:

```text
http://localhost:8080
```

Package:

```bash
cd backend
mvn clean package
```

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend service:

```text
http://localhost:3000
```

Build:

```bash
cd frontend
npm run build
```

Vite proxies `/api` requests to `http://localhost:8080`.

## Environment Variables

Backend configuration supports environment variable overrides:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JPA_DDL_AUTO
JPA_SHOW_SQL
JWT_SECRET
JWT_EXPIRATION
REGISTRATION_ENABLED
APP_LOG_LEVEL
SECURITY_LOG_LEVEL
```

Production must set a strong `JWT_SECRET`. Do not rely on the local default value.

## Documentation

- [crab_station.md](./crab_station.md): product and business requirements.
- [plan.md](./plan.md): development, repair, and verification plan.
- [DEVELOPMENT_MEMORY.md](./DEVELOPMENT_MEMORY.md): development memory, change log, and discussion notes.
- [AGENTS.md](./AGENTS.md): AI/collaborator coding guidelines.

## Current Notes

- The scheduled task module currently stores task configuration only. It does not execute tasks yet.
- The API configuration module currently stores platform API settings only. It does not call third-party APIs yet.
- Express analysis supports dynamic Excel columns, but standardized fields and batch import tracking are planned future improvements.
