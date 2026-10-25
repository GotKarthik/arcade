# Arcade

A full-stack browser-based gaming platform built with **Java 21 / Spring Boot 3** on the backend and **React / Vite** on the frontend, backed by **PostgreSQL** with Flyway migrations.

## Available Games

1. **Number Detective** (`/games/number-detective`): Guess the secret number (0–100) in 7 tries. Directional clues (`TOO_LOW`, `TOO_HIGH`) with secret confidentiality and ranked leaderboard.
2. **Rock Paper Scissors** (`/games/rock-paper-scissors`): Single-player 5-round showdown against the server's AI with authoritative scoring and history.
3. **Tic-Tac-Toe** (`/games/tic-tac-toe`): Classic 3x3 turn-based strategy for two players on the same device with server-side win/draw validation.
4. **Memory Match** (`/games/memory-match`): 4x4 card grid featuring 8 pairs. Anti-cheat masking, moves tracking, elapsed-time stopwatch, and hall-of-fame leaderboard.

---

## Quick Start

### Prerequisites

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 8+ |
| PostgreSQL | 14+ |

### 1. Database Setup

```bash
psql -U postgres -c "CREATE USER arcade_user WITH PASSWORD 'arcade_pass';"
psql -U postgres -c "CREATE DATABASE arcade OWNER arcade_user;"
```

### 2. Backend Startup

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn spring-boot:run
# → API: http://localhost:8080/api
# → Swagger UI: http://localhost:8080/swagger-ui.html
# → OpenAPI Spec: http://localhost:8080/api-docs
```

### 3. Frontend Startup

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## Running Automated Tests

```bash
# Backend test suite (66 tests: unit + integration)
cd backend && JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test

# Frontend test suite (22 component tests)
cd frontend && npm test

# Frontend production build verification
cd frontend && npm run build
```

---

## Project Structure

```
arcade/
├── backend/                              Java 21 / Spring Boot 3 / Maven
│   └── src/main/java/com/arcade/backend/
│       ├── controller/                   ArcadeController (catalog + all 4 games)
│       ├── service/                      NumberDetective, RockPaperScissors, TicTacToe, MemoryMatch
│       ├── model/                        Entities (GameResult, RpsMatch, TttMatch, MemoryMatch)
│       ├── repository/                   JPA repositories
│       ├── dto/                          Request / Response records
│       └── exception/                    GlobalExceptionHandler, InvalidMoveException
│   └── src/main/resources/
│       ├── application.yml               Database & CORS configuration
│       └── db/migration/                 Flyway V1 & V2 migrations
├── frontend/                             React 18 / Vite / Vanilla CSS
│   └── src/
│       ├── pages/                        HomePage, NumberDetective, RockPaperScissors, TicTacToe, MemoryMatch
│       ├── components/                   Navbar, Footer
│       ├── api/                          client.js (REST client)
│       ├── styles/                       global.css (Arcade Design System)
│       └── __tests__/                    Vitest & React Testing Library suites
└── docs/                                 Comprehensive architectural & operational docs
    ├── API.md
    ├── ARCHITECTURE.md
    ├── DATABASE.md
    ├── DEPLOYMENT.md
    ├── REQUIREMENTS.md
    ├── SECURITY.md
    ├── TESTING.md
    └── TROUBLESHOOTING.md
```
