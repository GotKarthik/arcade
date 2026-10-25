package com.arcade.backend.model;

import com.arcade.backend.exception.InvalidMoveException;

public enum RpsMove {
    ROCK,
    PAPER,
    SCISSORS;

    public boolean beats(RpsMove other) {
        return (this == ROCK && other == SCISSORS)
                || (this == SCISSORS && other == PAPER)
                || (this == PAPER && other == ROCK);
    }

    public static RpsMove fromString(String text) {
        if (text == null) {
            throw new InvalidMoveException("Move cannot be null. Must be ROCK, PAPER, or SCISSORS.");
        }
        try {
            return RpsMove.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidMoveException("Invalid move: '" + text + "'. Must be ROCK, PAPER, or SCISSORS.");
        }
    }
}
