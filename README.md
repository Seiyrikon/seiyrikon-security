# Seiyrikon Security

A reusable Spring Boot security library that provides JWT authentication, database-driven roles, token blacklisting (logout) and auto-created security tables. You implement one small interface; the library handles the rest.

- **Group / Artifact:** `com.github.Seiyrikon:seiyrikon-security`
- **Tested with:** Spring Boot 4.0.x, Java 17 (works in projects on JDK 21), MySQL 8
- **JWT library:** JJWT 0.13.0

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
10. [Logout](#10-logout)
11. [Request flow](#11-request-flow)
12. [Security notes](#12-security-notes)
13. [Troubleshooting](#13-troubleshooting)
14. [Updating the library](#14-updating-the-library)

---

## 1. How it works

```
Your app implements SeiyrikonSecurityAuthProvider
  -> findUser()       : look up your user by username/email
  -> verifyPassword() : compare raw password with your stored hash

Library handles:
  - password verification via your provider
  - fetching the user's roles from the role tables
  - generating the JWT (roles stored as claims, 30-minute expiry)
  - validating the JWT on every request (JwtAuthenticationFilter)
  - blacklisting tokens on logout
  - auto-creating the role / user_role tables
  - the SecurityFilterChain (stateless, CSRF disabled, whitelist-based)
```

The library is registered through Spring Boot auto-configuration
(`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`), so consuming apps do **not** need component scanning of the library's packages.

---

## 2. Build and install the library

In the `seiyrikon-security` project:

```bash
mvn clean install
```

This builds `target/seiyrikon-security-1.0.0.jar` and installs it into your local Maven repository (`~/.m2`). Any project on this machine can then depend on it.

> The library's `spring-boot-maven-plugin` has `repackage` **skipped**. This is required. A repackaged "fat" JAR cannot be used as a dependency and causes `SecurityConfig.class ... does not exist` errors.

### Sharing the JAR manually (without `.m2` access)

Give the JAR file to another machine and install it there:

```bash
mvn install:install-file \
  -Dfile=seiyrikon-security-1.0.0.jar \
  -DgroupId=com.github.Seiyrikon \
  -DartifactId=seiyrikon-security \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

> Installing only the JAR does not bring the library's transitive dependencies. Installing from the project with `mvn install` also installs the `pom.xml`, which does. Prefer sharing via `mvn install` or an artifact repository if the other machine reports missing classes.

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

    <!-- Your database driver (MySQL shown) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- .env support. For Spring Boot 4 use springboot4-dotenv (NOT spring-dotenv) -->
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
SS_ROLE_TABLE=role
SS_USER_ROLE_TABLE=user_role
```

Generate a strong secret (the library Base64-decodes it, and HS256 needs at least 256 bits / 32 bytes):

```bash
openssl rand -base64 32
```

> Avoid very generic variable names like `ROLE` or `USER_ROLE`; they can collide with OS environment variables.
> Add `.env` to `.gitignore`.

### 3.3 `application.properties`

```properties
spring.application.name=demo

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Seiyrikon Security
seiyrikon-security.jwt.secret-key=${JWT_SECRET_KEY}
seiyrikon-security.table-configuration.role-table=${SS_ROLE_TABLE}
seiyrikon-security.table-configuration.user-role-table=${SS_USER_ROLE_TABLE}

# Endpoints that do NOT require a token (comma-separated)
seiyrikon-security.whitelisted-paths=/api/v1/login,/api/v1/logout
```

On startup you should see `JWT Secret Key loaded successfully.` in the console. If the app fails with "JWT_SECRET_KEY is not set", your `.env` is not being loaded (see [Troubleshooting](#13-troubleshooting)).

---

## 4. Database setup

### Option A: Auto-create (default)

On startup the library's `SeiyrikonSecurityTableInitializer` creates the tables if they do not exist, using the table and column names from your properties. Nothing to do.

Disable it if you manage the schema yourself (see the `autoCreateSeiyrikonSecurityTable` field in `SeiyrikonSecurityTableProperties` for the exact property name).

### Option B: Create manually

You can name the tables anything, but **the columns must follow this structure** (column names are also configurable, see [Configuration reference](#5-configuration-reference)).

**Role table** (default name `roles`)

| Column | Type | Notes |
|--------|------|-------|
| `id`   | `VARCHAR(255)` | Primary key. Can hold numbers or strings. |
| `name` | `VARCHAR(255)` | Unique. e.g. `ROLE_ADMIN`, `ROLE_USER` |

**User-role table** (default name `user_roles`)

| Column    | Type | Notes |
|-----------|------|-------|
| `user_id` | `VARCHAR(255)` | Your user's primary key (any type, stored as text) |
| `role_id` | `VARCHAR(255)` | References the role table's `id` |

Primary key is `(user_id, role_id)`, so a user can have many roles and a role can belong to many users.

```sql
CREATE TABLE role (
    id   VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_role (
    user_id VARCHAR(255) NOT NULL,
    role_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);
```

> The library also keeps a token blacklist (used by logout). Check `SeiyrikonSecurityTableInitializer` / `SeiyrikonSecurityTokenBlacklistRepository` for the table name and columns it uses.

### Seed some roles

```sql
INSERT INTO role (id, name) VALUES ('1', 'ROLE_ADMIN'), ('2', 'ROLE_USER');

-- Assign roles to a user (user_id must match your user's primary key)
INSERT INTO user_role (user_id, role_id) VALUES ('ADMIN1', '1');
```

### PostgreSQL (with a custom schema)

**Driver** (replaces the MySQL one):

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Datasource.** Set the schema on the JDBC URL so it applies to *every* query, including the library's `JdbcTemplate` DDL and native SQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db?currentSchema=your_schema
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

> `spring.jpa.properties.hibernate.default_schema` is **not enough** on its own. It only affects Hibernate-generated SQL, not the library's native queries or `JdbcTemplate` calls.

**Alternative:** schema-qualify the table names instead of using `currentSchema`:

```properties
seiyrikon-security.table-configuration.role-table=your_schema.role
seiyrikon-security.table-configuration.user-role-table=your_schema.user_role
```

**Create the schema first.** Auto-create only creates tables, not the schema itself:

```sql
CREATE SCHEMA IF NOT EXISTS your_schema;
```

**Manual table creation** (if auto-create is off):

```sql
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
```

**MySQL-only SQL to avoid in the library** (check `SeiyrikonSecurityTokenBlacklistRepository` and the table initializer):

| MySQL | PostgreSQL |
|-------|-----------|
| `INSERT IGNORE INTO t (...) VALUES (...)` | `INSERT INTO t (...) VALUES (...) ON CONFLICT (token) DO NOTHING` |
| `DATETIME` | `TIMESTAMP` |
| backtick identifiers | double quotes (or none) |

`CREATE TABLE IF NOT EXISTS`, `VARCHAR(255)`, `NOW()` and the role/user_role join query work on both.

### Role naming rule

`hasRole('ADMIN')` expects the database value to be **`ROLE_ADMIN`** (Spring adds the `ROLE_` prefix). If you store plain values like `ADMIN`, use `hasAuthority('ADMIN')` instead.

---

## 5. Configuration reference

| Property | Purpose | Default |
|----------|---------|---------|
| `seiyrikon-security.jwt.secret-key` | Base64 HMAC secret used to sign/verify tokens | **required** |
| `seiyrikon-security.table-configuration.role-table` | Role table name | `roles` |
| `seiyrikon-security.table-configuration.user-role-table` | User-role table name | `user_roles` |
| `seiyrikon-security.table-configuration.<column>` | Column names (role id/name, user-role user id/role id) | `id`, `name`, `user_id`, `role_id` |
| `seiyrikon-security.whitelisted-paths` | Paths that skip authentication | library default |

Everything except the secret key has a default, so you only override what differs. The exact column property names follow the field names in `SeiyrikonSecurityTableProperties` (relaxed binding, e.g. `roleNameColumn` becomes `role-name-column`).

---

## 6. Implement the auth provider

The library calls your code through `SeiyrikonSecurityAuthProvider`. Implement it as a Spring bean:

```java
@Service
@RequiredArgsConstructor
public class UserAuthProvider implements SeiyrikonSecurityAuthProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // e.g. BCryptPasswordEncoder bean

    @Override
    public SeiyrikonSecurityUser findUser(String identifier) {
        UserModel user = userRepository.findByUserName(identifier);
        if (user == null) return null;

        return SeiyrikonSecurityUser.builder()
                .userId(user.getUserId())
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

- You do **not** load roles here. The library fetches them from the role tables using `userId`.
- `userId` can be a `Long`, `String` or `UUID`; it is stored as the token subject and as text in the user-role table.
- The `SeiyrikonSecurityAuthServiceImpl` bean only activates when your provider bean exists (`@ConditionalOnBean`).

---

## 7. Login endpoint

```java
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final SeiyrikonSecurityAuthServiceImpl authService;

    @PostMapping("/login")   // no @PreAuthorize here!
    public ResponseEntity<?> login(@RequestBody LoginBody body) {
        SeiyrikonSecurityAuthResponse response =
                authService.authenticate(body.getUsername(), body.getPassword());
        return ResponseEntity.ok(response);
    }
}
```

`authenticate()` finds the user, verifies the password, loads roles from the database, and returns the access token. Remember to whitelist the path (`seiyrikon-security.whitelisted-paths`).

**Test with Postman / curl:**

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
@PreAuthorize("hasRole('ADMIN')")            // DB value: ROLE_ADMIN
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

Authenticated requests store a `SeiyrikonSecurityPrincipal` (implements `UserDetails`) containing the `userId` and authorities.

```java
// Anywhere in your code, no injection needed
Object userId        = SeiyrikonSecurityPrincipal.getCurrentUserId();
List<String> roles   = SeiyrikonSecurityPrincipal.getCurrentRoles();
boolean isAdmin      = SeiyrikonSecurityPrincipal.currentHasRole("ROLE_ADMIN");
SeiyrikonSecurityPrincipal principal = SeiyrikonSecurityPrincipal.getCurrent();

// Or as a controller parameter
@GetMapping("/me")
public String me(@AuthenticationPrincipal SeiyrikonSecurityPrincipal principal) {
    return "Hello " + principal.getUserId();
}
```

> This requires the filter to set `SeiyrikonSecurityPrincipal` as the principal of the `UsernamePasswordAuthenticationToken` (instead of a raw `userId` string). If `getCurrent()` throws "Invalid principal type", the filter is still setting a plain string.

---

## 10. Logout

JWTs are stateless, so logging out means **blacklisting** the token until it expires. The filter rejects any blacklisted token.

```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    // call the library's logout / blacklist method with the token
    return ResponseEntity.ok("Logged out successfully");
}
```

Add the logout path to `seiyrikon-security.whitelisted-paths` or the token check in the filter will run first. Periodically delete blacklist rows whose expiry has passed so the table does not grow forever.

---

## 11. Request flow

```
Request with "Authorization: Bearer <jwt>"
 -> Filter reads the token, extracts userId (subject)
 -> Looks up the user via your AuthProvider.findUser()
 -> Rejects if the user is missing or the token is blacklisted
 -> Reads roles from the token claims
 -> Sets Authentication in the SecurityContext
 -> @PreAuthorize checks roles
 -> Controller runs
```

No header, or a header without the `Bearer ` prefix: the filter skips and the request continues unauthenticated. It only succeeds on whitelisted paths.

---

## 12. Security notes

- **Never compare passwords in plain text** (the demo does this for testing only). Store BCrypt hashes and use `PasswordEncoder.matches()`.
- **Keep the secret out of source control** and use a long random Base64 value. Rotating it invalidates all existing tokens.
- **Token lifetime:** access tokens expire after 30 minutes. Short-lived access tokens plus a refresh token is the recommended pattern if you need longer sessions.
- **Token tampering:** the JWT signature already guarantees the subject, roles and expiry cannot be modified without the secret key. A token for user A cannot be re-pointed to user B without invalidating the signature.
- **Roles in the token** are a snapshot taken at login. Role changes take effect on the next login (or reload roles from the database in the filter if you need immediate effect).
- Use HTTPS in production; bearer tokens are credentials.

---

## 13. Troubleshooting

| Symptom | Cause / Fix |
|---------|-------------|
| `SecurityConfig.class ... does not exist` | Library was repackaged as a fat JAR. Keep `repackage` skipped in the library's `spring-boot-maven-plugin`, then `mvn clean install`. |
| `required a bean of type JwtUtil` | A library class relied on component scanning. Register library beans via `@AutoConfiguration` and the `.imports` file. |
| `pattern must start with a /` | A path in a request matcher or whitelist is missing its leading `/`. |
| Library beans/tables/startup message missing | `@Component` classes in the library are not scanned by your app. Register them as `@Bean`s in the auto-configuration class and list it in `AutoConfiguration.imports`. |
| `bean 'springSecurityFilterChain' ... already defined` | Rename your `SecurityFilterChain` bean method (e.g. `seiyrikonSecurityFilterChain`). |
| SQL error near `{ROLE}` / `${SS_ROLE_TABLE}` | `.env` is not loaded. On Spring Boot 4 use `springboot4-dotenv` (5.x), not `spring-dotenv` 4.0.0. Keep `.env` in the project root. |
| SQL syntax error near `ur.user_id = ...` | Missing space in the concatenated SQL (`... = ur.role_id` + `WHERE`). End each line of the string with a space. |
| **401 on `/login`** | Path is not whitelisted, or the whitelist properties are not bound (check the `@ConfigurationProperties` prefix and that the properties class is enabled). |
| **403 on a protected endpoint** | Authenticated but missing the role. Check the DB value (`ROLE_ADMIN`) vs. the expression (`hasRole('ADMIN')`), and that `user_role` has a row for that `user_id`. |
| `@PreAuthorize` on login always fails | Remove it. There is no authentication before login. |
| `@EnableGlobalMethodSecurity` deprecated | Use `@EnableMethodSecurity`. |
| Library changes not showing in your project | `mvn clean install` in the library, then `mvn clean install -U` in your project. |
| Library compiled on JDK 17, project on JDK 21 | Works. The reverse (library on 21, project on 17) does not. |

---

## 14. Updating the library

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
- [ ] Add the library, JPA, web, DB driver and `springboot4-dotenv` dependencies
- [ ] Create `.env` with `JWT_SECRET_KEY` (Base64) and table names
- [ ] Set datasource + `seiyrikon-security.*` properties
- [ ] Whitelist `/login` (and `/logout`)
- [ ] Run the app and confirm the tables exist and `JWT Secret Key loaded successfully.` prints
- [ ] Seed roles (`ROLE_ADMIN`, `ROLE_USER`) and assign them in `user_role`
- [ ] Implement `SeiyrikonSecurityAuthProvider` (use BCrypt)
- [ ] Add the login endpoint, call it, and use the token on protected endpoints
- [ ] Protect endpoints with `@PreAuthorize`
