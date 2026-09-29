import java.util.*;

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
        private double f = 0;

        /**
         * Builds a state from a layout and its parent.
         *
         * @param l the underlying {@link Ilayout}.
         * @param n the parent state; may be {@code null} for the root.
         */

        public State(Ilayout l, State n) {
            layout = l;
            father = n;
            if (father!=null) {
                g = father.g + l.getK();
            }
            else g = 0.0;
        }

        public String toString() { return layout.toString(); }

        /**
         * @return the cumulative path cost {@code g} for this state.
         */
        public double getG() {return g;}

        public double getF() {return f;}

        public void calcF(Ilayout goal){
            this.layout.setH(goal.getDataKeyValues());

            double h = this.layout.getH();

            this.f = g + h;
        }

        @Override
        public int hashCode() {
            return layout.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State other)) return false;
            return layout.equals(other.layout);
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
                State new_state = new State(e, n);
                new_state.calcF(objective);
                sucs.add(new_state);
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

    public Iterator<State> solve(Ilayout s, Ilayout goal) {
        objective = goal;
        abertos = new PriorityQueue<>(10, (s1, s2) -> (int) Math.signum(s1.getF()-s2.getF()));
        Map<Ilayout, State> openMap = new HashMap<>();
        fechados = new HashMap<> ();

        State current_best = null;

        abertos.add(new State(s, null));

        assert abertos.peek() != null;
        openMap.put(abertos.peek().layout, abertos.peek());

        while (!abertos.isEmpty()) {

            State current = abertos.remove();
            openMap.remove(current.layout);

/*
            //Tester for current best path
            System.out.println("Current Best Path: ");
            State tester = current;
            while (tester != null){
                System.out.printf(tester + ": Cost %.0f, ", tester.g);
                tester = tester.father;
            }

            System.out.println("\n");
*/


            //Find first solution
            if (current.layout.equals(objective)){
                List<State> list = new ArrayList<>();
                while (current != null){
                    list.add(current);
                    current = current.father;
                }
                Collections.reverse(list);
                return list.iterator();
            }

/*
            //Always find best
            if (current.layout.equals(objective)){
                if (current_best == null)
                    current_best = current;
                else if (current_best.g > current.g)
                    current_best = current;
            }
*/
            fechados.put(current.layout, current);

            //Add children to open queue,
            for (State state : sucessores(current)){
                State closed = fechados.get(state.layout);
                if (closed == null || closed.g > state.g) {
                    State open = openMap.get(state.layout);
                    if (open != null) {
                        if (open.g <= state.g)
                            continue;

                        abertos.remove(open);
                    }

                    abertos.add(state);
                    openMap.put(state.layout, state);
                }
            }
        }

        if (current_best != null){
            List<State> list = new ArrayList<>();
            while (current_best != null){
                list.add(current_best);
                current_best = current_best.father;
            }
            Collections.reverse(list);
            return list.iterator();
        }

        return null;
    }
}
