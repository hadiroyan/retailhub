# API Endpoints

This document lists the available REST API endpoints in RetailHub.

Base URL:
http://localhost:8080

---

## Authentication

| Method | Endpoint                      | Description                      |
| ------ | ----------------------------- | -------------------------------- |
| POST   | `/api/auth/login`             | User login                       |
| POST   | `/api/auth/register-customer` | Register new customer            |
| POST   | `/api/auth/register-owner`    | Register new store owner         |
| GET    | `/api/auth/me`                | Get currently authenticated user |
| POST   | `/api/auth/logout`            | Logout current user              |

---

More endpoints will be added as new features are implemented.
