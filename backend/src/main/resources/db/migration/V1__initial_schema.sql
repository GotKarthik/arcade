-- Flyway migration V1: initial schema

CREATE TABLE IF NOT EXISTS game_results (
    id            BIGSERIAL PRIMARY KEY,
    game_type     VARCHAR(64)  NOT NULL,
    session_id    VARCHAR(128) NOT NULL,
    outcome       VARCHAR(16)  NOT NULL,   -- 'WON' or 'LOST'
    guesses_used  INTEGER      NOT NULL,
    max_guesses   INTEGER      NOT NULL,
    secret_number INTEGER      NOT NULL,
    completed_at  TIMESTAMPTZ  NOT NULL,
    player_name   VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_game_results_game_type ON game_results(game_type);
CREATE INDEX IF NOT EXISTS idx_game_results_outcome   ON game_results(outcome);
