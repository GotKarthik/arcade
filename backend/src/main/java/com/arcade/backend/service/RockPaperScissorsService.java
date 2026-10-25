package com.arcade.backend.service;

import com.arcade.backend.dto.RpsRoundResponse;
import com.arcade.backend.dto.RpsSessionResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.SessionNotFoundException;
import com.arcade.backend.model.*;
import com.arcade.backend.repository.RpsMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RockPaperScissorsService {

    public static final int DEFAULT_MAX_ROUNDS = 5;

    private final RpsMoveGenerator moveGenerator;
    private final RpsMatchRepository matchRepository;
    private final ConcurrentHashMap<String, RpsSession> sessions = new ConcurrentHashMap<>();

    public RockPaperScissorsService(RpsMoveGenerator moveGenerator, RpsMatchRepository matchRepository) {
        this.moveGenerator = moveGenerator;
        this.matchRepository = matchRepository;
    }

    public RpsSession createSession(String playerName) {
        String sessionId = UUID.randomUUID().toString();
        RpsSession session = new RpsSession(sessionId, DEFAULT_MAX_ROUNDS, playerName);
        sessions.put(sessionId, session);
        return session;
    }

    public RpsSession getSession(String sessionId) {
        RpsSession session = sessions.get(sessionId);
        if (session == null) {
            throw new SessionNotFoundException("Session not found: " + sessionId);
        }
        return session;
    }

    @Transactional
    public RpsRoundResponse playRound(String sessionId, String playerMoveStr) {
        RpsSession session = getSession(sessionId);

        if (session.isFinished()) {
            throw new GameAlreadyFinishedException("Match is already completed. Please restart to play again.");
        }

        RpsMove playerMove = RpsMove.fromString(playerMoveStr);
        RpsMove computerMove = moveGenerator.nextMove();

        RpsRoundResult result;
        if (playerMove == computerMove) {
            result = RpsRoundResult.DRAW;
        } else if (playerMove.beats(computerMove)) {
            result = RpsRoundResult.WIN;
        } else {
            result = RpsRoundResult.LOSS;
        }

        int roundNumber = session.getRoundsPlayed() + 1;
        RpsRound round = new RpsRound(roundNumber, playerMove, computerMove, result);
        session.addRound(round);

        // Persist when match completes
        if (session.isFinished()) {
            persistMatch(session);
        }

        return new RpsRoundResponse(
                roundNumber,
                playerMove.name(),
                computerMove.name(),
                result.name(),
                session.getPlayerScore(),
                session.getComputerScore(),
                session.getDraws(),
                session.getRoundsRemaining(),
                session.getOutcome().name(),
                session.isFinished()
        );
    }

    public RpsSession restartSession(String oldSessionId) {
        RpsSession oldSession = sessions.get(oldSessionId);
        String playerName = oldSession != null ? oldSession.getPlayerName() : "Player";
        if (oldSessionId != null) {
            sessions.remove(oldSessionId);
        }
        return createSession(playerName);
    }

    public List<RpsMatch> getRecentMatches() {
        return matchRepository.findRecentMatches();
    }

    public RpsSessionResponse toSessionResponse(RpsSession session) {
        return new RpsSessionResponse(
                session.getSessionId(),
                session.getOutcome().name(),
                session.getPlayerScore(),
                session.getComputerScore(),
                session.getDraws(),
                session.getRoundsPlayed(),
                session.getRoundsRemaining(),
                session.getMaxRounds(),
                session.getRounds(),
                session.getPlayerName()
        );
    }

    private void persistMatch(RpsSession session) {
        RpsMatch match = new RpsMatch();
        match.setSessionId(session.getSessionId());
        match.setOutcome(session.getOutcome().name());
        match.setPlayerScore(session.getPlayerScore());
        match.setComputerScore(session.getComputerScore());
        match.setDraws(session.getDraws());
        match.setRoundsPlayed(session.getRoundsPlayed());
        match.setMaxRounds(session.getMaxRounds());
        match.setCompletedAt(session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now());
        match.setPlayerName(session.getPlayerName());
        matchRepository.save(match);
    }
}
