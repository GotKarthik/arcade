package com.arcade.backend.service;

import com.arcade.backend.model.CardInternal;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RandomMemoryDeckGenerator implements MemoryDeckGenerator {

    public static final String[] SYMBOLS = {
        "🚀", "⭐", "💎", "🔥", "⚡", "🎮", "👾", "🌈"
    };

    private final SecureRandom random = new SecureRandom();

    @Override
    public List<CardInternal> generateDeck() {
        List<CardInternal> deck = new ArrayList<>(16);
        // Create 8 pairs
        for (int pairId = 0; pairId < SYMBOLS.length; pairId++) {
            String symbol = SYMBOLS[pairId];
            deck.add(new CardInternal(-1, pairId, symbol));
            deck.add(new CardInternal(-1, pairId, symbol));
        }
        // Shuffle
        Collections.shuffle(deck, random);

        // Assign positions 0..15
        List<CardInternal> positioned = new ArrayList<>(16);
        for (int i = 0; i < deck.size(); i++) {
            CardInternal c = deck.get(i);
            positioned.add(new CardInternal(i, c.pairId(), c.symbol()));
        }
        return Collections.unmodifiableList(positioned);
    }
}
