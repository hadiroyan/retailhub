# RetailHub

> **Multi-Store Retail Management Platform** - Manage stores, inventory, and users in one centralized system.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-blue.svg)](https://quarkus.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.x-red.svg)](https://maven.apache.org/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-green.svg)](https://vuejs.org/)

---

## Overview

RetailHub is a retail management system designed to support multiple store locations within a single platform.

It provides:

- Multi-store data isolation
- JWT-based authentication
- Role-Based Access Control (RBAC)
- Store owner and customer management
- Database migrations with versioning

This project is built as a modern RESTful API using Java and Quarkus, and the frontend is developed using Vue.js and Vite.

---

## Features

- **Multi-store management** — owners can create and manage multiple stores under one account
- **Role-based access control** — `SUPER_ADMIN`, `OWNER`, `ADMIN`, `MANAGER`, `STAFF`, `CUSTOMER` with granular permissions per role
- **Product catalog** — categories, products, multi-image upload (up to 5 images per product)
- **Sales order flow** — customer checkout, order tracking, status management (`PENDING → PROCESSING → SHIPPED → DELIVERED`)
- **Purchase order flow** — supplier management and PO tracking (`PENDING → CONFIRMED → RECEIVED`)
- **Employee management** — store owners/admins can add and manage staff per store
- **Email verification** — OTP-based email verification with rate limiting
- **JWT cookie authentication** — secure httpOnly cookie-based auth with RSA-256 signing

---

## Screenshots

> _Screenshots coming soon._

---

## Live Demo

> _Not deployed yet. Link will be added here once available._

---

## Tech Stack

**Backend**

- Java 21
- Quarkus
- PostgreSQL
- Hibernate ORM (Panache)
- SmallRye JWT
- Flyway
- Maven

**Frontend**

- Vue.js 3
- Vite
- Pinia
- Vue Router
- Tailwind CSS
- Axios

For detailed information, see:
[Tech Stack Documentation](docs/TECH_STACK.md)

---

## Architecture Summary

- REST API architecture
- Vue.js Single Page Application (SPA)
- Multi-tenant store isolation (store-based data separation)
- JWT authentication (RSA-256)
- Role and privilege-based authorization
- Automatic database migration on startup

---

## Quick Start

### 1. Prerequisites

Make sure you have installed:

- Java 21+
- PostgreSQL
- Maven
- Node.js 20+
- npm

---

## Backend

### **1. Clone the Repository**

```bash
git clone https://github.com/hadiroyan/retailhub.git
cd retailhub
```

### **2. Setup Database**

```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE retailhub_db;
\q

# Create PostgreSQL database for testing
psql -U postgres
CREATE DATABASE retailhub_test_db;
\q
```

### **3. Configure Environment**

```bash
# Copy example env file
cd backend
cp .env.example .env

# Edit .env with your settings
# DB_USERNAME=postgres
# DB_PASSWORD=your_password
# JWT_ISSUER=RetailHub
```

### **4. Generate JWT Keys**

JWT uses RSA-256 asymmetric encryption.

Generate keys by following:
[JWT Key Generation Guide](backend/keys/README.md)

### **5. Start Backend**

```bash
cd backend
./mvnw quarkus:dev
```

Backend runs at:  
**http://localhost:8080**

Swagger UI (API Documentation):  
**http://localhost:8080/swagger-ui**

---

## Frontend

### 1. Configure Environment

```bash
cd frontend
cp .env.example .env
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Run Development Server

```bash
npm run dev
```

Frontend runs at:  
**http://localhost:5173**

---

## Documentation

Additional documentation is available in the `docs` directory:

- [Project Structure](docs/PROJECT_STRUCTURE.md)
- [Tech Stack](docs/TECH_STACK.md)
- [Database](docs/DATABASE.md)
- [Features](docs/FEATURES.md)
- [API Endpoints](docs/API_ENDPOINTS.md)

## Development Notes

- Database migrations run automatically on application startup.
- Default development users are seeded via migration scripts.
- This project is intended for learning, experimentation, and portfolio demonstration.

## Contributing

This project is actively developed as part of my portfolio. Bug reports, suggestions, and feedback are always welcome. Feel free to open an issue.

---
