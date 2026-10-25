package com.arcade.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for submitting a guess.
 */
public record GuessRequest(
        @NotNull(message = "guess is required")
        @Min(value = 0, message = "guess must be >= 0")
        @Max(value = 100, message = "guess must be <= 100")
        Integer guess
) {}
