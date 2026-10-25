// Base API URL — reads from Vite env variable at build time
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

async function request(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.message || `HTTP ${res.status}`);
  }
  return data;
}

export const api = {
  // Catalog
  getGames: () => request('/api/games'),

  // Number Detective
  createSession: () =>
    request('/api/games/number-detective/sessions', { method: 'POST' }),
  getSession: (id) =>
    request(`/api/games/number-detective/sessions/${id}`),
  submitGuess: (id, guess) =>
    request(`/api/games/number-detective/sessions/${id}/guesses`, {
      method: 'POST',
      body: JSON.stringify({ guess }),
    }),
  restart: (id) =>
    request(`/api/games/number-detective/sessions/${id}/restart`, {
      method: 'POST',
    }),
  getLeaderboard: (game = 'number-detective') =>
    request(`/api/leaderboard?game=${game}`),

  // Rock Paper Scissors
  createRpsSession: (playerName) =>
    request('/api/games/rock-paper-scissors/sessions', {
      method: 'POST',
      body: JSON.stringify({ playerName: playerName || 'Player' }),
    }),
  getRpsSession: (id) =>
    request(`/api/games/rock-paper-scissors/sessions/${id}`),
  playRpsMove: (id, move) =>
    request(`/api/games/rock-paper-scissors/sessions/${id}/moves`, {
      method: 'POST',
      body: JSON.stringify({ move }),
    }),
  restartRps: (id) =>
    request(`/api/games/rock-paper-scissors/sessions/${id}/restart`, {
      method: 'POST',
    }),
  getRpsHistory: () =>
    request('/api/games/rock-paper-scissors/history'),

  // Tic-Tac-Toe
  createTttSession: () =>
    request('/api/games/tic-tac-toe/sessions', { method: 'POST' }),
  getTttSession: (id) =>
    request(`/api/games/tic-tac-toe/sessions/${id}`),
  playTttMove: (id, position, player) =>
    request(`/api/games/tic-tac-toe/sessions/${id}/moves`, {
      method: 'POST',
      body: JSON.stringify({ position, player }),
    }),
  restartTtt: (id) =>
    request(`/api/games/tic-tac-toe/sessions/${id}/restart`, {
      method: 'POST',
    }),
  getTttHistory: () =>
    request('/api/games/tic-tac-toe/history'),

  // Memory Match
  createMemorySession: (playerName) =>
    request('/api/games/memory-match/sessions', {
      method: 'POST',
      body: JSON.stringify({ playerName: playerName || 'Player' }),
    }),
  getMemorySession: (id) =>
    request(`/api/games/memory-match/sessions/${id}`),
  flipMemoryCard: (id, position) =>
    request(`/api/games/memory-match/sessions/${id}/flips`, {
      method: 'POST',
      body: JSON.stringify({ position }),
    }),
  restartMemory: (id) =>
    request(`/api/games/memory-match/sessions/${id}/restart`, {
      method: 'POST',
    }),
  getMemoryLeaderboard: () =>
    request('/api/games/memory-match/leaderboard'),
};
