# Deployment Guide

This guide covers deployment options, required environment variables, containerization, and verification steps.

## Architecture & Hosting Options

| Component | Recommended Providers | Runtime / Artifact |
|---|---|---|
| **Frontend** | Vercel, Netlify, Cloudflare Pages | Static SPA (`dist/` output from `npm run build`) |
| **Backend** | Render, Railway, Fly.io, AWS App Runner | Java 21 Spring Boot JAR (`mvn clean package`) |
| **Database** | Supabase, Neon, Render PostgreSQL, AWS RDS | PostgreSQL 15+ |

---

## Required Environment Variables

### Backend Configuration

| Variable | Required | Description | Example |
|---|---|---|---|
| `DB_URL` | Yes | JDBC URL for PostgreSQL connection | `jdbc:postgresql://postgres.example.com:5432/arcade` |
| `DB_USERNAME` | Yes | PostgreSQL username | `arcade_user` |
| `DB_PASSWORD` | Yes | PostgreSQL password | `supersecretpassword` |
| `PORT` | No | HTTP port for Spring Boot (default: 8080) | `8080` |
| `CORS_ALLOWED_ORIGINS` | No | Comma-separated allowed CORS origins | `https://arcade.example.com` |

### Frontend Configuration

| Variable | Required | Description | Example |
|---|---|---|---|
| `VITE_API_URL` | Yes | Full base URL of the deployed backend | `https://api.arcade.example.com` |

---

## Containerized Deployment (Docker)

### Backend Dockerfile (`backend/Dockerfile`)
```dockerfile
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/arcade-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile (`frontend/Dockerfile`)
```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## Deployment Verification Checklist

Once deployed to staging or production:
1. `GET /api/health` returns `{"status":"UP","service":"arcade-backend"}`.
2. `GET /api/games` returns game metadata array.
3. Database migrations applied: check `flyway_schema_history` table in Postgres.
4. `POST /api/games/number-detective/sessions` returns HTTP 201 with a fresh session UUID.
5. In browser: open frontend URL, play a full game of Number Detective to completion, and verify leaderboard updates.
