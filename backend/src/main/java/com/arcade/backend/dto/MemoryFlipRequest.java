package com.arcade.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MemoryFlipRequest(
    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be between 0 and 15")
    @Max(value = 15, message = "Position must be between 0 and 15")
    Integer position,
    String playerName
) {}
