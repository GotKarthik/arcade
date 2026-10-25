package com.arcade.backend.service;

import com.arcade.backend.dto.MemoryCardDto;
import com.arcade.backend.dto.MemorySessionResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.InvalidMoveException;
import com.arcade.backend.model.CardInternal;
import com.arcade.backend.model.MemoryMatch;
import com.arcade.backend.model.MemorySession;
import com.arcade.backend.model.MemoryTurnState;
import com.arcade.backend.repository.MemoryMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MemoryMatchServiceTest {

    private MemoryMatchRepository matchRepository;
    private MemoryMatchService service;

    // Deterministic deck for testing: pair 0 at [0,1], pair 1 at [2,3], ..., pair 7 at [14,15]
    static class DeterministicDeckGenerator implements MemoryDeckGenerator {
        @Override
        public List<CardInternal> generateDeck() {
            List<CardInternal> deck = new ArrayList<>();
            for (int pairId = 0; pairId < 8; pairId++) {
                String sym = "SYM_" + pairId;
                deck.add(new CardInternal(pairId * 2, pairId, sym));
                deck.add(new CardInternal(pairId * 2 + 1, pairId, sym));
            }
            return deck;
        }
    }

    @BeforeEach
    void setUp() {
        matchRepository = mock(MemoryMatchRepository.class);
        service = new MemoryMatchService(new DeterministicDeckGenerator(), matchRepository);
    }

    @Test
    @DisplayName("Deck generator produces 16 cards and exactly 8 matching pairs")
    void deckGeneration() {
        RandomMemoryDeckGenerator randomGen = new RandomMemoryDeckGenerator();
        List<CardInternal> deck = randomGen.generateDeck();

        assertThat(deck).hasSize(16);
        long distinctPairs = deck.stream().map(CardInternal::pairId).distinct().count();
        assertThat(distinctPairs).isEqualTo(8);

        // Every pairId occurs exactly twice
        for (int p = 0; p < 8; p++) {
            final int pairId = p;
            long count = deck.stream().filter(c -> c.pairId() == pairId).count();
            assertThat(count).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Valid first card flip sets turnState to AWAITING_SECOND")
    void firstCardFlip() {
        MemorySession session = service.createSession("Alice");
        MemorySession updated = service.flipCard(session.getSessionId(), 0);

        assertThat(updated.getTurnState()).isEqualTo(MemoryTurnState.AWAITING_SECOND);
        assertThat(updated.getFirstCardPos()).isEqualTo(0);
        assertThat(updated.getSecondCardPos()).isNull();
    }

    @Test
    @DisplayName("Matching pair reveals both cards permanently and increments pairsMatched")
    void matchingPair() {
        MemorySession session = service.createSession("Alice");
        service.flipCard(session.getSessionId(), 0);
        MemorySession updated = service.flipCard(session.getSessionId(), 1);

        assertThat(updated.getTurnState()).isEqualTo(MemoryTurnState.PAIR_MATCHED);
        assertThat(updated.isPositionMatched(0)).isTrue();
        assertThat(updated.isPositionMatched(1)).isTrue();
        assertThat(updated.getPairsMatched()).isEqualTo(1);
        assertThat(updated.getMovesCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mismatching pair sets state to PAIR_MISMATCHED and cards are cleared on next turn")
    void mismatchingPair_andAutoClear() {
        MemorySession session = service.createSession("Alice");
        // Pos 0 has SYM_0, pos 2 has SYM_1
        service.flipCard(session.getSessionId(), 0);
        MemorySession afterMismatch = service.flipCard(session.getSessionId(), 2);

        assertThat(afterMismatch.getTurnState()).isEqualTo(MemoryTurnState.PAIR_MISMATCHED);
        assertThat(afterMismatch.isPositionMatched(0)).isFalse();
        assertThat(afterMismatch.isPositionMatched(2)).isFalse();
        assertThat(afterMismatch.getMovesCount()).isEqualTo(1);

        // Next flip automatically resets previous mismatch and starts new turn
        MemorySession nextTurn = service.flipCard(session.getSessionId(), 4);
        assertThat(nextTurn.getTurnState()).isEqualTo(MemoryTurnState.AWAITING_SECOND);
        assertThat(nextTurn.getFirstCardPos()).isEqualTo(4);
    }

    @Test
    @DisplayName("Anti-cheat: unrevealed cards have null symbols in session response")
    void antiCheatMasking() {
        MemorySession session = service.createSession("Alice");
        service.flipCard(session.getSessionId(), 0);

        MemorySessionResponse response = service.toSessionResponse(session);

        // Card 0 is revealed -> has symbol
        assertThat(response.grid().get(0).symbol()).isEqualTo("SYM_0");
        assertThat(response.grid().get(0).revealed()).isTrue();

        // Cards 1..15 are face down -> symbol MUST be null
        for (int i = 1; i < 16; i++) {
            MemoryCardDto card = response.grid().get(i);
            assertThat(card.revealed()).isFalse();
            assertThat(card.matched()).isFalse();
            assertThat(card.symbol()).isNull();
        }
    }

    @Test
    @DisplayName("Rejects selecting the same card twice in a turn")
    void cannotSelectSameCardTwice() {
        MemorySession session = service.createSession("Alice");
        service.flipCard(session.getSessionId(), 0);

        assertThatThrownBy(() -> service.flipCard(session.getSessionId(), 0))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("Cannot select the same card twice");
    }

    @Test
    @DisplayName("Rejects selecting an already matched card")
    void cannotSelectAlreadyMatchedCard() {
        MemorySession session = service.createSession("Alice");
        service.flipCard(session.getSessionId(), 0);
        service.flipCard(session.getSessionId(), 1); // matched

        assertThatThrownBy(() -> service.flipCard(session.getSessionId(), 0))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("already been matched");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 16, 99})
    @DisplayName("Rejects out-of-range card positions")
    void outOfRangeCardPositions(int pos) {
        MemorySession session = service.createSession("Alice");

        assertThatThrownBy(() -> service.flipCard(session.getSessionId(), pos))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("between 0 and 15");
    }

    @Test
    @DisplayName("Completing all 8 pairs triggers completed status and saves result")
    void gameCompletion() {
        MemorySession session = service.createSession("Speedy");

        // Match all 8 pairs
        for (int p = 0; p < 8; p++) {
            service.flipCard(session.getSessionId(), p * 2);
            service.flipCard(session.getSessionId(), p * 2 + 1);
        }

        MemorySession finished = service.getSession(session.getSessionId());
        assertThat(finished.isCompleted()).isTrue();
        assertThat(finished.getPairsMatched()).isEqualTo(8);
        assertThat(finished.getMovesCount()).isEqualTo(8);

        ArgumentCaptor<MemoryMatch> captor = ArgumentCaptor.forClass(MemoryMatch.class);
        verify(matchRepository).save(captor.capture());
        MemoryMatch saved = captor.getValue();
        assertThat(saved.getOutcome()).isEqualTo("COMPLETED");
        assertThat(saved.getMovesCount()).isEqualTo(8);
        assertThat(saved.getPlayerName()).isEqualTo("Speedy");
        assertThat(saved.getPairsMatched()).isEqualTo(8);

        // In completed response, all symbols become visible
        MemorySessionResponse response = service.toSessionResponse(finished);
        assertThat(response.completed()).isTrue();
        assertThat(response.grid()).allMatch(c -> c.symbol() != null);
    }

    @Test
    @DisplayName("Rejects flips after game completion")
    void flipAfterCompletionRejected() {
        MemorySession session = service.createSession("Alice");
        for (int p = 0; p < 8; p++) {
            service.flipCard(session.getSessionId(), p * 2);
            service.flipCard(session.getSessionId(), p * 2 + 1);
        }

        assertThatThrownBy(() -> service.flipCard(session.getSessionId(), 0))
                .isInstanceOf(GameAlreadyFinishedException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    @DisplayName("Restart creates a fresh session with 0 moves and 0 pairs")
    void restartSession() {
        MemorySession session = service.createSession("Alice");
        service.flipCard(session.getSessionId(), 0);
        service.flipCard(session.getSessionId(), 1);

        MemorySession restarted = service.restartSession(session.getSessionId());

        assertThat(restarted.getSessionId()).isNotEqualTo(session.getSessionId());
        assertThat(restarted.getMovesCount()).isEqualTo(0);
        assertThat(restarted.getPairsMatched()).isEqualTo(0);
    }
}
