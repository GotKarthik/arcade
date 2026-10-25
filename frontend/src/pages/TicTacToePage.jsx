import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

export default function TicTacToePage() {
  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [history, setHistory] = useState([])

  const loadHistory = () => {
    api.getTttHistory()
      .then(setHistory)
      .catch(() => {})
  }

  useEffect(() => {
    loadHistory()
  }, [])

  const startMatch = async () => {
    setLoading(true)
    setError(null)
    try {
      const newSession = await api.createTttSession()
      setSession(newSession)
    } catch (err) {
      setError(err.message || 'Failed to start Tic-Tac-Toe match')
    } finally {
      setLoading(false)
    }
  }

  const makeMove = async (position) => {
    if (!session || session.finished || loading || session.board[position] !== null) return
    setLoading(true)
    setError(null)
    try {
      const updated = await api.playTttMove(session.sessionId, position, session.currentTurn)
      setSession(updated)
      if (updated.finished) {
        loadHistory()
      }
    } catch (err) {
      setError(err.message || 'Invalid move')
    } finally {
      setLoading(false)
    }
  }

  const restartMatch = async () => {
    if (!session) return
    setLoading(true)
    setError(null)
    try {
      const fresh = await api.restartTtt(session.sessionId)
      setSession(fresh)
    } catch (err) {
      setError(err.message || 'Failed to restart')
    } finally {
      setLoading(false)
    }
  }

  const isWinningCell = (index) => {
    return session?.winningLine?.includes(index)
  }

  return (
    <div className="container">
      <div className="game-wrapper">
        <Link to="/" className="back-link">← Back to Catalog</Link>

        <div className="game-header">
          <div className="badge">Local 2-Player</div>
          <h1>Tic-Tac-<span>Toe</span></h1>
          <p>Two players, one device. Get three marks in a row — horizontally, vertically, or diagonally.</p>
        </div>

        <div className="game-panel">
          {error && <div className="error-box" role="alert">⚠ {error}</div>}

          {!session ? (
            <div className="start-prompt">
              <div className="instructions">
                <h3>Instructions</h3>
                <ul>
                  <li>Player X moves first, followed by Player O.</li>
                  <li>Click any empty square to place your symbol.</li>
                  <li>First player to form a line of 3 wins.</li>
                </ul>
              </div>

              <button
                id="start-ttt-btn"
                className="btn btn-primary"
                onClick={startMatch}
                disabled={loading}
              >
                {loading ? 'Starting...' : 'Start Match'}
              </button>
            </div>
          ) : (
            <div>
              {/* Turn / Outcome Banner */}
              <div className="ttt-turn-banner">
                <span>Game Status:</span>
                {!session.finished ? (
                  <span className={`ttt-turn-badge ${session.currentTurn.toLowerCase()}`}>
                    Player {session.currentTurn}'s Turn
                  </span>
                ) : (
                  <span className="badge">
                    {session.outcome === 'X_WON' && '🎉 Player X Won!'}
                    {session.outcome === 'O_WON' && '🎉 Player O Won!'}
                    {session.outcome === 'DRAW' && '🤝 Draw Match!'}
                  </span>
                )}
              </div>

              {/* 3x3 Grid Board */}
              <div className="ttt-board" role="grid" aria-label="Tic Tac Toe board">
                {session.board.map((cell, index) => (
                  <button
                    key={index}
                    id={`cell-${index}`}
                    className={`ttt-cell ${cell ? cell.toLowerCase() : ''} ${isWinningCell(index) ? 'winning-cell' : ''}`}
                    onClick={() => makeMove(index)}
                    disabled={cell !== null || session.finished || loading}
                    aria-label={`Cell ${index}, ${cell || 'empty'}`}
                  >
                    {cell}
                  </button>
                ))}
              </div>

              {/* End of game banner */}
              {session.finished && (
                <div className="end-banner" style={{ marginTop: '1.5rem' }}>
                  <div className="end-banner-icon">
                    {session.outcome === 'DRAW' ? '🤝' : '🏆'}
                  </div>
                  <h2>
                    {session.outcome === 'X_WON' && 'Player X Wins!'}
                    {session.outcome === 'O_WON' && 'Player O Wins!'}
                    {session.outcome === 'DRAW' && "It's a Draw!"}
                  </h2>
                  <p>Match concluded in {session.movesCount} moves.</p>
                  <button
                    id="restart-ttt-btn"
                    className="btn btn-primary"
                    style={{ marginTop: '1rem' }}
                    onClick={restartMatch}
                    disabled={loading}
                  >
                    Play Again
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Match History */}
        <div className="leaderboard-panel" style={{ marginTop: '2.5rem' }}>
          <h3>Recent Matches</h3>
          {history.length === 0 ? (
            <p className="empty-history">No games played yet. Play a match above!</p>
          ) : (
            <div className="leaderboard-list">
              {history.slice(0, 5).map((match, idx) => (
                <div key={match.id || idx} className="leaderboard-row">
                  <span className="leaderboard-name">Match #{match.id}</span>
                  <span className="leaderboard-score">{match.outcome} ({match.movesCount} moves)</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
