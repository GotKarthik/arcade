package com.arcade.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the REST API using full Spring Boot context + H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArcadeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/health returns UP status")
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/games returns game catalog")
    void listGames_returnsCatalog() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("number-detective"));
    }

    @Test
    @DisplayName("POST /api/games/number-detective/sessions creates a session")
    void createSession_returns201WithSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/games/number-detective/sessions"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.guessesUsed").value(0))
                .andExpect(jsonPath("$.guessesRemaining").value(7))
                .andExpect(jsonPath("$.secretNumber").isEmpty())
                .andReturn();

        // Verify secret is null (not a real number) — confidentiality confirmed
        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("\"secretNumber\":null");
    }

    @Test
    @DisplayName("Full game flow: create session, submit guesses, win")
    void fullGameFlow_win() throws Exception {
        // Create session
        MvcResult createResult = mockMvc.perform(post("/api/games/number-detective/sessions"))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> session = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), Map.class);
        String sessionId = (String) session.get("sessionId");

        // Submit wrong guess
        String wrongGuess = objectMapper.writeValueAsString(Map.of("guess", 50));
        mockMvc.perform(post("/api/games/number-detective/sessions/{id}/guesses", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(wrongGuess))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionStatus").value("IN_PROGRESS"));

        // GET session — secret still hidden
        mockMvc.perform(get("/api/games/number-detective/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guessesUsed").value(1));
    }

    @Test
    @DisplayName("Invalid guess (out of range) returns 400")
    void guess_outOfRange_returns400() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/games/number-detective/sessions"))
                .andExpect(status().isCreated()).andReturn();
        Map<?, ?> session = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), Map.class);
        String sessionId = (String) session.get("sessionId");

        String badGuess = objectMapper.writeValueAsString(Map.of("guess", 150));
        mockMvc.perform(post("/api/games/number-detective/sessions/{id}/guesses", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badGuess))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Unknown session ID returns 404")
    void getSession_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/games/number-detective/sessions/non-existent-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Guess on finished session returns 409")
    void guess_finishedSession_returns409() throws Exception {
        // Create session
        MvcResult createResult = mockMvc.perform(post("/api/games/number-detective/sessions"))
                .andExpect(status().isCreated()).andReturn();
        Map<?, ?> session = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), Map.class);
        String sessionId = (String) session.get("sessionId");

        // Exhaust all guesses with wrong answers
        for (int i = 0; i < 7; i++) {
            String guess = objectMapper.writeValueAsString(Map.of("guess", 1));
            mockMvc.perform(post("/api/games/number-detective/sessions/{id}/guesses", sessionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(guess))
                    .andExpect(status().isOk());
        }

        // Another guess should return 409
        String extraGuess = objectMapper.writeValueAsString(Map.of("guess", 2));
        mockMvc.perform(post("/api/games/number-detective/sessions/{id}/guesses", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(extraGuess))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/leaderboard returns list")
    void leaderboard_returnsList() throws Exception {
        mockMvc.perform(get("/api/leaderboard?game=number-detective"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Restart creates a new session")
    void restart_createsNewSession() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/games/number-detective/sessions"))
                .andExpect(status().isCreated()).andReturn();
        Map<?, ?> session = objectMapper.readValue(
            createResult.getResponse().getContentAsString(), Map.class);
        String sessionId = (String) session.get("sessionId");

        MvcResult restartResult = mockMvc.perform(
                post("/api/games/number-detective/sessions/{id}/restart", sessionId))
                .andExpect(status().isCreated())
                .andReturn();

        Map<?, ?> newSession = objectMapper.readValue(
            restartResult.getResponse().getContentAsString(), Map.class);

        assertThat(newSession.get("sessionId")).isNotEqualTo(sessionId);
        assertThat(newSession.get("guessesUsed")).isEqualTo(0);
    }
}
