package com.arcade.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rps_matches")
public class RpsMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String outcome; // PLAYER_WON, COMPUTER_WON, DRAW

    @Column(nullable = false)
    private Integer playerScore;

    @Column(nullable = false)
    private Integer computerScore;

    @Column(nullable = false)
    private Integer draws;

    @Column(nullable = false)
    private Integer roundsPlayed;

    @Column(nullable = false)
    private Integer maxRounds;

    @Column(nullable = false)
    private Instant completedAt;

    @Column
    private String playerName;

    public RpsMatch() {}

    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public Integer getPlayerScore() { return playerScore; }
    public void setPlayerScore(Integer playerScore) { this.playerScore = playerScore; }
    public Integer getComputerScore() { return computerScore; }
    public void setComputerScore(Integer computerScore) { this.computerScore = computerScore; }
    public Integer getDraws() { return draws; }
    public void setDraws(Integer draws) { this.draws = draws; }
    public Integer getRoundsPlayed() { return roundsPlayed; }
    public void setRoundsPlayed(Integer roundsPlayed) { this.roundsPlayed = roundsPlayed; }
    public Integer getMaxRounds() { return maxRounds; }
    public void setMaxRounds(Integer maxRounds) { this.maxRounds = maxRounds; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
