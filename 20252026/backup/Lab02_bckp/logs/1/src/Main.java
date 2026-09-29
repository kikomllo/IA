import java.util.Iterator;
import java.util.Scanner;

public class Main {
    public static void main (String [] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        GSolver gs = new GSolver();
        Iterator<BestFirst.State> it =
                gs.solve(
                        new ArrayCfg(sc.nextLine()),
                        new ArrayCfg(sc.nextLine()));
        if (it==null) System.out.println("no solution found");
        else {
            while(it.hasNext()) {
                BestFirst.State i = it.next();
                //System.out.println(i);
                if (!it.hasNext()) System.out.printf("%.0f", i.getG());
            }
        }
        sc.close();
    }
}