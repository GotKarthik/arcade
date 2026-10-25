# 1. In-Memory Active Sessions

Date: 2026-10-25  
Status: Accepted

## Context
Number Detective requires tracking active game state (current secret number, number of attempts used, previous guesses) for each user session. We needed to choose how active sessions are managed before game completion.

## Decision
Store active in-progress sessions in server memory using `ConcurrentHashMap<String, NumberDetectiveSession>`. Completed game results are persisted to PostgreSQL.

## Consequences
- **Advantages:** Zero database write latency on individual guess submissions; simple implementation without caching server overhead.
- **Trade-offs:** Active games are lost if the server process restarts. For multi-instance scaling, Redis will be required in future versions.
