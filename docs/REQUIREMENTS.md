# Arcade Requirements

## Functional Requirements

### Game Catalog
- FR-1.1: System shall provide a catalog of playable games accessible at `GET /api/games`.
- FR-1.2: System shall launch with Number Detective as the flagship launch title.

### Number Detective Game Mechanics
- FR-2.1: The server shall generate a cryptographically secure random integer between 0 and 100 inclusive for each session.
- FR-2.2: The server shall strictly hide the target number from the client while the game session is `IN_PROGRESS`.
- FR-2.3: Each session shall allow a maximum of 7 guess attempts.
- FR-2.4: For each guess submitted:
  - If guess < secret: return `TOO_LOW` and decrement remaining attempts.
  - If guess > secret: return `TOO_HIGH` and decrement remaining attempts.
  - If guess == secret: return `CORRECT`, set session status to `WON`, reveal secret number, and persist game result.
  - If attempts reach 7 without correct guess: set status to `LOST`, reveal secret number, and persist game result.
- FR-2.5: Game sessions shall support restarting via `POST /api/games/number-detective/sessions/{id}/restart`.

### Leaderboard & Persistence
- FR-3.1: Only completed winning games shall be ranked on the leaderboard.
- FR-3.2: Leaderboard rankings shall sort primarily by fewest guesses used (ascending) and secondarily by completion timestamp (descending).
- FR-3.3: Historical game records must persist across backend application restarts using PostgreSQL.

## Non-Functional Requirements

### Performance & Latency
- NFR-1.1: REST API response times for guess evaluation shall not exceed 100ms under standard operational conditions.
- NFR-1.2: Frontend bundle size shall remain under 200KB gzipped.

### Security
- NFR-2.1: Server-authoritative logic: client-side tampering cannot bypass attempt limits or force a win state.
- NFR-2.2: CORS policy must restrict cross-origin requests to configured frontend origins.
- NFR-2.3: SQL queries must use parameterized JPA repository methods to eliminate SQL injection vulnerabilities.
- NFR-2.4: Database credentials and external secrets must be injected strictly via environment variables.

### Reliability & Availability
- NFR-3.1: Health check endpoint `GET /api/health` shall report service readiness.
- NFR-3.2: Input validation must reject non-numeric inputs and numbers outside the [0, 100] range with informative HTTP 400 responses.
