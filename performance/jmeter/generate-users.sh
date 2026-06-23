#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$ROOT_DIR/generated"
OUT_FILE="$OUT_DIR/users.csv"
COUNT="${1:-10000}"
PASSWORD="${2:-loadtest1234}"

mkdir -p "$OUT_DIR"

awk -v count="$COUNT" -v password="$PASSWORD" 'BEGIN {
  for (i = 1; i <= count; i++) {
    printf "loaduser%05d,%s\n", i, password
  }
}' > "$OUT_FILE"

echo "generated $OUT_FILE ($COUNT users)"
