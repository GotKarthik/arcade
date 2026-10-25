package com.arcade.backend.controller;

import com.arcade.backend.dto.*;
import com.arcade.backend.model.*;
import com.arcade.backend.service.MemoryMatchService;
import com.arcade.backend.service.NumberDetectiveService;
import com.arcade.backend.service.RockPaperScissorsService;
import com.arcade.backend.service.TicTacToeService;
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

    private final NumberDetectiveService numberDetectiveService;
    private final RockPaperScissorsService rpsService;
    private final TicTacToeService tttService;
    private final MemoryMatchService memoryService;

    public ArcadeController(
            NumberDetectiveService numberDetectiveService,
            RockPaperScissorsService rpsService,
            TicTacToeService tttService,
            MemoryMatchService memoryService) {
        this.numberDetectiveService = numberDetectiveService;
        this.rpsService = rpsService;
        this.tttService = tttService;
        this.memoryService = memoryService;
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
                "path", "/games/number-detective",
                "icon", "🔢"
            ),
            Map.of(
                "id", "rock-paper-scissors",
                "name", "Rock Paper Scissors",
                "description", "Challenge the computer in a best-of-5 classic showdown.",
                "path", "/games/rock-paper-scissors",
                "icon", "✂️"
            ),
            Map.of(
                "id", "tic-tac-toe",
                "name", "Tic-Tac-Toe",
                "description", "Classic 3x3 strategy game for two players on the same device.",
                "path", "/games/tic-tac-toe",
                "icon", "❌"
            ),
            Map.of(
                "id", "memory-match",
                "name", "Memory Match",
                "description", "Test your recall by matching 8 pairs in a 4x4 card grid.",
                "path", "/games/memory-match",
                "icon", "🃏"
            )
        );
    }

    // =========================================================================
    // Number Detective
    // =========================================================================

    @PostMapping("/games/number-detective/sessions")
    @Operation(summary = "Start a new Number Detective game session")
    public ResponseEntity<SessionResponse> createNumberDetectiveSession() {
        NumberDetectiveSession session = numberDetectiveService.createSession();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(numberDetectiveService.toSessionResponse(session));
    }

    @GetMapping("/games/number-detective/sessions/{id}")
    @Operation(summary = "Retrieve public session status")
    public SessionResponse getNumberDetectiveSession(@PathVariable String id) {
        NumberDetectiveSession session = numberDetectiveService.getSession(id);
        return numberDetectiveService.toSessionResponse(session);
    }

    @PostMapping("/games/number-detective/sessions/{id}/guesses")
    @Operation(summary = "Submit a guess")
    public GuessResponse submitGuess(
            @PathVariable String id,
            @Valid @RequestBody GuessRequest request) {
        return numberDetectiveService.processGuess(id, request.guess());
    }

    @PostMapping("/games/number-detective/sessions/{id}/restart")
    @Operation(summary = "Start a new round (creates a fresh session)")
    public ResponseEntity<SessionResponse> restartNumberDetective(@PathVariable String id) {
        NumberDetectiveSession newSession = numberDetectiveService.restartSession(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(numberDetectiveService.toSessionResponse(newSession));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Retrieve ranked completed results for Number Detective")
    public List<LeaderboardEntry> leaderboard(
            @RequestParam(defaultValue = "number-detective") String game) {
        return numberDetectiveService.getLeaderboard();
    }

    // =========================================================================
    // Rock Paper Scissors
    // =========================================================================

    @PostMapping("/games/rock-paper-scissors/sessions")
    @Operation(summary = "Start a new Rock Paper Scissors match")
    public ResponseEntity<RpsSessionResponse> createRpsSession(
            @RequestBody(required = false) Map<String, String> body) {
        String playerName = body != null ? body.get("playerName") : null;
        RpsSession session = rpsService.createSession(playerName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rpsService.toSessionResponse(session));
    }

    @GetMapping("/games/rock-paper-scissors/sessions/{id}")
    @Operation(summary = "Retrieve Rock Paper Scissors session status")
    public RpsSessionResponse getRpsSession(@PathVariable String id) {
        return rpsService.toSessionResponse(rpsService.getSession(id));
    }

    @PostMapping("/games/rock-paper-scissors/sessions/{id}/moves")
    @Operation(summary = "Play a round in Rock Paper Scissors")
    public RpsRoundResponse playRpsMove(
            @PathVariable String id,
            @Valid @RequestBody RpsMoveRequest request) {
        return rpsService.playRound(id, request.move());
    }

    @PostMapping("/games/rock-paper-scissors/sessions/{id}/restart")
    @Operation(summary = "Restart Rock Paper Scissors match")
    public ResponseEntity<RpsSessionResponse> restartRps(@PathVariable String id) {
        RpsSession session = rpsService.restartSession(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rpsService.toSessionResponse(session));
    }

    @GetMapping("/games/rock-paper-scissors/history")
    @Operation(summary = "Get recent Rock Paper Scissors matches")
    public List<RpsMatch> getRpsHistory() {
        return rpsService.getRecentMatches();
    }

    // =========================================================================
    // Tic-Tac-Toe
    // =========================================================================

    @PostMapping("/games/tic-tac-toe/sessions")
    @Operation(summary = "Start a new Tic-Tac-Toe match")
    public ResponseEntity<TttSessionResponse> createTttSession() {
        TttSession session = tttService.createSession();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tttService.toSessionResponse(session));
    }

    @GetMapping("/games/tic-tac-toe/sessions/{id}")
    @Operation(summary = "Retrieve Tic-Tac-Toe session status")
    public TttSessionResponse getTttSession(@PathVariable String id) {
        return tttService.toSessionResponse(tttService.getSession(id));
    }

    @PostMapping("/games/tic-tac-toe/sessions/{id}/moves")
    @Operation(summary = "Make a move in Tic-Tac-Toe")
    public TttSessionResponse playTttMove(
            @PathVariable String id,
            @Valid @RequestBody TttMoveRequest request) {
        TttSession session = tttService.makeMove(id, request.position(), request.player());
        return tttService.toSessionResponse(session);
    }

    @PostMapping("/games/tic-tac-toe/sessions/{id}/restart")
    @Operation(summary = "Restart Tic-Tac-Toe match")
    public ResponseEntity<TttSessionResponse> restartTtt(@PathVariable String id) {
        TttSession session = tttService.restartSession(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tttService.toSessionResponse(session));
    }

    @GetMapping("/games/tic-tac-toe/history")
    @Operation(summary = "Get recent Tic-Tac-Toe matches")
    public List<TttMatch> getTttHistory() {
        return tttService.getRecentMatches();
    }

    // =========================================================================
    // Memory Match
    // =========================================================================

    @PostMapping("/games/memory-match/sessions")
    @Operation(summary = "Start a new Memory Match game")
    public ResponseEntity<MemorySessionResponse> createMemorySession(
            @RequestBody(required = false) Map<String, String> body) {
        String playerName = body != null ? body.get("playerName") : null;
        MemorySession session = memoryService.createSession(playerName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memoryService.toSessionResponse(session));
    }

    @GetMapping("/games/memory-match/sessions/{id}")
    @Operation(summary = "Retrieve Memory Match session status")
    public MemorySessionResponse getMemorySession(@PathVariable String id) {
        return memoryService.toSessionResponse(memoryService.getSession(id));
    }

    @PostMapping("/games/memory-match/sessions/{id}/flips")
    @Operation(summary = "Flip a card in Memory Match")
    public MemorySessionResponse flipMemoryCard(
            @PathVariable String id,
            @Valid @RequestBody MemoryFlipRequest request) {
        MemorySession session = memoryService.flipCard(id, request.position());
        return memoryService.toSessionResponse(session);
    }

    @PostMapping("/games/memory-match/sessions/{id}/restart")
    @Operation(summary = "Restart Memory Match game")
    public ResponseEntity<MemorySessionResponse> restartMemory(@PathVariable String id) {
        MemorySession session = memoryService.restartSession(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(memoryService.toSessionResponse(session));
    }

    @GetMapping("/games/memory-match/leaderboard")
    @Operation(summary = "Get Memory Match leaderboard")
    public List<MemoryLeaderboardEntry> getMemoryLeaderboard() {
        return memoryService.getLeaderboard();
    }
}
