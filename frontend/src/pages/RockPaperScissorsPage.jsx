import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'

const MOVE_ICONS = {
  ROCK: '🪨',
  PAPER: '📄',
  SCISSORS: '✂️',
}

export default function RockPaperScissorsPage() {
  const [session, setSession] = useState(null)
  const [lastRound, setLastRound] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [playerName, setPlayerName] = useState('')
  const [history, setHistory] = useState([])

  const loadHistory = () => {
    api.getRpsHistory()
      .then(setHistory)
      .catch(() => {})
  }

  useEffect(() => {
    loadHistory()
  }, [])

  const startMatch = async () => {
    setLoading(true)
    setError(null)
    setLastRound(null)
    try {
      const newSession = await api.createRpsSession(playerName)
      setSession(newSession)
    } catch (err) {
      setError(err.message || 'Failed to start match')
    } finally {
      setLoading(false)
    }
  }

  const playMove = async (move) => {
    if (!session || session.outcome !== 'IN_PROGRESS' || loading) return
    setLoading(true)
    setError(null)
    try {
      const roundRes = await api.playRpsMove(session.sessionId, move)
      setLastRound(roundRes)
      // Refresh session
      const updated = await api.getRpsSession(session.sessionId)
      setSession(updated)
      if (roundRes.matchFinished) {
        loadHistory()
      }
    } catch (err) {
      setError(err.message || 'Failed to submit move')
    } finally {
      setLoading(false)
    }
  }

  const handleRestart = async () => {
    if (!session) return
    setLoading(true)
    setError(null)
    setLastRound(null)
    try {
      const restarted = await api.restartRps(session.sessionId)
      setSession(restarted)
    } catch (err) {
      setError(err.message || 'Failed to restart')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="container">
      <div className="game-wrapper">
        <Link to="/" className="back-link">← Back to Catalog</Link>

        <div className="game-header">
          <div className="badge">5-Round Showdown</div>
          <h1>Rock Paper <span>Scissors</span></h1>
          <p>Challenge the server's AI in a classic battle of wits. First to dominate 5 rounds wins.</p>
        </div>

        <div className="game-panel">
          {error && <div className="error-box" role="alert">⚠ {error}</div>}

          {!session ? (
            <div className="start-prompt">
              <div className="instructions">
                <h3>Match Rules</h3>
                <ul>
                  <li>Rock crushes Scissors.</li>
                  <li>Scissors cuts Paper.</li>
                  <li>Paper covers Rock.</li>
                  <li>Play 5 authoritative rounds against the server.</li>
                </ul>
              </div>

              <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                <input
                  type="text"
                  placeholder="Enter your nickname (optional)"
                  value={playerName}
                  onChange={(e) => setPlayerName(e.target.value)}
                  className="guess-input"
                  style={{ maxWidth: '300px' }}
                  aria-label="Player nickname"
                />
              </div>

              <button
                id="start-rps-btn"
                className="btn btn-primary"
                onClick={startMatch}
                disabled={loading}
              >
                {loading ? 'Starting...' : 'Start Match'}
              </button>
            </div>
          ) : (
            <div>
              {/* Scoreboard */}
              <div className="rps-scoreboard">
                <div className="rps-score-item">
                  <h4>{session.playerName || 'You'}</h4>
                  <div className="rps-score-val" id="player-score">{session.playerScore}</div>
                </div>
                <div className="rps-score-item">
                  <h4>Draws</h4>
                  <div className="rps-score-val" id="draws-count">{session.draws}</div>
                </div>
                <div className="rps-score-item">
                  <h4>Computer</h4>
                  <div className="rps-score-val" id="computer-score">{session.computerScore}</div>
                </div>
                <div className="rps-score-item">
                  <h4>Remaining</h4>
                  <div className="rps-score-val" id="rounds-remaining">{session.roundsRemaining}</div>
                </div>
              </div>

              {/* Last round reveal */}
              {lastRound && (
                <div className="rps-round-display">
                  <div className="rps-move-box">
                    <span className="icon">{MOVE_ICONS[lastRound.playerMove]}</span>
                    <span>You ({lastRound.playerMove})</span>
                  </div>
                  <div className="rps-vs">VS</div>
                  <div className="rps-move-box">
                    <span className="icon">{MOVE_ICONS[lastRound.computerMove]}</span>
                    <span>Computer ({lastRound.computerMove})</span>
                  </div>
                  <div style={{ marginLeft: '1rem' }}>
                    <span className={`chip chip-${lastRound.result === 'WIN' ? 'low' : lastRound.result === 'LOSS' ? 'high' : 'correct'}`}>
                      {lastRound.result === 'WIN' ? 'Round Won! 🎉' : lastRound.result === 'LOSS' ? 'Round Lost 😢' : 'Draw 🤝'}
                    </span>
                  </div>
                </div>
              )}

              {/* Match in progress: Choose move */}
              {session.outcome === 'IN_PROGRESS' ? (
                <div>
                  <h3 style={{ textAlign: 'center', margin: '1.5rem 0 0.5rem' }}>Choose your move:</h3>
                  <div className="rps-choices" role="group" aria-label="Move choices">
                    <button
                      id="rps-rock-btn"
                      className="rps-btn"
                      onClick={() => playMove('ROCK')}
                      disabled={loading}
                      aria-label="Choose Rock"
                    >
                      <span className="rps-btn-icon">🪨</span>
                      <span>Rock</span>
                    </button>
                    <button
                      id="rps-paper-btn"
                      className="rps-btn"
                      onClick={() => playMove('PAPER')}
                      disabled={loading}
                      aria-label="Choose Paper"
                    >
                      <span className="rps-btn-icon">📄</span>
                      <span>Paper</span>
                    </button>
                    <button
                      id="rps-scissors-btn"
                      className="rps-btn"
                      onClick={() => playMove('SCISSORS')}
                      disabled={loading}
                      aria-label="Choose Scissors"
                    >
                      <span className="rps-btn-icon">✂️</span>
                      <span>Scissors</span>
                    </button>
                  </div>
                </div>
              ) : (
                /* Match finished */
                <div className="end-banner">
                  {session.outcome === 'PLAYER_WON' && (
                    <>
                      <div className="end-banner-icon">🏆</div>
                      <h2>VICTORY!</h2>
                      <p>You outsmarted the computer with a final score of {session.playerScore} to {session.computerScore}!</p>
                    </>
                  )}
                  {session.outcome === 'COMPUTER_WON' && (
                    <>
                      <div className="end-banner-icon">🤖</div>
                      <h2>DEFEAT!</h2>
                      <p>The computer won the match {session.computerScore} to {session.playerScore}.</p>
                    </>
                  )}
                  {session.outcome === 'DRAW' && (
                    <>
                      <div className="end-banner-icon">🤝</div>
                      <h2>DRAW MATCH!</h2>
                      <p>Both players tied with {session.playerScore} wins each.</p>
                    </>
                  )}
                  <button
                    id="rps-play-again-btn"
                    className="btn btn-primary"
                    style={{ marginTop: '1.5rem' }}
                    onClick={handleRestart}
                    disabled={loading}
                  >
                    Play Again
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* History section */}
        <div className="leaderboard-panel" style={{ marginTop: '2.5rem' }}>
          <h3>Recent Matches</h3>
          {history.length === 0 ? (
            <p className="empty-history">No matches played yet. Be the first!</p>
          ) : (
            <div className="leaderboard-list">
              {history.slice(0, 5).map((match, idx) => (
                <div key={match.id || idx} className="leaderboard-row">
                  <span className="leaderboard-name">{match.playerName || 'Player'}</span>
                  <span className={`chip chip-${match.outcome === 'PLAYER_WON' ? 'low' : match.outcome === 'COMPUTER_WON' ? 'high' : 'correct'}`}>
                    {match.outcome === 'PLAYER_WON' ? 'Won' : match.outcome === 'COMPUTER_WON' ? 'Lost' : 'Draw'} ({match.playerScore}-{match.computerScore})
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
