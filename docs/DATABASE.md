# Database Architecture & Flyway Migrations

Arcade utilizes **PostgreSQL** in development and production environments, and **H2** in-memory mode for isolated automated test suites.

Schema migrations are strictly managed via **Flyway**.

---

## Schema History

### Migration V1: `V1__initial_schema.sql`
Creates the completed games table for Number Detective:
- `game_results`: `id`, `game_type`, `session_id`, `outcome`, `guesses_used`, `max_guesses`, `secret_number`, `completed_at`, `player_name`.

### Migration V2: `V2__add_new_games_results.sql`
Adds dedicated persistence tables for the three additional games:
- `rps_matches`:
  - `id BIGSERIAL PRIMARY KEY`
  - `session_id VARCHAR(128) NOT NULL`
  - `outcome VARCHAR(16) NOT NULL` (`PLAYER_WON`, `COMPUTER_WON`, `DRAW`)
  - `player_score INTEGER NOT NULL`
  - `computer_score INTEGER NOT NULL`
  - `draws INTEGER NOT NULL`
  - `rounds_played INTEGER NOT NULL`
  - `max_rounds INTEGER NOT NULL`
  - `completed_at TIMESTAMPTZ NOT NULL`
  - `player_name VARCHAR(128)`
- `ttt_matches`:
  - `id BIGSERIAL PRIMARY KEY`
  - `session_id VARCHAR(128) NOT NULL`
  - `outcome VARCHAR(16) NOT NULL` (`X_WON`, `O_WON`, `DRAW`)
  - `winning_line VARCHAR(32)`
  - `moves_count INTEGER NOT NULL`
  - `completed_at TIMESTAMPTZ NOT NULL`
- `memory_matches`:
  - `id BIGSERIAL PRIMARY KEY`
  - `session_id VARCHAR(128) NOT NULL`
  - `outcome VARCHAR(16) NOT NULL` (`COMPLETED`)
  - `moves_count INTEGER NOT NULL`
  - `duration_seconds INTEGER NOT NULL`
  - `pairs_matched INTEGER NOT NULL`
  - `total_pairs INTEGER NOT NULL`
  - `completed_at TIMESTAMPTZ NOT NULL`
  - `player_name VARCHAR(128)`
