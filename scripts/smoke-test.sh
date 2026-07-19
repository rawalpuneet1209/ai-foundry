#!/usr/bin/env bash
set -euo pipefail
curl --fail --silent "${BASE_URL:-http://localhost:8080}/actuator/health"
curl --fail --silent -H 'Content-Type: application/json' -d '{"message":"Explain overdraft protection"}' "${BASE_URL:-http://localhost:8080}/api/v1/chat/completions"
