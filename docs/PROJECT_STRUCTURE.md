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
├── frontend/             # Vue.js frontend (planned)
│   ├── src/
│   ├── package.json
│   └── README.md
│
├── docs/                 # Project documentation
│   ├── API_ENDPOINTS.md
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

## Frontend Overview

- Planned Vue.js application
- Will consume backend REST API
