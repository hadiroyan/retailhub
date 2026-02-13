# Database

This document describes the database structure and migration strategy used in RetailHub.

---

## 🗄 Database Overview

RetailHub uses PostgreSQL as the primary database.

The schema is divided into two main areas:

### 1. Authentication & Authorization

| Table        | Description                                         |
| ------------ | --------------------------------------------------- |
| `users`      | Application user accounts                           |
| `roles`      | System roles (e.g., OWNER, ADMIN, CUSTOMER)         |
| `privileges` | Fine-grained permissions                            |
| `user_roles` | User-role assignments (supports multi-store access) |

---

### 2. Business Domain

| Table             | Description           |
| ----------------- | --------------------- |
| `stores`          | Store locations       |
| `categories`      | Product categories    |
| `products`        | Product catalog       |
| `suppliers`       | Supplier data         |
| `purchase_orders` | Purchase transactions |
| `customers`       | Customer records      |
| `sales_orders`    | Sales transactions    |

---

## Database Migrations

This project uses **Flyway** for version-controlled database migrations.

Migrations are located in: `backend/src/main/resources/db/migration`  
They run automatically when the application starts.

### Migration Files

```
V1__create_auth_tables.sql      - Authentication schema
V2__seed_auth_data.sql          - Roles & privileges seed
V3__seed_test_data.sql          - Test users (dev/test only)
V4__create_retailhub_tables.sql - Business tables
```

Each migration is incremental and should never be modified after being committed.

---

## Development Seed Data

For development purposes, test users are inserted via migration.

Default password for all test users: `password123`

| Email                    | Role        | Enabled |
| ------------------------ | ----------- | ------- |
| test.superadmin@test.com | SUPER_ADMIN | Yes     |
| test.owner@test.com      | OWNER       | Yes     |
| test.customer@test.com   | CUSTOMER    | Yes     |
| test.disabled@test.com   | CUSTOMER    | No      |

⚠️ These accounts are for development only.  
Do not use default credentials in production.
