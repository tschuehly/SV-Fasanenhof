#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PORT="${1:-8091}"
DIST_DIR="$ROOT_DIR/build/site"

if command -v fswatch &>/dev/null; then
  WATCHER="fswatch"
elif command -v inotifywait &>/dev/null; then
  WATCHER="inotifywait"
else
  WATCHER="poll"
fi

build() {
  if command -v kotlin &>/dev/null; then
    (cd "$ROOT_DIR" && kotlin build.main.kts --live-reload 2>&1)
  else
    echo "ERROR: kotlin is required and was not found on PATH." >&2
    return 1
  fi
}

port_pids() {
  if command -v lsof &>/dev/null; then
    lsof -tiTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true
  elif command -v fuser &>/dev/null; then
    fuser "$PORT"/tcp 2>/dev/null || true
  fi
}

free_port() {
  local pids
  pids="$(port_pids | tr '\n' ' ')"

  if [[ -z "${pids// }" ]]; then
    return
  fi

  echo "Port $PORT is already in use by PID(s): $pids"
  echo "Stopping existing listener(s)..."
  kill $pids 2>/dev/null || true

  for _ in {1..20}; do
    if [[ -z "$(port_pids)" ]]; then
      return
    fi
    sleep 0.1
  done

  pids="$(port_pids | tr '\n' ' ')"
  if [[ -n "${pids// }" ]]; then
    echo "Force-stopping remaining listener(s): $pids"
    kill -9 $pids 2>/dev/null || true
  fi
}

echo "Building..."
build

free_port
python3 "$ROOT_DIR/devserver.py" --port "$PORT" --directory "$DIST_DIR" &
HTTP_PID=$!
trap "kill $HTTP_PID 2>/dev/null" EXIT

echo "Serving on http://localhost:$PORT"
echo "Watching for changes... (Ctrl+C to stop)"

if [[ "$WATCHER" == "fswatch" ]]; then
  fswatch -o \
    "$ROOT_DIR/build.main.kts" \
    "$ROOT_DIR/devserver.py" \
    "$ROOT_DIR/src/main/resources/assets/" \
    "$ROOT_DIR/content/" \
  | while read -r; do
    echo ""
    echo "Change detected, rebuilding..."
    build
  done
elif [[ "$WATCHER" == "inotifywait" ]]; then
  while true; do
    inotifywait -q -r -e modify,create,delete \
      "$ROOT_DIR/build.main.kts" \
      "$ROOT_DIR/devserver.py" \
      "$ROOT_DIR/src/main/resources/assets/" \
      "$ROOT_DIR/content/" 2>/dev/null
    echo ""
    echo "Change detected, rebuilding..."
    build
  done
else
  get_hash() {
    find "$ROOT_DIR/content" "$ROOT_DIR/src/main/resources/assets" \
      -type f \( -name '*.md' -o -name '*.css' -o -name '*.png' -o -name '*.kts' -o -name '*.py' \) 2>/dev/null | \
      xargs stat -f '%m' 2>/dev/null || \
      xargs stat -c '%Y' 2>/dev/null || echo ""
  }

  LAST_HASH="$(get_hash)"
  while true; do
    sleep 1
    CURRENT_HASH="$(get_hash)"
    if [[ "$CURRENT_HASH" != "$LAST_HASH" ]]; then
      echo ""
      echo "Change detected, rebuilding..."
      build
      LAST_HASH="$CURRENT_HASH"
    fi
  done
fi
