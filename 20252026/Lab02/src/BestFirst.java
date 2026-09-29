import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;



/**
 * Best-first search engine that expands states in order of
 * path cost {@code g}, where {@code g} is the sum of {@code getK()}
 * along the path.
 *
 * @author Francisco Melo, a84085
 * @version 13-10-2025
 */

public class BestFirst {

    public static class State {
        private Ilayout layout;
        private State father;
        private double g;


        /**
         * Builds a state from a layout and its parent.
         *
         * @param l the underlying {@link Ilayout}.
         * @param n the parent state; may be {@code null} for the root.
         */

        public State(Ilayout l, State n) {
            layout = l;
            father = n;
            if (father!=null)
                g = father.g + l.getK();
            else g = 0.0;
        }

        public String toString() { return layout.toString(); }


        /**
         * @return the cumulative path cost {@code g} for this state.
         */
        public double getG() {return g;}

        public int hashCode() {
            return toString().hashCode();
        }

        public boolean equals (Object o) {
            if (o==null) return false;
            if (!(o instanceof State n)) return false;
            return this.layout.equals(n.layout);
        }
    }

    protected Queue<State> abertos;
    private Map<Ilayout, State> fechados;
    private Ilayout objective;


    /**
     * Generates the list of successor states for the given state.
     *
     * <p>Expansion is based on {@link Ilayout#children()} from the
     * current state's layout. Direct backtracking is prevented
     * (a child identical to {@code father.layout} is skipped).</p>
     *
     * @param n the state to expand.
     * @return a list of successor {@link State}s.
     */
    private List<State> sucessores(State n) {
        List<State> sucs = new ArrayList<>();
        for(Ilayout e: n.layout.children()) {
            if (n.father == null || !e.equals(n.father.layout)){
                sucs.add(new State(e, n));
            }
        }

/*
        //Childrens and current costs - Debugging
        System.out.println("Current tested " + n);
        System.out.println("Children " + sucs + "\nCosts: ");
        for (State state : sucs)
            System.out.printf("%.0f ", state.g);
        System.out.println("\n");
*/
        return sucs;
    }

    public Iterator<BestFirst.State> solve(Ilayout s, Ilayout goal) {
        objective = goal;
        abertos = new PriorityQueue<>(10, (s1, s2) -> (int) Math.signum(s1.getG()-s2.getG()));
        Map<Ilayout, State> abertos_mirror = new HashMap<>();
        fechados = new HashMap<> ();

        State current_best = null;

        abertos.add(new State(s, null));

        assert abertos.peek() != null;
        abertos_mirror.put(abertos.peek().layout, abertos.peek());

        while (!abertos.isEmpty()) {

            State actual = abertos.remove();
            abertos_mirror.remove(actual.layout);

/*
            //Tester for current best path
            System.out.println("Current Best Path: ");
            State tester = actual;
            while (tester != null){
                System.out.printf(tester + ": Cost %.0f, ", tester.g);
                tester = tester.father;
            }

            System.out.println("\n");
*/


            //Find first solution
            if (actual.layout.equals(objective)){
                List<State> list = new ArrayList<>();
                while (actual != null){
                    list.add(actual);
                    actual = actual.father;
                }
                Collections.reverse(list);
                return list.iterator();
            }


//            //Always find best
//            if (actual.layout.equals(objective)){
//                if (current_best == null)
//                    current_best = actual;
//                else if (current_best.g > actual.g)
//                    current_best = actual;
//            }

            fechados.put(actual.layout, actual);

            //Add children to open queue
            for (State state : sucessores(actual)){
                if (!fechados.containsKey(state.layout) || fechados.containsKey(state.layout) && fechados.get(state.layout).g > state.g) {
                    if (abertos.contains(state)) {
                        if (abertos_mirror.get(state.layout).g <= state.g)
                            continue;
                        abertos.remove(state);
                    }

                    abertos.add(state);
                    abertos_mirror.put(state.layout, state);
                }
            }
        }

//        //Always find best
//        if (current_best != null){
//            List<State> list = new ArrayList<>();
//            while (current_best != null){
//                list.add(current_best);
//                current_best = current_best.father;
//            }
//            Collections.reverse(list);
//            return list.iterator();
//        }

        return null;
    }
}
