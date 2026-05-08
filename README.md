# Booking Microservices Platform

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.3-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![License](https://img.shields.io/badge/license-MIT-black)

Scalable microservices-based booking platform built with Spring Boot, Spring Cloud, Docker, and resilient inter-service communication patterns.

</div>

---

# Overview

This project demonstrates a production-oriented microservices architecture for handling users and booking operations.

The platform includes:

* Service Discovery using Eureka
* API Gateway with centralized routing and API-key validation
* Independent microservices with isolated persistence layers
* Inter-service communication through OpenFeign
* Fault tolerance using Resilience4j
* Docker-first deployment strategy
* H2 databases for local development and testing

The system is structured around independently deployable services and follows common cloud-native backend design principles.

---

# Architecture

```text
                           ┌─────────────────────┐
                           │  Discovery Service  │
                           │      (Eureka)       │
                           └─────────┬───────────┘
                                     │
                    Service Registration & Discovery
                                     │
        ┌────────────────────────────┴────────────────────────────┐
        │                                                         │
┌───────▼────────┐                                    ┌──────────▼─────────┐
│   API Gateway  │                                    │  Users Service     │
│ Spring Gateway │◄────────────Feign Client──────────►│ CRUD + H2 Database │
│ API Key Filter │                                    └────────────────────┘
└───────┬────────┘
        │
        │
        ▼
┌────────────────────┐
│  Bookings Service  │
│ CRUD + Resilience  │
│      + H2 DB       │
└────────────────────┘
```

---

# Tech Stack

## Backend

* Java 21
* Spring Boot 3.3.4
* Spring Cloud 2023.0.3
* Spring Web
* Spring Data JPA
* Spring Cloud Gateway
* Eureka Discovery Server
* OpenFeign
* Resilience4j

## Database

* H2 In-Memory Database

## DevOps

* Docker
* Docker Compose
* Maven

---

# Microservices

| Service           | Port | Responsibility                             |
| ----------------- | ---: | ------------------------------------------ |
| discovery-service | 8761 | Service registry and discovery             |
| api-gateway       | 8085 | Centralized routing and API-key validation |
| users-service     | 8081 | User management and persistence            |
| bookings-service  | 8082 | Booking management and user validation     |

---

# Features

## API Gateway

* Centralized entry point
* Request routing
* API-key authentication filter
* Load-balanced communication
* Hidden internal service topology

## Service Discovery

* Dynamic service registration
* Runtime service lookup
* Decoupled infrastructure
* Cloud-native service resolution

## Users Service

* Create users
* Retrieve users
* H2-backed persistence
* RESTful API design

## Bookings Service

* Create bookings
* Retrieve bookings
* Validate user existence through Feign
* Retry and Circuit Breaker support
* Aggregated booking details endpoint

## Resilience Patterns

Implemented with Resilience4j:

* Retry
* Circuit Breaker
* Fault isolation
* External service failure handling

---

# Project Structure

```text
booking-micro/
│
├── api-gateway/
├── bookings-service/
├── discovery-service/
├── users-service/
│
├── compose.yml
├── pom.xml
└── README.md
```

---

# Running the Project

## Prerequisites

Install:

* Java 21
* Maven 3.9+
* Docker
* Docker Compose

---

## Run with Docker Compose

From the project root:

```bash
docker compose up --build -d
```

View logs:

```bash
docker compose logs -f
```

Stop all services:

```bash
docker compose down -v
```

---

# Service Startup Order

The infrastructure is automatically orchestrated using:

* `depends_on`
* health checks
* container dependency management

Startup sequence:

```text
Discovery Service
      ↓
Users Service
      ↓
Bookings Service
      ↓
API Gateway
```

---

# API Security

All gateway requests require:

```http
X-API-Key: sifra123
```

Example:

```bash
curl http://localhost:8085/api/users \
  -H "X-API-Key: sifra123"
```

---

# API Endpoints

## Users API

### Create User

```http
POST /api/users
```

Request:

```json
{
  "name": "Ana",
  "email": "ana@example.com"
}
```

Example:

```bash
curl -X POST http://localhost:8085/api/users \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sifra123" \
  -d '{
    "name":"Ana",
    "email":"ana@example.com"
  }'
```

---

### Get All Users

```http
GET /api/users
```

Example:

```bash
curl http://localhost:8085/api/users \
  -H "X-API-Key: sifra123"
```

---

## Bookings API

### Create Booking

```http
POST /api/bookings
```

Request:

```json
{
  "userId": 1,
  "startTime": "2026-01-01T10:00:00",
  "endTime": "2026-01-01T11:00:00"
}
```

Example:

```bash
curl -X POST http://localhost:8085/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-API-Key: sifra123" \
  -d '{
    "userId":1,
    "startTime":"2026-01-01T10:00:00",
    "endTime":"2026-01-01T11:00:00"
  }'
```

---

# Health Checks

## Eureka Dashboard

```text
http://localhost:8761
```

Expected registered services:

* api-gateway
* users-service
* bookings-service

---

## Actuator Health Endpoints

### Gateway

```bash
curl http://localhost:8085/actuator/health
```

### Users Service

```bash
curl http://localhost:8081/actuator/health
```

### Bookings Service

```bash
curl http://localhost:8082/actuator/health
```

---

# H2 Console Access

## Users Database

URL:

```text
http://localhost:8081/h2-console
```

Configuration:

```text
JDBC URL: jdbc:h2:mem:usersdb
Username: sa
Password:
```

---

## Bookings Database

URL:

```text
http://localhost:8082/h2-console
```

Configuration:

```text
JDBC URL: jdbc:h2:mem:bookingsdb
Username: sa
Password:
```

---

# Inter-Service Communication

The `bookings-service` communicates with `users-service` using OpenFeign.

Typical flow:

```text
Client Request
      ↓
API Gateway
      ↓
Bookings Service
      ↓
Feign Client
      ↓
Users Service
```

This architecture enables:

* service decoupling
* scalable deployments
* centralized routing
* fault-tolerant communication

---

# Resilience Strategy

The project applies Resilience4j to external service calls.

Implemented strategies:

| Pattern            | Purpose                                  |
| ------------------ | ---------------------------------------- |
| Retry              | Automatically retries transient failures |
| Circuit Breaker    | Prevents cascading failures              |
| Timeout Protection | Avoids long blocking operations          |

Benefits:

* improved stability
* graceful degradation
* increased fault tolerance
* reduced downstream pressure

---

# Dockerized Deployment

Every microservice includes:

* dedicated Dockerfile
* isolated runtime
* containerized deployment
* environment-specific configuration

Deployment is fully reproducible through Docker Compose.

---

# Build the Project Manually

## Maven Build

```bash
mvn clean install
```

---

## Run Individual Services

### Discovery Service

```bash
cd discovery-service
mvn spring-boot:run
```

### Users Service

```bash
cd users-service
mvn spring-boot:run
```

### Bookings Service

```bash
cd bookings-service
mvn spring-boot:run
```

### API Gateway

```bash
cd api-gateway
mvn spring-boot:run
```

---

# Engineering Highlights

## Cloud-Native Concepts

* Service Discovery
* API Gateway Pattern
* Decentralized Persistence
* Fault Tolerance
* Containerized Infrastructure
* Service Isolation

## Production-Oriented Design

* modular architecture
* scalable communication model
* centralized access layer
* independent service lifecycle
* resilient external calls

---

# Future Improvements

Potential production upgrades:

* PostgreSQL or MySQL persistence
* JWT authentication
* Distributed tracing
* Prometheus + Grafana monitoring
* Centralized logging
* Kubernetes deployment
* CI/CD pipelines
* Rate limiting
* OAuth2/OpenID Connect
* Distributed caching

---

# License

MIT License

---

# Author

Built as a Spring Cloud microservices architecture project focused on distributed systems fundamentals, resilient communication patterns, and Docker-based deployment workflows.
