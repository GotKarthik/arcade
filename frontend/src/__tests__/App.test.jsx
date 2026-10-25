import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import App from '../App'
import NumberDetectivePage from '../pages/NumberDetectivePage'
import HomePage from '../pages/HomePage'

// Mock the API client
vi.mock('../api/client', () => ({
  api: {
    getGames: vi.fn(),
    createSession: vi.fn(),
    getSession: vi.fn(),
    submitGuess: vi.fn(),
    restart: vi.fn(),
    getLeaderboard: vi.fn(),
  },
}))

import { api } from '../api/client'

function renderWithRouter(component, { route = '/' } = {}) {
  return render(
    <MemoryRouter initialEntries={[route]}>{component}</MemoryRouter>
  )
}

describe('HomePage', () => {
  beforeEach(() => {
    api.getGames.mockResolvedValue([
      {
        id: 'number-detective',
        name: 'Number Detective',
        description: 'Guess the secret number',
        path: '/games/number-detective',
      },
    ])
  })

  it('renders the hero section', async () => {
    renderWithRouter(<HomePage />)
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent(/Browser/i)
    await waitFor(() => {
      expect(screen.getByText('Number Detective')).toBeInTheDocument()
    })
  })

  it('renders game cards from API', async () => {
    renderWithRouter(<HomePage />)
    await waitFor(() => {
      expect(screen.getByText('Number Detective')).toBeInTheDocument()
    })
    expect(screen.getByText(/Guess the secret number/i)).toBeInTheDocument()
  })

  it('handles API failure gracefully', async () => {
    api.getGames.mockRejectedValue(new Error('Network error'))
    renderWithRouter(<HomePage />)
    // Should fall back to static data
    await waitFor(() => {
      expect(screen.getByText('Number Detective')).toBeInTheDocument()
    })
  })
})

describe('NumberDetectivePage — start game', () => {
  beforeEach(() => {
    api.getLeaderboard.mockResolvedValue([])
    api.createSession.mockResolvedValue({
      sessionId: 'test-session-1',
      status: 'IN_PROGRESS',
      guessesUsed: 0,
      guessesRemaining: 7,
      maxGuesses: 7,
      secretNumber: null,
    })
    api.submitGuess.mockResolvedValue({
      guess: 50,
      result: 'TOO_LOW',
      sessionStatus: 'IN_PROGRESS',
      guessesUsed: 1,
      guessesRemaining: 6,
      secretNumber: null,
    })
    api.restart.mockResolvedValue({
      sessionId: 'test-session-2',
      status: 'IN_PROGRESS',
      guessesUsed: 0,
      guessesRemaining: 7,
      maxGuesses: 7,
      secretNumber: null,
    })
  })

  it('shows instructions before game starts', () => {
    renderWithRouter(<NumberDetectivePage />)
    expect(screen.getByText(/How to Play/i)).toBeInTheDocument()
    expect(screen.getByText(/7 guesses/i)).toBeInTheDocument()
  })

  it('shows start button', () => {
    renderWithRouter(<NumberDetectivePage />)
    expect(screen.getByRole('button', { name: /Start Game/i })).toBeInTheDocument()
  })

  it('starts a game when start button is clicked', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => {
      expect(api.createSession).toHaveBeenCalled()
      expect(screen.getByLabelText(/Enter your guess/i)).toBeInTheDocument()
    })
  })

  it('submits a guess and shows feedback', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => screen.getByLabelText(/Enter your guess/i))

    const input = screen.getByLabelText(/Enter your guess/i)
    fireEvent.change(input, { target: { value: '50' } })

    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Submit guess/i }))
    })

    await waitFor(() => {
      expect(api.submitGuess).toHaveBeenCalledWith('test-session-1', 50)
      expect(screen.getByText(/too low/i)).toBeInTheDocument()
    })
  })

  it('shows remaining attempts', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => {
      expect(screen.getByText(/7.*guess/i)).toBeInTheDocument()
    })
  })

  it('rejects non-numeric input', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => screen.getByLabelText(/Enter your guess/i))

    const input = screen.getByLabelText(/Enter your guess/i)
    fireEvent.change(input, { target: { value: 'abc' } })

    // Submit the form directly
    const form = input.closest('form')
    await act(async () => {
      fireEvent.submit(form)
    })
    await waitFor(() => {
      expect(screen.getByText(/Please enter a number/i)).toBeInTheDocument()
    })
  })

  it('rejects out-of-range input', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => screen.getByLabelText(/Enter your guess/i))

    const input = screen.getByLabelText(/Enter your guess/i)
    fireEvent.change(input, { target: { value: '150' } })
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Submit guess/i }))
    })
    expect(screen.getByText(/Number must be between/i)).toBeInTheDocument()
  })
})

describe('NumberDetectivePage — win state', () => {
  beforeEach(() => {
    api.getLeaderboard.mockResolvedValue([])
    api.createSession.mockResolvedValue({
      sessionId: 'test-session-win',
      status: 'IN_PROGRESS',
      guessesUsed: 0,
      guessesRemaining: 7,
      maxGuesses: 7,
      secretNumber: null,
    })
    api.submitGuess.mockResolvedValue({
      guess: 42,
      result: 'CORRECT',
      sessionStatus: 'WON',
      guessesUsed: 1,
      guessesRemaining: 6,
      secretNumber: 42,
    })
  })

  it('shows win state after correct guess', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => screen.getByLabelText(/Enter your guess/i))

    fireEvent.change(screen.getByLabelText(/Enter your guess/i), { target: { value: '42' } })
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Submit guess/i }))
    })

    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 2, name: /You Won/i })).toBeInTheDocument()
      expect(screen.getByText('42')).toBeInTheDocument()
    })
  })

  it('shows restart button after game ends', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => screen.getByLabelText(/Enter your guess/i))
    fireEvent.change(screen.getByLabelText(/Enter your guess/i), { target: { value: '42' } })
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Submit guess/i }))
    })
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Play Again/i })).toBeInTheDocument()
    })
  })
})

describe('NumberDetectivePage — network error', () => {
  beforeEach(() => {
    api.getLeaderboard.mockResolvedValue([])
    api.createSession.mockRejectedValue(new Error('Network failure'))
  })

  it('displays error message on API failure', async () => {
    renderWithRouter(<NumberDetectivePage />)
    await act(async () => {
      fireEvent.click(screen.getByRole('button', { name: /Start Game/i }))
    })
    await waitFor(() => {
      expect(screen.getByText(/Network failure/i)).toBeInTheDocument()
    })
  })
})
