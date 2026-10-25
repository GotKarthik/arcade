# Arcade

A browser-based gaming platform built with **Java 21 / Spring Boot** on the backend and **React / Vite** on the frontend, backed by **PostgreSQL**.

## Quick Start

### Prerequisites

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 8+ |
| PostgreSQL | 14+ |

### 1. Database setup

```bash
psql -U postgres -c "CREATE USER arcade_user WITH PASSWORD 'arcade_pass';"
psql -U postgres -c "CREATE DATABASE arcade OWNER arcade_user;"
```

### 2. Backend

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run
# → http://localhost:8080
# → Swagger UI: http://localhost:8080/swagger-ui.html
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

## Running Tests

```bash
# Backend (unit + integration)
cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test

# Frontend
cd frontend && npm test
```

## Project Structure

```
arcade/
├── backend/           Java 21 / Spring Boot 3 / Maven
│   └── src/main/java/com/arcade/backend/
│       ├── controller/     HTTP layer
│       ├── service/        Game logic
│       ├── model/          Domain objects
│       ├── repository/     JPA repositories
│       ├── dto/            API contracts
│       ├── config/         CORS, OpenAPI
│       └── exception/      Error handling
├── frontend/          React 18 / Vite 5
│   └── src/
│       ├── api/            Backend API client
│       ├── components/     Shared UI components
│       ├── hooks/          Game state hooks
│       └── pages/          Route-level pages
└── docs/              Full documentation
```

## Documentation

| Doc | Content |
|---|---|
| [REQUIREMENTS.md](docs/REQUIREMENTS.md) | Full product requirements |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design and decisions |
| [API.md](docs/API.md) | REST API reference |
| [DATABASE.md](docs/DATABASE.md) | Schema and migrations |
| [TESTING.md](docs/TESTING.md) | Test strategy and results |
| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | Deployment guide |
| [SECURITY.md](docs/SECURITY.md) | Security considerations |
| [DEVELOPMENT_LOG.md](docs/DEVELOPMENT_LOG.md) | Build history |

## API Endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | Health check |
| GET | `/api/games` | List games |
| POST | `/api/games/number-detective/sessions` | Start session |
| GET | `/api/games/number-detective/sessions/{id}` | Session status |
| POST | `/api/games/number-detective/sessions/{id}/guesses` | Submit guess |
| POST | `/api/games/number-detective/sessions/{id}/restart` | New round |
| GET | `/api/leaderboard?game=number-detective` | Leaderboard |

## Known Limitations

- Active game sessions are in-memory only and do not survive backend restarts.
- No user accounts in this release — all play is anonymous.
- A single backend instance is assumed (no distributed session support).
