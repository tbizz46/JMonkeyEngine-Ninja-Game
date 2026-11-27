#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
JDK="../jMonkeyEngine SD.app/Contents/Home"
if [ ! -x "$JDK/bin/java" ]; then
  echo "Java was not found. Keep this game beside the jMonkeyEngine app."
  exit 1
fi
mkdir -p build/classes .runtime
"$JDK/bin/javac" -encoding UTF-8 -cp 'lib/*' -d build/classes src/mygame/*.java
if [ "${1:-}" = "--build" ]; then echo "Build passed."; exit 0; fi
if [ "${1:-}" = "--test" ]; then
  "$JDK/bin/javac" -cp 'lib/*:build/classes' -d build/classes tests/mygame/*.java
  "$JDK/bin/java" -ea -cp 'lib/*:build/classes' mygame.DuelStateTest
  "$JDK/bin/java" --enable-native-access=ALL-UNNAMED -Djava.io.tmpdir="$PWD/.runtime" -ea -cp 'lib/*:build/classes:assets' mygame.ModelImportsTest
  "$JDK/bin/java" -ea -cp 'lib/*:build/classes:assets' mygame.GameAudioTest
  "$JDK/bin/java" -ea -cp 'lib/*:build/classes:assets' mygame.NinjaCombatPoseTest
  "$JDK/bin/java" -ea -cp 'lib/*:build/classes' mygame.FlipTest
  exit 0
fi
exec "$JDK/bin/java" -XstartOnFirstThread --enable-native-access=ALL-UNNAMED -Djava.io.tmpdir="$PWD/.runtime" -cp 'build/classes:assets:lib/*' mygame.Main "$@"
