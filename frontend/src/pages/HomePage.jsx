import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { api } from '../api/client'

const DEFAULT_GAMES = [
  {
    id: 'number-detective',
    name: 'Number Detective',
    description: 'Guess the secret number between 0 and 100 in 7 tries.',
    path: '/games/number-detective',
    icon: '🔢',
  },
  {
    id: 'rock-paper-scissors',
    name: 'Rock Paper Scissors',
    description: 'Challenge the computer in a best-of-5 classic showdown.',
    path: '/games/rock-paper-scissors',
    icon: '✂️',
  },
  {
    id: 'tic-tac-toe',
    name: 'Tic-Tac-Toe',
    description: 'Classic 3x3 strategy game for two players on the same device.',
    path: '/games/tic-tac-toe',
    icon: '❌',
  },
  {
    id: 'memory-match',
    name: 'Memory Match',
    description: 'Test your recall by matching 8 pairs in a 4x4 card grid.',
    path: '/games/memory-match',
    icon: '🃏',
  },
]

export default function HomePage() {
  const [games, setGames] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getGames()
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          setGames(data)
        } else {
          setGames(DEFAULT_GAMES)
        }
      })
      .catch(() => setGames(DEFAULT_GAMES))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="container">
      <section className="hero">
        <div className="badge">Free · No Registration · 4 Playable Games</div>
        <h1>Your Browser <span>Game Arcade</span></h1>
        <p>
          Jump into fun, lightweight games right in your browser.
          No downloads, no accounts — just play.
        </p>
      </section>

      <section aria-label="Available games">
        {loading ? (
          <div className="loader"><div className="spinner" /></div>
        ) : (
          <div className="game-grid">
            {games.map((game) => (
              <Link
                key={game.id}
                to={game.path || `/games/${game.id}`}
                className="game-card"
                aria-label={`Play ${game.name}`}
              >
                <div className="game-card-icon">{game.icon || '🎮'}</div>
                <h3>{game.name}</h3>
                <p>{game.description}</p>
                <span className="game-card-tag">Play Now →</span>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
