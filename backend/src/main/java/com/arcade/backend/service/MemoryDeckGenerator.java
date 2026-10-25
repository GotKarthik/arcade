package com.arcade.backend.service;

import com.arcade.backend.model.CardInternal;
import java.util.List;

@FunctionalInterface
public interface MemoryDeckGenerator {
    List<CardInternal> generateDeck();
}
