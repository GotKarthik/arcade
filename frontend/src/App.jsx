import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import HomePage from './pages/HomePage'
import NumberDetectivePage from './pages/NumberDetectivePage'

function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/games/number-detective" element={<NumberDetectivePage />} />
        </Routes>
      </main>
      <Footer />
    </>
  )
}

export default App
