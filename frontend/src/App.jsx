import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import HomePage from './pages/HomePage'
import NumberDetectivePage from './pages/NumberDetectivePage'
import RockPaperScissorsPage from './pages/RockPaperScissorsPage'
import TicTacToePage from './pages/TicTacToePage'
import MemoryMatchPage from './pages/MemoryMatchPage'

function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/games/number-detective" element={<NumberDetectivePage />} />
          <Route path="/games/rock-paper-scissors" element={<RockPaperScissorsPage />} />
          <Route path="/games/tic-tac-toe" element={<TicTacToePage />} />
          <Route path="/games/memory-match" element={<MemoryMatchPage />} />
        </Routes>
      </main>
      <Footer />
    </>
  )
}

export default App
