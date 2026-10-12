#!/usr/bin/env bash
# Compila src/ e test/ e roda todos os testes JUnit 4 pela linha de comando
# (alternativa ao "Run As > JUnit Test" do Eclipse). Requer JDK 17+.
set -euo pipefail
cd "$(dirname "$0")/.."
CP="lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}java"
JAVAC_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
if ! "$JAVAC_BIN" -version >/dev/null 2>&1; then
  echo "Configure JAVA_HOME para um JDK 17 ou superior (pode ser o JDK do Eclipse)." >&2
  exit 1
fi
rm -rf build && mkdir -p build/main build/test
"$JAVAC_BIN" -encoding UTF-8 --release 17 -d build/main $(find src -name '*.java')
"$JAVAC_BIN" -encoding UTF-8 --release 17 -cp "build/main:$CP" -d build/test $(find test -name '*.java')
CLASSES=$(cd build/test && find . -name '*Test.class' | sed 's|^\./||; s|\.class$||; s|/|.|g' | sort)
"$JAVA_BIN" -cp "build/main:build/test:$CP" org.junit.runner.JUnitCore $CLASSES
