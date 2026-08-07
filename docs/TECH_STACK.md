# Tech Stack

This project uses modern and production-ready technologies.

---

## Backend

| Technology              | Description                                     |
| ----------------------- | ----------------------------------------------- |
| Java 21                 | Programming language                            |
| Quarkus                 | Java framework for building REST APIs           |
| RESTEasy Reactive       | REST endpoint implementation                    |
| PostgreSQL              | Relational database                             |
| Hibernate ORM (Panache) | Object-relational mapping                       |
| SmallRye JWT            | JWT authentication (RSA-256 signed)             |
| Flyway                  | Database migration tool                         |
| BCrypt                  | Password hashing                                |
| Quarkus Mailer          | Email sending (used for OTP verification, etc.) |
| Maven                   | Build and dependency management                 |

## Development Tools

| Technology | Description                                                                                                                               |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| Mailpit    | Local email testing and SMTP capture, auto-started via **Quarkus Dev Services** in dev mode                                               |
| Docker     | Required in the background for Quarkus Dev Services (e.g. to run the Mailpit container); no manual `docker run` needed during development |

---

## Frontend

| Technology   | Description                 |
| ------------ | --------------------------- |
| Vue.js 3     | Frontend framework          |
| Vite         | Development build tool      |
| Pinia        | State management            |
| Vue Router   | Routing                     |
| Axios        | HTTP client                 |
| Tailwind CSS | Utility-first CSS framework |
