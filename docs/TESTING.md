# Testing Guide

Arcade maintains extensive automated test suites across both the backend and frontend.

---

## Test Execution Summary

| Suite | Component | Tests | Status |
|---|---|---|---|
| Backend Integration | `ArcadeControllerIntegrationTest` | 9 | PASS |
| Backend Integration | `NewGamesIntegrationTest` | 5 | PASS |
| Backend Unit | `NumberDetectiveServiceTest` | 13 | PASS |
| Backend Unit | `RockPaperScissorsServiceTest` | 13 | PASS |
| Backend Unit | `TicTacToeServiceTest` | 13 | PASS |
| Backend Unit | `MemoryMatchServiceTest` | 13 | PASS |
| Frontend Component | `App.test.jsx` | 13 | PASS |
| Frontend Component | `NewGames.test.jsx` | 9 | PASS |
| **Total** | | **88 Tests** | **100% PASS** |

---

## Running the Tests

### Backend Suite
```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test
```

### Frontend Suite
```bash
cd frontend
npm test
```

### Frontend Production Build
```bash
cd frontend
npm run build
```
