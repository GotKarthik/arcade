package com.arcade.backend.service;

import com.arcade.backend.dto.GuessResponse;
import com.arcade.backend.dto.LeaderboardEntry;
import com.arcade.backend.dto.SessionResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.SessionNotFoundException;
import com.arcade.backend.model.GameResult;
import com.arcade.backend.model.GameStatus;
import com.arcade.backend.model.NumberDetectiveSession;
import com.arcade.backend.model.NumberDetectiveSession.GuessResult;
import com.arcade.backend.repository.GameResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

/**
 * Service layer for Number Detective.
 *
 * <p>Active sessions are kept in a {@link ConcurrentHashMap} — they are in-memory only.
 * Sessions do not survive backend restarts. Only completed results are persisted.
 *
 * <p>Random number generation uses {@link java.security.SecureRandom}-backed randomness
 * supplied via the {@code randomSupplier} constructor argument, making game logic
 * fully testable with deterministic values.
 */
@Service
public class NumberDetectiveService {

    private static final String GAME_TYPE = "number-detective";

    private final Map<String, NumberDetectiveSession> sessions = new ConcurrentHashMap<>();
    private final GameResultRepository resultRepository;
    private final java.util.function.IntSupplier randomSupplier;

    /** Production constructor — uses SecureRandom. Spring autowires this one. */
    @Autowired
    public NumberDetectiveService(GameResultRepository resultRepository) {
        this.resultRepository = resultRepository;
        java.security.SecureRandom sr = new java.security.SecureRandom();
        this.randomSupplier = () ->
            sr.nextInt(NumberDetectiveSession.MAX_NUMBER - NumberDetectiveSession.MIN_NUMBER + 1)
            + NumberDetectiveSession.MIN_NUMBER;
    }

    /** Test constructor — accepts an injected IntSupplier for determinism. */
    public NumberDetectiveService(GameResultRepository resultRepository,
                                   java.util.function.IntSupplier randomSupplier) {
        this.resultRepository = resultRepository;
        this.randomSupplier = randomSupplier;
    }

    // ---- Session management ----

    public NumberDetectiveSession createSession() {
        int secret = randomSupplier.getAsInt();
        NumberDetectiveSession session = new NumberDetectiveSession(secret);
        sessions.put(session.getId(), session);
        return session;
    }

    public NumberDetectiveSession getSession(String sessionId) {
        NumberDetectiveSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException(sessionId);
        }
        return session;
    }

    public NumberDetectiveSession restartSession(String sessionId) {
        // Validate old session exists
        getSession(sessionId);
        // Remove old session and create a new one
        sessions.remove(sessionId);
        return createSession();
    }

    // ---- Game actions ----

    public GuessResponse processGuess(String sessionId, int guess) {
        NumberDetectiveSession session = getSession(sessionId);

        if (session.isFinished()) {
            throw new GameAlreadyFinishedException(sessionId);
        }

        GuessResult result = session.processGuess(guess);

        if (session.isFinished()) {
            persistResult(session);
        }

        Integer revealedSecret = session.isFinished() ? session.getSecretNumber() : null;

        return new GuessResponse(
                guess,
                result.name(),
                session.getStatus().name(),
                session.getGuessesUsed(),
                session.getGuessesRemaining(),
                revealedSecret
        );
    }

    // ---- Leaderboard ----

    public List<LeaderboardEntry> getLeaderboard() {
        List<GameResult> results = resultRepository.findLeaderboard(GAME_TYPE);
        return IntStream.range(0, results.size())
                .mapToObj(i -> {
                    GameResult r = results.get(i);
                    return new LeaderboardEntry(
                            i + 1,
                            r.getPlayerName() != null ? r.getPlayerName() : "Anonymous",
                            r.getGuessesUsed(),
                            r.getMaxGuesses(),
                            r.getCompletedAt()
                    );
                })
                .toList();
    }

    // ---- Helpers ----

    public SessionResponse toSessionResponse(NumberDetectiveSession session) {
        Integer revealedSecret = session.isFinished() ? session.getSecretNumber() : null;
        return new SessionResponse(
                session.getId(),
                session.getStatus().name(),
                session.getGuessesUsed(),
                session.getGuessesRemaining(),
                NumberDetectiveSession.MAX_GUESSES,
                revealedSecret
        );
    }

    private void persistResult(NumberDetectiveSession session) {
        GameResult result = new GameResult();
        result.setGameType(GAME_TYPE);
        result.setSessionId(session.getId());
        result.setOutcome(session.getStatus().name());
        result.setGuessesUsed(session.getGuessesUsed());
        result.setMaxGuesses(NumberDetectiveSession.MAX_GUESSES);
        result.setSecretNumber(session.getSecretNumber());
        result.setCompletedAt(session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now());
        resultRepository.save(result);
    }
}
