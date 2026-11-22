#!/usr/bin/env bash
# Run the experiments of the report (item viii) and write results/*.csv and results/RESUMO.md.
#   ./run_experiments.sh          -> full run (can take several minutes)
#   ./run_experiments.sh quick    -> a few games per configuration, to check that everything works
# Run it from the T4_IA folder. Needs JDK 25 (or at least the javac on the PATH).
set -e
cd "$(dirname "$0")"

JAVAC=$(command -v javac || echo "$HOME/.jdks/openjdk-25/bin/javac")
JAVA=$(dirname "$JAVAC")/java

rm -rf out-exp && mkdir -p out-exp
"$JAVAC" -d out-exp src/Ilayout.java src/Board.java src/XAgent.java src/MCTS.java src/MCTSAgent.java \
    test/Minimax.java test/TestPositions.java test/Experiments.java
"$JAVA" -cp out-exp Experiments "$@"
rm -rf out-exp
