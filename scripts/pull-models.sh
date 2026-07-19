#!/usr/bin/env bash
set -euo pipefail
command -v ollama >/dev/null || { echo "Ollama is required" >&2; exit 1; }
ollama pull "${OLLAMA_CHAT_MODEL:-llama3.2}"
ollama pull "${OLLAMA_EMBEDDING_MODEL:-nomic-embed-text}"
