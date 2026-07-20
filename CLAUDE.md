# Quotation System API - Project Constitution

## Architecture
- **Pattern**: Hexagonal (Ports & Adapters)
- **Core**: domain/ (logica de negocio pura, sin Spring)
- **Application**: application/ (casos de uso, DTOs, puertos)
- **Infrastructure**: infrastructure/ (controllers, JPA, config)
- **Entry**: Controllers -> Services -> Domain, nunca lo contrario
- **Dependency direction**: Siempre apunta HACIA el domain, nunca hacia afuera

## Code Style
- **Language**: English (todas las clases, métodos, variables, comentarios)
- **Naming**: PascalCase (User, ProductController), camelCase (getUserId), UPPERCASE (MAX_RETRIES)
- **Methods**: <30 líneas máximo, una responsabilidad por método
- **Classes**: Una responsabilidad por clase (Single Responsibility Principle)
- **Package structure**: Sigue la carpeta física (com.quotation.infrastructure.persistence.*)

## Testing
- **Framework**: JUnit5 + Mockito
- **Target coverage**: >60% para producción
- **Unit tests**: services/ sin BD (mocks de repositories)
- **Integration tests**: persistence/ con @DataJpaTest
- **Controller tests**: rest/ con MockMvc
- **Test naming**: testMethod_WhenCondition_ThenExpectedBehavior

## Database
- **ORM**: JPA/Hibernate
- **Migration**: Flyway (src/main/resources/db/migration/V###__*.sql)
- **Entities**: Sí bidireccionales @ManyToOne/@OneToMany, con LAZY loading
- **Validation**: @NotNull, @NotBlank, @Min, @Max en entidades

## Security (Enterprise-Grade - Fase D)

### JWT Configuration
**Access Token:**
- Duration: 15 minutes (short-lived)
- Claims: sub (userId), roles, iat, exp, jti
- Algorithm: HS256 (HMAC with SHA-256)
- Signed with: application.jwt.secret (load from env)

**Refresh Token:**
- Duration: 7 days (long-lived)
- Stored in: separate token table (JwtRefreshToken entity)
- Rotation: each refresh generates new refresh + access token
- Revocation: delete from DB

**Token Provider Class (JwtTokenProvider.java):**
```java
@Component
public class JwtTokenProvider {
    private final String jwtSecret = env.getProperty("app.jwt.secret");
    private final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000; // 15 min
    private final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7 days
    
    // generateAccessToken(userId, roles) → JWT
    // generateRefreshToken(userId) → JWT + save to DB
    // validateToken(token) → true/false
    // getClaimsFromToken(token) → Map
    // revokeRefreshToken(token) → delete from DB
}
```

### Password Security
- **Encoding:** BCrypt with strength 12 (rounds)
- **Validation Rules:**
    - Minimum 8 characters
    - At least 1 uppercase letter
    - At least 1 number
    - At least 1 special character (!@#$%^&*)
- **No plaintext logging** - passwords never in logs
- **Password Reset:** email link with temporary token (valid 1 hour)

### Endpoint Protection
⚠️ All endpoints use the prefix /api/v1/ (not /api/ as old versions of this spec showed)

Public endpoints:
POST /api/v1/auth/login        (no auth required)
POST /api/v1/auth/register     (no auth required)
Protected (ROLE_CLIENT):
GET /api/v1/quotations         (own quotations)
POST /api/v1/quotations        (create own)
GET /api/v1/quotations/{id}    (own only)
Protected (ROLE_ADMIN):
GET /api/v1/quotations         (all)
DELETE /api/v1/quotations/{id} (any)
POST /api/v1/products          (create)
PUT /api/v1/products/{id}      (update)
DELETE /api/v1/products/{id}   (delete)
GET/PUT/DELETE /api/v1/users/** (admin only)

### Roles
- **ROLE_CLIENT:** create/view own quotations, view own profile
- **ROLE_ADMIN:** manage products, view all quotations, delete any

### CORS Configuration
**Development:**
Allowed Origins: http://localhost:3000, http://localhost:8080
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type, Accept
Credentials: true

**Production:**
Allowed Origins: https://example.com, https://app.example.com
Allowed Methods: GET, POST, PUT, DELETE
Allowed Headers: Authorization, Content-Type
Credentials: true

### Rate Limiting
- `/api/v1/auth/login`: max 5 failed attempts per IP in 15 minutes → 429 Too Many Requests
- Implementation: `LoginRateLimitFilter` (Bucket4j 8.x, `ConcurrentHashMap<IP, Bucket>`)
- Bucket4j 8.x API: `Bucket.builder().addLimit(Bandwidth.builder().capacity(n).refillIntervally(n, Duration.ofMinutes(m)).build()).build()`

### Exception Handling (HTTP Status Codes)
401 Unauthorized:

Token expired
Invalid token signature
Token missing
Invalid credentials (login failed)

403 Forbidden:

Valid token, but insufficient permissions
Client accessing admin endpoint
Accessing quotation of other user

400 Bad Request:

Invalid password format
Missing required fields

500 Server Error:

Don't expose JWT secret or internal errors


### Audit Logging
**Log events (without sensitive data):**

User login: user_id, timestamp, ip_address, success/failure
Token refresh: user_id, timestamp
Endpoint access (admin-only): user_id, endpoint, timestamp
Unauthorized access attempts: ip_address, attempted_endpoint, timestamp
Password change: user_id, timestamp


**What NOT to log:**
- ❌ Passwords (plaintext or hashed)
- ❌ JWT tokens (full token)
- ❌ Refresh tokens
- ❌ API secrets or keys

### Testing Security — Critical Patterns (Java 25 + Spring Security)

⚠️ Running Java 25: ByteBuddy does NOT support mocking concrete classes. Only mock interfaces.
If `@MockBean SomeConcreteClass` appears, it will fail with:
`Java 25 (69) is not supported by Byte Buddy — set net.bytebuddy.experimental`

**Pattern for @WebMvcTest controller tests (non-security tests):**
```java
@WebMvcTest(
    value = MyController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MyControllerTest { ... }
```
Do NOT add `@MockBean(JwtTokenProvider.class)` — JwtTokenProvider is a concrete class and will fail on Java 25.

⚠️ `JwtAuthenticationFilter` and `LoginRateLimitFilter` must NOT be `@Component`.
They are created as `@Bean` inside `SecurityConfig` only. If they were `@Component`,
`@WebMvcTest` would scan them as Filters and fail to inject `JwtTokenProvider`.

**Pattern for security integration tests:**
Use `@SpringBootTest + @AutoConfigureMockMvc` (real filter chain, no exclusions).
`@MockBean` only for use-case interfaces (not concrete infrastructure beans).

**JwtTokenProvider tests (standalone, no Spring context):**
Use `ReflectionTestUtils.setField(provider, "jwtSecret", SECRET)` to inject values.
JJWT 0.12.3 API (different from older versions):
```java
// Generate
Jwts.builder().id(uuid).subject(userId).claim("roles", roles)
    .issuedAt(now).expiration(expiry).signWith(key).compact()
// Validate
Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()
```

JwtTokenProvider tests:
✓ generateAccessToken returns valid JWT
✓ validateToken returns true for valid token
✓ validateToken returns false for expired token
✓ validateToken returns false for tampered token
✓ validateToken returns false for wrong secret
✓ getClaims extracts correct userId and roles
✓ token has jti claim (UUID)
SecurityConfig tests (@SpringBootTest):
✓ ROLE_CLIENT cannot POST /api/v1/products (403)
✓ ROLE_ADMIN can GET /api/v1/products (not 401/403)
✓ Unauthenticated request → 401
✓ /api/v1/auth/** is public (not 401/403)
AuthController tests (@WebMvcTest, security excluded):
✓ POST /api/v1/auth/login with valid credentials returns access_token + refresh_token
✓ POST /api/v1/auth/login with invalid credentials returns 401
✓ POST /api/v1/auth/login with invalid email format returns 400
✓ POST /api/v1/auth/refresh with valid token returns new tokens
✓ POST /api/v1/auth/refresh with expired token returns 401
✓ POST /api/v1/auth/logout returns 204
Integration tests (@SpringBootTest):
✓ Valid token → GET protected → not 401
✓ Tampered token → GET protected → 401
✓ No token → GET protected → 401 with JSON body
✓ Rate limiting: 6th login attempt in 15 min returns 429
✓ Swagger /v3/api-docs is public

### Folder Structure
infrastructure/security/
├─ JwtTokenProvider.java              (token generation/validation)
├─ JwtAuthenticationFilter.java        (JWT request interceptor)
├─ SecurityConfig.java                 (Spring Security @Configuration)
├─ PasswordValidator.java              (password strength validation)
├─ PasswordEncoderConfig.java         (BCrypt bean, strength=12)
├─ UserDetailsServiceImpl.java         (load user from DB)
├─ RoleAuthorizationException.java    (custom exception)
└─ SecurityUtil.java                  (extract userId from token, etc.)
domain/entity/
├─ User.java                           (updated: add password, roles, createdAt)
├─ JwtRefreshToken.java               (store refresh tokens in DB)
└─ AuditLog.java                      (optional: log security events)

### Configuration (application.yml)
⚠️ CORS config lives under `app.cors.*` (not `spring.security.cors.*` — that namespace is non-standard)

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:quotation-dev-secret-key-min-32-chars-for-hs256-ok}
    access-token-expiry: 900000     # 15 minutes in ms
    refresh-token-expiry: 604800000 # 7 days in ms
  cors:
    allowed-origins: http://localhost:3000,http://localhost:8080
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: Authorization,Content-Type,Accept
    allow-credentials: true
  security:
    bcrypt-strength: 12
    rate-limit:
      login-attempts: 5
      window-minutes: 15
```

### Dependencies (pom.xml)
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Rate limiting -->
<!-- ⚠️ CORRECT groupId is com.bucket4j — io.github.bucket4j does NOT exist on Maven Central -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

### Key Points
- **Stateless:** No sessions, every request self-contained with JWT
- **Token Rotation:** Refresh tokens are rotated to prevent token reuse
- **Audit Trail:** Security events logged (logins, endpoint access, failures)
- **Rate Limiting:** Prevents brute force on login
- **Secure Defaults:** Short token expiry, strong password requirements
- **Error Handling:** Don't expose internal details in error messages

## Building & Running
- **Build**: mvn clean install
- **Dev**: mvn spring-boot:run
- **Test**: mvn test
- **Docker**: docker-compose -f docker-compose.yml up
- **Database**: PostgreSQL en docker-compose, H2 en tests

## Commits
- **Format**: Conventional Commits (feat:, fix:, refactor:, test:, docs:, chore:)
- **Scope**: (auth), (quotation), (persistence), (pdf), (validation)
- **Body**: Incluye WHY y HOW si es complejo (no QUÉ, eso es el código)
- **Example**: feat(quotation): implement quotation calculation with volume discounts

## Common Commands
```bash
mvn clean install
mvn spring-boot:run
mvn test
docker-compose up
```

## Key Files to Remember
- pom.xml: Dependencies, profiles
- application.yml: Main config
- db/migration/: All DB schema changes
- domain/: NEVER import Spring here
- infrastructure/config/: All @Configuration, @Bean

## When Stuck
1. Check CLAUDE.md first
2. Review domain/entity/ para entender modelo
3. Revisa application/port/out/ para interfaces
4. Mira infrastructure/output/ para implementaciones
5. Tests in src/test/ for examples

## Known Gotchas & Hard-Won Lessons

### Java 25 + Lombok
Lombok 1.18.36 (shipped with Spring Boot 3.4) does NOT work on Java 25.
Fix: pin `<lombok.version>1.18.40</lombok.version>` + explicit `annotationProcessorPaths` in pom.xml.

### Java 25 + Mockito (@MockBean)
`@MockBean` on **concrete classes** fails on Java 25 — ByteBuddy doesn't support bytecode version 69.
Rule: only `@MockBean` interfaces. For concrete infrastructure beans (JwtTokenProvider, filters),
avoid mocking in controller tests entirely (exclude security and disable filters instead).

### @WebMvcTest + Spring Security filters
`@WebMvcTest` scans `@Component` beans that implement `Filter`. If `JwtAuthenticationFilter`
were `@Component`, the test context would fail (needs `JwtTokenProvider` which is not in the slice).
Fix: filters are NOT `@Component` — they are `@Bean` in `SecurityConfig` only.

### Bucket4j dependency
The groupId `io.github.bucket4j` does **not exist** on Maven Central.
Correct dependency: `com.bucket4j:bucket4j-core:8.10.1`.
Bucket4j 8.x API changed from 7.x: use `Bandwidth.builder().capacity().refillIntervally().build()`.

### Flyway + H2 in tests
Tests use `application-test.yml` with `flyway.enabled: false` and `ddl-auto: create-drop`.
Never enable Flyway in test profile — H2 doesn't support all PostgreSQL-specific SQL.

### JJWT 0.12.3 API
Different from older JJWT versions. Key methods:
- `Jwts.builder().id()`, `.subject()`, `.claim()`, `.issuedAt()`, `.expiration()`, `.signWith(key).compact()`
- `Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()`
- Key creation: `Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))`

## Established Patterns
- Services in application/ inject ports (interfaces), not implementations
- Repositories implement ports, not controllers
- DTOs map between REST and domain
- Exceptions in domain/ inherits RuntimeException
- GlobalExceptionHandler catches and converts to HTTP