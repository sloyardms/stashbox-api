# stashbox-api

> REST API backend for [Stashbox](https://github.com/sloyardms/stashbox) — a self-hosted bookmarking platform.

For running the **full stack** (API + frontend + all services), see the [stashbox](https://github.com/sloyardms/stashbox) repository.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Clone the repository](#1-clone-the-repository)
  - [2. Start the necessary services](#2-start-the-necessary-services)
  - [3. Run the application](#3-run-the-application)
- [Database Migrations](#database-migrations)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Related Repositories](#related-repositories)
- [License](#license)

---

## Overview

**stashbox-api** is the Spring Boot backend for the Stashbox platform. It exposes a REST API consumed by the Next.js frontend, handling all business logic for stash items, groups, tags, notes, and file attachments.

Authentication is delegated to **Keycloak** — the API validates JWT tokens issued by your Keycloak realm and scopes all data to the authenticated user. For standalone development, a local Keycloak instance can be spun up via the included Docker Compose file.

---

## Features

- **Stash Items** — save links, text snippets, or images with optional titles, descriptions, and thumbnails
- **Groups** — organize items into named groups with per-group constraints (e.g. unique titles, unique URLs)
- **Tags** — user-scoped tags with usage tracking per group and fuzzy autocomplete search
- **Notes** — annotate any stash item with rich notes, file attachments, and ordering
- **File Uploads** — attach files to notes.
- **Soft Delete / Trash Bin** — deleted items are recoverable from a trash bin before permanent removal
- **URL Filters** — user-defined regex filters that auto-extract titles from URLs when saving links
- **Full-text Search** — search stash items across title, URL, and description using PostgreSQL tsvector
- **Tag Autocomplete** — trigram-based fuzzy search on tag names with item count context per group
- **Pagination** — all list endpoints are paginated via Spring Data

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4 |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | PostgreSQL 18 |
| Migrations | Flyway |
| Auth | Keycloak (OAuth2 / JWT) |
| Image Processing | imgproxy |
| Image Cache | Nginx |
| Containerization | Docker + Docker Compose |
| Build | Maven |

---

## Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- [Java 25](https://openjdk.org/)
- [Maven](https://maven.apache.org/) (or use the included `mvnw` wrapper)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (recommended)

---

## Getting Started

These steps cover running **stashbox-api standalone** for backend development. The Docker Compose here starts only the services the API depends on — PostgreSQL, Keycloak, imgproxy and Nginx.

### 1. Clone the repository

```bash
git clone https://github.com/your-username/stashbox-api.git
cd stashbox-api
```

You can fill in your values in the .env file or leave it as is with the default ones:

| Variable | Default | Description |
|---|---|---|
| `STASHBOX_PORT` | `9001` | Port the API listens on |
| `STASHBOX_DB_URL` | `jdbc:postgresql://localhost:5432/stashboxdb` | PostgreSQL JDBC URL |
| `STASHBOX_DB_USER` | `stashboxdbuser` | Database username |
| `STASHBOX_DB_PASSWORD` | `stashboxdbpassword` | Database password |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8085/realms/stashbox` | Keycloak realm issuer URI |

### 2. Start the necessary services

To start the necessary services for stashbox-api to work you need to run the docker-compose.dev.yaml file inside the /docker folder, this will pick up the values inside the .env file.

```bash
docker compose -f docker/docker-compose.dev.yaml up -d
```

### 3. Run the application

From IntelliJ IDEA, run `StashboxApiApplication` with the `dev` profile active. If you want to use the values from the .env file inside /docker folder, you need to change the run configuration to set the environment variables to point to the .env file.

The API will be available at `http://localhost:9001`.
Health check: `http://localhost:9001/actuator/health`

---

## Database Migrations

Migrations are managed by **Flyway** and run automatically on startup. Migration files are located in:

```
src/main/resources/db/migration/
```

| File | Description |
|---|---|
| `V1__init_schema.sql` | Initial schema — all core tables |
| `V2__add_fulltext_search.sql` | Full-text search vectors and trigram indexes |

No manual steps required — Flyway applies pending migrations in version order on every startup.

---

## Testing

### Automated Tests

Integration tests use [Testcontainers](https://testcontainers.com/) to spin up PostgreSQL and Keycloak instances, and [REST Assured](https://rest-assured.io/) for API-level assertions. No external services or Docker Compose setup needed — Testcontainers manages everything automatically.

---

## Related Repositories

| Repo                                                      | Description                            |
|-----------------------------------------------------------|----------------------------------------|
| [stashbox](https://github.com/sloyardms/stashbox)         | Root repo — full stack Docker Compose  |
| [stashbox-web](https://github.com/sloyardms/stashbox-web) | Next.js frontend                       |

---

## License

This project is licensed under the AGPL v3 License. See [LICENSE](LICENSE) for details.