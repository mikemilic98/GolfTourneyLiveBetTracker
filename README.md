# Golf Tournament Live Leaderboard Tracker

A live leaderboard tracker for golf tournament betting. Users pick their top players before a tournament; the app fetches live rankings from ESPN and displays who's winning based on combined placement scores.

## Tech Stack

- **Backend:** Kotlin + Spring Boot 3.x
- **Frontend:** React 18+ (Vite)
- **Database:** PostgreSQL
- **Auth:** Spring Security + JWT
- **Real-time:** SSE (Server-Sent Events) for live leaderboard updates

## Quick Start

### Prerequisites

- **Docker** (for full-stack run)
- Or for local dev: JDK 17+, Node.js 18+, Docker (PostgreSQL only)

### Option A: Full stack with Docker (recommended for test run)

```bash
./scripts/test-run.sh docker
```

Wait ~30 seconds for services to start, then open **http://localhost** (port 80).

### Option B: Local development

```bash
./scripts/test-run.sh local
```

Then in separate terminals:
```bash
cd backend && ./gradlew bootRun
cd frontend && npm install && npm run dev
```

Open **http://localhost:5173**

## Test Run Checklist

1. **Register** – Go to /register, create an account
2. **Login** – Sign in at /
3. **Create admin user** (for Admin features): Connect to Postgres and run:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'your@email.com';
   ```
4. **Admin setup** (Admin tab):
   - Create a tournament (name + ESPN URL, e.g. `https://www.espn.com/golf/leaderboard?tournamentId=401811938`)
   - Click "Ingest Roster" to fetch player names from ESPN
   - Create game rules for that tournament (num picks, drops)
5. **Picks** – Select tournament, choose players from roster, save
6. **Live** – Select tournament to see leaderboard (updates every 5s when ESPN has live data)

## API Endpoints

| Endpoint | Auth |
|----------|------|
| POST /api/auth/register, /api/auth/login | Public |
| GET/POST/PUT/DELETE /api/tournaments | Admin (create/update/delete) |
| GET/POST /api/tournaments/{id}/ingest-roster | Admin |
| GET/POST/PUT/DELETE /api/rules | Admin |
| GET /api/picks/me, POST /api/picks/me | User (own picks) |
| GET/POST/PUT/DELETE /api/picks | Admin (any user) |
| GET /api/live/{id}/scores, GET /api/live/{id}/stream | Authenticated |
| GET /api/users | Admin |
