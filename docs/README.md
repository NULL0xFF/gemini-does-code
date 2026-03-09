# Ark Resolver

A full-stack web application for coordinating raid schedules, managing group rosters, and organizing parties for MMO games. Users authenticate via Discord OAuth2, create or join groups with invite codes, set availability on interactive heatmap grids, and form raid parties.

## Architecture

```
ark-resolver/
├── frontend/          SvelteKit 2 + Svelte 5 SPA (Tailwind CSS 4, daisyUI 5)
├── backend/           Spring Boot 3 multi-module Gradle project
│   ├── server/        Main application (REST API, JPA, Security)
│   ├── plugin-api/    PF4J extension point interfaces
│   └── sample-plugin/ Reference plugin implementation
└── docs/              Project documentation
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend Framework | SvelteKit 2 + Svelte 5 (runes) |
| Styling | Tailwind CSS 4 + daisyUI 5 |
| Backend Framework | Spring Boot 3.5.11, Java 21 |
| Database | MariaDB (production), H2 (development) |
| ORM | Spring Data JPA / Hibernate 6 |
| Authentication | Discord OAuth2 + custom JWT (HMAC-SHA256) |
| API Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Plugin System | PF4J 3.13 with Spring integration |
| Build Tools | Gradle 9.3 (Kotlin DSL), Vite 7, npm |

## Quick Start

### Prerequisites

- Java 21+
- Node.js 20+
- MariaDB 10.6+ (or use the H2 dev profile)
- Discord application with OAuth2 credentials

### Backend

```bash
cd backend
cp .env.example .env
# Edit .env with your Discord OAuth2 credentials and DB settings

# Development (H2 in-memory database):
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production (MariaDB):
./gradlew bootRun
```

The API server starts on `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/api/docs`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server starts on `http://localhost:5173`.

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | MariaDB host | `localhost` |
| `DB_PORT` | MariaDB port | `3306` |
| `DB_NAME` | Database name | `ark_resolver` |
| `DB_USER` | Database user | `root` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET` | HMAC-SHA256 signing key (min 32 chars) | placeholder |
| `FRONTEND_URL` | Frontend origin for CORS | `http://localhost:5173` |
| `DISCORD_CLIENT_ID` | Discord OAuth2 client ID | (required) |
| `DISCORD_CLIENT_SECRET` | Discord OAuth2 client secret | (required) |
| `DISCORD_REDIRECT_URI` | OAuth2 redirect URI | `${FRONTEND_URL}/login/callback` |
| `VITE_API_URL` | Backend API URL (frontend env) | `http://localhost:8080` |
| `BASE_PATH` | SvelteKit base path for deployment | (empty) |

## Domain Model

```
User ──< GroupMember >── Group
                              │
                              ├──< InviteCode
                              │
                              └──< ScheduleInstance
                                        │
                                        ├──< MemberAvailability
                                        │
                                        └──< Party ──< PartyMember >── User
```

### Roles (per group)

| Role | Permissions |
|------|------------|
| **MANAGER** | Full control: CRUD group, manage members, kick, transfer ownership, create schedules/parties, manage invites |
| **AUDITOR** | Create/edit/delete schedules and parties, generate and revoke invite codes |
| **MEMBER** | View schedules, submit availability, join/leave parties |

## Development Conventions

- **Git Workflow**: `main` (production) / `dev` (active development) / `feat/*` (feature branches). PRs target `dev`.
- **Commit Messages**: Conventional Commits with emoji: `type: :emoji: message`. Never use `@` in commit messages.
- **Code Style**: [Google Style Guides](https://google.github.io/styleguide/).
- **Comments**: JavaDoc on public APIs. No inline update comments.
- **CI**: GitHub Action builds frontend on PRs to `dev`.

## Documentation

- [Backend API Reference](./backend-api.md)
- [Frontend Architecture](./frontend.md)
