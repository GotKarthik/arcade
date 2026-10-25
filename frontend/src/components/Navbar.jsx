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
            Games
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
      </ul>
    </nav>
  )
}
