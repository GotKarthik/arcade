package com.arcade.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NewGamesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/games returns all 4 games in catalog")
    void listGames_returnsFourGames() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id", is("number-detective")))
                .andExpect(jsonPath("$[1].id", is("rock-paper-scissors")))
                .andExpect(jsonPath("$[2].id", is("tic-tac-toe")))
                .andExpect(jsonPath("$[3].id", is("memory-match")));
    }

    // =========================================================================
    // Rock Paper Scissors Integration Tests
    // =========================================================================

    @Test
    @DisplayName("RPS flow: create session, play move, get round feedback")
    void rpsFlow() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/games/rock-paper-scissors/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\": \"Hero\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.outcome", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.roundsRemaining", is(5)))
                .andReturn();

        String sessionId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.sessionId");

        // Submit move
        mockMvc.perform(post("/api/games/rock-paper-scissors/sessions/" + sessionId + "/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"move\": \"ROCK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roundNumber", is(1)))
                .andExpect(jsonPath("$.playerMove", is("ROCK")))
                .andExpect(jsonPath("$.computerMove", notNullValue()))
                .andExpect(jsonPath("$.result", anyOf(is("WIN"), is("LOSS"), is("DRAW"))))
                .andExpect(jsonPath("$.roundsRemaining", is(4)));

        // History endpoint
        mockMvc.perform(get("/api/games/rock-paper-scissors/history"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RPS rejects invalid move format")
    void rpsInvalidMove() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/games/rock-paper-scissors/sessions"))
                .andExpect(status().isCreated())
                .andReturn();
        String sessionId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.sessionId");

        mockMvc.perform(post("/api/games/rock-paper-scissors/sessions/" + sessionId + "/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"move\": \"INVALID\"}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Tic-Tac-Toe Integration Tests
    // =========================================================================

    @Test
    @DisplayName("TTT flow: create session, make move, check board")
    void tttFlow() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/games/tic-tac-toe/sessions"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.currentTurn", is("X")))
                .andExpect(jsonPath("$.outcome", is("IN_PROGRESS")))
                .andReturn();

        String sessionId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.sessionId");

        // Player X moves to center (4)
        mockMvc.perform(post("/api/games/tic-tac-toe/sessions/" + sessionId + "/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 4, \"player\": \"X\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.board[4]", is("X")))
                .andExpect(jsonPath("$.currentTurn", is("O")))
                .andExpect(jsonPath("$.movesCount", is(1)));

        // Next move with wrong player X should be rejected
        mockMvc.perform(post("/api/games/tic-tac-toe/sessions/" + sessionId + "/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 0, \"player\": \"X\"}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Memory Match Integration Tests
    // =========================================================================

    @Test
    @DisplayName("Memory Match flow: create session, flip card, verify anti-cheat masking")
    void memoryFlow() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/games/memory-match/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\": \"MemoryMaster\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId", notNullValue()))
                .andExpect(jsonPath("$.turnState", is("AWAITING_FIRST")))
                .andExpect(jsonPath("$.grid", hasSize(16)))
                // Verify all cards are face down with null symbols initially
                .andExpect(jsonPath("$.grid[0].symbol", nullValue()))
                .andReturn();

        String sessionId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.sessionId");

        // Flip card at position 3
        mockMvc.perform(post("/api/games/memory-match/sessions/" + sessionId + "/flips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.turnState", is("AWAITING_SECOND")))
                .andExpect(jsonPath("$.grid[3].revealed", is(true)))
                .andExpect(jsonPath("$.grid[3].symbol", notNullValue()))
                // Verify card 0 is still face down with null symbol
                .andExpect(jsonPath("$.grid[0].symbol", nullValue()));

        // Leaderboard endpoint
        mockMvc.perform(get("/api/games/memory-match/leaderboard"))
                .andExpect(status().isOk());
    }
}
