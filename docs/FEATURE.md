# Features

This document describes the current development status of RetailHub.

---

## Completed

### 1. Authentication System

- JWT-based authentication
- Customer and Owner registration
- Cookie-based session handling
- Role-Based Access Control (RBAC)
- Privilege-based authorization

### 2. Database Architecture

- Multi-store data isolation
- User and role management
- Database migrations using Flyway
- Development seed data

### 3. Security

- Password hashing with BCrypt
- JWT signed using RSA-256
- HttpOnly cookies (XSS protection)
- SameSite=Strict (basic CSRF protection)

---

## In Progress

### Store Management

- Create, update, delete stores
- Store-scoped data access
- Owner-store relationship

### Product Management

- Product CRUD
- Category management
- Stock tracking

---

## Planned

- Employee Management
- Sales & Orders
- Suppliers & Purchases
- Reports & Analytics
- Vue.js Frontend
