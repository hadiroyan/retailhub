# Features

This document describes the current development status of RetailHub.

---

## Completed

### 1. Authentication System

- JWT-based authentication (cookie-based)
- Customer and Owner registration
- Cookie-based session handling
- Role-Based Access Control (RBAC)
- Privilege-based authorization

### 2. Email Verification

- OTP-based email verification (6-digit code, 10-minute expiry)
- OTP sent automatically on registration (customer & owner) and on employee creation
- Resend OTP with rate limiting (max 5 requests/hour)
- Unverified users are still allowed to log in (not blocked)
- Inline verification form on the Profile page with a warning banner for unverified accounts
- Email delivery via Quarkus Mailer + Mailpit (dev) / SMTP (prod)

### 3. Database Architecture

- Multi-store data isolation
- User and role management
- Database migrations using Flyway
- Development seed data

### 4. Security

- Password hashing with BCrypt
- JWT signed using RSA-256
- HttpOnly cookies (XSS protection)
- SameSite=Strict (basic CSRF protection)

### 5. Store Management

- Create, update, delete stores
- Store-scoped data access
- Owner-store relationship
- Store status management (e.g. suspend by Super Admin)

### 6. Product Management

- Product CRUD
- Category management
- Stock tracking
- Multi-image upload (up to 5 images per product, JPEG/PNG/WEBP, max 5MB)
- First image used as thumbnail on explore/store detail pages

### 7. Employee Management

- Add, update, and remove employees per store
- Role assignment scoped to store (Admin, Manager, Staff)

### 8. Sales & Orders

- Customer checkout and order placement
- Order status flow: `PENDING → PROCESSING → SHIPPED → DELIVERED`
- Order cancellation from `PENDING` or `PROCESSING` (with automatic stock return)
- Order tracking for customers
- Store-side order management and status updates

### 9. Suppliers & Purchases

- Supplier CRUD per store
- Purchase order creation and management
- Purchase order status flow: `PENDING → CONFIRMED → RECEIVED` (with automatic stock addition on receipt)

---

## Planned

### High Priority

- **Email Notifications** — order confirmation on checkout, order status updates to customers, PO received notification to store managers
- **Analytics Dashboard** — revenue by period, top-selling products, order statistics

### Medium Priority

- **Filter by Date Range** — for store order list and purchase order list (monthly/quarterly audit use cases)
- **Export Reports** — CSV/PDF export for sales orders and purchase orders
- **Customer Reviews** — product rating and review after order is delivered

### Low Priority / Nice to Have

- **Infinite Scroll** 
- **Real-time Notifications** 
- **Product Image Lightbox/Zoom** — enhance multi-image product detail view
- **Forgot Password via SMS** — alternative OTP delivery via WhatsApp/SMS

---
 