package com.arcade.backend.model;

/**
 * Represents the status of a game session.
 */
public enum GameStatus {
    /** Game is still in progress. */
    IN_PROGRESS,
    /** Player guessed correctly. */
    WON,
    /** Player exhausted all attempts. */
    LOST
}
