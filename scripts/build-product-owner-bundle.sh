#!/bin/zsh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SOURCE_DIR="$REPO_ROOT/handoff/product-owner"
BUNDLE_NAME="authority-duplicate-identification"
TARGET_DIR="$REPO_ROOT/target/$BUNDLE_NAME"
JAR_TARGET="$TARGET_DIR/mod-linked-data.jar"
ZIP_TARGET="$REPO_ROOT/target/$BUNDLE_NAME.zip"

JAR_SOURCE="$(find "$REPO_ROOT/target" -maxdepth 1 -type f -name 'mod-linked-data-*.jar' ! -name '*.original' | head -n 1)"

if [[ -z "$JAR_SOURCE" || ! -f "$JAR_SOURCE" ]]; then
  echo "Packaged jar not found in $REPO_ROOT/target"
  echo "Run: mvn -DskipTests -Dcheckstyle.skip package"
  exit 1
fi

rm -rf "$TARGET_DIR"
rm -f "$ZIP_TARGET"
mkdir -p "$TARGET_DIR"
cp -R "$SOURCE_DIR"/. "$TARGET_DIR"/
cp "$JAR_SOURCE" "$JAR_TARGET"

chmod +x \
  "$TARGET_DIR/reset-db.command" \
  "$TARGET_DIR/start-db.command" \
  "$TARGET_DIR/stop-db.command" \
  "$TARGET_DIR/start-server.command"

(
  cd "$TARGET_DIR"
  zip -rq "$ZIP_TARGET" .
)

echo "Bundle created at: $TARGET_DIR"
echo "Zip created at: $ZIP_TARGET"
