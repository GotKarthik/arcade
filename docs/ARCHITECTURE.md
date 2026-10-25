# Architecture

## Overview

Arcade is a modular monolith. The backend is a single Spring Boot 3 application. The frontend is a separate Vite+React SPA. They communicate exclusively via REST/JSON.

```
Browser (React SPA)
    ↓ HTTP/JSON (REST API)
Spring Boot 3 (port 8080)
    ├── Controllers (HTTP layer)
    ├── Services (game rules)
    ├── Repositories (JPA)
    └── PostgreSQL (persistence)
```

## Backend Modules

| Package | Responsibility |
|---|---|
| `controller` | HTTP endpoints, request validation, response shaping |
| `service` | Game rules, session lifecycle, business logic |
| `model` | Domain objects: `NumberDetectiveSession`, `GameStatus`, `GameResult` |
| `repository` | JPA + Flyway: `GameResultRepository` |
| `dto` | API contracts: `SessionResponse`, `GuessRequest/Response`, `LeaderboardEntry` |
| `config` | CORS, OpenAPI/Swagger |
| `exception` | Domain exceptions, `GlobalExceptionHandler` |

## Session Lifecycle

```
POST /sessions → IN_PROGRESS session (in-memory ConcurrentHashMap)
POST /guesses  → TOO_LOW / TOO_HIGH / CORRECT
               → If WON or LOST: persist to game_results table
POST /restart  → New session ID, fresh state
GET  /sessions/{id} → Public view (secret hidden while IN_PROGRESS)
```

## Key Design Decisions

### In-memory active sessions
Active sessions use a `ConcurrentHashMap`. Sessions do not survive backend restarts. This keeps the first release simple. See `docs/decisions/001-in-memory-sessions.md`.

### Secret confidentiality
`SecretNumber` is never included in API responses while the game is `IN_PROGRESS`. It is revealed only in `WON` or `LOST` state. This is enforced in `SessionResponse` and `GuessResponse` DTOs.

### Server-authoritative logic
All game rules (attempt limits, win/loss detection) live in `NumberDetectiveService`. The frontend renders backend-provided state only.

### Flyway migrations
Schema is managed by Flyway. No DDL in application code. `ddl-auto=validate` in production.

### SecureRandom
Session IDs use UUID v4. The secret number uses `java.security.SecureRandom`.

## Frontend Architecture

- **React 18** with functional components and hooks
- **React Router 6** for client-side routing
- **Custom hook** `useNumberDetective`: all game state and API calls
- **API client** `src/api/client.js`: wraps fetch, reads `VITE_API_URL`
- **No Redux**: state is local to the game hook

## Scalability Limitations

- Single backend instance assumed (ConcurrentHashMap is not distributed)
- No authentication in this release
- Leaderboard returns all wins (add pagination before scale)
