package com.arcade.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "memory_matches")
public class MemoryMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String outcome; // COMPLETED

    @Column(nullable = false)
    private Integer movesCount;

    @Column(nullable = false)
    private Integer durationSeconds;

    @Column(nullable = false)
    private Integer pairsMatched;

    @Column(nullable = false)
    private Integer totalPairs;

    @Column(nullable = false)
    private Instant completedAt;

    @Column
    private String playerName;

    public MemoryMatch() {}

    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public Integer getMovesCount() { return movesCount; }
    public void setMovesCount(Integer movesCount) { this.movesCount = movesCount; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public Integer getPairsMatched() { return pairsMatched; }
    public void setPairsMatched(Integer pairsMatched) { this.pairsMatched = pairsMatched; }
    public Integer getTotalPairs() { return totalPairs; }
    public void setTotalPairs(Integer totalPairs) { this.totalPairs = totalPairs; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
