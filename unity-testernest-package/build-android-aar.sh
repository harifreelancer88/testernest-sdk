#!/usr/bin/env bash
set -euo pipefail

GRADLE_ROOT="${1:-..}"

pushd "$GRADLE_ROOT" >/dev/null
./gradlew :testernest-android:assembleRelease :testernest-core:assembleRelease
popd >/dev/null

SRC_ANDROID="$GRADLE_ROOT/testernest-android/build/outputs/aar/testernest-android-release.aar"
SRC_CORE="$GRADLE_ROOT/testernest-core/build/outputs/aar/testernest-core-release.aar"
DEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/Assets/Testernest/Plugins/Android"

mkdir -p "$DEST_DIR"
cp -f "$SRC_ANDROID" "$DEST_DIR/"
cp -f "$SRC_CORE" "$DEST_DIR/"

echo "Copied AARs to $DEST_DIR"
