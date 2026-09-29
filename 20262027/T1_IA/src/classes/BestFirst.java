package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import interfaces.Ilayout;

public class BestFirst {
    protected Queue<State> abertos;
    private Map<Ilayout, State> fechados;
    private State actual;
    private Ilayout objective;

    public static class State {
        private Ilayout layout;
        private State father;
        private double g;

        public State(Ilayout l, State n) {
            layout = l;
            father = n;
            if (father != null)
                g = father.g + l.getK();
            else
                g = 0.0;
        }

        public String toString() {
            return layout.toString();
        }

        public double getG() {
            return g;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (this.getClass() != o.getClass())
                return false;
            State n = (State) o;
            return this.layout.equals(n.layout);
        }
    }

    final private List<State> sucessores(State n) {
        List<State> sucs = new ArrayList<>();
        List<Ilayout> children = n.layout.children();
        for (Ilayout e : children) {
            if (n.father == null || !e.equals(n.father.layout)) {
                State nn = new State(e, n);
                sucs.add(nn);
            }
        }
        return sucs;
    }

    /**
     * Procura o caminho de menor custo entre a configuracao inicial e a objetivo.
     *
     * <p>Um sucessor so e adicionado a {@code abertos} se ainda nao estiver
     * em {@code abertos} nem em {@code fechados}.
     *
     * @param s configuracao inicial
     * @param goal configuracao objetivo
     * @return iterador sobre os estados do caminho, do inicial ao objetivo
     *         (ambos incluidos), ou {@code null} se nao existir solucao
     */
    final public Iterator<State> solve(Ilayout s, Ilayout goal) {
        objective = goal;
        abertos = new PriorityQueue<>(10,
                (s1, s2) -> (int) Math.signum(s1.getG() - s2.getG()));
        fechados = new HashMap<>();
        abertos.add(new State(s, null));
        List<State> sucs;

        while(true){
            // Por verificar vazio = erro. (O erro e processado na main do prof)
            if (abertos.size() == 0){ return null; }

            // Pop no primeiro dos abertos (menor custo, lista ordenada por custo)
            actual = abertos.poll();


            if (actual.layout.isGoal(objective)){
                List<State> prList = new ArrayList<>(); // (PrintList) lista que sera enviada para a Main printar

                // Adicionar uma lista do primeiro ate ao ultimo pois e a unica ordem a que temos acesso
                while (actual.father != null){
                    prList.add(actual);
                    actual = actual.father;
                }
                prList.add(actual); // Adicionar a {@code State} dada pelo user
                prList = prList.reversed(); // Inverter lista para dar match a ordem exigida nos prints

                return prList.iterator();
            }

            sucs = sucessores(actual);
            fechados.put(actual.layout, actual);

            for (State suc : sucs){
                // Se {@code fechados} ou {@code abertos} for igual ao sucessor atual, nao adicionar aos abertos
                // Resumo: Se ja o verificamos ou se ja esta por verificar uma opcao mais barata, nao adicionar a lista "por verificar"
                if (fechados.containsKey(suc.layout) || abertos.contains(suc)) continue;
                abertos.add(suc);
            }
        }
    }
}