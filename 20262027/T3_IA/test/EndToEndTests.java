import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Edge to edge tests: the text goes in through {@code System.in} and the answer is read from
 * {@code System.out}, exactly as Mooshak does it. Run them from the T3_IA folder
 * (the tests that read files use {@code test/inputs/}).
 *
 * <p>The expected answers of T01..T10 were computed with the reference solver
 * ({@link TestSupport#optimal}), which does not share code with the classes under test.
 */
class EndToEndTests {

    // ---------------------------------------------------------------
    // The inputs we have (test/inputs): expected output of each one
    // ---------------------------------------------------------------

    @Test
    public void testSample1File() throws Exception {
        assertEquals("37\n", runMain(readInput("sample1.txt")));
    }

    @Test
    public void testHardBoardWithFourEmptyTubes() throws Exception {
        // 5 colors, H = 8, 4 empty tubes: the heaviest shape found in the stress tests (about 25000 states
        // expanded). Too big for the reference solver: 39 is the answer of this A*, and h is proven not to
        // overestimate (see AStarHeuristicTests), so it can not be too big by a wrong heuristic.
        assertEquals("39\n", runMain(readInput("hard1.txt")));
    }

    @Test
    public void testT01() throws Exception {
        assertEquals("0\n", runMain(readInput("T01.txt")));
    }

    @Test
    public void testT02() throws Exception {
        assertEquals("1\n", runMain(readInput("T02.txt")));
    }

    @Test
    public void testT03() throws Exception {
        assertEquals("5\n", runMain(readInput("T03.txt")));
    }

    @Test
    public void testT04() throws Exception {
        assertEquals("6\n", runMain(readInput("T04.txt")));
    }

    @Test
    public void testT05() throws Exception {
        assertEquals("no solution found\n", runMain(readInput("T05.txt")));
    }

    @Test
    public void testT06() throws Exception {
        assertEquals("2\n", runMain(readInput("T06.txt")));
    }

    @Test
    public void testT07() throws Exception {
        assertEquals("10\n", runMain(readInput("T07.txt")));
    }

    @Test
    public void testT08() throws Exception {
        assertEquals("1\n", runMain(readInput("T08.txt")));
    }

    @Test
    public void testT09() throws Exception {
        assertEquals("17\n", runMain(readInput("T09.txt")));
    }

    @Test
    public void testT10() throws Exception {
        assertEquals("no solution found\n", runMain(readInput("T10.txt")));
    }

    // ---------------------------------------------------------------
    // Format of the input and of the output
    // ---------------------------------------------------------------

    @Test
    public void testOutputIsOnlyTheNumberAndANewline() throws Exception {
        String out = runMain(readInput("sample1.txt"));
        assertTrue(out.matches("[0-9]+\n"), "unexpected output: [" + out + "]");
    }

    @Test
    public void testInputWithoutFinalNewline() throws Exception {
        assertEquals("1\n", runMain("3 4\nE E B E\nE R B E\nR R B E"));
    }

    @Test
    public void testInputWithWindowsLineEndings() throws Exception {
        assertEquals("1\n", runMain("3 4\r\nE E B E\r\nE R B E\r\nR R B E\r\n"));
    }

    // ---------------------------------------------------------------
    // Edge shapes of the board
    // ---------------------------------------------------------------

    @Test
    public void testOneTubeFull() throws Exception {
        assertEquals("0\n", runMain("3 1\nR\nR\nR\n"));
    }

    @Test
    public void testHeightOneAllFull() throws Exception {
        assertEquals("0\n", runMain("1 5\nR B P Y G\n"));
    }

    @Test
    public void testHeightOneWithEmptyTube() throws Exception {
        assertEquals("0\n", runMain("1 6\nR B P Y G E\n"));
    }

    @Test
    public void testSingleColorSpreadInThreeTubes() throws Exception {
        // R | R | R, capacity 3: move two balls onto the third tube
        assertEquals("2\n", runMain("3 3\nE E E\nE E E\nR R R\n"));
    }

    @Test
    public void testManyEmptyTubes() throws Exception {
        // B | R | B | R and three empty tubes: each ball goes onto the one with its color
        assertEquals("2\n", runMain("2 7\nE E E E E E E\nB R B R E E E\n"));
    }

    @Test
    public void testAllTubesFullAndMixedHasNoSolution() throws Exception {
        assertEquals("no solution found\n", runMain("3 3\nR B P\nB P R\nP R B\n"));
    }

    @Test
    public void testFiveColorsAlreadySolved() throws Exception {
        assertEquals("0\n", runMain("2 7\nR B P Y G E E\nR B P Y G E E\n"));
    }

    @Test
    public void testTallTubes() throws Exception {
        // H = 25 (more than any packed encoding would hold) and a solved board with an empty tube
        StringBuilder in = new StringBuilder("25 3\n");
        for (int i = 0; i < 25; i++) in.append("R B E\n");
        assertEquals("0\n", runMain(in.toString()));
    }

    // ---------------------------------------------------------------
    // A real run of the program: new JVM, input from a file. Includes the
    // start of the JVM, so this is what Mooshak measures.
    // ---------------------------------------------------------------

    @Test
    @Tag("perf")
    public void testSample1InANewProcessUnderOneSecond() throws Exception {
        Path classes = Path.of("out");
        assertTrue(Files.isDirectory(classes), "compile into ./out first (use run_tests.sh)");

        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        long worst = 0;
        for (int run = 0; run < 3; run++) {
            ProcessBuilder pb = new ProcessBuilder(java, "-cp", classes.toString(), "Main");
            pb.redirectInput(Path.of("test", "inputs", "sample1.txt").toFile());
            pb.redirectErrorStream(true);

            long t0 = System.nanoTime();
            Process p = pb.start();
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(p.waitFor(10, TimeUnit.SECONDS));
            long ms = (System.nanoTime() - t0) / 1_000_000;

            assertEquals(0, p.exitValue());
            assertEquals("37\n", out.replace("\r\n", "\n"));
            worst = Math.max(worst, ms);
        }
        assertTrue(worst < 1000, "the whole program took " + worst + " ms");
    }

    @Test
    @Tag("perf")
    public void testHardBoardInANewProcessUnderOneSecond() throws Exception {
        // The thin one: about 0.5 s here, and it has jumped above 1 s when the machine was busy
        Path classes = Path.of("out");
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        ProcessBuilder pb = new ProcessBuilder(java, "-cp", classes.toString(), "Main");
        pb.redirectInput(Path.of("test", "inputs", "hard1.txt").toFile());
        pb.redirectErrorStream(true);

        long t0 = System.nanoTime();
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(p.waitFor(20, TimeUnit.SECONDS));
        long ms = (System.nanoTime() - t0) / 1_000_000;

        assertEquals("39\n", out.replace("\r\n", "\n"));
        System.out.println("[edge to edge] hard board, new process: " + ms + " ms");
        assertTrue(ms < 1000, "the whole program took " + ms + " ms");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String readInput(String name) throws Exception {
        return Files.readString(Path.of("test", "inputs", name));
    }

    // Feed input to the supplied Main and capture its complete output.
    private String runMain(String input) throws Exception {
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(bytes, true, "UTF-8");
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captured);
            new Main().main();
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
            captured.close();
        }
        return bytes.toString("UTF-8").replace("\r\n", "\n");
    }
}
