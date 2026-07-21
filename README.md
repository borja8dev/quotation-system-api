# Quotation System API

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql&logoColor=white)
![Tests](https://img.shields.io/badge/tests-157%20passing-success)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

REST API for quotation management with volume discounts, business validations, JWT authentication, role-based access control, and PDF export.

**Current Version:** v1.3.0 (Portfolio Ready)

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Quick Start](#quick-start)
5. [Docker](#docker)
6. [API Endpoints](#api-endpoints)
7. [PDF Export](#pdf-export)
8. [Business Rules](#business-rules)
9. [Testing](#testing)
10. [Development Phases](#development-phases)
11. [Development Guidelines](#development-guidelines)

---

## Project Overview

**What is this?** A fully functional REST API that automates quotation generation for a carpentry materials company. Built with professional architecture, comprehensive testing, and production-ready code quality.

**Project Status:** Complete (v1.3.0)

**What's Included:**
- Fully functional REST API with 20+ endpoints
- JWT authentication with role-based access control
- Quotation calculation engine with volume discounts
- PDF export with professional formatting
- PostgreSQL persistence with Flyway migrations
- Full Docker stack (app + database)
- 157 tests, 0 failures (integration + E2E coverage)
- Hexagonal Architecture with zero-dependency domain layer
- Professional documentation

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

## Architecture

**Pattern:** Hexagonal (Ports and Adapters)

Dependencies always point inward toward the domain layer. Domain services are pure Java with zero Spring dependencies.

INFRASTRUCTURE (Spring, JPA, REST)
|
v
APPLICATION (Use Cases, DTOs, Ports)
|
v
DOMAIN (Entities, Services, Exceptions -- no Spring)


### Project Structure

src/main/java/com/quotation/
|
+-- domain/ [Business Logic - no Spring]
| +-- entity/
| | +-- User.java
| | +-- Product.java
| | +-- Quotation.java
| | +-- QuotationLine.java
| | +-- JwtRefreshToken.java
| +-- service/ [Pure calculation engine]
| | +-- PricingCalculator.java
| | +-- DiscountCalculator.java
| | +-- QuotationValidator.java
| | +-- QuotationStateMachine.java
| | +-- ValidityCalculator.java
| +-- enums/
| | +-- QuotationStatus.java
| | +-- ProductCategory.java
| | +-- SaleUnit.java
| | +-- RateType.java
| +-- exception/
| +-- QuotationValidationException.java
| +-- InvalidStatusTransitionException.java
| +-- InvalidProductDimensionsException.java
| +-- (+ 6 more domain exceptions)
|
+-- application/ [Use Cases and Orchestration]
| +-- service/
| | +-- QuotationService.java
| | +-- QuotationCalculationService.java
| | +-- ProductService.java
| | +-- UserService.java
| | +-- AuthService.java
| +-- port/in/
| +-- port/out/
| +-- dto/
|
+-- infrastructure/ [Spring adapters]
+-- input/rest/
+-- output/persistence/
+-- security/
+-- config/


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

- Header: "QUOTATION Madera y Tableros" + quote number, issue date, expiry date
- Client section: name and email
- Line items table: product, quantity, unit price, discount (with breakdown), subtotal
- Totals: subtotal bruto > descuento > total neto > IVA 21% > TOTAL
- Footer: validity period and contact info

```bash
# Download a PDF (replace TOKEN and ID)
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/v1/quotations/1/pdf \
  --output presupuesto.pdf
```

VAT (21%) is computed at render time (not stored in database).

---

## Business Rules

### Volume Discounts (TABLERO products only)

| Total boards | Discount |
|---|---|
| < 24 | 0% |
| 24 - 47 | 3% |
| 48+ | 6% |

### Stacking Discounts

Discounts stack additively:

| Condition | Discount |
|---|---|
| Volume tier | 0-6% |
| 16mm board thickness | +3% |
| Regular customer | +2% on all products |

Example: Regular customer, 48 boards of 16mm = 6% + 3% + 2% = 11% total

### Quotation State Machine

DRAFT --> SENT --> ACCEPTED --> ARCHIVED
|
+--> REJECTED

Any state --> EXPIRED


Invalid transitions return HTTP 409 Conflict.

### Dynamic Validity Period

| Season | Non-regular | Regular |
|---|---|---|
| June - August (summer) | 30 days | 60 days |
| December (holiday) | 30 days | 60 days |
| Rest of year | 45 days | 90 days |

### Product Validations

- Board thickness: 4-40mm
- Standard dimensions: 244x122, 366x122, 305x122, 280x122, 260x122 cm
- At least one price must be set (pricePerUnit, pricePerM2, or pricePerRateUnit)
- Line quantity must be positive

---

## Testing

157 tests, 0 failures.

| Layer | Tests | Framework |
|---|---|---|
| Domain services | 43 | JUnit5 + AssertJ |
| Application services | 21 | JUnit5 + Mockito |
| Controllers | 21 | MockMvc |
| Security | 22 | SpringBootTest + MockMvc |
| Persistence | 22 | @DataJpaTest + H2 |
| Integration | 15 | SpringBootTest + H2 |
| E2E | 3 | SpringBootTest + MockMvc + H2 |
| JWT provider | 7 | JUnit5 (no Spring) |
| PDF adapter | 3 | JUnit5 (no Spring) |

Test naming: `methodName_WhenCondition_ThenExpectedBehavior`

Tests use H2 in-memory (no Docker required).

---

## Development Phases

| Phase | Version | Focus | Status |
|-------|---------|-------|--------|
| A | v0.1.0 | Domain entities, JPA, Flyway | Complete |
| B | v0.2.0 | REST API, validation, Swagger | Complete |
| C | v0.3.0 | PostgreSQL persistence, Docker | Complete |
| D | v1.0.0 | JWT security, RBAC, rate limiting | Complete |
| E | v1.1.0 | Calculation engine, discounts, state machine | Complete |
| F | v1.2.0 | PDF generation, Docker full stack | Complete |
| G | v1.3.0 | Code polish, constants, E2E tests | Complete |

Summary: 7 phases, 23 commits, 7 releases, 157 tests, Hexagonal Architecture

---

## Development Guidelines

### Code Style

- Language: English for all code
- Naming: PascalCase (classes), camelCase (methods/variables), UPPERCASE (constants)
- Methods: Less than 30 lines, single responsibility
- Architecture: Domain layer has zero Spring imports
- Money: BigDecimal with RoundingMode.HALF_UP, scale 2

### Commits

Conventional Commits: `feat(scope): description`

Scopes: auth, quotation, persistence, validation, pdf, docs, testing

### Key Files

- CLAUDE.md: development guidelines, security spec
- pom.xml: dependencies and build config
- application.yml: main configuration
- db/migration/: schema evolution
- docker-compose.yml: full stack (app + PostgreSQL 15)
- docker-compose-dev.yml: PostgreSQL only

---

## Author

Borja Rodriguez
Backend Developer | Java + Spring Boot
Valencia, Spain
GitHub: [github.com/borja8dev](https://github.com/borja8dev)

---

## License

MIT License - See LICENSE for details.

---

**Status:** Complete (v1.3.0 - Portfolio Ready)
**Repository:** github.com/borja8dev/quotation-system-api
