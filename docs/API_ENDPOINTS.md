# API Endpoints

This document lists the available REST API endpoints in RetailHub.

Base URL:
[http://localhost:8080](http://localhost:8080)

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

## Store _(planned)_


| Method | Endpoint                     | Description                                |
| ------ | ---------------------------- | ------------------------------------------ |
| POST   | `/api/v1/stores`             | Create store (OWNER)                       |
| GET    | `/api/v1/stores`             | List stores (Public/Role-filtered)         |
| GET    | `/api/v1/stores/{slug}`      | Store detail (Public)                      |
| PUT    | `/api/v1/stores/{id}`        | Update store (OWNER of store, SUPER_ADMIN) |
| DELETE | `/api/v1/stores/{id}`        | Delete store (OWNER of store, SUPER_ADMIN) |
| PATCH  | `/api/v1/stores/{id}/status` | Change status (SUPER_ADMIN only)           |


---

### More endpoints will be added as new features are implemented.

