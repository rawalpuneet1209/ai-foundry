#!/usr/bin/env bash
set -euo pipefail
command -v docker >/dev/null || { echo "Docker is required" >&2; exit 1; }
docker compose -f docker/docker-compose.yml up --build -d
