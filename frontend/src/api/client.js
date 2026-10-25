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
  getGames: () => request('/api/games'),

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
};
