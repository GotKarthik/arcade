# Database

## Technology

- PostgreSQL 15
- JPA / Hibernate (ORM)
- Flyway (schema migrations)

## Schema

### `game_results`

Stores completed game sessions (wins and losses). Active sessions are in-memory.

| Column | Type | Notes |
|---|---|---|
| `id` | BIGSERIAL PK | Auto-increment |
| `game_type` | VARCHAR(64) | e.g. `number-detective` |
| `session_id` | VARCHAR(128) | UUID of the session |
| `outcome` | VARCHAR(16) | `WON` or `LOST` |
| `guesses_used` | INTEGER | How many guesses player made |
| `max_guesses` | INTEGER | Always 7 for Number Detective |
| `secret_number` | INTEGER | The secret number (0–100) |
| `completed_at` | TIMESTAMPTZ | When game ended |
| `player_name` | VARCHAR(128) | NULL for anonymous players |

## Migrations

Flyway migrations are in `backend/src/main/resources/db/migration/`.

| Version | Description |
|---|---|
| V1 | Initial schema: `game_results` table and indexes |

## Local Setup

```bash
psql -U postgres -c "CREATE USER arcade_user WITH PASSWORD 'arcade_pass';"
psql -U postgres -c "CREATE DATABASE arcade OWNER arcade_user;"
```

Migrations run automatically on startup. To run manually:
```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn flyway:migrate -pl backend
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/arcade` | JDBC connection URL |
| `DB_USERNAME` | `arcade_user` | Database username |
| `DB_PASSWORD` | `arcade_pass` | Database password |

## Limitations

- Active sessions are NOT persisted. They live in a `ConcurrentHashMap` and are lost on restart.
- No TTL or cleanup for very old completed results yet.
