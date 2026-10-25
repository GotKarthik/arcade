package com.arcade.backend.service;

import com.arcade.backend.dto.GuessResponse;
import com.arcade.backend.dto.SessionResponse;
import com.arcade.backend.exception.GameAlreadyFinishedException;
import com.arcade.backend.exception.SessionNotFoundException;
import com.arcade.backend.model.NumberDetectiveSession;
import com.arcade.backend.repository.GameResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NumberDetectiveService.
 * Uses deterministic secret numbers via constructor injection.
 * No database required — GameResultRepository is mocked.
 */
@ExtendWith(MockitoExtension.class)
class NumberDetectiveServiceTest {

    @Mock
    private GameResultRepository resultRepository;

    private NumberDetectiveService service;

    /** Always produce secret = 42 for deterministic testing. */
    @BeforeEach
    void setUp() {
        service = new NumberDetectiveService(resultRepository, () -> 42);
    }

    // ---- Session creation ----

    @Test
    @DisplayName("createSession returns a session in IN_PROGRESS status")
    void createSession_returnsInProgressSession() {
        NumberDetectiveSession session = service.createSession();
        assertThat(session).isNotNull();
        assertThat(session.getStatus().name()).isEqualTo("IN_PROGRESS");
        assertThat(session.getId()).isNotBlank();
        assertThat(session.getGuessesUsed()).isZero();
        assertThat(session.getGuessesRemaining()).isEqualTo(NumberDetectiveSession.MAX_GUESSES);
    }

    @Test
    @DisplayName("Secret number is within valid range (0-100)")
    void createSession_secretInRange() {
        // Use random supplier to test range
        NumberDetectiveService randomService = new NumberDetectiveService(resultRepository);
        for (int i = 0; i < 200; i++) {
            NumberDetectiveSession s = randomService.createSession();
            assertThat(s.getSecretNumber())
                .isBetween(NumberDetectiveSession.MIN_NUMBER, NumberDetectiveSession.MAX_NUMBER);
        }
    }

    // ---- Secret confidentiality ----

    @Test
    @DisplayName("Session response does NOT reveal secret while in progress")
    void sessionResponse_hidesSecretWhileInProgress() {
        NumberDetectiveSession session = service.createSession();
        SessionResponse response = service.toSessionResponse(session);
        assertThat(response.secretNumber()).isNull();
    }

    // ---- Guess feedback ----

    @Test
    @DisplayName("Too-low guess returns TOO_LOW")
    void guess_tooLow_returnsTooLow() {
        NumberDetectiveSession session = service.createSession();
        GuessResponse response = service.processGuess(session.getId(), 10);
        assertThat(response.result()).isEqualTo("TOO_LOW");
        assertThat(response.sessionStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.secretNumber()).isNull();
    }

    @Test
    @DisplayName("Too-high guess returns TOO_HIGH")
    void guess_tooHigh_returnsTooHigh() {
        NumberDetectiveSession session = service.createSession();
        GuessResponse response = service.processGuess(session.getId(), 90);
        assertThat(response.result()).isEqualTo("TOO_HIGH");
        assertThat(response.sessionStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.secretNumber()).isNull();
    }

    @Test
    @DisplayName("Correct guess returns CORRECT and reveals secret")
    void guess_correct_returnsCorrectAndRevealsSecret() {
        NumberDetectiveSession session = service.createSession();
        GuessResponse response = service.processGuess(session.getId(), 42);
        assertThat(response.result()).isEqualTo("CORRECT");
        assertThat(response.sessionStatus()).isEqualTo("WON");
        assertThat(response.secretNumber()).isEqualTo(42);
        verify(resultRepository).save(any());
    }

    // ---- Attempt counting ----

    @Test
    @DisplayName("Attempt counter increments correctly")
    void guess_incrementsAttemptCount() {
        NumberDetectiveSession session = service.createSession();
        service.processGuess(session.getId(), 10);
        service.processGuess(session.getId(), 20);
        GuessResponse r = service.processGuess(session.getId(), 30);
        assertThat(r.guessesUsed()).isEqualTo(3);
        assertThat(r.guessesRemaining()).isEqualTo(NumberDetectiveSession.MAX_GUESSES - 3);
    }

    @Test
    @DisplayName("Loss after exhausting all attempts")
    void guess_exhaustedAttempts_losesGame() {
        NumberDetectiveSession session = service.createSession();
        // All 7 guesses wrong (secret=42)
        for (int i = 0; i < NumberDetectiveSession.MAX_GUESSES; i++) {
            service.processGuess(session.getId(), 1);
        }
        assertThat(session.getStatus().name()).isEqualTo("LOST");
        verify(resultRepository).save(any());
    }

    // ---- Finished session ----

    @Test
    @DisplayName("Guess submitted to finished session throws GameAlreadyFinishedException")
    void guess_alreadyFinished_throwsException() {
        NumberDetectiveSession session = service.createSession();
        service.processGuess(session.getId(), 42); // WIN
        assertThatThrownBy(() -> service.processGuess(session.getId(), 50))
                .isInstanceOf(GameAlreadyFinishedException.class);
    }

    @Test
    @DisplayName("Session response reveals secret after game ends")
    void sessionResponse_revealsSecretAfterEnd() {
        NumberDetectiveSession session = service.createSession();
        service.processGuess(session.getId(), 42); // WIN
        SessionResponse response = service.toSessionResponse(session);
        assertThat(response.secretNumber()).isEqualTo(42);
    }

    // ---- Unknown session ----

    @Test
    @DisplayName("Unknown session ID throws SessionNotFoundException")
    void getSession_unknownId_throwsNotFound() {
        assertThatThrownBy(() -> service.getSession("non-existent-id"))
                .isInstanceOf(SessionNotFoundException.class);
    }

    // ---- Session isolation ----

    @Test
    @DisplayName("Two independent sessions do not interfere")
    void sessions_areIsolated() {
        NumberDetectiveSession s1 = service.createSession();
        NumberDetectiveSession s2 = service.createSession();
        assertThat(s1.getId()).isNotEqualTo(s2.getId());

        service.processGuess(s1.getId(), 1);
        assertThat(s1.getGuessesUsed()).isEqualTo(1);
        assertThat(s2.getGuessesUsed()).isEqualTo(0);
    }

    // ---- Restart ----

    @Test
    @DisplayName("Restart creates a new session with fresh state")
    void restartSession_createsFreshSession() {
        NumberDetectiveSession original = service.createSession();
        String originalId = original.getId();
        service.processGuess(originalId, 1); // make one guess

        NumberDetectiveSession newSession = service.restartSession(originalId);
        assertThat(newSession.getId()).isNotEqualTo(originalId);
        assertThat(newSession.getGuessesUsed()).isZero();
        assertThat(newSession.getStatus().name()).isEqualTo("IN_PROGRESS");

        // Old session should be gone
        assertThatThrownBy(() -> service.getSession(originalId))
                .isInstanceOf(SessionNotFoundException.class);
    }
}
