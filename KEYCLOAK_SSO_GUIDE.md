# Keycloak SSO Implementation Guide

This document explains the principles of Single Sign-On (SSO) with Keycloak and how it's implemented in this project.

## What is Keycloak SSO?

Keycloak is an open-source Identity and Access Management solution that provides Single Sign-On (SSO) capabilities. SSO allows users to authenticate once and gain access to multiple applications without having to log in again.

## Core Principles of Keycloak SSO

1. **Centralized Authentication**: All authentication is handled by Keycloak, not individual applications
2. **OAuth2/OpenID Connect**: Industry-standard protocols for authentication and authorization
3. **JWT Tokens**: JSON Web Tokens are used to securely transmit user information between systems
4. **Role-Based Access Control**: User permissions are determined by roles assigned in Keycloak
5. **User Federation**: Ability to connect to existing user stores (LDAP, Active Directory, etc.)

## How SSO Works with Keycloak

The SSO flow works as follows:

1. **User Access**: User attempts to access a protected resource in an application
2. **Redirection**: Application redirects to Keycloak for authentication
3. **Authentication**: User logs in with Keycloak (if not already authenticated)
4. **Token Issuance**: Keycloak issues JWT tokens (access token, refresh token, ID token)
5. **Redirection Back**: User is redirected back to the application with tokens
6. **Resource Access**: Application validates tokens and grants access to resources
7. **Token Refresh**: When access token expires, refresh token is used to get new tokens
8. **Single Logout**: Logging out from one application logs out from all applications

## Project Implementation

### 1. Keycloak Setup

- **Realm**: A security domain in Keycloak (sso-demo)
- **Clients**: Applications registered with Keycloak (main-app, resource-service)
- **Roles**: User permissions (user, admin, manager)
- **Users**: Test accounts with different roles

### 2. Spring Boot Integration

The integration is achieved through several components:

#### Security Configuration

`KeycloakSecurityConfig.java` configures Spring Security to use Keycloak as an OAuth2/OIDC provider:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class KeycloakSecurityConfig {
    // ...
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(...)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(...))
            // ...
    }
    // ...
}
```

#### Role Mapping

Keycloak roles are mapped to Spring Security authorities:

```java
static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Extract roles from JWT token and convert to Spring Security authorities
        // ...
    }
}
```

#### Logout Handling

`KeycloakLogoutHandler.java` ensures proper logout from Keycloak:

```java
@Component
public class KeycloakLogoutHandler implements LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Call Keycloak's end-session endpoint
        // ...
    }
}
```

#### User Synchronization

`UserService.java` synchronizes Keycloak users with the application database:

```java
@Service
public class UserService {
    // ...
    public Optional<User> getCurrentUser() {
        // Extract user info from JWT token
        // Create or update user in local database
        // ...
    }
    // ...
}
```

### 3. Application Configuration

The `application.yml` file contains the necessary configuration for Keycloak integration:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-id: main-app
            client-secret: main-app-secret
            # ...
        provider:
          keycloak:
            issuer-uri: http://localhost:8090/realms/sso-demo
            # ...
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8090/realms/sso-demo
          jwk-set-uri: http://localhost:8090/realms/sso-demo/protocol/openid-connect/certs
```

## Setting Up Your Own Keycloak SSO

To implement Keycloak SSO in your own project:

1. **Install Keycloak**:
   - Use Docker: `docker run -p 8090:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:23.0.3 start-dev`
   - Or use the provided docker-compose.yml file

2. **Create a Realm**:
   - Log in to Keycloak Admin Console (http://localhost:8090/admin)
   - Create a new realm (e.g., "sso-demo")

3. **Create Clients**:
   - Create a client for each application that will use SSO
   - Configure valid redirect URIs
   - Generate client secrets for confidential clients

4. **Define Roles**:
   - Create realm roles (e.g., user, admin, manager)
   - Assign roles to users

5. **Create Test Users**:
   - Create users with different roles for testing

6. **Configure Your Spring Boot Application**:
   - Add dependencies:
     - spring-boot-starter-security
     - spring-boot-starter-oauth2-client
     - spring-boot-starter-oauth2-resource-server
   - Configure application.yml with Keycloak settings
   - Implement security configuration
   - Add role-based access control

7. **Test SSO Flow**:
   - Start Keycloak
   - Start your application
   - Try accessing protected resources
   - Verify single sign-on and single logout

## Benefits of This Implementation

1. **Security**: Industry-standard security protocols
2. **Centralized User Management**: Manage users in one place
3. **Reduced Development Time**: No need to implement authentication
4. **Better User Experience**: Users log in once for multiple applications
5. **Flexibility**: Easy to add new applications to the SSO ecosystem
6. **Scalability**: Keycloak can handle millions of users

## DDD Integration

This project follows Domain-Driven Design principles:

- **Domain Layer**: Core business logic and entities
- **Application Layer**: Use cases and application services
- **Infrastructure Layer**: External services, repositories, and adapters
- **Presentation Layer**: REST API endpoints and controllers

The Keycloak integration is primarily in the infrastructure layer, keeping the domain model clean and focused on business logic.
