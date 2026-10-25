import { Link, useLocation } from 'react-router-dom'

export default function Navbar() {
  const { pathname } = useLocation()
  return (
    <nav className="navbar" role="navigation" aria-label="Main navigation">
      <Link to="/" className="navbar-logo" aria-label="Arcade Home">
        🎮 Arcade
      </Link>
      <ul className="navbar-nav">
        <li>
          <Link
            to="/"
            className={`nav-link ${pathname === '/' ? 'active' : ''}`}
          >
            Catalog
          </Link>
        </li>
        <li>
          <Link
            to="/games/number-detective"
            className={`nav-link ${pathname.startsWith('/games/number-detective') ? 'active' : ''}`}
          >
            Number Detective
          </Link>
        </li>
        <li>
          <Link
            to="/games/rock-paper-scissors"
            className={`nav-link ${pathname.startsWith('/games/rock-paper-scissors') ? 'active' : ''}`}
          >
            RPS
          </Link>
        </li>
        <li>
          <Link
            to="/games/tic-tac-toe"
            className={`nav-link ${pathname.startsWith('/games/tic-tac-toe') ? 'active' : ''}`}
          >
            Tic-Tac-Toe
          </Link>
        </li>
        <li>
          <Link
            to="/games/memory-match"
            className={`nav-link ${pathname.startsWith('/games/memory-match') ? 'active' : ''}`}
          >
            Memory Match
          </Link>
        </li>
      </ul>
    </nav>
  )
}
