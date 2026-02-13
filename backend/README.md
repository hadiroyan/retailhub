# RetailHub Backend

RESTful API built with Quarkus for multi-store retail management.

---

## Overview

The backend provides:

- JWT authentication (RSA-256)
- Role-Based Access Control (RBAC)
- Store-level data isolation (multi-store support)
- RESTful API with Swagger documentation
- PostgreSQL integration with Flyway migrations

---

## Tech Stack

- Java 21
- Quarkus
- PostgreSQL
- Hibernate ORM (Panache)
- SmallRye JWT
- Flyway
- Maven

---

## Prerequisites

Make sure you have installed:

- Java 21+
- PostgreSQL
- Maven (or use Maven Wrapper)

Verify installation:

```bash
java -version
psql --version

```

## **Quick Start**

### **1. Database Setup**

```bash
psql -U postgres
CREATE DATABASE retailhub_db;
\q
```

### **2. Environment Configuration**

```bash
# Copy example env file
cp .env.example .env
```

Update database and JWT configuration inside .env.

**Example .env:**

```env
# Database Configuration
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
DB_HOST=your-db-host
DB_PORT=your-db-port
DB_NAME=your-db-name

# JWT Configuration
JWT_ISSUER=https://your-domain.com
JWT_PUBLIC_KEY=path/to/publicKey.pem
JWT_PRIVATE_KEY=path/to/privateKey.pem
```

### **3. Generate JWT Keys**

Generate keys by following:
[JWT Key Generation Guide](keys/README.md)

### **4. Start Application**

```bash
# Development mode
./mvnw quarkus:dev
```

**Application URLs:**

- API Base: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui

---

## Authentication Model

RetailHub uses JWT stored in HttpOnly cookies.

- Roles
- SUPER_ADMIN
- OWNER
- ADMIN
- MANAGER
- STAFF
- CUSTOMER

Authorization is enforced using role and privilege-based checks.

## Database

- PostgreSQL database
- Flyway versioned migrations
- Migrations run automatically on startup

Migration files are located in: `src/main/resources/db/migration/`
