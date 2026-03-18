#!/bin/bash
# Test run script for Golf Tournament Live Leaderboard Tracker
# Run from project root: ./scripts/test-run.sh [mode]
# Modes: docker (full stack), local (postgres + backend + frontend)

set -e
MODE="${1:-docker}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "=== Golf Leaderboard Tracker - Test Run ($MODE mode) ==="

case "$MODE" in
  docker)
    echo "Starting full stack with Docker Compose..."
    docker compose up -d
    echo ""
    echo "Waiting for services to be ready (30s)..."
    sleep 30
    echo ""
    echo "App should be available at:"
    echo "  Frontend: http://localhost"
    echo "  Backend API: http://localhost:8080"
    echo ""
    echo "To view logs: docker compose logs -f"
    echo "To stop: docker compose down"
    ;;
  local)
    echo "Starting Postgres only (assumes backend/frontend run manually)..."
    docker compose up -d postgres
    echo "Waiting for Postgres (5s)..."
    sleep 5
    echo ""
    echo "Postgres is up. Now run:"
    echo "  Backend: cd backend && ./gradlew bootRun"
    echo "  Frontend: cd frontend && npm install && npm run dev"
    echo ""
    echo "Then open: http://localhost:5173"
    ;;
  *)
    echo "Usage: $0 [docker|local]"
    echo "  docker - Full stack (postgres + backend + frontend) via Docker"
    echo "  local  - Postgres only; run backend/frontend manually"
    exit 1
    ;;
esac

echo ""
echo "=== Quick Test Checklist ==="
echo "1. Register a new user at /register"
echo "2. Login at /"
echo "3. Go to Admin (if you have an admin user) - create tournament, ingest roster, create rules"
echo "4. Or: Create tournament via API, ingest roster, create rules"
echo "5. Go to Picks - select tournament, choose players, save"
echo "6. Go to Live - select tournament to see leaderboard (updates every 5s when event has data)"
