#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
JDK="../jMonkeyEngine SD.app/Contents/Home"
./run.command --build
mkdir -p dist
stage=$(mktemp -d "$PWD/dist/.package-XXXXXX")
mkdir -p "$stage/input" "$stage/output"
cp lib/*.jar "$stage/input/"
"$JDK/bin/jar" --create --file "$stage/input/night-ninja-duel.jar" \
  --main-class mygame.Main -C build/classes mygame -C assets .
echo "Building the Mac app..."
"$JDK/bin/jpackage" --type app-image --input "$stage/input" \
  --main-jar night-ninja-duel.jar --main-class mygame.Main \
  --name "Night Ninja Duel" --dest "$stage/output" --runtime-image "$JDK" \
  --java-options '-XstartOnFirstThread' \
  --java-options '--enable-native-access=ALL-UNNAMED' \
  --mac-package-identifier com.terenceboyce.nightninjaduel
if [ -d 'dist/Night Ninja Duel.app' ]; then
  mv 'dist/Night Ninja Duel.app' "$stage/Previous Night Ninja Duel.app"
fi
mv "$stage/output/Night Ninja Duel.app" 'dist/Night Ninja Duel.app'
echo 'Built: dist/Night Ninja Duel.app'
echo "The previous app, if any, is kept in $stage."
