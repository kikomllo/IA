import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests of how {@link Console} reads the move of the human: it must ask again until the move is valid. */
class ConsoleTests {

    private PrintStream originalOut;

    @BeforeEach
    void silenceTheConsole() {
        originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
    }

    @AfterEach
    void restoreTheConsole() {
        System.setOut(originalOut);
    }

    private static Console consoleReading(String input) {
        return new Console(new Scanner(input));
    }

    /** Replaces the board of the console, which is private. */
    private static void setBoard(Console console, Board board) throws ReflectiveOperationException {
        Field f = Console.class.getDeclaredField("board");
        f.setAccessible(true);
        f.set(console, board);
    }

    @Test
    void aValidIndexIsReturnedAtOnce() {
        assertEquals(4, consoleReading("4\n").getHumanMove());
    }

    @Test
    void textThatIsNotANumberIsAskedAgain() {
        assertEquals(2, consoleReading("abc\n\n3.5\n2\n").getHumanMove());
    }

    @Test
    void anIndexOutsideTheBoardIsAskedAgain() {
        assertEquals(8, consoleReading("-1\n9\n100\n8\n").getHumanMove());
    }

    @Test
    void anOccupiedCellIsAskedAgain() throws ReflectiveOperationException {
        Console console = consoleReading("0\n4\n1\n");
        setBoard(console, TestPositions.play(0, 4));
        assertEquals(1, console.getHumanMove());
    }

    @Test
    void theMoveIsNotPlayedByTheReader() throws ReflectiveOperationException {
        Console console = consoleReading("5\n");
        Board board = new Board();
        setBoard(console, board);
        console.getHumanMove();
        assertEquals(9, board.getAvailableMoves().size());
    }

    @Test
    void anInputThatEndsWithoutAValidMoveIsReported() {
        assertThrows(IllegalStateException.class, () -> consoleReading("x\n9\n").getHumanMove());
        assertThrows(IllegalStateException.class, () -> consoleReading("").getHumanMove());
    }
}
