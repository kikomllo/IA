#!/usr/bin/env bash
# Compile src + test into ./out and run the JUnit tests.
#   ./run_tests.sh          -> all tests (including the slow "perf" ones)
#   ./run_tests.sh fast     -> everything except the "perf" tests
# Run it from the T3_IA folder.
set -e
cd "$(dirname "$0")"

JAVAC=$(command -v javac || echo "$HOME/.jdks/openjdk-25/bin/javac")
JAVA=$(dirname "$JAVAC")/java
JUNIT=lib/junit-platform-console-standalone-1.10.2.jar

rm -rf out && mkdir -p out
"$JAVAC" -d out -cp "$JUNIT" $(find src test -name '*.java')

EXCLUDE=""
[ "$1" = "fast" ] && EXCLUDE="--exclude-tag perf"
"$JAVA" -jar "$JUNIT" execute -cp out --scan-classpath out $EXCLUDE \
    --details=tree --disable-banner
