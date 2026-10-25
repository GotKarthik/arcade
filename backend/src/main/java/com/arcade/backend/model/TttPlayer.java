package com.arcade.backend.model;

public enum TttPlayer {
    X,
    O;

    public TttPlayer other() {
        return this == X ? O : X;
    }
}
