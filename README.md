# Quotation System API

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql&logoColor=white)
![Tests](https://img.shields.io/badge/tests-157%20passing-success)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

REST API for quotation management with volume discounts, business validations, JWT authentication, role-based access control, and PDF export.

**Current Version:** v1.3.0 (Phase G - Code Polish + Portfolio Ready)

**Status:** Development/Demo (MVP)

---

## Table of Contents

1. [Project Scope](#project-scope)
2. [Current Status](#current-status)
3. [Tech Stack](#tech-stack)
4. [Architecture](#architecture)
5. [Quick Start](#quick-start)
6. [Docker](#docker)
7. [API Endpoints](#api-endpoints)
8. [PDF Export](#pdf-export)
9. [Business Rules](#business-rules)
10. [Testing](#testing)
11. [Project Phases](#project-phases)
12. [Development Guidelines](#development-guidelines)

---

## Project Scope

This is a demonstration MVP, not a production system currently in deployment.

**What's included (v0.1 - v1.3):**
- Fully functional REST API with 20 endpoints
- JWT authentication + role-based access control
- Quotation calculation engine with volume discounts and business validations
- PDF export: `GET /api/v1/quotations/{id}/pdf` returns a styled PDF with VAT
- PostgreSQL persistence with Flyway migrations
- Docker: full production stack (app + DB) via `docker-compose up -d`
- Professional testing suite (157 tests, 0 failures, includes E2E auth→quotation→PDF flow)

**What's NOT included yet:**
- Frontend/Web UI (planned as separate project)
- Email notifications

---

## Current Status

### v1.3.0 - Code Polish + Portfolio Ready (Phase G)

- **Constants extracted** — all magic numbers and strings replaced with named `private static final` constants across domain, security, and PDF layers.
- **E2E integration test** — full `register → create quotation → download PDF` flow covered via `@SpringBootTest + MockMvc + H2` (no mocks).
- **SOLID cleanup** — no behavior changes; zero regressions.

**Tests:** 157 total, 0 failures (+3 E2E tests)

### v1.2.0 - PDF Generation + Docker (Phase F)

- **PDF export** — `GET /api/v1/quotations/{id}/pdf` returns a professional PDF with client info, line items, discount breakdown, VAT (21%), and validity footer. Built with OpenPDF (LGPL).
- **Dockerfile** — multi-stage build (Maven + JRE Alpine). Run via `docker-compose up -d`.
- **Full Docker stack** — `docker-compose.yml` with app + PostgreSQL, healthchecks, and named volumes.
- **Spring profiles** — `application-dev.yml` (verbose logging) and `application-prod.yml` (production-hardened, Swagger disabled).
- **Actuator** — `/actuator/health` endpoint for container healthchecks.

**Tests:** 154 total, 0 failures

### v1.1.0 - Quotation Calculation Engine (Phase E)

The calculation engine adds real business logic to quotation creation. All calculation services live in the domain layer as pure Java classes (no Spring dependencies), following hexagonal architecture principles.

**Calculation Features:**
- **Volume discounts for boards** -- 24+ boards = 3%, 48+ boards = 6% (applied across all TABLERO lines in a quotation)
- **16mm thickness bonus** -- additional 3% discount for 16mm boards (stacks with volume)
- **Regular customer bonus** -- additional 2% on all product lines (stacks with volume + thickness)
- **Discount audit trail** -- each line includes a `discountBreakdown` field (e.g., "Volume 6% (48+ boards) + 16mm bonus 3% + Regular customer 2% = 11%")
- **Dynamic validity period** -- summer/holiday: 30/60 days, rest of year: 45/90 days (non-regular/regular)
- **Business validations** -- board thickness range (4-40mm), standard dimensions (244x122, 366x122, etc.)
- **Quotation state machine** -- DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED, ARCHIVED with enforced transitions

**New domain services (pure Java, no Spring):**
- `PricingCalculator` -- resolves unit price per product type (per unit, per m2, per rate)
- `DiscountCalculator` -- two-pass engine: counts total boards, then applies stacked discounts per line
- `QuotationValidator` -- validates thickness, dimensions, quantities, and pricing
- `QuotationStateMachine` -- enforces valid status transitions
- `ValidityCalculator` -- resolves validity days by season and customer type

**Tests added:** 73 new (149 total at v1.1.0), including 15 integration tests with real persistence

### v1.0.0 - JWT Security + RBAC (Phase D)

- JWT access tokens (HS256, 15 min expiry) with refresh token rotation (7 days, stored in DB)
- BCrypt password encoding (strength 12) with password policy enforcement
- Role-based access: `ROLE_CLIENT` vs `ROLE_ADMIN` with endpoint-level authorization
- Rate limiting on login: 5 attempts per IP in 15 minutes (Bucket4j)
- Stateless authentication via `JwtAuthenticationFilter`

### v0.3.0 - Persistence Layer (Phase C)

- PostgreSQL via Docker Compose with healthcheck
- `orphanRemoval = true` on `Quotation.lines`
- `@DataJpaTest` suite (17 persistence tests against H2)

### v0.2.0 - REST API (Phase B)

- 15 REST endpoints (CRUD for User, Product, Quotation)
- Input validation with `@Valid` and custom constraints
- Centralized error handling with `GlobalExceptionHandler`
- Swagger/OpenAPI documentation at `/swagger-ui.html`

### v0.1.0 - Domain Layer (Phase A)

- Maven project with Spring Boot 3.4
- Domain entities with full JPA mappings and Flyway V001 migration

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21+ |
| Framework | Spring Boot 3.4 |
| Database | PostgreSQL 15 (production), H2 (testing) |
| ORM | JPA/Hibernate 6.6 |
| Authentication | JWT (JJWT 0.12.3) + BCrypt |
| Rate Limiting | Bucket4j 8.10.1 |
| PDF Generation | OpenPDF 2.0.3 (LGPL) |
| Testing | JUnit5 + Mockito + AssertJ |
| API Documentation | Swagger/OpenAPI 3.0 (springdoc 2.7) |
| Database Migrations | Flyway |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |

---

## Architecture

**Pattern:** Hexagonal (Ports and Adapters)

Dependencies always point inward toward the domain layer. Domain services are pure Java with zero Spring dependencies.

```
INFRASTRUCTURE (Spring, JPA, REST)
  |
  v
APPLICATION (Use Cases, DTOs, Ports)
  |
  v
DOMAIN (Entities, Services, Exceptions -- no Spring)
```

### Project Structure

```
src/main/java/com/quotation/
|
+-- domain/                              [Business Logic - no Spring]
|   +-- entity/
|   |   +-- User.java
|   |   +-- Product.java
|   |   +-- Quotation.java
|   |   +-- QuotationLine.java
|   |   +-- JwtRefreshToken.java
|   +-- service/                         [Pure calculation engine]
|   |   +-- PricingCalculator.java
|   |   +-- DiscountCalculator.java
|   |   +-- QuotationValidator.java
|   |   +-- QuotationStateMachine.java
|   |   +-- ValidityCalculator.java
|   +-- enums/
|   |   +-- QuotationStatus.java         [DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED, ARCHIVED]
|   |   +-- ProductCategory.java         [TABLERO, SERVICIO, FERRETERIA, ...]
|   |   +-- SaleUnit.java                [TABLERO, UNIDAD, MINUTO, METRO_LINEAL, ...]
|   |   +-- RateType.java                [PER_MINUTE, PER_LINEAR_METER, FIXED]
|   +-- exception/
|       +-- QuotationValidationException.java
|       +-- InvalidStatusTransitionException.java
|       +-- InvalidProductDimensionsException.java
|       +-- (+ 6 more domain exceptions)
|
+-- application/                         [Use Cases and Orchestration]
|   +-- service/
|   |   +-- QuotationService.java        [Implements QuotationUseCase]
|   |   +-- QuotationCalculationService.java  [Orchestrates domain services]
|   |   +-- ProductService.java
|   |   +-- UserService.java
|   |   +-- AuthService.java
|   +-- port/in/                         [Input ports - use case interfaces]
|   +-- port/out/                        [Output ports - repository interfaces]
|   +-- dto/                             [Request/Response objects]
|
+-- infrastructure/                      [Spring adapters]
    +-- input/rest/                       [REST controllers]
    +-- output/persistence/              [JPA repository implementations]
    +-- security/                        [JWT, filters, Spring Security config]
    +-- config/                          [GlobalExceptionHandler, CORS, etc.]
```

---

## Quick Start

### Prerequisites

- Java 21+ (`java -version`)
- Maven 3.8+ (`mvn -version`)
- Docker (`docker --version`) -- for PostgreSQL

### Run

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Build and test
mvn clean install

# 3. Start the application
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Run Tests Only

```bash
mvn test
# 157 tests, 0 failures
```

Tests use H2 in-memory (no Docker needed).

---

## Docker

### Local development (DB only)

```bash
docker-compose -f docker-compose-dev.yml up -d
mvn spring-boot:run
```

### Full production stack (app + DB)

```bash
# 1. Create .env from the template
cp env.example .env
# Edit .env: set DB_PASSWORD and JWT_SECRET

# 2. Build and start
docker-compose up -d

# 3. Check status
docker-compose ps
# Both 'quotation_postgres' and 'quotation_app' should be healthy

# 4. Verify
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

The app container waits for PostgreSQL to be healthy before starting (depends_on + healthcheck).

---

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register user, returns tokens |
| POST | `/api/v1/auth/login` | Login, returns access + refresh tokens |
| POST | `/api/v1/auth/refresh` | Rotate refresh token |
| POST | `/api/v1/auth/logout` | Revoke refresh tokens |

### Quotations (Authenticated)

### Quotations (Authenticated)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/quotations` | Any | Create quotation (calculates discounts automatically) |
| GET | `/api/v1/quotations` | Any | List quotations |
| GET | `/api/v1/quotations/{id}` | Any | Get quotation by ID |
| GET | `/api/v1/quotations/{id}/pdf` | Any | Download quotation as PDF |
| PATCH | `/api/v1/quotations/{id}/status` | Any | Update status (state machine enforced) |
| GET | `/api/v1/quotations/user/{userId}` | Any | Get quotations by user |

### Products (Authenticated)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/products` | Any | List products |
| GET | `/api/v1/products/{id}` | Any | Get product by ID |
| POST | `/api/v1/products` | ADMIN | Create product |
| PUT | `/api/v1/products/{id}` | ADMIN | Update product |
| DELETE | `/api/v1/products/{id}` | ADMIN | Delete product |

### Users (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users` | Create user |
| GET | `/api/v1/users` | List users |
| GET | `/api/v1/users/{id}` | Get user |
| PUT | `/api/v1/users/{id}` | Update user |
| DELETE | `/api/v1/users/{id}` | Delete user |

---

## PDF Export

`GET /api/v1/quotations/{id}/pdf` returns a `application/pdf` file with:

- **Header** — "QUOTATION Madera y Tableros" + quote number, issue date, expiry date
- **Client section** — name and email
- **Line items table** — product, quantity, unit price, discount (with breakdown), subtotal
- **Totals** — subtotal bruto → descuento → total neto → IVA 21% → TOTAL
- **Footer** — validity period and contact info

```bash
# Download a PDF (replace TOKEN and ID)
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/v1/quotations/1/pdf \
  --output presupuesto.pdf
```

VAT (21%) is computed at render time — it is not stored in the database.

---

## Business Rules

### Volume Discounts (TABLERO products only)

Discounts are calculated based on the **total board count across the entire quotation**, not per line.

| Total boards | Discount |
|---|---|
| < 24 | 0% |
| 24 - 47 | 3% |
| 48+ | 6% |

### Stacking Discounts

Discounts stack additively:

| Condition | Discount |
|---|---|
| Volume tier (see above) | 0-6% |
| 16mm board thickness | +3% |
| Regular customer (`is_regular = true`) | +2% on all products |

**Example:** Regular customer buys 48 boards of 16mm thickness:
6% (volume) + 3% (16mm) + 2% (regular) = **11% total discount**

### Quotation State Machine

```
DRAFT --> SENT --> ACCEPTED --> ARCHIVED
                |
                +--> REJECTED

Any state --> EXPIRED
```

Invalid transitions (e.g., DRAFT directly to ACCEPTED) return HTTP 409 Conflict.

### Dynamic Validity Period

| Season | Non-regular | Regular |
|---|---|---|
| June - August (summer) | 30 days | 60 days |
| December (holiday) | 30 days | 60 days |
| Rest of year | 45 days | 90 days |

### Product Validations

- Board thickness: 4 - 40mm
- Standard board dimensions: 244x122, 366x122, 305x122, 280x122, 260x122 cm
- At least one price must be set on the product (pricePerUnit, pricePerM2, or pricePerRateUnit)
- Line quantity must be positive

---

## Testing

**157 tests, 0 failures.**

| Layer | Tests | Framework | Speed |
|---|---|---|---|
| Domain services | 43 | JUnit5 + AssertJ | < 0.01s |
| Application services | 21 | JUnit5 + Mockito | < 0.5s |
| Controllers | 21 | MockMvc | < 0.3s |
| Security | 22 | SpringBootTest + MockMvc | ~1.5s |
| Persistence | 22 | @DataJpaTest + H2 | ~0.5s |
| Integration (Phase E) | 15 | SpringBootTest + H2 | ~0.4s |
| E2E (Phase G) | 3 | SpringBootTest + MockMvc + H2 | ~3s |
| JWT provider | 7 | JUnit5 (no Spring) | < 0.01s |
| PDF adapter | 3 | JUnit5 (no Spring) | < 0.1s |

Test naming convention: `methodName_WhenCondition_ThenExpectedBehavior`

### Test Configuration

Tests use H2 in-memory database with `MODE=PostgreSQL`, `ddl-auto: create-drop`, and Flyway disabled. No Docker required for testing.

---

## Project Phases

| Phase | Version | Focus | Status |
|---|---|---|---|
| A | v0.1.0 | Domain entities, JPA, Flyway | Complete |
| B | v0.2.0 | REST API, validation, Swagger | Complete |
| C | v0.3.0 | PostgreSQL, Docker, persistence tests | Complete |
| D | v1.0.0 | JWT security, RBAC, rate limiting | Complete |
| E | v1.1.0 | Calculation engine, discounts, state machine | Complete |
| F | v1.2.0 | PDF generation, Docker full stack, profiles | Complete |
| **G** | **v1.3.0** | **Code polish, constants, E2E tests, portfolio ready** | **Current** |

---

## Development Timeline

| Phase | Version | Focus | Status |
|-------|---------|-------|--------|
| A | v0.1.0 | Domain entities, JPA, Flyway | Complete |
| B | v0.2.0 | REST API, validation, Swagger | Complete |
| C | v0.3.0 | PostgreSQL persistence, Docker | Complete |
| D | v1.0.0 | JWT security, RBAC, rate limiting | Complete |
| E | v1.1.0 | Calculation engine, discounts, state machine | Complete |
| F | v1.2.0 | PDF generation, Docker full stack | Complete |
| G | v1.3.0 | Code polish, constants, E2E tests | Complete |

**Summary:** 7 phases • 23 commits • 7 releases • 157 tests • Hexagonal Architecture

## Development Guidelines

### Code Style

- **Language:** English for all code
- **Naming:** PascalCase (classes), camelCase (methods/variables), UPPERCASE (constants)
- **Methods:** < 30 lines, single responsibility
- **Architecture:** Domain layer has zero Spring imports
- **Money:** BigDecimal with RoundingMode.HALF_UP, scale 2

### Commits

Conventional Commits: `feat(scope): description`

Scopes: `auth`, `quotation`, `persistence`, `validation`, `pdf`, `docs`, `testing`

### Key Files

- `CLAUDE.md` -- development guidelines, security spec, known gotchas
- `pom.xml` -- dependencies and build config
- `application.yml` -- main configuration
- `db/migration/V001-V003` -- schema evolution
- `docker-compose.yml` -- Full stack (app + PostgreSQL 15)
- `docker-compose-dev.yml` -- PostgreSQL only (for local dev)

---

## Author

**Borja Rodriguez**
Backend Developer | Java + Spring Boot
Valencia, Spain
[GitHub](https://github.com/borja8dev)

---

## License

MIT License - See [LICENSE](LICENSE) for details.

---

**Last Updated:** July 2026 | **Status:** Portfolio Ready - v1.3.0
**Current Version:** v1.3.0 - Code Polish + Portfolio Ready (FINAL)
**Repository:** [GitHub](https://github.com/borja8dev/quotation-system-api)
