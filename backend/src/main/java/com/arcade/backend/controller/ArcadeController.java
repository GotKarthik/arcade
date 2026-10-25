package com.arcade.backend.controller;

import com.arcade.backend.dto.GuessRequest;
import com.arcade.backend.dto.GuessResponse;
import com.arcade.backend.dto.LeaderboardEntry;
import com.arcade.backend.dto.SessionResponse;
import com.arcade.backend.model.NumberDetectiveSession;
import com.arcade.backend.service.NumberDetectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Arcade", description = "Arcade gaming platform API")
public class ArcadeController {

    private final NumberDetectiveService service;

    public ArcadeController(NumberDetectiveService service) {
        this.service = service;
    }

    // ---- Health ----

    @GetMapping("/health")
    @Operation(summary = "Application health check")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "arcade-backend");
    }

    // ---- Game catalog ----

    @GetMapping("/games")
    @Operation(summary = "List all available games")
    public List<Map<String, String>> listGames() {
        return List.of(
            Map.of(
                "id", "number-detective",
                "name", "Number Detective",
                "description", "Guess the secret number between 0 and 100 in 7 tries.",
                "path", "/games/number-detective"
            )
        );
    }

    // ---- Number Detective sessions ----

    @PostMapping("/games/number-detective/sessions")
    @Operation(summary = "Start a new Number Detective game session")
    public ResponseEntity<SessionResponse> createSession() {
        NumberDetectiveSession session = service.createSession();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.toSessionResponse(session));
    }

    @GetMapping("/games/number-detective/sessions/{id}")
    @Operation(summary = "Retrieve public session status")
    public SessionResponse getSession(@PathVariable String id) {
        NumberDetectiveSession session = service.getSession(id);
        return service.toSessionResponse(session);
    }

    @PostMapping("/games/number-detective/sessions/{id}/guesses")
    @Operation(summary = "Submit a guess")
    public GuessResponse submitGuess(
            @PathVariable String id,
            @Valid @RequestBody GuessRequest request) {
        return service.processGuess(id, request.guess());
    }

    @PostMapping("/games/number-detective/sessions/{id}/restart")
    @Operation(summary = "Start a new round (creates a fresh session)")
    public ResponseEntity<SessionResponse> restart(@PathVariable String id) {
        NumberDetectiveSession newSession = service.restartSession(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.toSessionResponse(newSession));
    }

    // ---- Leaderboard ----

    @GetMapping("/leaderboard")
    @Operation(summary = "Retrieve ranked completed results")
    public List<LeaderboardEntry> leaderboard(
            @RequestParam(defaultValue = "number-detective") String game) {
        return service.getLeaderboard();
    }
}
