package com.arcade.backend.service;

import com.arcade.backend.dto.RpsRoundResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.InvalidMoveException;
import com.arcade.backend.model.*;
import com.arcade.backend.repository.RpsMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RockPaperScissorsServiceTest {

    private RpsMatchRepository matchRepository;
    private TestMoveGenerator moveGenerator;
    private RockPaperScissorsService service;

    static class TestMoveGenerator implements RpsMoveGenerator {
        private RpsMove nextMove = RpsMove.ROCK;

        public void setNextMove(RpsMove move) {
            this.nextMove = move;
        }

        @Override
        public RpsMove nextMove() {
            return nextMove;
        }
    }

    @BeforeEach
    void setUp() {
        matchRepository = mock(RpsMatchRepository.class);
        moveGenerator = new TestMoveGenerator();
        service = new RockPaperScissorsService(moveGenerator, matchRepository);
    }

    @ParameterizedTest(name = "Player {0} beats Computer {1}")
    @CsvSource({
        "ROCK, SCISSORS",
        "SCISSORS, PAPER",
        "PAPER, ROCK"
    })
    @DisplayName("Every winning combination returns WIN")
    void winningCombinations(String playerMove, String computerMove) {
        moveGenerator.setNextMove(RpsMove.valueOf(computerMove));
        RpsSession session = service.createSession("Alice");

        RpsRoundResponse response = service.playRound(session.getSessionId(), playerMove);

        assertThat(response.result()).isEqualTo("WIN");
        assertThat(response.playerScore()).isEqualTo(1);
        assertThat(response.computerScore()).isEqualTo(0);
        assertThat(response.draws()).isEqualTo(0);
    }

    @ParameterizedTest(name = "Player {0} loses to Computer {1}")
    @CsvSource({
        "ROCK, PAPER",
        "SCISSORS, ROCK",
        "PAPER, SCISSORS"
    })
    @DisplayName("Every losing combination returns LOSS")
    void losingCombinations(String playerMove, String computerMove) {
        moveGenerator.setNextMove(RpsMove.valueOf(computerMove));
        RpsSession session = service.createSession("Alice");

        RpsRoundResponse response = service.playRound(session.getSessionId(), playerMove);

        assertThat(response.result()).isEqualTo("LOSS");
        assertThat(response.playerScore()).isEqualTo(0);
        assertThat(response.computerScore()).isEqualTo(1);
        assertThat(response.draws()).isEqualTo(0);
    }

    @ParameterizedTest(name = "Both choose {0} results in DRAW")
    @CsvSource({
        "ROCK",
        "PAPER",
        "SCISSORS"
    })
    @DisplayName("All draw combinations return DRAW")
    void drawCombinations(String move) {
        moveGenerator.setNextMove(RpsMove.valueOf(move));
        RpsSession session = service.createSession("Alice");

        RpsRoundResponse response = service.playRound(session.getSessionId(), move);

        assertThat(response.result()).isEqualTo("DRAW");
        assertThat(response.playerScore()).isEqualTo(0);
        assertThat(response.computerScore()).isEqualTo(0);
        assertThat(response.draws()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects invalid move strings")
    void invalidMoves() {
        RpsSession session = service.createSession("Alice");

        assertThatThrownBy(() -> service.playRound(session.getSessionId(), "LIZARD"))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("Invalid move");

        assertThatThrownBy(() -> service.playRound(session.getSessionId(), null))
                .isInstanceOf(InvalidMoveException.class);
    }

    @Test
    @DisplayName("Match progression: tracks 5 rounds and concludes with PLAYER_WON")
    void matchProgression_playerWon() {
        RpsSession session = service.createSession("Winner");
        moveGenerator.setNextMove(RpsMove.SCISSORS); // Player playing ROCK will win

        for (int i = 0; i < 4; i++) {
            RpsRoundResponse r = service.playRound(session.getSessionId(), "ROCK");
            assertThat(r.matchFinished()).isFalse();
            assertThat(r.roundsRemaining()).isEqualTo(4 - i);
        }

        // 5th round concludes match
        RpsRoundResponse finalRound = service.playRound(session.getSessionId(), "ROCK");
        assertThat(finalRound.matchFinished()).isTrue();
        assertThat(finalRound.roundsRemaining()).isEqualTo(0);
        assertThat(finalRound.matchOutcome()).isEqualTo("PLAYER_WON");
        assertThat(finalRound.playerScore()).isEqualTo(5);

        // Verification of persistence
        ArgumentCaptor<RpsMatch> captor = ArgumentCaptor.forClass(RpsMatch.class);
        verify(matchRepository, times(1)).save(captor.capture());
        RpsMatch saved = captor.getValue();
        assertThat(saved.getOutcome()).isEqualTo("PLAYER_WON");
        assertThat(saved.getPlayerScore()).isEqualTo(5);
        assertThat(saved.getPlayerName()).isEqualTo("Winner");
    }

    @Test
    @DisplayName("Rejects extra moves once match is completed")
    void extraMovesRejectedAfterCompletion() {
        RpsSession session = service.createSession("Alice");
        moveGenerator.setNextMove(RpsMove.ROCK);

        for (int i = 0; i < 5; i++) {
            service.playRound(session.getSessionId(), "ROCK");
        }

        assertThatThrownBy(() -> service.playRound(session.getSessionId(), "ROCK"))
                .isInstanceOf(GameAlreadyFinishedException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    @DisplayName("Restart creates a fresh session with reset scores")
    void restartSession() {
        RpsSession session = service.createSession("Alice");
        moveGenerator.setNextMove(RpsMove.SCISSORS);
        service.playRound(session.getSessionId(), "ROCK");

        RpsSession restarted = service.restartSession(session.getSessionId());

        assertThat(restarted.getSessionId()).isNotEqualTo(session.getSessionId());
        assertThat(restarted.getPlayerScore()).isEqualTo(0);
        assertThat(restarted.getRoundsRemaining()).isEqualTo(5);
    }
}
