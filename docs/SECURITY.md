# Security Policy & Architecture

## Security Principles

Arcade adheres to defensive engineering practices across both the Spring Boot backend and the React frontend.

---

## 1. Game State Integrity & Server Authority

- **Server-Authoritative State:** All secret number generation, guess evaluation, attempt counting, and win/loss state transitions occur strictly on the backend.
- **Secret Concealment:** The active secret number is stored strictly in server-side session memory and is masked as `null` in both `SessionResponse` and `GuessResponse` while the game status is `IN_PROGRESS`.
- **Cryptographic Randomness:** Target numbers are generated using `java.security.SecureRandom`, preventing pseudo-random number generator (PRNG) state reconstruction attacks.

---

## 2. Input Validation & Defenses

- **Jakarta Bean Validation:** All incoming payloads are strictly validated before execution.
  - Guess submission requires `0 <= guess <= 100`.
  - Violations result in standard HTTP 400 Bad Request responses with field error descriptors.
- **SQL Injection Prevention:** All database operations utilize Spring Data JPA parameter-binding queries. No dynamic string concatenation is used in SQL/JPQL queries.
- **XSS & Content Security:** React automatically encodes rendered values to prevent client-side script injection.

---

## 3. Network & Transport Security

- **Cross-Origin Resource Sharing (CORS):** The backend CORS filter permits requests only from explicitly whitelisted domains configured in `CORS_ALLOWED_ORIGINS` (defaults to `http://localhost:5173` for development).
- **Error Obfuscation:** In production, `server.error.include-stacktrace: never` and `server.error.include-message: never` prevent internal stack trace leakage.

---

## 4. Secrets Management

- **No Committed Secrets:** Passwords and keys are never committed into git. Default placeholders exist only for local sandbox development.
- **Environment Injection:** Production credentials (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) must be supplied via secure environment variables or secret vaults.

---

## 5. Known Security Limitations & Future Roadmap

- **In-Memory Active Sessions:** Active sessions reside in JVM heap memory. A distributed Redis session store should be introduced for multi-replica horizontal clustering.
- **Unauthenticated Gameplay:** Games are currently played anonymously. Future releases will integrate OAuth2/JWT authentication.
- **Rate Limiting:** IP-based rate limiting (e.g. Bucket4j or Cloudflare rate limiting) should be added to prevent brute-force session creation.
