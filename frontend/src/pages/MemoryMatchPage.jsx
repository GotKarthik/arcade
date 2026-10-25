import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

export default function MemoryMatchPage() {
  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [playerName, setPlayerName] = useState('')
  const [elapsedTime, setElapsedTime] = useState(0)
  const [leaderboard, setLeaderboard] = useState([])
  const timerRef = useRef(null)

  const loadLeaderboard = () => {
    api.getMemoryLeaderboard()
      .then(setLeaderboard)
      .catch(() => {})
  }

  useEffect(() => {
    loadLeaderboard()
    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [])

  // Timer effect while game is active
  useEffect(() => {
    if (session && !session.completed) {
      timerRef.current = setInterval(() => {
        setElapsedTime((prev) => prev + 1)
      }, 1000)
    } else {
      if (timerRef.current) clearInterval(timerRef.current)
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [session?.completed, session?.sessionId])

  const startGame = async () => {
    setLoading(true)
    setError(null)
    setElapsedTime(0)
    try {
      const newSession = await api.createMemorySession(playerName)
      setSession(newSession)
    } catch (err) {
      setError(err.message || 'Failed to start Memory Match')
    } finally {
      setLoading(false)
    }
  }

  const handleCardClick = async (position) => {
    if (!session || session.completed || loading) return
    const card = session.grid[position]
    if (card.matched || card.revealed) return

    setLoading(true)
    setError(null)
    try {
      const updated = await api.flipMemoryCard(session.sessionId, position)
      setSession(updated)

      // If this flip resulted in a mismatch, wait 900ms and automatically reset the view
      if (updated.turnState === 'PAIR_MISMATCHED') {
        setTimeout(async () => {
          try {
            const refreshed = await api.getMemorySession(session.sessionId)
            setSession(refreshed)
          } catch (e) {
            // ignore
          }
        }, 900)
      }

      if (updated.completed) {
        loadLeaderboard()
      }
    } catch (err) {
      setError(err.message || 'Error flipping card')
    } finally {
      setLoading(false)
    }
  }

  const restartGame = async () => {
    if (!session) return
    setLoading(true)
    setError(null)
    setElapsedTime(0)
    try {
      const fresh = await api.restartMemory(session.sessionId)
      setSession(fresh)
    } catch (err) {
      setError(err.message || 'Failed to restart')
    } finally {
      setLoading(false)
    }
  }

  const formatTime = (secs) => {
    const mins = Math.floor(secs / 60)
    const remaining = secs % 60
    return `${mins}:${remaining < 10 ? '0' : ''}${remaining}`
  }

  return (
    <div className="container">
      <div className="game-wrapper">
        <Link to="/" className="back-link">← Back to Catalog</Link>

        <div className="game-header">
          <div className="badge">4x4 Card Grid · 8 Pairs</div>
          <h1>Memory <span>Match</span></h1>
          <p>Flip cards two by two to find all matching pairs. Memorize positions to clear the board in minimum moves.</p>
        </div>

        <div className="game-panel">
          {error && <div className="error-box" role="alert">⚠ {error}</div>}

          {!session ? (
            <div className="start-prompt">
              <div className="instructions">
                <h3>How to Play</h3>
                <ul>
                  <li>Flip any card to reveal its secret symbol.</li>
                  <li>Flip a second card to try to find its match.</li>
                  <li>Matching pairs remain face-up. Mismatches turn back face-down.</li>
                  <li>Clear all 8 pairs in the fewest moves and fastest time!</li>
                </ul>
              </div>

              <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                <input
                  type="text"
                  placeholder="Enter player nickname (optional)"
                  value={playerName}
                  onChange={(e) => setPlayerName(e.target.value)}
                  className="guess-input"
                  style={{ maxWidth: '300px' }}
                  aria-label="Player nickname"
                />
              </div>

              <button
                id="start-memory-btn"
                className="btn btn-primary"
                onClick={startGame}
                disabled={loading}
              >
                {loading ? 'Starting...' : 'Start Game'}
              </button>
            </div>
          ) : (
            <div>
              {/* Stats Bar */}
              <div className="memory-stats">
                <div>
                  <strong>Pairs:</strong> <span id="pairs-matched">{session.pairsMatched}</span> / {session.totalPairs}
                </div>
                <div>
                  <strong>Moves:</strong> <span id="moves-count">{session.movesCount}</span>
                </div>
                <div>
                  <strong>Time:</strong> <span id="elapsed-time">{formatTime(session.durationSeconds ?? elapsedTime)}</span>
                </div>
              </div>

              {/* 4x4 Cards Grid */}
              <div className="memory-grid" role="grid" aria-label="Memory match cards grid">
                {session.grid.map((card) => {
                  const isVisible = card.revealed || card.matched
                  return (
                    <button
                      key={card.position}
                      id={`card-${card.position}`}
                      className={`memory-card ${card.matched ? 'matched' : ''} ${card.revealed ? 'revealed' : ''} ${!isVisible ? 'face-down' : ''}`}
                      onClick={() => handleCardClick(card.position)}
                      disabled={card.matched || card.revealed || session.completed || loading}
                      aria-label={`Card ${card.position}, ${isVisible ? card.symbol : 'hidden'}`}
                    >
                      {isVisible ? card.symbol : null}
                    </button>
                  )
                })}
              </div>

              {/* Win Banner */}
              {session.completed && (
                <div className="end-banner" style={{ marginTop: '2rem' }}>
                  <div className="end-banner-icon">🎉</div>
                  <h2>Puzzle Solved!</h2>
                  <p>
                    Completed in <strong>{session.movesCount} moves</strong> and <strong>{session.durationSeconds || elapsedTime} seconds</strong>!
                  </p>
                  <button
                    id="restart-memory-btn"
                    className="btn btn-primary"
                    style={{ marginTop: '1.25rem' }}
                    onClick={restartGame}
                    disabled={loading}
                  >
                    Play Again
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Leaderboard */}
        <div className="leaderboard-panel" style={{ marginTop: '2.5rem' }}>
          <h3>Memory Match Hall of Fame</h3>
          {leaderboard.length === 0 ? (
            <p className="empty-history">No completions yet. Solve the puzzle to claim 1st place!</p>
          ) : (
            <div className="leaderboard-list">
              {leaderboard.slice(0, 10).map((entry) => (
                <div key={entry.rank} className="leaderboard-row">
                  <span className={`rank-badge ${entry.rank === 1 ? 'gold' : entry.rank === 2 ? 'silver' : entry.rank === 3 ? 'bronze' : ''}`}>
                    #{entry.rank}
                  </span>
                  <span className="leaderboard-name">{entry.playerName}</span>
                  <span className="leaderboard-score">{entry.movesCount} moves · {entry.durationSeconds}s</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
