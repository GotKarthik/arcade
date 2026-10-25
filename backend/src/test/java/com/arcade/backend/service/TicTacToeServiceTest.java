package com.arcade.backend.service;

import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.InvalidMoveException;
import com.arcade.backend.model.TttMatch;
import com.arcade.backend.model.TttOutcome;
import com.arcade.backend.model.TttPlayer;
import com.arcade.backend.model.TttSession;
import com.arcade.backend.repository.TttMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TicTacToeServiceTest {

    private TttMatchRepository matchRepository;
    private TicTacToeService service;

    @BeforeEach
    void setUp() {
        matchRepository = mock(TttMatchRepository.class);
        service = new TicTacToeService(matchRepository);
    }

    @Test
    @DisplayName("Horizontal win on row 0: X wins with line [0, 1, 2]")
    void horizontalWin_row0() {
        TttSession session = service.createSession();
        // X: 0, O: 3, X: 1, O: 4, X: 2
        service.makeMove(session.getSessionId(), 0, "X");
        service.makeMove(session.getSessionId(), 3, "O");
        service.makeMove(session.getSessionId(), 1, "X");
        service.makeMove(session.getSessionId(), 4, "O");
        TttSession endSession = service.makeMove(session.getSessionId(), 2, "X");

        assertThat(endSession.getOutcome()).isEqualTo(TttOutcome.X_WON);
        assertThat(endSession.getWinningLine()).isEqualTo(List.of(0, 1, 2));
        assertThat(endSession.isFinished()).isTrue();

        verify(matchRepository, times(1)).save(any(TttMatch.class));
    }

    @Test
    @DisplayName("Horizontal win on row 1: O wins with line [3, 4, 5]")
    void horizontalWin_row1_oWins() {
        TttSession session = service.createSession();
        // X: 0, O: 3, X: 1, O: 4, X: 8, O: 5
        service.makeMove(session.getSessionId(), 0, "X");
        service.makeMove(session.getSessionId(), 3, "O");
        service.makeMove(session.getSessionId(), 1, "X");
        service.makeMove(session.getSessionId(), 4, "O");
        service.makeMove(session.getSessionId(), 8, "X");
        TttSession endSession = service.makeMove(session.getSessionId(), 5, "O");

        assertThat(endSession.getOutcome()).isEqualTo(TttOutcome.O_WON);
        assertThat(endSession.getWinningLine()).isEqualTo(List.of(3, 4, 5));
    }

    @Test
    @DisplayName("Vertical win on col 0: [0, 3, 6]")
    void verticalWin_col0() {
        TttSession session = service.createSession();
        // X: 0, O: 1, X: 3, O: 2, X: 6
        service.makeMove(session.getSessionId(), 0, "X");
        service.makeMove(session.getSessionId(), 1, "O");
        service.makeMove(session.getSessionId(), 3, "X");
        service.makeMove(session.getSessionId(), 2, "O");
        TttSession endSession = service.makeMove(session.getSessionId(), 6, "X");

        assertThat(endSession.getOutcome()).isEqualTo(TttOutcome.X_WON);
        assertThat(endSession.getWinningLine()).isEqualTo(List.of(0, 3, 6));
    }

    @Test
    @DisplayName("Main diagonal win: [0, 4, 8]")
    void diagonalWin_main() {
        TttSession session = service.createSession();
        // X: 0, O: 1, X: 4, O: 2, X: 8
        service.makeMove(session.getSessionId(), 0, "X");
        service.makeMove(session.getSessionId(), 1, "O");
        service.makeMove(session.getSessionId(), 4, "X");
        service.makeMove(session.getSessionId(), 2, "O");
        TttSession endSession = service.makeMove(session.getSessionId(), 8, "X");

        assertThat(endSession.getOutcome()).isEqualTo(TttOutcome.X_WON);
        assertThat(endSession.getWinningLine()).isEqualTo(List.of(0, 4, 8));
    }

    @Test
    @DisplayName("Anti-diagonal win: [2, 4, 6]")
    void diagonalWin_anti() {
        TttSession session = service.createSession();
        // X: 2, O: 0, X: 4, O: 1, X: 6
        service.makeMove(session.getSessionId(), 2, "X");
        service.makeMove(session.getSessionId(), 0, "O");
        service.makeMove(session.getSessionId(), 4, "X");
        service.makeMove(session.getSessionId(), 1, "O");
        TttSession endSession = service.makeMove(session.getSessionId(), 6, "X");

        assertThat(endSession.getOutcome()).isEqualTo(TttOutcome.X_WON);
        assertThat(endSession.getWinningLine()).isEqualTo(List.of(2, 4, 6));
    }

    @Test
    @DisplayName("Draw match when all 9 squares are filled with no 3-in-a-row")
    void drawMatch() {
        TttSession session = service.createSession();
        // Board layout for draw:
        // X O X
        // X X O
        // O X O
        // Moves:
        // X: 0, O: 1
        // X: 2, O: 5
        // X: 3, O: 6
        // X: 4, O: 8
        // X: 7
        service.makeMove(session.getSessionId(), 0, "X");
        service.makeMove(session.getSessionId(), 1, "O");
        service.makeMove(session.getSessionId(), 2, "X");
        service.makeMove(session.getSessionId(), 5, "O");
        service.makeMove(session.getSessionId(), 3, "X");
        service.makeMove(session.getSessionId(), 6, "O");
        service.makeMove(session.getSessionId(), 4, "X");
        service.makeMove(session.getSessionId(), 8, "O");
        TttSession endSession = service.makeMove(session.getSessionId(), 7, "X");

        assertThat(endSession.getOutcome()).isEqualTo(TttOutcome.DRAW);
        assertThat(endSession.getWinningLine()).isNull();
        assertThat(endSession.isFinished()).isTrue();

        ArgumentCaptor<TttMatch> captor = ArgumentCaptor.forClass(TttMatch.class);
        verify(matchRepository).save(captor.capture());
        assertThat(captor.getValue().getOutcome()).isEqualTo("DRAW");
        assertThat(captor.getValue().getMovesCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("Rejects move on already occupied cell")
    void occupiedCellRejected() {
        TttSession session = service.createSession();
        service.makeMove(session.getSessionId(), 4, "X");

        assertThatThrownBy(() -> service.makeMove(session.getSessionId(), 4, "O"))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("already occupied");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 9, 100})
    @DisplayName("Rejects out-of-range cell positions")
    void outOfRangePositions(int pos) {
        TttSession session = service.createSession();

        assertThatThrownBy(() -> service.makeMove(session.getSessionId(), pos, "X"))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("between 0 and 8");
    }

    @Test
    @DisplayName("Enforces turn sequence: O cannot move when it is X's turn")
    void turnEnforcement() {
        TttSession session = service.createSession();
        assertThat(session.getCurrentTurn()).isEqualTo(TttPlayer.X);

        assertThatThrownBy(() -> service.makeMove(session.getSessionId(), 0, "O"))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("It is player X's turn");
    }

    @Test
    @DisplayName("Rejects moves after match completion")
    void moveAfterCompletionRejected() {
        TttSession session = service.createSession();
        // Win row 0 for X
        service.makeMove(session.getSessionId(), 0, "X");
        service.makeMove(session.getSessionId(), 3, "O");
        service.makeMove(session.getSessionId(), 1, "X");
        service.makeMove(session.getSessionId(), 4, "O");
        service.makeMove(session.getSessionId(), 2, "X");

        assertThatThrownBy(() -> service.makeMove(session.getSessionId(), 8, "O"))
                .isInstanceOf(GameAlreadyFinishedException.class)
                .hasMessageContaining("already finished");
    }

    @Test
    @DisplayName("Session isolation: moves in one session do not affect another")
    void sessionIsolation() {
        TttSession session1 = service.createSession();
        TttSession session2 = service.createSession();

        service.makeMove(session1.getSessionId(), 0, "X");

        assertThat(session1.getCell(0)).isEqualTo("X");
        assertThat(session2.getCell(0)).isNull();
    }
}
