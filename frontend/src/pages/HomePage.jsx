import { Link } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { api } from '../api/client'

export default function HomePage() {
  const [games, setGames] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.getGames()
      .then(setGames)
      .catch(() => setGames([{
        id: 'number-detective',
        name: 'Number Detective',
        description: 'Guess the secret number between 0 and 100 in 7 tries.',
        path: '/games/number-detective',
      }]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="container">
      <section className="hero">
        <div className="badge">Free · No Registration · Play Now</div>
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
                <div className="game-card-icon">🔢</div>
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
