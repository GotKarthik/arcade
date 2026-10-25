package com.arcade.backend.dto;

import java.util.List;

public record TttSessionResponse(
    String sessionId,
    String[] board,
    String currentTurn,
    String outcome,
    List<Integer> winningLine,
    int movesCount,
    boolean finished
) {}
