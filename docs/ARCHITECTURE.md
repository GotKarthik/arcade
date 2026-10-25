# Architecture

## Overview

Arcade is a modular monolith. The backend is a single Spring Boot 3 application with independent game domain services and JPA repositories. The frontend is a Vite + React SPA. Communication is exclusively via REST/JSON.

```
Browser (React SPA)
    ↓ HTTP/JSON (REST API)
Spring Boot 3 (port 8080)
    ├── ArcadeController (Catalog + 4 Games)
    ├── Services:
    │   ├── NumberDetectiveService
    │   ├── RockPaperScissorsService
    │   ├── TicTacToeService
    │   └── MemoryMatchService
    ├── Repositories (JPA):
    │   ├── GameResultRepository
    │   ├── RpsMatchRepository
    │   ├── TttMatchRepository
    │   └── MemoryMatchRepository
    └── PostgreSQL (Flyway V1 & V2)
```

## Backend Modules & Responsibilities

| Service | Session State | Persistence Entity | Anti-Cheat / Authority |
|---|---|---|---|
| `NumberDetectiveService` | `NumberDetectiveSession` | `GameResult` (`game_results`) | Secret number masked as `null` until completion. Server evaluates `TOO_LOW`/`TOO_HIGH`. |
| `RockPaperScissorsService` | `RpsSession` | `RpsMatch` (`rps_matches`) | Computer move generated on server via `SecureRandom`. Authoritative 5-round scoring. |
| `TicTacToeService` | `TttSession` | `TttMatch` (`ttt_matches`) | Server owns 3x3 board array, enforces turn order (X then O), checks 8 winning lines & draws. |
| `MemoryMatchService` | `MemorySession` | `MemoryMatch` (`memory_matches`) | Card mapping strictly concealed; face-down cards return `null` symbols. Server-side timestamps. |

## Frontend Architecture

- **React 18** functional components with hooks.
- **React Router 6** client-side routing (`/`, `/games/number-detective`, `/games/rock-paper-scissors`, `/games/tic-tac-toe`, `/games/memory-match`).
- **Unified Design System (`global.css`):** Consistent tokens, color palette, glassmorphism, responsive grids, and micro-animations.
- **REST Client (`client.js`):** Modular async API layer reading `VITE_API_URL`.
