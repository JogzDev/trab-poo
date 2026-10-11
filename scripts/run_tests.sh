#!/usr/bin/env bash
# Compila src/ e test/ e roda todos os testes JUnit 4 pela linha de comando
# (alternativa ao "Run As > JUnit Test" do Eclipse). Requer JDK 17+.
set -euo pipefail
cd "$(dirname "$0")/.."
CP="lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar"
rm -rf build && mkdir -p build/main build/test
javac -encoding UTF-8 --release 17 -d build/main $(find src -name '*.java')
javac -encoding UTF-8 --release 17 -cp "build/main:$CP" -d build/test $(find test -name '*.java')
CLASSES=$(cd build/test && find . -name '*Test.class' | sed 's|^\./||; s|\.class$||; s|/|.|g' | sort)
java -cp "build/main:build/test:$CP" org.junit.runner.JUnitCore $CLASSES
