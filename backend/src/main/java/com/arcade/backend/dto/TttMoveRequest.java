package com.arcade.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TttMoveRequest(
    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be between 0 and 8")
    @Max(value = 8, message = "Position must be between 0 and 8")
    Integer position,
    String player
) {}
