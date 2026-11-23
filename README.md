# Seiyrikon Security

A reusable Spring Boot security library: JWT authentication, database-driven roles, token blacklisting (logout), and JPA-managed security tables. You implement one small interface; the library does the rest.

- **Artifact:** `com.github.Seiyrikon:seiyrikon-security`
- **Stack:** Spring Boot 4.0.x, Java 17 (usable from projects on JDK 21), JJWT 0.13.0
- **Databases:** PostgreSQL (with schema) and MySQL, via JPA/Hibernate
- **Tables are fixed:** `role`, `user_role`, `token_blacklist` (no naming configuration)

---

## Table of Contents

1. [How it works](#1-how-it-works)
2. [Build and install the library](#2-build-and-install-the-library)
3. [Set up your project](#3-set-up-your-project)
4. [Database setup](#4-database-setup)
5. [Configuration reference](#5-configuration-reference)
6. [Implement the auth provider](#6-implement-the-auth-provider)
7. [Login endpoint](#7-login-endpoint)
8. [Protecting endpoints](#8-protecting-endpoints)
9. [Accessing the current user](#9-accessing-the-current-user)
10. [Logout and token cleanup](#10-logout-and-token-cleanup)
11. [Request flow](#11-request-flow)
12. [Library internals (for maintenance)](#12-library-internals-for-maintenance)
13. [Security notes](#13-security-notes)
14. [Troubleshooting](#14-troubleshooting)
15. [Updating the library](#15-updating-the-library)
16. [Quick start checklist](#quick-start-checklist)

---

## 1. How it works

```
Your app implements SeiyrikonSecurityAuthProvider
  -> findUser()       : look up your user
  -> verifyPassword() : compare the raw password with your stored hash

The library handles:
  - password verification through your provider
  - loading the user's roles from the role / user_role tables (JPA)
  - generating the JWT (roles in claims, 30-minute expiry)
  - validating the JWT on every request (JWT authentication filter)
  - blacklisting tokens on logout + scheduled cleanup of expired ones
  - the SecurityFilterChain (stateless, CSRF disabled, whitelist-based)
```

The library is registered through Spring Boot auto-configuration
(`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`).
It also registers its own package with `@AutoConfigurationPackage`, so its JPA entities and repositories are discovered automatically. Your app needs no extra scanning configuration.

---

## 2. Build and install the library

In the `seiyrikon-security` project:

```bash
mvn clean install
```

This builds `target/seiyrikon-security-1.0.0.jar` and installs it into your local Maven repository (`~/.m2`). Any project on this machine can then depend on it.

> The library's `spring-boot-maven-plugin` has `repackage` **skipped**. This is required. A repackaged "fat" JAR cannot be used as a dependency and causes `SecurityConfig.class ... does not exist` errors.

### Sharing the JAR with another machine

```bash
mvn install:install-file \
  -Dfile=seiyrikon-security-1.0.0.jar \
  -DgroupId=com.github.Seiyrikon \
  -DartifactId=seiyrikon-security \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

> This installs only the JAR, not its transitive dependencies. If the other machine reports missing classes, install with `mvn install` from the project instead (it also installs the `pom.xml`), or publish to an artifact repository.

---

## 3. Set up your project

### 3.1 `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>
    <!-- Needed for @Valid / @NotBlank on request DTOs -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database driver: PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- ...or MySQL
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    -->

    <!-- .env support. For Spring Boot 4 use springboot4-dotenv (NOT spring-dotenv 4.0.0) -->
    <dependency>
        <groupId>me.paulschwarz</groupId>
        <artifactId>springboot4-dotenv</artifactId>
        <version>5.1.0</version>
    </dependency>

    <!-- This library -->
    <dependency>
        <groupId>com.github.Seiyrikon</groupId>
        <artifactId>seiyrikon-security</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### 3.2 `.env` (project root, next to `pom.xml`)

```properties
JWT_SECRET_KEY=<base64-encoded-secret>
```

Generate a strong secret. The library Base64-decodes it, and HS256 needs at least 256 bits (32 bytes):

```bash
openssl rand -base64 32
```

Add `.env` to `.gitignore`.

### 3.3 `application.properties`

```properties
spring.application.name=backend

# Database (PostgreSQL, with schema)
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db?currentSchema=your_schema
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Creates/updates the library's tables (role, user_role, token_blacklist)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.default_schema=your_schema

# Seiyrikon Security
seiyrikon-security.jwt.secret-key=${JWT_SECRET_KEY}

# Endpoints that do NOT require a token (comma-separated)
seiyrikon-security.whitelisted-paths=/api/v1/login,/api/v1/logout
```

On startup you should see `JWT Secret Key loaded successfully.` in the console. If the app fails with "JWT_SECRET_KEY is not set", your `.env` is not being loaded (see [Troubleshooting](#14-troubleshooting)).

---

## 4. Database setup

### 4.1 Tables

The library's entities map to three fixed tables:

| Table | Columns | Notes |
|-------|---------|-------|
| `role` | `id` VARCHAR(255) PK, `name` VARCHAR(255) UNIQUE | e.g. `ROLE_ADMIN`, `ROLE_USER` |
| `user_role` | `user_id` VARCHAR(255), `role_id` VARCHAR(255), PK `(user_id, role_id)` | many-to-many between users and roles |
| `token_blacklist` | `token` VARCHAR(1024) PK, `expiry` TIMESTAMP | logged-out tokens until they expire |

All IDs are stored as **text**, so your user primary key can be a `Long`, `UUID` or `String`. The library converts with `String.valueOf(userId)`.

### 4.2 Auto-create with Hibernate (development)

With `spring.jpa.hibernate.ddl-auto=update`, Hibernate creates the three tables on startup, because the library's entities are registered with your app. Notes:

- `update` only **adds** tables/columns. It never drops or alters existing ones.
- Hibernate does not create the **schema**. Create it once (PostgreSQL):

  ```sql
  CREATE SCHEMA IF NOT EXISTS your_schema;
  ```
- Hibernate's docs advise against `update` in production. For production, use a migration tool (below) and `ddl-auto=validate`.

### 4.3 Manual / migration SQL (production)

PostgreSQL:

```sql
CREATE SCHEMA IF NOT EXISTS your_schema;

CREATE TABLE IF NOT EXISTS your_schema.role (
    id   VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS your_schema.user_role (
    user_id VARCHAR(255) NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (role_id) REFERENCES your_schema.role(id)
);

CREATE TABLE IF NOT EXISTS your_schema.token_blacklist (
    token  VARCHAR(1024) PRIMARY KEY,
    expiry TIMESTAMP NOT NULL
);
```

For MySQL, drop the schema qualifiers and use `DATETIME` instead of `TIMESTAMP` if you prefer. Putting this in a Flyway migration (`db/migration/V1__seiyrikon_security.sql`) gives every new system identical, versioned tables.

> The entities map `user_role.role_id` as a plain column, so Hibernate's `update` does **not** create the foreign key to `role`. The SQL above does. Keep the constraint in your migration if you want the database to reject unknown roles.

### 4.4 Seed some roles

```sql
INSERT INTO role (id, name) VALUES ('1', 'ROLE_ADMIN'), ('2', 'ROLE_USER');

-- user_id must match your user's primary key (as text)
INSERT INTO user_role (user_id, role_id) VALUES ('ADMIN1', '1');
```

### 4.5 Role naming rule

`hasRole('ADMIN')` expects the database value to be **`ROLE_ADMIN`** (Spring adds the `ROLE_` prefix). If you store plain values like `ADMIN`, use `hasAuthority('ADMIN')` instead.

---

## 5. Configuration reference

| Property | Purpose | Default |
|----------|---------|---------|
| `seiyrikon-security.jwt.secret-key` | Base64 HMAC secret used to sign/verify tokens | **required** |
| `seiyrikon-security.whitelisted-paths` | Comma-separated paths that skip authentication | library default |
| `seiyrikon-security.blacklist-cleanup-cron` | Cron for deleting expired blacklisted tokens | `0 0 * * * *` (hourly) |

Table and column names are no longer configurable.

---

## 6. Implement the auth provider

The library calls your code through `SeiyrikonSecurityAuthProvider`. Implement it as a Spring bean:

```java
@Service
@RequiredArgsConstructor
public class UserAuthProvider implements SeiyrikonSecurityAuthProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // e.g. a BCryptPasswordEncoder bean

    @Override
    public SeiyrikonSecurityUser findUser(String identifier) {
        UserModel user = userRepository.findByUserName(identifier);
        if (user == null) return null;

        return SeiyrikonSecurityUser.builder()
                .userId(String.valueOf(user.getUserId()))
                .username(user.getUserName())
                .hashedPassword(user.getHashedPassword())
                .build();
    }

    @Override
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
```

Notes:

- `SeiyrikonSecurityUser` (package `com.seiyrikon.seiyrikon_security.model`) holds `userId`, `username` and `hashedPassword`. All are `String`.
- You do **not** load roles here. The library reads them from `role` / `user_role` using `userId`.
- **`findUser` is called with different values.** At login it receives the username. As the JWT filter is currently written it is also called with the **userId** from the token subject. Make sure your implementation handles both (or change the filter to look users up by id).
- If your user table's primary key is numeric, convert the token's string subject before querying it, e.g. `Long.parseLong(userId)`.
- The `SeiyrikonSecurityAuthServiceImpl` bean only activates when your provider bean exists (`@ConditionalOnBean`).

---

## 7. Login endpoint

Use a standalone request DTO. Do **not** extend `SeiyrikonSecurityUser`:

```java
@Data
public class AuthLoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
```

```java
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final SeiyrikonSecurityAuthServiceImpl authService;

    @PostMapping("/login")   // no @PreAuthorize here!
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest request) {
        SeiyrikonSecurityAuthResponse response =
                authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
```

`authenticate()` finds the user, verifies the password, loads roles, and returns the access token. Whitelist the path in `seiyrikon-security.whitelisted-paths`.

```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secret"}'
```

Use the returned token on later requests:

```
Authorization: Bearer <accessToken>
```

---

## 8. Protecting endpoints

Method security (`@EnableMethodSecurity`) is already enabled by the library.

```java
@GetMapping("/profile")
@PreAuthorize("isAuthenticated()")
public String profile() { ... }

@GetMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")             // DB value: ROLE_ADMIN
public String admin() { ... }

@GetMapping("/staff")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public String staff() { ... }

@GetMapping("/reports")
@PreAuthorize("hasAuthority('READ_REPORTS')") // DB value: READ_REPORTS (no prefix)
public String reports() { ... }
```

Rules of thumb:

- Anything **not** in the whitelist requires a valid token (`anyRequest().authenticated()`).
- Never put `@PreAuthorize` on the login endpoint. There is no authentication yet at that point.
- **401** means no/invalid authentication. **403** means authenticated but missing the required role.

---

## 9. Accessing the current user

Authenticated requests store a `SeiyrikonSecurityPrincipal` (implements `UserDetails`) holding the `userId` and authorities.

```java
// Anywhere in your code, no injection needed
Object userId      = SeiyrikonSecurityPrincipal.getCurrentUserId();
List<String> roles = SeiyrikonSecurityPrincipal.getCurrentRoles();
boolean isAdmin    = SeiyrikonSecurityPrincipal.currentHasRole("ROLE_ADMIN");
SeiyrikonSecurityPrincipal principal = SeiyrikonSecurityPrincipal.getCurrent();

// Or as a controller parameter
@GetMapping("/me")
public String me(@AuthenticationPrincipal SeiyrikonSecurityPrincipal principal) {
    return "Hello " + principal.getUserId();
}
```

> This requires the filter to set `SeiyrikonSecurityPrincipal` as the principal of the `UsernamePasswordAuthenticationToken` (not a raw `userId` string). If `getCurrent()` throws "Invalid principal type", the filter is still setting a plain string.

---

## 10. Logout and token cleanup

JWTs are stateless, so logout means **blacklisting** the token until it expires. The filter rejects any token found in `token_blacklist`.

```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    // call the library's logout method with the token (strip "Bearer ")
    return ResponseEntity.ok("Logged out successfully");
}
```

Whitelist the logout path, or the filter's token check runs on it first.

**Cleanup.** A scheduled job in the library deletes blacklist rows whose `expiry` has passed (default: hourly). Expired tokens are already rejected by the expiry check, so cleanup only keeps the table small. Change the schedule with:

```properties
seiyrikon-security.blacklist-cleanup-cron=0 0 3 * * *
```

---

## 11. Request flow

```
Request with "Authorization: Bearer <jwt>"
 -> Filter reads the token, extracts userId (subject)
 -> Looks up the user via your AuthProvider.findUser()
 -> Rejects if the user is missing or the token is in token_blacklist
 -> Reads roles from the token claims
 -> Sets Authentication in the SecurityContext
 -> @PreAuthorize checks roles
 -> Controller runs
```

No header, or one without the `Bearer ` prefix: the filter skips and the request continues unauthenticated. It only succeeds on whitelisted paths.

---

## 12. Library internals (for maintenance)

### Entities

```java
@Entity
@Table(name = "role")
@Getter @Setter @NoArgsConstructor
public class SeiyrikonSecurityRole {
    @Id @Column(name = "id") private String id;
    @Column(name = "name", nullable = false, unique = true) private String name;
}

@Embeddable
@EqualsAndHashCode
@Getter @Setter @NoArgsConstructor
public class SeiyrikonSecurityUserRoleId implements Serializable {
    @Column(name = "user_id") private String userId;
    @Column(name = "role_id") private String roleId;
}

@Entity
@Table(name = "user_role")
@Getter @Setter @NoArgsConstructor
public class SeiyrikonSecurityUserRole {
    @EmbeddedId private SeiyrikonSecurityUserRoleId id;
}

@Entity
@Table(name = "token_blacklist")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SeiyrikonSecurityTokenBlacklist {
    @Id @Column(name = "token", length = 1024) private String token;
    @Column(name = "expiry", nullable = false) private Instant expiry;
}
```

### Repositories

```java
public interface SeiyrikonSecurityUserRoleRepository
        extends JpaRepository<SeiyrikonSecurityUserRole, SeiyrikonSecurityUserRoleId> {

    @Query("""
        select r.name from SeiyrikonSecurityRole r
        join SeiyrikonSecurityUserRole ur on r.id = ur.id.roleId
        where ur.id.userId = :userId
        """)
    List<String> findRoleNamesByUserId(@Param("userId") String userId);
}

public interface SeiyrikonSecurityTokenBlacklistRepository
        extends JpaRepository<SeiyrikonSecurityTokenBlacklist, String> {

    @Modifying
    @Transactional
    @Query("delete from SeiyrikonSecurityTokenBlacklist t where t.expiry < :now")
    int deleteExpired(@Param("now") Instant now);
}
```

- Logout: `blacklistRepository.save(new SeiyrikonSecurityTokenBlacklist(jwt, expiry))`
- Filter check: `blacklistRepository.existsById(jwt)`
- Pass user ids as `String.valueOf(userId)`.

### Cleanup scheduler

```java
@RequiredArgsConstructor
public class SeiyrikonSecurityTokenCleanupScheduler {

    private final SeiyrikonSecurityTokenBlacklistRepository blacklistRepository;

    @Scheduled(cron = "${seiyrikon-security.blacklist-cleanup-cron:0 0 * * * *}")
    public void cleanExpiredTokens() {
        blacklistRepository.deleteExpired(Instant.now());
    }
}
```

### Auto-configuration

```java
@AutoConfiguration
@EnableScheduling
@AutoConfigurationPackage(basePackages = "com.seiyrikon.seiyrikon_security")
@Import(SeiyrikonSecurityFilterChainConfiguratiion.class)
@EnableConfigurationProperties({
        SeiyrikonSecurityTokenConfiguration.class,
        SeiyrikonSecurityFilterChainProperties.class
})
public class SeiyrikonSecurityAutoConfiguration {
    // @Bean methods: startUpCheck, jwtUtilComponent, authService (@ConditionalOnBean),
    // jwtAuthenticationFilter, tokenCleanupScheduler, ...
}
```

Rules that keep this working:

- Register library classes as `@Bean`s here. Do not rely on `@Component` (the consuming app does not scan the library's packages).
- Use `@Import` for the `@EnableWebSecurity` configuration class. Do not register it as a `@Bean`.
- Name the filter chain bean `seiyrikonSecurityFilterChain` (not `springSecurityFilterChain`).
- Use `@AutoConfigurationPackage`, not `@EntityScan`. `@EntityScan` replaces the app's default scan and its own entities stop being found.
- List the auto-configuration in `AutoConfiguration.imports`.
- Keep `repackage` skipped in the library's `pom.xml`.

---

## 13. Security notes

- **Never compare passwords in plain text** (a test shortcut only). Store BCrypt hashes and use `PasswordEncoder.matches()`.
- **Keep the secret out of source control**, and use a long random Base64 value. Rotating it invalidates all existing tokens.
- **Token lifetime:** access tokens expire after 30 minutes. For longer sessions, add a refresh token.
- **Token tampering:** the JWT signature guarantees the subject, roles and expiry cannot be changed without the secret key. A token for user A cannot be re-pointed to user B.
- **Roles in the token** are a snapshot from login time. Role changes apply on the next login (or reload roles from the database in the filter for immediate effect).
- Use HTTPS in production. Bearer tokens are credentials.

---

## 14. Troubleshooting

| Symptom | Cause / Fix |
|---------|-------------|
| `SecurityConfig.class ... does not exist` | Library was repackaged as a fat JAR. Keep `repackage` skipped, then `mvn clean install`. |
| `required a bean of type JwtUtil` | A library class relied on component scanning. Register it with `@Bean` in the auto-configuration. |
| `pattern must start with a /` | A path in the whitelist or a request matcher is missing its leading `/`. |
| Library beans / startup message missing | `@Component` in the library isn't scanned. Register as `@Bean` and list the class in `AutoConfiguration.imports`. |
| `bean 'springSecurityFilterChain' ... already defined` | Rename your `SecurityFilterChain` bean method. |
| SQL error near `${...}` / `{...}` | `.env` not loaded. On Spring Boot 4 use `springboot4-dotenv` 5.x. Keep `.env` in the project root. |
| `operator does not exist: character varying = bigint` | A `Long` id was bound to a `VARCHAR` column. Pass ids as `String.valueOf(userId)`. |
| `Not a managed type` / `Unknown entity` / repository not found | The library's package isn't registered. Add `@AutoConfigurationPackage(basePackages = "com.seiyrikon.seiyrikon_security")` to the auto-configuration. |
| App's own entities stopped working | `@EntityScan` was used in the library. Replace with `@AutoConfigurationPackage`. |
| `relation "role" does not exist` | Tables not created. Use `ddl-auto=update` or run the SQL, and make sure the schema exists and is set on the URL. |
| `There is no no-arg constructor available in SeiyrikonSecurityUser` | A DTO extends it. Use a standalone request DTO, or add `@NoArgsConstructor` / `@AllArgsConstructor` to the class. |
| Logout fails with "value too long" | `token_blacklist.token` is shorter than the JWT. Use `VARCHAR(1024)` (or hash the token). |
| MySQL-only SQL on PostgreSQL | `INSERT IGNORE`, `DATETIME` and backticks fail on Postgres. Use JPA `save()` / `ON CONFLICT DO NOTHING` / `TIMESTAMP`. |
| **401 on `/login`** | Path isn't whitelisted, or the whitelist properties aren't bound (check the `@ConfigurationProperties` prefix and that the class is enabled). |
| **403 on a protected endpoint** | Authenticated but missing the role. Compare the DB value (`ROLE_ADMIN`) with the expression (`hasRole('ADMIN')`), and check `user_role` has a row for that `user_id`. |
| `@PreAuthorize` on login always fails | Remove it. There is no authentication before login. |
| `@EnableGlobalMethodSecurity` deprecated | Use `@EnableMethodSecurity`. |
| Library changes not showing in your project | `mvn clean install` in the library, then `mvn clean install -U` in your project. |
| Library on JDK 17, project on JDK 21 | Works. The reverse does not. |

---

## 15. Updating the library

```bash
# In seiyrikon-security
mvn clean install

# In your consuming project
mvn clean install -U
```

If you change the version in the library's `pom.xml`, update the dependency version in the consuming project too.

---

## Quick start checklist

- [ ] `mvn clean install` the library
- [ ] Add the library, JPA, web, validation, DB driver and `springboot4-dotenv` dependencies
- [ ] Create `.env` with a Base64 `JWT_SECRET_KEY`
- [ ] Set datasource (with `currentSchema`), `ddl-auto`, and `seiyrikon-security.*` properties
- [ ] `CREATE SCHEMA IF NOT EXISTS ...` (PostgreSQL)
- [ ] Whitelist `/login` and `/logout`
- [ ] Run the app. Confirm `role`, `user_role`, `token_blacklist` exist and `JWT Secret Key loaded successfully.` prints
- [ ] Seed roles (`ROLE_ADMIN`, `ROLE_USER`) and assign them in `user_role`
- [ ] Implement `SeiyrikonSecurityAuthProvider` (BCrypt, handle username and userId lookups)
- [ ] Add the login endpoint with a standalone DTO and try it
- [ ] Protect endpoints with `@PreAuthorize`
