import { useState, useCallback } from 'react';
import { api } from '../api/client';

export function useNumberDetective() {
  const [session, setSession] = useState(null);
  const [lastGuess, setLastGuess] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const startGame = useCallback(async () => {
    setLoading(true);
    setError(null);
    setLastGuess(null);
    try {
      const data = await api.createSession();
      setSession(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const submitGuess = useCallback(async (guess) => {
    if (!session) return;
    setLoading(true);
    setError(null);
    try {
      const result = await api.submitGuess(session.sessionId, guess);
      setLastGuess(result);
      setSession((prev) => ({
        ...prev,
        status: result.sessionStatus,
        guessesUsed: result.guessesUsed,
        guessesRemaining: result.guessesRemaining,
        secretNumber: result.secretNumber,
      }));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [session]);

  const restart = useCallback(async () => {
    if (!session) return startGame();
    setLoading(true);
    setError(null);
    setLastGuess(null);
    try {
      const newSession = await api.restart(session.sessionId);
      setSession(newSession);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [session, startGame]);

  return { session, lastGuess, loading, error, startGame, submitGuess, restart };
}
