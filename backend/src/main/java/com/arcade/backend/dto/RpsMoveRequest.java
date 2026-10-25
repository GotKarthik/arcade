package com.arcade.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record RpsMoveRequest(
    @NotBlank(message = "Move is required")
    String move,
    String playerName
) {}
