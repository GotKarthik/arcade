package com.arcade.backend.dto;

/**
 * Standard error response body for all API errors.
 */
public record ErrorResponse(
        int status,
        String error,
        String message
) {}
