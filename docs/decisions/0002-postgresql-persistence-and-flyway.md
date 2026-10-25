# 2. PostgreSQL Persistence and Flyway Migrations

Date: 2026-10-25  
Status: Accepted

## Context
A durable datastore is needed to store historical completed game outcomes and rank players on a global leaderboard. We needed a reproducible schema management mechanism.

## Decision
Adopt PostgreSQL with Spring Data JPA and Flyway migration tool.
- Flyway controls database versioning via SQL migration scripts (`V1__create_game_results_table.sql`).
- JPA `hibernate.ddl-auto` is set to `validate` in production to prevent unintended automated schema mutations.

## Consequences
- **Advantages:** Reliable ACID transactions, relational indexing on `(game_type, outcome, guesses_used, completed_at)`, and reproducible zero-downtime database migrations.
- **Trade-offs:** Requires running PostgreSQL server locally and in deployment environments.
