# 4. Multi-Game Domain Services and Dedicated Tables

Date: 2026-10-25  
Status: Accepted

## Context
Adding Rock Paper Scissors, Tic-Tac-Toe, and Memory Match to the Arcade platform required a design decision on service architecture and persistence. We needed to choose between a generic polymorphic game engine or dedicated, game-specific domain services and tables.

## Decision
Adopt dedicated domain services (`RockPaperScissorsService`, `TicTacToeService`, `MemoryMatchService`) and separate database tables (`rps_matches`, `ttt_matches`, `memory_matches`) alongside `game_results`.

## Rationale
- Each game has distinct rules, move models, turn states, and win conditions. Attempting to force a 5-round RPS match, a 3x3 board, and a 16-card grid into a single generic game model would introduce unnecessary complexity, fragile type casting, and leaky abstractions.
- Independent tables preserve historical Number Detective data intact and permit database indexes tailored to each game's specific metrics (e.g. `(moves_count, duration_seconds)` for Memory Match, `completed_at` for RPS/TTT).
