package com.arcade.backend.dto;

import com.arcade.backend.model.RpsRound;
import java.util.List;

public record RpsSessionResponse(
    String sessionId,
    String outcome,
    int playerScore,
    int computerScore,
    int draws,
    int roundsPlayed,
    int roundsRemaining,
    int maxRounds,
    List<RpsRound> rounds,
    String playerName
) {}
