package com.arcade.backend.model;

import com.arcade.backend.model.GameStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * In-memory active game session for Number Detective.
 *
 * <p><strong>Limitation:</strong> active sessions exist only in memory.
 * They do not survive backend restarts. Completed sessions are persisted
 * to the database via {@link GameResult}.
 */
public class NumberDetectiveSession {

    public static final int MAX_GUESSES = 7;
    public static final int MIN_NUMBER = 0;
    public static final int MAX_NUMBER = 100;

    private final String id;
    private final int secretNumber;
    private final Instant createdAt;
    private final List<Integer> guesses;
    private GameStatus status;
    private Instant completedAt;

    public NumberDetectiveSession(int secretNumber) {
        this.id = UUID.randomUUID().toString();
        this.secretNumber = secretNumber;
        this.createdAt = Instant.now();
        this.guesses = new ArrayList<>();
        this.status = GameStatus.IN_PROGRESS;
    }

    /** Returns true when the guess was accepted and processed. */
    public GuessResult processGuess(int guess) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game session is already finished.");
        }
        if (guess < MIN_NUMBER || guess > MAX_NUMBER) {
            throw new IllegalArgumentException(
                "Guess must be between " + MIN_NUMBER + " and " + MAX_NUMBER + ".");
        }

        guesses.add(guess);

        if (guess == secretNumber) {
            status = GameStatus.WON;
            completedAt = Instant.now();
            return GuessResult.CORRECT;
        }

        if (guesses.size() >= MAX_GUESSES) {
            status = GameStatus.LOST;
            completedAt = Instant.now();
            return guess < secretNumber ? GuessResult.TOO_LOW : GuessResult.TOO_HIGH;
        }

        return guess < secretNumber ? GuessResult.TOO_LOW : GuessResult.TOO_HIGH;
    }

    public boolean isFinished() {
        return status != GameStatus.IN_PROGRESS;
    }

    public String getId() { return id; }
    public int getSecretNumber() { return secretNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public List<Integer> getGuesses() { return Collections.unmodifiableList(guesses); }
    public GameStatus getStatus() { return status; }
    public int getGuessesUsed() { return guesses.size(); }
    public int getGuessesRemaining() { return Math.max(0, MAX_GUESSES - guesses.size()); }

    public enum GuessResult {
        CORRECT, TOO_LOW, TOO_HIGH
    }
}
