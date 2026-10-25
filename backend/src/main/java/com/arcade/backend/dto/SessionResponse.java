package com.arcade.backend.dto;

/**
 * Response: public view of a session. Never contains the secret number unless game is over.
 */
public record SessionResponse(
        String sessionId,
        String status,          // "IN_PROGRESS" | "WON" | "LOST"
        int guessesUsed,
        int guessesRemaining,
        int maxGuesses,
        Integer secretNumber    // null while IN_PROGRESS; revealed on game end
) {}
