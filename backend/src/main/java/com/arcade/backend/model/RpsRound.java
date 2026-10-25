package com.arcade.backend.model;

public record RpsRound(
    int roundNumber,
    RpsMove playerMove,
    RpsMove computerMove,
    RpsRoundResult result
) {}
