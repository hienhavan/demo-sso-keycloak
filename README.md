# Keycloak SSO Demo Project

This project demonstrates Single Sign-On (SSO) implementation using Keycloak with Spring Boot, following Domain-Driven Design (DDD) principles.

## Project Overview

This application showcases:
- Keycloak integration with Spring Security
- SSO authentication and authorization
- Role-based access control
- Token validation and management
- Multiple client applications sharing the same authentication

## Architecture

The project follows DDD architecture with the following layers:
- **Domain Layer**: Core business logic and entities
- **Application Layer**: Use cases and application services
- **Infrastructure Layer**: External services, repositories, and adapters
- **Presentation Layer**: REST API endpoints and controllers

## Technical Stack

- Java 17
- Spring Boot 3.2.x
- Spring Security
- Keycloak 23.x
- PostgreSQL (for application data)
- Maven for dependency management
- Docker & Docker Compose for containerization

## Getting Started

### Prerequisites
- Java 17+
- Docker and Docker Compose
- Maven

### Setup and Run

1. Clone the repository
2. Start the containers:
   ```
   docker-compose up -d
   ```
3. Build the application:
   ```
   mvn clean install
   ```
4. Run the application:
   ```
   mvn spring-boot:run
   ```

### Accessing the Application

- Main Application: http://localhost:8080
- Keycloak Admin Console: http://localhost:8090/admin (admin/admin)

## Keycloak Configuration

The project includes pre-configured Keycloak settings:
- Realm: `sso-demo`
- Clients: 
  - `main-app`
  - `resource-service`
- Roles: 
  - `user`
  - `admin`
  - `manager`
- Test users with various roles

## Features Demonstration

1. **Authentication Flow**: Login/logout process with Keycloak
2. **Authorization**: Role-based access control to different resources
3. **Token Management**: JWT token handling and validation
4. **User Management**: User registration and profile management
5. **Multi-tenancy**: Support for multiple applications sharing authentication

## Project Structure

The project follows DDD principles with a clear separation of concerns:

```
src/main/java/com/example/demo/
├── application/        # Application services and use cases
├── domain/             # Domain model, entities, and business logic
├── infrastructure/     # External services, repositories, security
└── presentation/       # Controllers, DTOs, and API endpoints
```
