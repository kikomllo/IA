import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Experiments for the report (item viii). Not a unit test: run it with {@code ./run_experiments.sh}.
 * <p>
 * It plays MCTS against a random agent, against perfect minimax and against another MCTS, for several
 * numbers of iterations and values of {@code c}, half of the games with MCTS as X and half as O, and
 * writes {@code results/resultados.csv}, {@code results/jogadas_otimas.csv} and {@code results/RESUMO.md}.
 * <p>
 * Simulations are counted without touching {@link MCTS}: the layout given to the search is wrapped in a
 * decorator that counts the calls to {@code getWinner()}; MCTS calls it once per backpropagated result
 * (the root simulation, each child simulation and each terminal leaf), which is exactly {@code n(root)}.
 */
public class Experiments {

    static final long BASE_SEED = 12345;
    static final int[] ITERATIONS = {100, 1000, 10000};
    static final double[] C_VALUES = {0.1, 0.2, 0.5, Math.sqrt(2)};
    static final String[] OPPONENTS = {"aleatorio", "minimax", "mcts"};
    static final Minimax ORACLE = new Minimax();

    /** Games per (configuration, role): fewer when the budget is large, to keep the run time reasonable. */
    static int gamesPerRole(int iterations) {
        return iterations >= 10000 ? 15 : iterations >= 1000 ? 50 : 100;
    }

    /** Decorator that counts the simulations MCTS performs (see the class comment). */
    static final class Counting implements Ilayout {
        private final Ilayout inner;
        private final AtomicLong counter;

        Counting(Ilayout inner, AtomicLong counter) { this.inner = inner; this.counter = counter; }

        public boolean move(int index) { return inner.move(index); }
        public boolean isGameOver() { return inner.isGameOver(); }
        public ID getTurn() { return inner.getTurn(); }
        public ID getWinner() { counter.incrementAndGet(); return inner.getWinner(); }
        public HashSet<Integer> getAvailableMoves() { return inner.getAvailableMoves(); }
        public List<Ilayout> children() {
            List<Ilayout> out = new ArrayList<>();
            for (Ilayout child : inner.children()) out.add(new Counting(child, counter));
            return out;
        }
        @Override public String toString() { return inner.toString(); }
    }

    /** Result of one block of games, from MCTS's point of view. */
    static final class Stats {
        int wins, draws, losses, moves;
        long nanos, simulations;
    }

    static int opponentMove(String opponent, Board b, Random random, int iterations, double c, long seed) {
        switch (opponent) {
            case "aleatorio": {
                List<Integer> free = new ArrayList<>(b.getAvailableMoves());
                Collections.sort(free);
                return free.get(random.nextInt(free.size()));
            }
            case "minimax":
                return ORACLE.randomOptimalMove(b, random);
            default:                                        // another MCTS with the same configuration
                return new MCTSAgent(iterations, c, seed).choose(b);
        }
    }

    static Stats play(int iterations, double c, String opponent, Ilayout.ID role, int games, long seedBase) {
        Stats s = new Stats();
        for (int g = 0; g < games; g++) {
            long seed = seedBase + g;
            Random random = new Random(seed);
            Board b = new Board();
            while (!b.isGameOver()) {
                int cell;
                if (b.getTurn() == role) {
                    MCTSAgent agent = new MCTSAgent(iterations, c, seed + 31L * b.getAvailableMoves().size());
                    long t0 = System.nanoTime();
                    cell = agent.choose(b);                 // timed without the counting decorator
                    s.nanos += System.nanoTime() - t0;
                    s.moves++;
                    AtomicLong counter = new AtomicLong();
                    int again = agent.choose(new Counting(b, counter));
                    if (again != cell) throw new IllegalStateException("decorator changed the move");
                    s.simulations += counter.get();
                } else {
                    cell = opponentMove(opponent, b, random, iterations, c, seed + 7777);
                }
                b.move(cell);
            }
            Ilayout.ID w = b.getWinner();
            if (w == role) s.wins++; else if (w == Ilayout.ID.Blank) s.draws++; else s.losses++;
        }
        return s;
    }

    public static void main(String[] args) throws IOException {
        boolean quick = args.length > 0 && args[0].equals("quick");
        Path dir = Path.of("results");
        Files.createDirectories(dir);
        long start = System.currentTimeMillis();

        List<String[]> rows = new ArrayList<>();
        // aggregated[opponent][iterIdx][cIdx] = {wins, draws, losses, games, nanos, moves, sims}
        Map<String, long[]> agg = new LinkedHashMap<>();

        for (int it : ITERATIONS) {
            if (quick && it > 1000) continue;
            int games = quick ? 4 : gamesPerRole(it);
            for (double c : C_VALUES) {
                for (String opp : OPPONENTS) {
                    long[] a = new long[7];
                    for (Ilayout.ID role : new Ilayout.ID[]{Ilayout.ID.X, Ilayout.ID.O}) {
                        Stats s = play(it, c, opp, role, games, BASE_SEED + 1000L * it);
                        rows.add(new String[]{"" + it, String.format(Locale.ROOT, "%.3f", c), opp, role.name(),
                            "" + games, "" + s.wins, "" + s.draws, "" + s.losses,
                            String.format(Locale.ROOT, "%.3f", s.nanos / 1e6 / Math.max(1, s.moves)),
                            String.format(Locale.ROOT, "%.1f", (double) s.simulations / Math.max(1, s.moves)),
                            "" + (BASE_SEED + 1000L * it)});
                        a[0] += s.wins; a[1] += s.draws; a[2] += s.losses; a[3] += games;
                        a[4] += s.nanos; a[5] += s.moves; a[6] += s.simulations;
                    }
                    agg.put(opp + "|" + it + "|" + String.format(Locale.ROOT, "%.3f", c), a);
                    System.out.printf(Locale.ROOT, "it=%d c=%.3f vs %s: V/E/D = %d/%d/%d%n", it, c, opp, a[0], a[1], a[2]);
                }
            }
        }

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(dir.resolve("resultados.csv"), StandardCharsets.UTF_8))) {
            out.println("iteracoes,c,adversario,papel,partidas,vitorias,empates,derrotas,tempo_medio_jogada_ms,simulacoes_por_jogada,semente_base");
            for (String[] r : rows) out.println(String.join(",", r));
        }

        // optimal-move rate by c and iterations
        int positions = quick ? 20 : 150;
        Map<String, Integer> optimal = new LinkedHashMap<>();
        List<Board> boards = new ArrayList<>();
        Random pr = new Random(2024);
        for (int i = 0; i < positions; i++) boards.add(TestPositions.randomPosition(pr));
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(dir.resolve("jogadas_otimas.csv"), StandardCharsets.UTF_8))) {
            out.println("iteracoes,c,posicoes,jogadas_otimas");
            for (int it : ITERATIONS) {
                if (quick && it > 1000) continue;
                for (double c : C_VALUES) {
                    int ok = 0;
                    for (int i = 0; i < positions; i++) {
                        int cell = new MCTSAgent(it, c, 500 + i).choose(boards.get(i));
                        if (ORACLE.optimalMoves(boards.get(i)).contains(cell)) ok++;
                    }
                    optimal.put(it + "|" + String.format(Locale.ROOT, "%.3f", c), ok);
                    out.printf(Locale.ROOT, "%d,%.3f,%d,%d%n", it, c, positions, ok);
                    System.out.printf(Locale.ROOT, "optimal moves it=%d c=%.3f: %d/%d%n", it, c, ok, positions);
                }
            }
        }

        writeSummary(dir.resolve("RESUMO.md"), agg, optimal, positions, quick);
        System.out.println("Done in " + (System.currentTimeMillis() - start) / 1000 + " s. Files in " + dir.toAbsolutePath());
    }

    static void writeSummary(Path file, Map<String, long[]> agg, Map<String, Integer> optimal,
                             int positions, boolean quick) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# Resultados das experiências (item viii)\n\n");
        sb.append("Gerado por `test/Experiments.java`").append(quick ? " (modo quick: poucas partidas)" : "").append(".\n\n");
        sb.append("- Semente base: ").append(BASE_SEED).append(" (+1000·iterações); MCTS com MAX = quem joga na raiz.\n");
        sb.append("- Partidas por combinação: 100 por papel com 100 iterações, 50 com 1 000, 15 com 10 000 (X e O em partes iguais); ")
          .append("cada célula das tabelas junta os dois papéis.\n");
        sb.append("- Células: **V/E/D** do MCTS (vitórias/empates/derrotas).\n");
        sb.append("- `mcts` = outro MCTS com a mesma configuração e semente diferente. `minimax` escolhe ao acaso entre as jogadas ótimas.\n");
        sb.append("- «Simulações» = `n(raiz)` por jogada (contadas com um decorador de `Ilayout`, sem alterar o `MCTS`).\n\n");

        for (String opp : OPPONENTS) {
            sb.append("## V/E/D contra ").append(opp).append("\n\n| iterações \\ c |");
            for (double c : C_VALUES) sb.append(String.format(Locale.ROOT, " %.2f |", c));
            sb.append("\n|---|").append("---|".repeat(C_VALUES.length)).append("\n");
            for (int it : ITERATIONS) {
                if (quick && it > 1000) continue;
                sb.append("| ").append(it).append(" |");
                for (double c : C_VALUES) {
                    long[] a = agg.get(opp + "|" + it + "|" + String.format(Locale.ROOT, "%.3f", c));
                    sb.append(" ").append(a[0]).append("/").append(a[1]).append("/").append(a[2]).append(" |");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("## Tempo médio por jogada (ms) e simulações por jogada (contra aleatório)\n\n");
        sb.append("| iterações | c | ms/jogada | simulações/jogada |\n|---|---|---|---|\n");
        for (int it : ITERATIONS) {
            if (quick && it > 1000) continue;
            for (double c : C_VALUES) {
                long[] a = agg.get("aleatorio|" + it + "|" + String.format(Locale.ROOT, "%.3f", c));
                sb.append(String.format(Locale.ROOT, "| %d | %.2f | %.2f | %.0f |%n", it, c,
                        a[4] / 1e6 / Math.max(1, a[5]), (double) a[6] / Math.max(1, a[5])));
            }
        }

        sb.append("\n## Jogadas ótimas em ").append(positions).append(" posições aleatórias (minimax como oráculo)\n\n");
        sb.append("| iterações \\ c |");
        for (double c : C_VALUES) sb.append(String.format(Locale.ROOT, " %.2f |", c));
        sb.append("\n|---|").append("---|".repeat(C_VALUES.length)).append("\n");
        for (int it : ITERATIONS) {
            if (quick && it > 1000) continue;
            sb.append("| ").append(it).append(" |");
            for (double c : C_VALUES)
                sb.append(" ").append(optimal.get(it + "|" + String.format(Locale.ROOT, "%.3f", c))).append("/").append(positions).append(" |");
            sb.append("\n");
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
