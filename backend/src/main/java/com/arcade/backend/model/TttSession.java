package com.arcade.backend.model;

import java.time.Instant;
import java.util.List;

public class TttSession {
    private final String sessionId;
    private final String[] board = new String[9]; // null or "X" or "O"
    private TttPlayer currentTurn = TttPlayer.X;
    private TttOutcome outcome = TttOutcome.IN_PROGRESS;
    private List<Integer> winningLine;
    private int movesCount = 0;
    private final Instant startedAt = Instant.now();
    private Instant completedAt;

    public TttSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isFinished() {
        return outcome != TttOutcome.IN_PROGRESS;
    }

    public String getSessionId() { return sessionId; }
    public String[] getBoard() { return board.clone(); }
    public String getCell(int index) { return board[index]; }
    public void setCell(int index, String value) { board[index] = value; movesCount++; }
    public TttPlayer getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(TttPlayer currentTurn) { this.currentTurn = currentTurn; }
    public TttOutcome getOutcome() { return outcome; }
    public void setOutcome(TttOutcome outcome) { this.outcome = outcome; }
    public List<Integer> getWinningLine() { return winningLine; }
    public void setWinningLine(List<Integer> winningLine) { this.winningLine = winningLine; }
    public int getMovesCount() { return movesCount; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
