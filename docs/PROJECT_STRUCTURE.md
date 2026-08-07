# Project Structure

Below is the overall structure of the RetailHub project.

```
retailhub/
├── backend/               # Quarkus backend application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java source code
│   │   │   └── resources/ # Config & migrations
│   │   └── test/          # Unit & integration tests
│   ├── keys/              # JWT RSA keys (gitignored)
│   ├── pom.xml            # Maven dependencies
│   └── README.md          # Backend documentation
│
├── frontend/             # Vue.js frontend application
│   ├── src/
│   ├── package.json
│   └── README.md
│
├── docs/                 # Project documentation
│   ├── API_ENDPOINTS.md
│   ├── DATABASE.md
│   ├── FEATURES.md
│   ├── PROJECT_STRUCTURE.md
│   └── TECH_STACK.md
│
└── README.md             # This file
```

---

## Backend Overview

- Built with Quarkus
- Exposes REST API
- Handles authentication and authorization
- Connects to PostgreSQL database

### Backend Package Structure

Source code is organized under `org.hadiroyan.retailhub`:

```
org.hadiroyan.retailhub
├── converter/      # e.g. JsonbListConverter
├── dto/
│   ├── request/
│   └── response/
├── exception/
├── mapper/
├── model/
├── repository/
├── resource/       # REST endpoints (JAX-RS resources)
├── service/        # Business logic
└── utils/          # e.g. CurrentUserUtil, CookieUtil
```

---

## Frontend Overview

- Built with Vue.js 3 and Vite
- Consumes the Quarkus REST API
- Uses Pinia for state management
- Uses Vue Router for client-side routing

### Frontend Source Structure

```
src/
├── assets/
│   ├── icons/
│   └── images/
├── components/
│   ├── common/
│   ├── forms/
│   ├── layout/
│   └── modals/
├── composables/
├── layouts/          # DashboardLayout, CustomerLayout, DefaultLayout
├── router/
├── services/         # api.js, authService.js, storeService.js, orderService.js, supplierService.js
├── stores/           # auth.js, storeStore.js, cartStore.js, orderStore.js, supplierStore.js
├── utils/            # constants.js, helper.js
└── views/
    ├── auth/
    ├── customer/
    ├── dashboard/
    ├── profile/
    └── stores/
```
