# Keycloak Admin Client (Java)

A small Java library to interact with Keycloak’s Admin REST API. It manages OAuth2 client-credentials tokens (with auto-refresh + events) and provides basic user management helpers built on OkHttp.

## Features

- Token management via client-credentials grant (Google OAuth Client)
- Auto token refresh with scheduled tasks and event callbacks
- OkHttp client with Bearer token interceptor
- User operations: create user, find user, change password, update email
- User role mappings: assign and remove realm-level roles
- Realm operations: create realm roles
- Simple types: `User`, `Credentials`, `RealmRole`

## Requirements

- Java 21+
- Maven 3.8+

## Installation

### Build locally

- Package: `mvn package`
- Install to local repo: `mvn install`
- Output JAR: `target/`

### Use as a dependency

Group and artifact from `pom.xml`:

```xml
<dependency>
  <groupId>dev.nishisan</groupId>
  <artifactId>keycloak-admin-client</artifactId>
  <version>1.0-SNAPSHOT</version>
  <scope>compile</scope>
  
</dependency>
```

This repository also includes a GitHub Actions workflow to publish to GitHub Packages. See Publishing for details.

## Quickstart

### Programmatic configuration

```java
import dev.nishisan.keycloak.admin.client.KeycloakAdminClient;
import dev.nishisan.keycloak.admin.client.auth.TokenResponseWrapper;
import dev.nishisan.keycloak.admin.client.config.SSOConfig;
import dev.nishisan.keycloak.admin.client.types.User;

SSOConfig cfg = new SSOConfig(
    "your-client-id",
    "your-client-secret",
    "your-realm",
    "https://sso.example.com"  // Keycloak base URL
);

KeycloakAdminClient kc = new KeycloakAdminClient(cfg);

// Token is auto-managed and refreshed
TokenResponseWrapper token = kc.getTokenManager().getToken();

// Create a user
User newUser = new User("username", "InitialP@ss!", "user@example.com");
newUser = kc.getUserManager().createUser(newUser); // id is populated if created

// Find a user (by username or email)
User found = kc.getUserManager().findUser("user@example.com");

// Change password
kc.getUserManager().changePassword(newUser.getId(), "NewP@ss!", false);

// Update email
kc.getUserManager().updateEmail(newUser.getId(), "new-email@example.com");
```

### Realm roles (create and assign/remove)

```java
import dev.nishisan.keycloak.admin.client.types.RealmRole;

// Create a realm role
RealmRole viewer = new RealmRole("viewer", "Can view resources");
viewer = kc.getRealmManager().createRole(viewer); // id is populated if created

// Assign realm roles to a user
kc.getUserManager().addRealmRole(newUser.getId(), viewer); // using the role object
kc.getUserManager().addRealmRoles(newUser.getId(), "editor", "auditor"); // by role names

// Remove realm roles from a user
kc.getUserManager().removeRealmRole(newUser.getId(), viewer);
kc.getUserManager().removeRealmRoles(newUser.getId(), "auditor");
```

### Token events (optional)

```java
import dev.nishisan.keycloak.admin.client.events.ITokenEventListener;
import dev.nishisan.keycloak.admin.client.auth.TokenResponseWrapper;

kc.getTokenManager().registerListener(new ITokenEventListener() {
  @Override public void onTokenIssued(TokenResponseWrapper t) {
    System.out.println("Token issued: " + t.getAccessToken());
  }
  @Override public void onTokenRefreshed(TokenResponseWrapper t) {
    System.out.println("Token refreshed: " + t.getRefreshToken());
  }
  @Override public String getUniqueName() { return "example-listener"; }
});
```

## Configuration (YAML example)

You can keep credentials outside of source control. The repo ignores the `config/` directory by default.

Create `config/sample.yaml`:

```yaml
clientId: your-client-id
clientSecret: your-client-secret
realm: your-realm
baseUrl: https://sso.example.com
```

Example usage (see `src/test/java/dev/nishisan/keycloak/admin/test/TokenTest.java`):

```java
ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
SSOConfig config = mapper.readValue(new File("config/sample.yaml"), SSOConfig.class);
KeycloakAdminClient client = new KeycloakAdminClient(config);
```

Note: The `src/test` directory contains simple runnable examples (`TokenTest`, `YamlTest`) with `main` methods. Run them from your IDE or with a proper classpath including dependencies.

## Logging

- Uses SLF4J (`slf4j-api`) with `slf4j-simple` backend by default.
- Adjust level with system property, for example: `-Dorg.slf4j.simpleLogger.defaultLogLevel=debug`.

## Security Notice

The HTTP client in `KeycloakAdminClient` is configured to:

- Trust all SSL certificates
- Skip hostname verification

This is convenient for development but not safe for production. Replace the trust manager and hostname verifier with secure defaults before deploying in production environments.

## Publishing (GitHub Packages)

- Workflow: `.github/workflows/publish.yml`
- On pushes to `main`, the action:
  - Bumps version
  - Builds with Java 21
  - Publishes to GitHub Packages
- The included `settings.xml` expects environment variables:
  - `GITHUB_USERNAME`
  - `GITHUB_TOKEN`

To consume from GitHub Packages, configure your Maven settings with the `github` server and repository, similar to `settings.xml` in this repo.

## Modules and API Overview

- `dev.nishisan.keycloak.admin.client.KeycloakAdminClient`
  - Entry point. Holds `SSOConfig`, token manager, OkHttp client, and managers
- `auth.KeyCloakOAuthClientManager`
  - Client-credentials token, auto-refresh, event listeners
- `config.SSOConfig`
  - `clientId`, `clientSecret`, `realm`, `baseUrl`, computed `getTokenUrl()`
- `management.UserManager`
  - `createUser`, `findUser`, `changePassword`, `updateEmail`
  - Role mappings: `addRealmRole`, `addRealmRoles(List)`, `addRealmRoles(String...)`, `removeRealmRole`, `removeRealmRoles(List)`, `removeRealmRoles(String...)`
- `management.RealmManager`
  - Realm roles: `createRole`
- `types.User`, `types.Credentials`, `types.RealmRole`
  - Minimal models mapped to Keycloak payloads
- `events.ITokenEventListener`, `events.SafeEventListener`
  - Callback interface for token lifecycle

## License

This project is licensed under the terms of the LGPL-3.0 license. See `LICENSE` for details.
