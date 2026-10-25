package com.arcade.backend.dto;

public record MemoryCardDto(
    int position,
    boolean revealed,
    boolean matched,
    String symbol
) {}
