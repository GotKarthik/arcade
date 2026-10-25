# Troubleshooting Guide

Common issues, diagnosis techniques, and remediation steps for the Arcade platform.

---

## 1. Backend Startup Issues

### Problem: `Database connection failed` / `Connection refused on port 5432`
- **Cause:** PostgreSQL is not running or credentials in `application.yml` do not match.
- **Diagnosis:**
  ```bash
  lsof -i :5432
  ps aux | grep postgres
  ```
- **Remediation:**
  1. Start PostgreSQL (e.g. `brew services start postgresql` or start container).
  2. Ensure the `arcade` database exists: `createdb arcade` or `psql -U postgres -c "CREATE DATABASE arcade;"`.
  3. Verify `.env` / environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### Problem: `Port 8080 already in use`
- **Diagnosis:**
  ```bash
  lsof -i :8080
  ```
- **Remediation:**
  Terminate existing process using port 8080:
  ```bash
  kill -9 $(lsof -t -i :8080)
  ```

---

## 2. Frontend & API Connectivity Issues

### Problem: `Failed to fetch` or CORS error in browser console
- **Cause:** Backend CORS origin whitelist does not include the frontend origin.
- **Diagnosis:** Check browser DevTools Network tab for preflight OPTIONS failure.
- **Remediation:**
  Ensure the backend was started with:
  ```bash
  CORS_ALLOWED_ORIGINS=http://localhost:5173
  ```
  Or check that `VITE_API_URL` points to the correct backend host (default: `http://localhost:8080`).

---

## 3. Database Migration Issues

### Problem: `FlywayException: Migration checksum mismatch`
- **Cause:** SQL migration scripts were modified after being applied.
- **Diagnosis:** Examine `flyway_schema_history` in PostgreSQL.
- **Remediation:**
  In development environments, clean or drop the database and re-run migrations:
  ```bash
  dropdb arcade && createdb arcade
  ```

---

## 4. Test Failures

### Vitest jsdom input handling
- If tests simulate text input on `<input>`, ensure numeric strings are wrapped in `await act(async () => ...)` and assertions wait on DOM updates using `await waitFor(...)`.
