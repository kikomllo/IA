import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import classes.BallSortLayout;
import interfaces.Ilayout;

/**
 * Unit tests do {@link BallSortLayout} e testes de integracao do {@link Main}
 * para o Ball Sort Puzzle.
 */
class PuzzleUnitTests {

    // ---------------------------------------------------------------
    // Construtor / toString
    // ---------------------------------------------------------------

    @Test
    public void testConstructorRoundTrip() {
        String config = "E E B E\nE R B E\nR R B E\n";
        BallSortLayout b = new BallSortLayout(config);
        assertEquals(config, b.toString());
    }

    @Test
    public void testConstructorRoundTrip2() {
        String config = "B E E\nR B E\n";
        BallSortLayout b = new BallSortLayout(config);
        assertEquals(config, b.toString());
    }

    // ---------------------------------------------------------------
    // equals() / hashCode()
    // ---------------------------------------------------------------

    @Test
    public void testEquals() {
        BallSortLayout b1 = new BallSortLayout("B E E\nR B E\n");
        BallSortLayout b2 = new BallSortLayout("B E E\nR B E\n");
        assertEquals(b1, b2);
    }

    @Test
    public void testEqualsDifferent() {
        BallSortLayout b1 = new BallSortLayout("B E E\nR B E\n");
        BallSortLayout b2 = new BallSortLayout("E B E\nB R E\n");
        assertFalse(b1.equals(b2));
    }

    @Test
    public void testHashCode() {
        BallSortLayout b1 = new BallSortLayout("B E E\nR B E\n");
        BallSortLayout b2 = new BallSortLayout("B E E\nR B E\n");
        assertEquals(b1.hashCode(), b2.hashCode());
    }

    // ---------------------------------------------------------------
    // children() - Illegal moves
    // ---------------------------------------------------------------

    @Test
    public void testChildrenLegalMoves() {
        BallSortLayout b = new BallSortLayout("B E E\nR B E\n");
        List<Ilayout> children = b.children();

        assertEquals(3, children.size());

        Set<String> expected = new HashSet<>();
        expected.add("E B E\nR B E\n"); 
        expected.add("E E E\nR B B\n"); 
        expected.add("B E E\nR E B\n"); 

        assertEquals(expected, asStrings(children));
    }

    // ---------------------------------------------------------------
    // children() - Illegal move by different color
    // ---------------------------------------------------------------

    @Test
    public void testChildrenIllegalDifferentColor() {
        BallSortLayout b = new BallSortLayout("E E E\nB R E\n");
        List<Ilayout> children = b.children();

        assertEquals(2, children.size());

        Set<String> expected = new HashSet<>();
        expected.add("E E E\nE R B\n");
        expected.add("E E E\nB E R\n");

        assertEquals(expected, asStrings(children));
    }

    // ---------------------------------------------------------------
    // children() - Illegal move by full tube
    // ---------------------------------------------------------------

    @Test
    public void testChildrenIllegalFullDestination() {
        BallSortLayout b = new BallSortLayout("R R E\n");
        List<Ilayout> children = b.children();

        assertEquals(2, children.size());

        Set<String> expected = new HashSet<>();
        expected.add("E R R\n");
        expected.add("R E R\n");

        assertEquals(expected, asStrings(children));
    }

    // ---------------------------------------------------------------
    // isGoal()
    // ---------------------------------------------------------------

    @Test
    public void testIsGoalTrue() {
        BallSortLayout b = new BallSortLayout("R B E\nR B E\n");
        assertTrue(b.isGoal(null));
    }

    @Test
    public void testIsGoalFalsePartial() {
        BallSortLayout b = new BallSortLayout("E B E\nR B E\n");
        assertFalse(b.isGoal(null));
    }

    @Test
    public void testIsGoalFalseMixedFull() {
        BallSortLayout b = new BallSortLayout("B R\nR B\n");
        assertFalse(b.isGoal(null));
    }

    // ---------------------------------------------------------------
    // getK()
    // ---------------------------------------------------------------

    @Test
    public void testGetK() {
        BallSortLayout b = new BallSortLayout("B E E\nR B E\n");
        Ilayout child = b.children().get(0);
        assertEquals(1.0, child.getK(), 0.0001);
    }

    // ---------------------------------------------------------------
    // Tests Main 
    // ---------------------------------------------------------------

    @Test
    public void testSample1() throws Exception {
        String input = "3 4\nE E B E\nE R B E\nR R B E\n";
        assertEquals("1\n", runMain(input));
    }

    @Test
    public void testSample2() throws Exception {
        String input = "3 3\nR B E\nB B E\nR R E\n";
        assertEquals("5\n", runMain(input));
    }

    @Test
    public void testSample3() throws Exception {
        String input = "3 4\nP R B E\nR B R E\nP B P E\n";
        assertEquals("6\n", runMain(input));
    }

    @Test
    public void testNoSolution() throws Exception {
        String input = "2 2\nB R\nR B\n";
        assertEquals("no solution found\n", runMain(input));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Set<String> asStrings(List<Ilayout> layouts) {
        Set<String> set = new HashSet<>();
        for (Ilayout l : layouts) {
            set.add(l.toString());
        }
        return set;
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
