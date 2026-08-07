# Database

This document describes the database structure and migration strategy used in RetailHub.

---

## Database Overview

RetailHub uses PostgreSQL as the primary database.

The schema is divided into three main areas:

### 1. Authentication & Authorization

| Table                       | Description                                                                    |
| --------------------------- | ------------------------------------------------------------------------------ |
| `users`                     | Application user accounts                                                      |
| `roles`                     | System roles (`SUPER_ADMIN`, `OWNER`, `ADMIN`, `MANAGER`, `STAFF`, `CUSTOMER`) |
| `privileges`                | Fine-grained permissions                                                       |
| `role_privileges`           | Role-privilege assignments                                                     |
| `user_roles`                | User-role assignments (supports multi-store access)                            |
| `email_verification_tokens` | OTP tokens for email verification (6-digit, 10-min expiry)                     |

---

### 2. Business Domain

| Table                  | Description                                   |
| ---------------------- | --------------------------------------------- |
| `stores`               | Store locations                               |
| `categories`           | Product categories                            |
| `products`             | Product catalog (includes `image_urls` JSONB) |
| `suppliers`            | Supplier data                                 |
| `purchase_orders`      | Purchase order transactions                   |
| `purchase_order_items` | Line items per purchase order                 |
| `sales_orders`         | Sales order transactions                      |
| `sales_order_items`    | Line items per sales order                    |

---

### 3. Legacy / Unused

| Table       | Status                                                                                                                                                                                                                                                                                                                                            |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `customers` | Created in `V4` migration, store-scoped customer records. **Not used** by the current application flow — customer identity is now handled via `users` + `user_roles` (role `CUSTOMER`). Table still exists in the database but is empty and not referenced by the codebase. Kept for schema history; candidate for removal in a future migration. |

---

## Database Migrations

This project uses **Flyway** for version-controlled database migrations.

Migrations are located in: `backend/src/main/resources/db/migration`  
They run automatically when the application starts.

### Migration Files

| Version | File                                                         | Description                                                                                               |
| ------- | ------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------- |
| V1      | `create_auth_tables.sql`                                     | Authentication schema                                                                                     |
| V2      | `seed_auth_data.sql`                                         | Roles & privileges seed                                                                                   |
| V3      | `seed_test_data.sql`                                         | Test users (dev/test only)                                                                                |
| V4      | `create_retailhub_table.sql`                                 | Business tables (stores, categories, products, sales_orders, purchase_orders, suppliers, customers, etc.) |
| V5      | `alter_users_products_table.sql`                             | Add phone/address to users; imageUrls JSONB to products                                                   |
| V6      | `alter_sales_orders_tables.sql`                              | Add phone/recipient_name to sales_orders                                                                  |
| V7      | `remove_order_date_and_make_exptected_delivery_nullable.sql` | Remove order_date from purchase_orders (redundant with created_at)                                        |
| V8      | `email_verification_tokens.sql`                              | Add email_verification_tokens table                                                                       |

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
