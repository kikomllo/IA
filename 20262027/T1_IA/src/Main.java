import java.util.Iterator;
import java.util.Scanner;

import classes.BestFirst;
import classes.Board;

/**
 * Main do 8-puzzle.
 *
 * <p>Le o input (layout inicial e goal), corre o {@link BestFirst} e faz print
 * de todos os boards do path, separados por uma linha em branco. No fim
 * faz print do custo total do path.
 *
 * <p>Exemplo de input:
 * <pre>
 * 023145678
 * 123405678
 * </pre>
 *
 * @see BestFirst
 * @see Board
 */
public class Main {

    void main() throws Exception {
        Scanner sc = new Scanner(System.in);
        BestFirst s = new BestFirst();
        Iterator<BestFirst.State> it = s.solve(new Board(sc.next()),
                new Board(sc.next()));
        if (it == null)
            IO.println("no solution found");
        else {
            while (it.hasNext()) {
                BestFirst.State i = it.next();
                IO.println(i);
                if (!it.hasNext())
                    IO.println((int) i.getG());
            }
        }
        sc.close();
    }
}
