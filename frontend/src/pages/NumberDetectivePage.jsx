import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useNumberDetective } from '../hooks/useNumberDetective'
import { api } from '../api/client'

const MAX_GUESSES = 7

function getChipClass(result) {
  if (result === 'CORRECT') return 'hit'
  if (result === 'TOO_LOW') return 'low'
  return 'high'
}

function getChipLabel(result) {
  if (result === 'CORRECT') return '✓'
  if (result === 'TOO_LOW') return '↑'
  return '↓'
}

export default function NumberDetectivePage() {
  const { session, lastGuess, loading, error, startGame, submitGuess, restart } =
    useNumberDetective()
  const [inputValue, setInputValue] = useState('')
  const [inputError, setInputError] = useState('')
  const [leaderboard, setLeaderboard] = useState([])
  const [guessHistory, setGuessHistory] = useState([])

  // Load leaderboard
  useEffect(() => {
    api.getLeaderboard().then(setLeaderboard).catch(() => {})
  }, [session?.status])

  // Track guess history locally for UI
  useEffect(() => {
    if (lastGuess) {
      setGuessHistory((prev) => [...prev, lastGuess])
    }
  }, [lastGuess])

  // Reset history on new game
  const handleStart = () => {
    setGuessHistory([])
    setInputValue('')
    setInputError('')
    startGame()
  }

  const handleRestart = () => {
    setGuessHistory([])
    setInputValue('')
    setInputError('')
    restart()
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const val = parseInt(inputValue, 10)
    if (isNaN(val) || inputValue.trim() === '') {
      setInputError('Please enter a number.')
      return
    }
    if (val < 0 || val > 100) {
      setInputError('Number must be between 0 and 100.')
      return
    }
    setInputError('')
    setInputValue('')
    submitGuess(val)
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleSubmit(e)
  }

  const isFinished = session?.status === 'WON' || session?.status === 'LOST'
  const guessesUsed = session?.guessesUsed || 0

  return (
    <div className="game-page">
      <div className="game-header">
        <Link to="/">← Games</Link>
        <h2 style={{ color: 'var(--clr-text)', margin: 0 }}>🔢 Number Detective</h2>
      </div>

      {!session ? (
        // Landing state
        <div className="game-panel">
          <div className="instructions">
            <h3>How to Play</h3>
            <ul>
              <li>I'm thinking of a number between 0 and 100.</li>
              <li>You have {MAX_GUESSES} guesses to find it.</li>
              <li>After each guess, I'll tell you if you're too high or too low.</li>
              <li>Guess exactly right to win!</li>
            </ul>
          </div>
          {error && (
            <div className="error-box" role="alert">⚠ {error}</div>
          )}
          <button
            id="start-game-btn"
            className="btn btn-primary"
            onClick={handleStart}
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center' }}
          >
            {loading ? <><div className="spinner" /> Starting…</> : '🎮 Start Game'}
          </button>
        </div>
      ) : (
        <div className="game-panel">
          {/* Attempts bar */}
          <div className="attempts-bar" aria-label={`${guessesUsed} of ${MAX_GUESSES} guesses used`}>
            {Array.from({ length: MAX_GUESSES }).map((_, i) => {
              const g = guessHistory[i]
              let cls = 'attempt-dot'
              if (g) cls += g.result === 'CORRECT' ? ' used' : ' wrong'
              return <div key={i} className={cls} />
            })}
          </div>

          <p style={{ textAlign: 'center', marginBottom: '1rem', fontSize: '0.9rem' }}>
            {isFinished
              ? session.status === 'WON'
                ? '🏆 You won!'
                : '💀 Better luck next time!'
              : `${session.guessesRemaining} guess${session.guessesRemaining !== 1 ? 'es' : ''} remaining`}
          </p>

          {/* Guess history chips */}
          {guessHistory.length > 0 && (
            <div className="guess-history" aria-label="Guess history">
              {guessHistory.map((g, i) => (
                <span
                  key={i}
                  className={`guess-chip ${getChipClass(g.result)}`}
                  title={g.result.replace('_', ' ')}
                >
                  {g.guess} {getChipLabel(g.result)}
                </span>
              ))}
            </div>
          )}

          {/* Last feedback bubble */}
          {lastGuess && (
            <div
              className={`feedback-bubble ${
                lastGuess.result === 'TOO_LOW' ? 'too-low' :
                lastGuess.result === 'TOO_HIGH' ? 'too-high' : 'correct'
              }`}
              role="status"
              aria-live="polite"
            >
              {lastGuess.result === 'CORRECT'
                ? `🎉 Correct! The number was ${lastGuess.guess}`
                : lastGuess.result === 'TOO_LOW'
                ? `📈 ${lastGuess.guess} is too low — guess higher!`
                : `📉 ${lastGuess.guess} is too high — guess lower!`}
            </div>
          )}

          {/* Game end state */}
          {isFinished && (
            <div className="game-end">
              {session.status === 'WON' ? (
                <>
                  <div className="result-icon">🏆</div>
                  <h2>You Won!</h2>
                  <div className="secret-reveal">{session.secretNumber}</div>
                  <p>
                    Solved in {guessesUsed} guess{guessesUsed !== 1 ? 'es' : ''}!
                  </p>
                </>
              ) : (
                <>
                  <div className="result-icon">💀</div>
                  <h2>Game Over</h2>
                  <div className="secret-reveal">{session.secretNumber}</div>
                  <p>The secret number was {session.secretNumber}. Better luck next time!</p>
                </>
              )}
              <button
                id="restart-btn"
                className="btn btn-primary"
                onClick={handleRestart}
                disabled={loading}
              >
                {loading ? 'Starting…' : '🔄 Play Again'}
              </button>
            </div>
          )}

          {/* Guess input */}
          {!isFinished && (
            <form onSubmit={handleSubmit} aria-label="Guess form">
              <div className="guess-row">
                <input
                  id="guess-input"
                  type="text"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  className="guess-input"
                  placeholder="0–100"
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  onKeyDown={handleKeyDown}
                  disabled={loading}
                  autoFocus
                  aria-label="Enter your guess"
                  aria-describedby={inputError ? 'input-error' : undefined}
                />
                <button
                  id="submit-guess-btn"
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                  aria-label="Submit guess"
                >
                  {loading ? <div className="spinner" /> : 'Guess'}
                </button>
              </div>
              {inputError && (
                <div id="input-error" className="error-box" role="alert">
                  ⚠ {inputError}
                </div>
              )}
            </form>
          )}

          {error && (
            <div className="error-box" role="alert" style={{ marginTop: '1rem' }}>
              ⚠ {error}
            </div>
          )}
        </div>
      )}

      {/* Leaderboard */}
      {leaderboard.length > 0 && (
        <div className="leaderboard">
          <h3>🏅 Top Players</h3>
          {leaderboard.slice(0, 10).map((entry, i) => (
            <div key={i} className="leaderboard-row">
              <div className={`rank-badge ${i === 0 ? 'gold' : i === 1 ? 'silver' : i === 2 ? 'bronze' : ''}`}>
                {entry.rank}
              </div>
              <span className="leaderboard-name">{entry.playerName}</span>
              <span className="leaderboard-score">
                {entry.guessesUsed}/{entry.maxGuesses} guesses
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
