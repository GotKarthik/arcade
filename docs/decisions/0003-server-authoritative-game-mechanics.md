# 3. Server-Authoritative Game Mechanics

Date: 2026-10-25  
Status: Accepted

## Context
Browser games are susceptible to client-side inspection and memory modification if rules or target numbers are managed in JavaScript.

## Decision
All core game logic, secret generation via `SecureRandom`, comparison logic, and attempt counts are strictly server-authoritative. The client receives only directional hints (`TOO_LOW`, `TOO_HIGH`) until the game finishes.

## Consequences
- **Advantages:** Prevents cheating via browser devtools; guarantees leaderboard authenticity.
- **Trade-offs:** Requires an HTTP round-trip per guess (~5-15ms local latency).
