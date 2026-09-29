import java.util.Iterator;
import java.util.Scanner;

public class Main {
    public static void main (String [] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        BestFirst s = new BestFirst();
        Iterator<BestFirst.State> it = s.solve(new Board(sc.nextLine().trim()), new Board(sc.nextLine().trim()));
        if (it==null) System.out.println("no solution found");
        else {
            System.out.println("Started printing");
            while(it.hasNext()) {
                BestFirst.State i = it.next();
                System.out.println(i);
                if (!it.hasNext()) System.out.printf("%.0f\n", i.getG());
            }
            System.out.println("Ended printing");
        }
        sc.close();
    }
}