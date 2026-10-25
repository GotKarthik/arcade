# API Reference

Base URL: `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/api-docs`

---

## Catalog & Health

### `GET /api/health`
Returns service readiness status.
```json
{ "service": "arcade-backend", "status": "UP" }
```

### `GET /api/games`
Lists all available arcade games.
```json
[
  { "id": "number-detective", "name": "Number Detective", "path": "/games/number-detective", "icon": "🔢" },
  { "id": "rock-paper-scissors", "name": "Rock Paper Scissors", "path": "/games/rock-paper-scissors", "icon": "✂️" },
  { "id": "tic-tac-toe", "name": "Tic-Tac-Toe", "path": "/games/tic-tac-toe", "icon": "❌" },
  { "id": "memory-match", "name": "Memory Match", "path": "/games/memory-match", "icon": "🃏" }
]
```

---

## 1. Number Detective

- `POST /api/games/number-detective/sessions` → Creates a fresh game session (HTTP 201). Secret is masked as `null`.
- `GET /api/games/number-detective/sessions/{id}` → Returns current status and remaining attempts.
- `POST /api/games/number-detective/sessions/{id}/guesses` → Submits integer guess `0 <= guess <= 100`. Returns `TOO_LOW`, `TOO_HIGH`, or `CORRECT`.
- `POST /api/games/number-detective/sessions/{id}/restart` → Creates fresh session.
- `GET /api/leaderboard?game=number-detective` → Returns top winning players ranked by fewest guesses.

---

## 2. Rock Paper Scissors

- `POST /api/games/rock-paper-scissors/sessions` → Creates 5-round match. Optional `{ "playerName": "Alice" }`.
- `GET /api/games/rock-paper-scissors/sessions/{id}` → Retrieves current scores, rounds remaining, and round history.
- `POST /api/games/rock-paper-scissors/sessions/{id}/moves` → Submits player move `{ "move": "ROCK" }`. Valid choices: `ROCK`, `PAPER`, `SCISSORS`. Returns round evaluation (`WIN`, `LOSS`, `DRAW`), updated score, and match finish flag.
- `POST /api/games/rock-paper-scissors/sessions/{id}/restart` → Starts new match.
- `GET /api/games/rock-paper-scissors/history` → Retrieves recent completed match records.

---

## 3. Tic-Tac-Toe

- `POST /api/games/tic-tac-toe/sessions` → Creates fresh 3x3 board. Player X starts first.
- `GET /api/games/tic-tac-toe/sessions/{id}` → Retrieves 9-cell board array, current turn (`X` or `O`), outcome, and winning line.
- `POST /api/games/tic-tac-toe/sessions/{id}/moves` → Submits move `{ "position": 4, "player": "X" }`. Position must be 0–8 and unoccupied. Detects horizontal, vertical, and diagonal wins or draw.
- `POST /api/games/tic-tac-toe/sessions/{id}/restart` → Resets board.
- `GET /api/games/tic-tac-toe/history` → Retrieves recent matches.

---

## 4. Memory Match

- `POST /api/games/memory-match/sessions` → Creates 4x4 card grid (16 cards, 8 pairs). All cards face down with symbols masked (`null`).
- `GET /api/games/memory-match/sessions/{id}` → Returns public view of grid and moves count.
- `POST /api/games/memory-match/sessions/{id}/flips` → Flips card `{ "position": 0 }`. If first card, reveals symbol and awaits second card. If second card, checks pair match; if matched, locks cards in face-up state; if mismatched, marks turn as `PAIR_MISMATCHED` (auto-resets on next turn).
- `POST /api/games/memory-match/sessions/{id}/restart` → Generates fresh shuffled deck.
- `GET /api/games/memory-match/leaderboard` → Returns ranked completions sorted by fewest moves then fastest duration.
