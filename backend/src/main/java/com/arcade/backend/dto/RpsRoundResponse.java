package com.arcade.backend.dto;

public record RpsRoundResponse(
    int roundNumber,
    String playerMove,
    String computerMove,
    String result,
    int playerScore,
    int computerScore,
    int draws,
    int roundsRemaining,
    String matchOutcome,
    boolean matchFinished
) {}
