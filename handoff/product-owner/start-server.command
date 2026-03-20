#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

export SPRING_PROFILES_ACTIVE=standalone
export SERVER_PORT=8081
export DB_HOST=localhost
export DB_PORT=5433
export DB_DATABASE=okapi_modules
export DB_USERNAME=folio_admin
export DB_PASSWORD=folio_admin
export MOD_LINKED_DATA_MARC_AUTHORITY_SIMILARITY_THRESHOLD=0.90

exec java -jar "$SCRIPT_DIR/mod-linked-data.jar"
