package com.arcade.backend.dto;

import java.time.Instant;

/**
 * Leaderboard entry DTO for the public leaderboard endpoint.
 */
public record LeaderboardEntry(
        int rank,
        String playerName,
        int guessesUsed,
        int maxGuesses,
        Instant completedAt
) {}
