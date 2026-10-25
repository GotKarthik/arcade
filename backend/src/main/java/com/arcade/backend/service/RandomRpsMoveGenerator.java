package com.arcade.backend.service;

import com.arcade.backend.model.RpsMove;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomRpsMoveGenerator implements RpsMoveGenerator {
    private static final RpsMove[] MOVES = RpsMove.values();
    private final SecureRandom random = new SecureRandom();

    @Override
    public RpsMove nextMove() {
        return MOVES[random.nextInt(MOVES.length)];
    }
}
