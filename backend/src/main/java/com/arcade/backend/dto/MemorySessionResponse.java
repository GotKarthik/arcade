package com.arcade.backend.dto;

import java.util.List;

public record MemorySessionResponse(
    String sessionId,
    String status,
    String turnState,
    List<MemoryCardDto> grid,
    int movesCount,
    int pairsMatched,
    int totalPairs,
    Integer durationSeconds,
    boolean completed,
    String playerName
) {}
