import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import RockPaperScissorsPage from '../pages/RockPaperScissorsPage'
import TicTacToePage from '../pages/TicTacToePage'
import MemoryMatchPage from '../pages/MemoryMatchPage'

// Mock the API client
vi.mock('../api/client', () => ({
  api: {
    // RPS
    createRpsSession: vi.fn(),
    getRpsSession: vi.fn(),
    playRpsMove: vi.fn(),
    restartRps: vi.fn(),
    getRpsHistory: vi.fn(),

    // TTT
    createTttSession: vi.fn(),
    getTttSession: vi.fn(),
    playTttMove: vi.fn(),
    restartTtt: vi.fn(),
    getTttHistory: vi.fn(),

    // Memory
    createMemorySession: vi.fn(),
    getMemorySession: vi.fn(),
    flipMemoryCard: vi.fn(),
    restartMemory: vi.fn(),
    getMemoryLeaderboard: vi.fn(),
  },
}))

import { api } from '../api/client'

function renderWithRouter(component, { route = '/' } = {}) {
  return render(
    <MemoryRouter initialEntries={[route]}>{component}</MemoryRouter>
  )
}

describe('RockPaperScissorsPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.getRpsHistory.mockResolvedValue([])
  })

  it('renders instructions and start button', () => {
    renderWithRouter(<RockPaperScissorsPage />)
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/Rock Paper/i)
    expect(screen.getByRole('button', { name: /Start Match/i })).toBeInTheDocument()
    expect(screen.getByText(/Rock crushes Scissors/i)).toBeInTheDocument()
  })

  it('starts match and reveals 3 move buttons and scoreboard', async () => {
    api.createRpsSession.mockResolvedValue({
      sessionId: 'rps-123',
      outcome: 'IN_PROGRESS',
      playerScore: 0,
      computerScore: 0,
      draws: 0,
      roundsRemaining: 5,
      playerName: 'Player',
    })

    renderWithRouter(<RockPaperScissorsPage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Match/i }))
    })

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Choose Rock/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /Choose Paper/i })).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /Choose Scissors/i })).toBeInTheDocument()
    })
    expect(screen.getByText('5')).toBeInTheDocument() // rounds remaining
  })

  it('submits move and displays round result', async () => {
    api.createRpsSession.mockResolvedValue({
      sessionId: 'rps-123',
      outcome: 'IN_PROGRESS',
      playerScore: 0,
      computerScore: 0,
      draws: 0,
      roundsRemaining: 5,
    })
    api.playRpsMove.mockResolvedValue({
      roundNumber: 1,
      playerMove: 'ROCK',
      computerMove: 'SCISSORS',
      result: 'WIN',
      playerScore: 1,
      computerScore: 0,
      draws: 0,
      roundsRemaining: 4,
      matchOutcome: 'IN_PROGRESS',
      matchFinished: false,
    })
    api.getRpsSession.mockResolvedValue({
      sessionId: 'rps-123',
      outcome: 'IN_PROGRESS',
      playerScore: 1,
      computerScore: 0,
      draws: 0,
      roundsRemaining: 4,
    })

    renderWithRouter(<RockPaperScissorsPage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Match/i }))
    })

    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Choose Rock/i }))
    })

    await waitFor(() => {
      expect(screen.getByText(/Round Won!/i)).toBeInTheDocument()
      expect(screen.getByText(/You \(ROCK\)/i)).toBeInTheDocument()
      expect(screen.getByText(/Computer \(SCISSORS\)/i)).toBeInTheDocument()
    })
  })
})

describe('TicTacToePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.getTttHistory.mockResolvedValue([])
  })

  it('renders start button and instructions', () => {
    renderWithRouter(<TicTacToePage />)
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/Tic-Tac-/i)
    expect(screen.getByRole('button', { name: /Start Match/i })).toBeInTheDocument()
  })

  it('starts match and renders 3x3 board grid', async () => {
    api.createTttSession.mockResolvedValue({
      sessionId: 'ttt-123',
      board: Array(9).fill(null),
      currentTurn: 'X',
      outcome: 'IN_PROGRESS',
      winningLine: null,
      movesCount: 0,
      finished: false,
    })

    renderWithRouter(<TicTacToePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Match/i }))
    })

    await waitFor(() => {
      expect(screen.getByText(/Player X's Turn/i)).toBeInTheDocument()
      expect(screen.getAllByRole('button', { name: /Cell/i })).toHaveLength(9)
    })
  })

  it('makes a move and updates the board cell', async () => {
    const initialBoard = Array(9).fill(null)
    api.createTttSession.mockResolvedValue({
      sessionId: 'ttt-123',
      board: initialBoard,
      currentTurn: 'X',
      outcome: 'IN_PROGRESS',
      winningLine: null,
      movesCount: 0,
      finished: false,
    })

    const afterMoveBoard = [...initialBoard]
    afterMoveBoard[4] = 'X'
    api.playTttMove.mockResolvedValue({
      sessionId: 'ttt-123',
      board: afterMoveBoard,
      currentTurn: 'O',
      outcome: 'IN_PROGRESS',
      winningLine: null,
      movesCount: 1,
      finished: false,
    })

    renderWithRouter(<TicTacToePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Match/i }))
    })

    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Cell 4/i }))
    })

    await waitFor(() => {
      expect(screen.getByText(/Player O's Turn/i)).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /Cell 4, X/i })).toBeInTheDocument()
    })
  })
})

describe('MemoryMatchPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.getMemoryLeaderboard.mockResolvedValue([])
  })

  it('renders instructions and start game button', () => {
    renderWithRouter(<MemoryMatchPage />)
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/Memory/i)
    expect(screen.getByRole('button', { name: /Start Game/i })).toBeInTheDocument()
  })

  it('starts game and displays 16 cards face-down', async () => {
    const grid = Array.from({ length: 16 }, (_, i) => ({
      position: i,
      revealed: false,
      matched: false,
      symbol: null,
    }))

    api.createMemorySession.mockResolvedValue({
      sessionId: 'mem-123',
      status: 'IN_PROGRESS',
      turnState: 'AWAITING_FIRST',
      grid,
      movesCount: 0,
      pairsMatched: 0,
      totalPairs: 8,
      durationSeconds: 0,
      completed: false,
    })

    renderWithRouter(<MemoryMatchPage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })

    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /Card \d+, hidden/i })).toHaveLength(16)
    })
  })

  it('flips card and reveals symbol', async () => {
    const grid = Array.from({ length: 16 }, (_, i) => ({
      position: i,
      revealed: false,
      matched: false,
      symbol: null,
    }))

    api.createMemorySession.mockResolvedValue({
      sessionId: 'mem-123',
      status: 'IN_PROGRESS',
      turnState: 'AWAITING_FIRST',
      grid,
      movesCount: 0,
      pairsMatched: 0,
      totalPairs: 8,
      durationSeconds: 0,
      completed: false,
    })

    const flippedGrid = [...grid]
    flippedGrid[0] = { position: 0, revealed: true, matched: false, symbol: '🚀' }
    api.flipMemoryCard.mockResolvedValue({
      sessionId: 'mem-123',
      status: 'IN_PROGRESS',
      turnState: 'AWAITING_SECOND',
      grid: flippedGrid,
      movesCount: 0,
      pairsMatched: 0,
      totalPairs: 8,
      durationSeconds: 0,
      completed: false,
    })

    renderWithRouter(<MemoryMatchPage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })

    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Card 0, hidden/i }))
    })

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Card 0, 🚀/i })).toBeInTheDocument()
    })
  })
})
