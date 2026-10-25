# Testing

## Strategy

All testing is automated. Tests must pass before any deployment.

## Backend Tests

Framework: JUnit 5, Mockito, Spring Boot Test  
Database: H2 in-memory for integration tests (Flyway disabled, Hibernate creates schema)

### Unit Tests: `NumberDetectiveServiceTest`

Uses a deterministic `IntSupplier` (always returns 42) for reproducible game behavior.

| Test | Verifies |
|---|---|
| `createSession_returnsInProgressSession` | Session status, ID, attempt counts |
| `createSession_secretInRange` | Secret number 0–100 across 200 random samples |
| `sessionResponse_hidesSecretWhileInProgress` | Null secret in response |
| `guess_tooLow_returnsTooLow` | Correct feedback for low guess |
| `guess_tooHigh_returnsTooHigh` | Correct feedback for high guess |
| `guess_correct_returnsCorrectAndRevealsSecret` | Win state, secret revealed, DB persisted |
| `guess_incrementsAttemptCount` | Attempt counter increments correctly |
| `guess_exhaustedAttempts_losesGame` | LOST after 7 wrong guesses, DB persisted |
| `guess_alreadyFinished_throwsException` | 409 on post-game guess |
| `sessionResponse_revealsSecretAfterEnd` | Secret revealed in session view after game ends |
| `getSession_unknownId_throwsNotFound` | 404 for missing session |
| `sessions_areIsolated` | Independent sessions don't share state |
| `restartSession_createsFreshSession` | New session ID, zero guesses, old session gone |

### Integration Tests: `ArcadeControllerIntegrationTest`

Full Spring Boot context with H2 database. Tests actual HTTP responses.

| Test | Verifies |
|---|---|
| `health_returnsUp` | GET /api/health → 200 |
| `listGames_returnsCatalog` | GET /api/games → game list |
| `createSession_returns201WithSession` | POST /sessions → 201, secret null |
| `fullGameFlow_win` | Start + guess + session check |
| `guess_outOfRange_returns400` | Invalid guess → 400 |
| `getSession_unknownId_returns404` | Unknown session → 404 |
| `guess_finishedSession_returns409` | Post-game guess → 409 |
| `leaderboard_returnsList` | GET /leaderboard → 200 array |
| `restart_createsNewSession` | POST /restart → new session ID |

### Running Backend Tests

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test
```

## Frontend Tests

Framework: Vitest + React Testing Library + jsdom

All API calls are mocked. Tests do not require a running backend.

| Test Group | Tests |
|---|---|
| HomePage | renders hero section, shows game cards from API, handles API failure |
| NumberDetectivePage — start | instructions shown, start button exists, game starts, guess submitted, remaining attempts, rejects invalid input, rejects out-of-range |
| NumberDetectivePage — win | win state shown, restart button shown |
| NumberDetectivePage — error | network error displayed |

### Running Frontend Tests

```bash
cd frontend
npm test
```

## Test Results (verified)

| Suite | Tests | Pass | Fail |
|---|---|---|---|
| Backend unit (Service) | 13 | 13 | 0 |
| Backend integration (Controller) | 9 | 9 | 0 |
| Frontend (Vitest/RTL) | 13 | 13 | 0 |
| **Total** | **35** | **35** | **0** |

_Last run: 2026-10-25_
