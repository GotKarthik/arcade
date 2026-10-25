package com.arcade.backend.dto;

import java.time.Instant;

public record MemoryLeaderboardEntry(
    int rank,
    String playerName,
    int movesCount,
    int durationSeconds,
    Instant completedAt
) {}
