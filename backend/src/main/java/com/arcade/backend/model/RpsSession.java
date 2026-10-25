package com.arcade.backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RpsSession {
    private final String sessionId;
    private final int maxRounds;
    private int playerScore;
    private int computerScore;
    private int draws;
    private final List<RpsRound> rounds = new ArrayList<>();
    private RpsMatchOutcome outcome = RpsMatchOutcome.IN_PROGRESS;
    private final Instant startedAt = Instant.now();
    private Instant completedAt;
    private String playerName;

    public RpsSession(String sessionId, int maxRounds, String playerName) {
        this.sessionId = sessionId;
        this.maxRounds = maxRounds;
        this.playerName = playerName != null && !playerName.isBlank() ? playerName.trim() : "Player";
    }

    public boolean isFinished() {
        return outcome != RpsMatchOutcome.IN_PROGRESS;
    }

    public int getRoundsPlayed() {
        return rounds.size();
    }

    public int getRoundsRemaining() {
        return Math.max(0, maxRounds - rounds.size());
    }

    public void addRound(RpsRound round) {
        rounds.add(round);
        if (round.result() == RpsRoundResult.WIN) {
            playerScore++;
        } else if (round.result() == RpsRoundResult.LOSS) {
            computerScore++;
        } else {
            draws++;
        }

        if (rounds.size() >= maxRounds) {
            if (playerScore > computerScore) {
                outcome = RpsMatchOutcome.PLAYER_WON;
            } else if (computerScore > playerScore) {
                outcome = RpsMatchOutcome.COMPUTER_WON;
            } else {
                outcome = RpsMatchOutcome.DRAW;
            }
            completedAt = Instant.now();
        }
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public int getMaxRounds() { return maxRounds; }
    public int getPlayerScore() { return playerScore; }
    public int getComputerScore() { return computerScore; }
    public int getDraws() { return draws; }
    public List<RpsRound> getRounds() { return Collections.unmodifiableList(rounds); }
    public RpsMatchOutcome getOutcome() { return outcome; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
