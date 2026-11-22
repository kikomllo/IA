#!/usr/bin/env bash
# Compile the project and its tests into ./out and run the JUnit tests.
#   ./run_tests.sh          -> all tests (including the slow "slow" ones)
#   ./run_tests.sh fast     -> everything except the "slow" tests
# Run it from the T4_IA folder. Needs JDK 25 (Console uses java.lang.IO); if the javac on the PATH is
# older, it falls back to ~/.jdks/openjdk-25.
#
# Console.java is compiled too, so that ConsoleTests can check how it reads the move of the human.
set -e
cd "$(dirname "$0")"

JAVAC=$(command -v javac || echo "$HOME/.jdks/openjdk-25/bin/javac")
JAVA=$(dirname "$JAVAC")/java
JUNIT=lib/junit-platform-console-standalone-1.10.2.jar

rm -rf out && mkdir -p out
"$JAVAC" -d out -cp "$JUNIT" src/Ilayout.java src/Board.java src/XAgent.java src/MCTS.java src/MCTSAgent.java src/Console.java \
    $(find test -name '*.java')

EXCLUDE=""
[ "$1" = "fast" ] && EXCLUDE="--exclude-tag slow"
"$JAVA" -jar "$JUNIT" execute -cp out --scan-classpath out $EXCLUDE \
    --details=tree --disable-banner
