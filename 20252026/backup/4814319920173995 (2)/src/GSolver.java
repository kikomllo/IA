import java.util.Iterator;

//Graph solver
public class GSolver {

    /**
     @Implements a graph solver suitable to solve this problem
     based on the version developed in the:
     Lab tutorial 1: Small Instances of the 8-puzzle
     */

    public Iterator<BestFirst.State>solve(Ilayout s, Ilayout goal) {
        BestFirst bf = new BestFirst();
        return bf.solve(s, goal);
    }

}