package com.arcade.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ttt_matches")
public class TttMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String outcome; // X_WON, O_WON, DRAW

    @Column
    private String winningLine;

    @Column(nullable = false)
    private Integer movesCount;

    @Column(nullable = false)
    private Instant completedAt;

    public TttMatch() {}

    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getWinningLine() { return winningLine; }
    public void setWinningLine(String winningLine) { this.winningLine = winningLine; }
    public Integer getMovesCount() { return movesCount; }
    public void setMovesCount(Integer movesCount) { this.movesCount = movesCount; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
