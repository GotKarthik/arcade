# API Reference

Base URL: `http://localhost:8080` (local) | `$BACKEND_URL` (production)

Swagger UI: `$BASE_URL/swagger-ui.html`

## Common Response Formats

### Error Response

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Session not found: abc-123"
}
```

HTTP status codes used:
- `200 OK` — success
- `201 Created` — new session created
- `400 Bad Request` — validation failure
- `404 Not Found` — session not found
- `409 Conflict` — guess on finished session
- `500 Internal Server Error` — unexpected failure

---

## GET /api/health

**Response `200`**
```json
{ "status": "UP", "service": "arcade-backend" }
```

---

## GET /api/games

**Response `200`**
```json
[
  {
    "id": "number-detective",
    "name": "Number Detective",
    "description": "Guess the secret number between 0 and 100 in 7 tries.",
    "path": "/games/number-detective"
  }
]
```

---

## POST /api/games/number-detective/sessions

Start a new game session.

**Response `201`**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "guessesUsed": 0,
  "guessesRemaining": 7,
  "maxGuesses": 7,
  "secretNumber": null
}
```
> `secretNumber` is always `null` while `IN_PROGRESS`.

---

## GET /api/games/number-detective/sessions/{id}

**Response `200`** — same shape as above  
**Response `404`** — session not found

---

## POST /api/games/number-detective/sessions/{id}/guesses

Submit a guess.

**Request**
```json
{ "guess": 42 }
```
`guess` must be an integer 0–100.

**Response `200`**
```json
{
  "guess": 42,
  "result": "CORRECT",
  "sessionStatus": "WON",
  "guessesUsed": 3,
  "guessesRemaining": 4,
  "secretNumber": 42
}
```

`result` values: `CORRECT` | `TOO_LOW` | `TOO_HIGH`  
`sessionStatus` values: `IN_PROGRESS` | `WON` | `LOST`  
`secretNumber` is revealed only when `sessionStatus` is `WON` or `LOST`.

**Response `400`** — invalid guess value  
**Response `404`** — session not found  
**Response `409`** — game already finished

---

## POST /api/games/number-detective/sessions/{id}/restart

Create a new session (old session removed).

**Response `201`** — new `SessionResponse` with new `sessionId`

---

## GET /api/leaderboard?game=number-detective

**Response `200`**
```json
[
  {
    "rank": 1,
    "playerName": "Anonymous",
    "guessesUsed": 2,
    "maxGuesses": 7,
    "completedAt": "2026-10-25T12:00:00Z"
  }
]
```

Only winning entries, ordered by fewest guesses then most recent.
