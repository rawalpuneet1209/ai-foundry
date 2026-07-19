#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repository_root"

mapfile_path="$(mktemp)"
trap 'rm -f "$mapfile_path"' EXIT

rg --files -g '*.yml' -g '*.yaml' -g '!target/**' -g '!.m2/**' | sort >"$mapfile_path"

if [[ ! -s "$mapfile_path" ]]; then
  echo "No YAML files found."
  exit 0
fi

while IFS= read -r yaml_file; do
  ruby -e 'require "yaml"; YAML.load_stream(File.read(ARGV.fetch(0)))' "$yaml_file"
done <"$mapfile_path"

if rg -n '^.{121,}$|\t|[[:blank:]]+$|:\s*\{|:\s*\[[^]]' \
  -g '*.yml' -g '*.yaml' -g '!target/**' -g '!.m2/**'; then
  echo "YAML style violations found." >&2
  exit 1
fi

if command -v yamllint >/dev/null 2>&1; then
  yamllint --config-file .yamllint.yml $(tr '\n' ' ' <"$mapfile_path")
fi

if command -v kubectl >/dev/null 2>&1; then
  kubectl kustomize k8s/overlays/local >/dev/null
  kubectl kustomize k8s/overlays/dev >/dev/null
fi

if command -v docker >/dev/null 2>&1; then
  docker compose -f docker/docker-compose.yml config --quiet
fi

echo "YAML validation passed."
