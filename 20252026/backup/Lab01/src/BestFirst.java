import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class BestFirst {
    protected Queue<State> abertos;
    private Map<Ilayout, State> fechados;
    private Ilayout objective;
    
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

    final private List<State> sucessores(State n) {
        List<State> sucs = new ArrayList<>();
        for(Ilayout e: n.layout.children()) {
            if (n.father == null || !e.equals(n.father.layout)){
                sucs.add(new State(e, n));
            }
        }

        return sucs;
    }

    final public Iterator<State> solve(Ilayout s, Ilayout goal) {
        objective = goal;
        abertos = new PriorityQueue<>(10, (s1, s2) -> (int) Math.signum(s1.getG()-s2.getG()));
        fechados = new HashMap<> ();
        abertos.add(new State(s, null));

        while (!abertos.isEmpty()) { 
            State actual = abertos.remove();

            if (actual.layout.equals(objective)){ 
                List<State> list = new ArrayList<>();
                while (actual != null){
                    list.add(actual);
                    actual = actual.father;
                }
                Collections.reverse(list);
                return list.iterator();
            }

            fechados.put(actual.layout, actual);
            
            for (State state : sucessores(actual)){
                if (!fechados.containsKey(state.layout))
                    if (!abertos.contains(state))
                        abertos.add(state);
            }
        }

        return null;
    }
}
