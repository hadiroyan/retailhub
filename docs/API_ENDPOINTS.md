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

## Store

| Method | Endpoint                     | Description                                |
| ------ | ---------------------------- | ------------------------------------------ |
| POST   | `/api/v1/stores`             | Create store (OWNER)                       |
| GET    | `/api/v1/stores`             | List stores (Public/Role-filtered)         |
| GET    | `/api/v1/stores/{slug}`      | Store detail (Public)                      |
| PUT    | `/api/v1/stores/{id}`        | Update store (OWNER of store, SUPER_ADMIN) |
| DELETE | `/api/v1/stores/{id}`        | Delete store (OWNER of store, SUPER_ADMIN) |
| PATCH  | `/api/v1/stores/{id}/status` | Change status (OWNER, SUPER_ADMIN)           |

---

## Category

| Method | Endpoint                                     | Description                             |
| ------ | -------------------------------------------- | --------------------------------------- |
| POST   | `/api/v1/stores/{storeId}/categories`        | Create category (OWNER, ADMIN, MANAGER) |
| GET    | `/api/v1/stores/{storeId}/categories`        | List category (Public)                  |
| GET    | `/api/v1/stores/{storeId}/categories/{slug}` | Category detail by slug (Public)        |
| PUT    | `/api/v1/stores/{storeId}/categories/{id}`   | Update category (OWNER, ADMIN, MANAGER) |
| DELETE | `/api/v1/stores/{storeId}/categories/{id}`   | Delete category (OWNER, ADMIN, MANAGER) |

---

## Product

| Method | Endpoint                                         | Description                                              |
| ------ | ------------------------------------------------ | -------------------------------------------------------- |
| POST   | `/api/v1/stores/{storeId}/products`              | Create product (OWNER, ADMIN, MANAGER)                   |
| GET    | `/api/v1/stores/{storeId}/products`              | List product (Public, filter: name/category/sortByPrice) |
| GET    | `/api/v1/stores/{storeId}/products/{sku}  `      | Product detail by SKU (Public)                           |
| GET    | `/api/v1/stores/{storeId}/products/{sku}/detail` | Product detail by SKU (INTERNAL: OWNER, ADMIN, MANAGER)  |
| PUT    | `/api/v1/stores/{storeId}/products/{id} `        | Update product (OWNER, ADMIN, MANAGER)                   |
| DELETE | `/api/v1/stores/{storeId}/products/{id}`         | Delete product (OWNER, ADMIN, MANAGER)                   |

---

### More endpoints will be added as new features are implemented.
