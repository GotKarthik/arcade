package com.arcade.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Persisted record of a completed game result.
 * Only written when a game session ends (win or loss).
 */
@Entity
@Table(name = "game_results")
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gameType;   // "number-detective"

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String outcome;    // "WON" | "LOST"

    @Column(nullable = false)
    private Integer guessesUsed;

    @Column(nullable = false)
    private Integer maxGuesses;

    @Column(nullable = false)
    private Integer secretNumber;

    @Column(nullable = false)
    private Instant completedAt;

    /** Optional: player-supplied nickname; null for anonymous players. */
    @Column
    private String playerName;

    public GameResult() {}

    // ----- Getters / Setters -----

    public Long getId() { return id; }
    public String getGameType() { return gameType; }
    public void setGameType(String gameType) { this.gameType = gameType; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public Integer getGuessesUsed() { return guessesUsed; }
    public void setGuessesUsed(Integer guessesUsed) { this.guessesUsed = guessesUsed; }
    public Integer getMaxGuesses() { return maxGuesses; }
    public void setMaxGuesses(Integer maxGuesses) { this.maxGuesses = maxGuesses; }
    public Integer getSecretNumber() { return secretNumber; }
    public void setSecretNumber(Integer secretNumber) { this.secretNumber = secretNumber; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
