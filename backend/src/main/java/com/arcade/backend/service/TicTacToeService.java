package com.arcade.backend.service;

import com.arcade.backend.dto.TttSessionResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.InvalidMoveException;
import com.arcade.backend.exception.SessionNotFoundException;
import com.arcade.backend.model.TttMatch;
import com.arcade.backend.model.TttOutcome;
import com.arcade.backend.model.TttPlayer;
import com.arcade.backend.model.TttSession;
import com.arcade.backend.repository.TttMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicTacToeService {

    // 8 winning lines: 3 rows, 3 cols, 2 diagonals
    private static final int[][] WINNING_LINES = {
        {0, 1, 2}, // Row 0
        {3, 4, 5}, // Row 1
        {6, 7, 8}, // Row 2
        {0, 3, 6}, // Col 0
        {1, 4, 7}, // Col 1
        {2, 5, 8}, // Col 2
        {0, 4, 8}, // Diag main
        {2, 4, 6}  // Diag anti
    };

    private final TttMatchRepository matchRepository;
    private final ConcurrentHashMap<String, TttSession> sessions = new ConcurrentHashMap<>();

    public TicTacToeService(TttMatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public TttSession createSession() {
        String sessionId = UUID.randomUUID().toString();
        TttSession session = new TttSession(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    public TttSession getSession(String sessionId) {
        TttSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }
        return session;
    }

    @Transactional
    public TttSession makeMove(String sessionId, int position, String requestedPlayer) {
        TttSession session = getSession(sessionId);

        if (session.isFinished()) {
            throw new GameAlreadyFinishedException("Match is already finished. Please restart to play again.");
        }

        if (position < 0 || position > 8) {
            throw new InvalidMoveException("Invalid position: " + position + ". Must be between 0 and 8.");
        }

        if (session.getCell(position) != null) {
            throw new InvalidMoveException("Cell " + position + " is already occupied.");
        }

        // Validate player if provided
        TttPlayer currentTurn = session.getCurrentTurn();
        if (requestedPlayer != null && !requestedPlayer.isBlank()) {
            try {
                TttPlayer player = TttPlayer.valueOf(requestedPlayer.trim().toUpperCase());
                if (player != currentTurn) {
                    throw new InvalidMoveException("It is player " + currentTurn + "'s turn, but received move for " + player + ".");
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidMoveException("Invalid player: '" + requestedPlayer + "'. Must be X or O.");
            }
        }

        // Apply move
        session.setCell(position, currentTurn.name());

        // Check for win
        List<Integer> winLine = checkWin(session.getBoard(), currentTurn.name());
        if (winLine != null) {
            session.setWinningLine(winLine);
            session.setOutcome(currentTurn == TttPlayer.X ? TttOutcome.X_WON : TttOutcome.O_WON);
            session.setCompletedAt(Instant.now());
            persistMatch(session);
            return session;
        }

        // Check for draw
        if (session.getMovesCount() >= 9) {
            session.setOutcome(TttOutcome.DRAW);
            session.setCompletedAt(Instant.now());
            persistMatch(session);
            return session;
        }

        // Switch turn
        session.setCurrentTurn(currentTurn.other());
        return session;
    }

    public TttSession restartSession(String oldSessionId) {
        if (oldSessionId != null) {
            sessions.remove(oldSessionId);
        }
        return createSession();
    }

    public List<TttMatch> getRecentMatches() {
        return matchRepository.findRecentMatches();
    }

    public TttSessionResponse toSessionResponse(TttSession session) {
        return new TttSessionResponse(
                session.getSessionId(),
                session.getBoard(),
                session.getCurrentTurn().name(),
                session.getOutcome().name(),
                session.getWinningLine(),
                session.getMovesCount(),
                session.isFinished()
        );
    }

    private List<Integer> checkWin(String[] board, String player) {
        for (int[] line : WINNING_LINES) {
            if (player.equals(board[line[0]]) && player.equals(board[line[1]]) && player.equals(board[line[2]])) {
                return List.of(line[0], line[1], line[2]);
            }
        }
        return null;
    }

    private void persistMatch(TttSession session) {
        TttMatch match = new TttMatch();
        match.setSessionId(session.getSessionId());
        match.setOutcome(session.getOutcome().name());
        match.setWinningLine(session.getWinningLine() != null ? session.getWinningLine().toString() : null);
        match.setMovesCount(session.getMovesCount());
        match.setCompletedAt(session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now());
        matchRepository.save(match);
    }
}
