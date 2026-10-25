package com.arcade.backend.model;

import java.time.Instant;
import java.util.*;

public class MemorySession {
    private final String sessionId;
    private final List<CardInternal> deck;
    private final Set<Integer> matchedPositions = new HashSet<>();
    private Integer firstCardPos = null;
    private Integer secondCardPos = null;
    private MemoryTurnState turnState = MemoryTurnState.AWAITING_FIRST;
    private int movesCount = 0;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private final Instant startedAt = Instant.now();
    private Instant completedAt;
    private String playerName;

    public MemorySession(String sessionId, List<CardInternal> deck, String playerName) {
        this.sessionId = sessionId;
        this.deck = deck;
        this.playerName = playerName != null && !playerName.isBlank() ? playerName.trim() : "Player";
    }

    public boolean isCompleted() {
        return status == GameStatus.WON;
    }

    public CardInternal getCard(int position) {
        return deck.get(position);
    }

    public boolean isPositionMatched(int position) {
        return matchedPositions.contains(position);
    }

    public void addMatched(int pos1, int pos2) {
        matchedPositions.add(pos1);
        matchedPositions.add(pos2);
        if (matchedPositions.size() >= deck.size()) {
            status = GameStatus.WON;
            completedAt = Instant.now();
        }
    }

    public int getPairsMatched() {
        return matchedPositions.size() / 2;
    }

    public int getTotalPairs() {
        return deck.size() / 2;
    }

    public Integer getDurationSeconds() {
        if (completedAt != null) {
            return (int) Math.max(1, completedAt.getEpochSecond() - startedAt.getEpochSecond());
        }
        return (int) Math.max(0, Instant.now().getEpochSecond() - startedAt.getEpochSecond());
    }

    // Getters / Setters
    public String getSessionId() { return sessionId; }
    public List<CardInternal> getDeck() { return deck; }
    public Set<Integer> getMatchedPositions() { return Collections.unmodifiableSet(matchedPositions); }
    public Integer getFirstCardPos() { return firstCardPos; }
    public void setFirstCardPos(Integer firstCardPos) { this.firstCardPos = firstCardPos; }
    public Integer getSecondCardPos() { return secondCardPos; }
    public void setSecondCardPos(Integer secondCardPos) { this.secondCardPos = secondCardPos; }
    public MemoryTurnState getTurnState() { return turnState; }
    public void setTurnState(MemoryTurnState turnState) { this.turnState = turnState; }
    public int getMovesCount() { return movesCount; }
    public void incrementMoves() { this.movesCount++; }
    public GameStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
