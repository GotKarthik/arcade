package com.arcade.backend.service;

import com.arcade.backend.model.RpsMove;

@FunctionalInterface
public interface RpsMoveGenerator {
    RpsMove nextMove();
}
