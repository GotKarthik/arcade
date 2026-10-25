-- Flyway migration V2: Add persistence tables for Rock-Paper-Scissors, Tic-Tac-Toe, and Memory Match

CREATE TABLE IF NOT EXISTS rps_matches (
    id             BIGSERIAL PRIMARY KEY,
    session_id     VARCHAR(128) NOT NULL,
    outcome        VARCHAR(16)  NOT NULL,   -- 'PLAYER_WON', 'COMPUTER_WON', 'DRAW'
    player_score   INTEGER      NOT NULL,
    computer_score INTEGER      NOT NULL,
    draws          INTEGER      NOT NULL,
    rounds_played  INTEGER      NOT NULL,
    max_rounds     INTEGER      NOT NULL,
    completed_at   TIMESTAMPTZ  NOT NULL,
    player_name    VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_rps_matches_outcome ON rps_matches(outcome);
CREATE INDEX IF NOT EXISTS idx_rps_matches_completed_at ON rps_matches(completed_at);

CREATE TABLE IF NOT EXISTS ttt_matches (
    id           BIGSERIAL PRIMARY KEY,
    session_id   VARCHAR(128) NOT NULL,
    outcome      VARCHAR(16)  NOT NULL,   -- 'X_WON', 'O_WON', 'DRAW'
    winning_line VARCHAR(32),
    moves_count  INTEGER      NOT NULL,
    completed_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ttt_matches_outcome ON ttt_matches(outcome);
CREATE INDEX IF NOT EXISTS idx_ttt_matches_completed_at ON ttt_matches(completed_at);

CREATE TABLE IF NOT EXISTS memory_matches (
    id               BIGSERIAL PRIMARY KEY,
    session_id       VARCHAR(128) NOT NULL,
    outcome          VARCHAR(16)  NOT NULL,   -- 'COMPLETED'
    moves_count      INTEGER      NOT NULL,
    duration_seconds INTEGER      NOT NULL,
    pairs_matched    INTEGER      NOT NULL,
    total_pairs      INTEGER      NOT NULL,
    completed_at     TIMESTAMPTZ  NOT NULL,
    player_name      VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_memory_matches_leaderboard ON memory_matches(moves_count, duration_seconds);
CREATE INDEX IF NOT EXISTS idx_memory_matches_completed_at ON memory_matches(completed_at);
