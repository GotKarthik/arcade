package com.arcade.backend.service;

import com.arcade.backend.dto.MemoryCardDto;
import com.arcade.backend.dto.MemoryLeaderboardEntry;
import com.arcade.backend.dto.MemorySessionResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.InvalidMoveException;
import com.arcade.backend.exception.SessionNotFoundException;
import com.arcade.backend.model.*;
import com.arcade.backend.repository.MemoryMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemoryMatchService {

    private final MemoryDeckGenerator deckGenerator;
    private final MemoryMatchRepository matchRepository;
    private final ConcurrentHashMap<String, MemorySession> sessions = new ConcurrentHashMap<>();

    public MemoryMatchService(MemoryDeckGenerator deckGenerator, MemoryMatchRepository matchRepository) {
        this.deckGenerator = deckGenerator;
        this.matchRepository = matchRepository;
    }

    public MemorySession createSession(String playerName) {
        String sessionId = UUID.randomUUID().toString();
        List<CardInternal> deck = deckGenerator.generateDeck();
        MemorySession session = new MemorySession(sessionId, deck, playerName);
        sessions.put(sessionId, session);
        return session;
    }

    public MemorySession getSession(String sessionId) {
        MemorySession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }
        return session;
    }

    @Transactional
    public MemorySession flipCard(String sessionId, int position) {
        MemorySession session = getSession(sessionId);

        if (session.isCompleted()) {
            throw new GameAlreadyFinishedException("Game is already completed. Please restart to play again.");
        }

        if (position < 0 || position >= session.getDeck().size()) {
            throw new InvalidMoveException("Invalid card position: " + position + ". Must be between 0 and 15.");
        }

        if (session.isPositionMatched(position)) {
            throw new InvalidMoveException("Card at position " + position + " has already been matched.");
        }

        // If currently showing a mismatch or previous match from the previous move, reset for new turn
        if (session.getTurnState() == MemoryTurnState.PAIR_MISMATCHED || session.getTurnState() == MemoryTurnState.PAIR_MATCHED) {
            session.setFirstCardPos(null);
            session.setSecondCardPos(null);
            session.setTurnState(MemoryTurnState.AWAITING_FIRST);
        }

        if (session.getTurnState() == MemoryTurnState.AWAITING_FIRST) {
            session.setFirstCardPos(position);
            session.setTurnState(MemoryTurnState.AWAITING_SECOND);
            return session;
        }

        if (session.getTurnState() == MemoryTurnState.AWAITING_SECOND) {
            if (session.getFirstCardPos() != null && session.getFirstCardPos() == position) {
                throw new InvalidMoveException("Cannot select the same card twice in a turn.");
            }

            session.setSecondCardPos(position);
            session.incrementMoves();

            CardInternal card1 = session.getCard(session.getFirstCardPos());
            CardInternal card2 = session.getCard(position);

            if (card1.pairId() == card2.pairId()) {
                // Match!
                session.addMatched(session.getFirstCardPos(), position);
                session.setTurnState(MemoryTurnState.PAIR_MATCHED);
                session.setFirstCardPos(null);
                session.setSecondCardPos(null);

                if (session.isCompleted()) {
                    persistMatch(session);
                }
            } else {
                // Mismatch
                session.setTurnState(MemoryTurnState.PAIR_MISMATCHED);
            }
            return session;
        }

        return session;
    }

    public MemorySession restartSession(String oldSessionId) {
        MemorySession old = sessions.get(oldSessionId);
        String playerName = old != null ? old.getPlayerName() : "Player";
        if (oldSessionId != null) {
            sessions.remove(oldSessionId);
        }
        return createSession(playerName);
    }

    public List<MemoryLeaderboardEntry> getLeaderboard() {
        List<MemoryMatch> matches = matchRepository.findLeaderboard();
        List<MemoryLeaderboardEntry> entries = new ArrayList<>();
        int rank = 1;
        for (MemoryMatch m : matches) {
            entries.add(new MemoryLeaderboardEntry(
                    rank++,
                    m.getPlayerName() != null ? m.getPlayerName() : "Anonymous",
                    m.getMovesCount(),
                    m.getDurationSeconds(),
                    m.getCompletedAt()
            ));
        }
        return entries;
    }

    public MemorySessionResponse toSessionResponse(MemorySession session) {
        List<MemoryCardDto> grid = new ArrayList<>(session.getDeck().size());
        boolean gameCompleted = session.isCompleted();

        for (int i = 0; i < session.getDeck().size(); i++) {
            CardInternal card = session.getCard(i);
            boolean matched = session.isPositionMatched(i);
            boolean isFlippedThisTurn = (session.getFirstCardPos() != null && session.getFirstCardPos() == i)
                    || (session.getSecondCardPos() != null && session.getSecondCardPos() == i);

            boolean revealed = matched || isFlippedThisTurn || gameCompleted;
            // Secret state is strictly hidden unless revealed or completed
            String symbol = revealed ? card.symbol() : null;

            grid.add(new MemoryCardDto(i, revealed, matched, symbol));
        }

        return new MemorySessionResponse(
                session.getSessionId(),
                session.getStatus().name(),
                session.getTurnState().name(),
                grid,
                session.getMovesCount(),
                session.getPairsMatched(),
                session.getTotalPairs(),
                session.getDurationSeconds(),
                gameCompleted,
                session.getPlayerName()
        );
    }

    private void persistMatch(MemorySession session) {
        MemoryMatch match = new MemoryMatch();
        match.setSessionId(session.getSessionId());
        match.setOutcome("COMPLETED");
        match.setMovesCount(session.getMovesCount());
        match.setDurationSeconds(session.getDurationSeconds());
        match.setPairsMatched(session.getPairsMatched());
        match.setTotalPairs(session.getTotalPairs());
        match.setCompletedAt(session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now());
        match.setPlayerName(session.getPlayerName());
        matchRepository.save(match);
    }
}
