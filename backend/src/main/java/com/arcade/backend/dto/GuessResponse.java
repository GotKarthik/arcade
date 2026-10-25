package com.arcade.backend.dto;

/**
 * Response after a guess is processed.
 */
public record GuessResponse(
        int guess,
        String result,          // "CORRECT" | "TOO_LOW" | "TOO_HIGH"
        String sessionStatus,   // "IN_PROGRESS" | "WON" | "LOST"
        int guessesUsed,
        int guessesRemaining,
        Integer secretNumber    // null while IN_PROGRESS; revealed on game end
) {}
