import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class BestFirst {

    public static class State {
        private Ilayout layout;
        private State father;
        private double g;

        public State(Ilayout l, State n) {
            layout = l;
            father = n;
            if (father!=null)
                g = father.g + l.getK();
            else g = 0.0;
        }

        public String toString() { return layout.toString(); }

        public double getG() {return g;}

        public int hashCode() {
            return toString().hashCode();
        }

        public boolean equals (Object o) {
            if (o==null) return false;
            if (!(o instanceof State)) return false;
            State n = (State) o;
            return this.layout.equals(n.layout);
        }
    }

    protected Queue<State> abertos;
    private Map<Ilayout, State> fechados;
    private Ilayout objective;

    final private List<State> sucessores(State n) {
        List<State> sucs = new ArrayList<>();
        for(Ilayout e: n.layout.children()) {
            if (n.father == null || !e.equals(n.father.layout)){
                sucs.add(new State(e, n));
            }
        }

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

            if (actual.layout.equals(objective)){
                if (current_best == null)
                    current_best = actual;
                else if (current_best.g > actual.g)
                    current_best = actual;
            }

            fechados.put(actual.layout, actual);
            
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
