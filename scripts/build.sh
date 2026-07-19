#!/usr/bin/env bash
set -euo pipefail
command -v mvn >/dev/null || { echo "Maven is required" >&2; exit 1; }
mvn clean verify
