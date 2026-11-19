package classes;

import interfaces.Ilayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class BestFirst {
    protected Queue<State> abertos;
    // Para cada layout, o menor g encontrado ate agora (esteja ainda em abertos ou ja expandido)
    private Map<Ilayout, Double> melhorG;
    private State actual;
    private Ilayout objective;

    public static class State {
        private Ilayout layout;
        private State father;
        private double g;
        // h e f = g + h sao calculados uma vez, ao criar o State (a fila compara-os muitas vezes)
        private double h;
        private double f;

        public State(Ilayout l, State n, Ilayout goal) {
            layout = l;
            father = n;
            if (father != null)
                g = father.g + l.getK();
            else
                g = 0.0;
            h = l.getH(goal);
            f = g + h;
        }

        public String toString() {
            return layout.toString();
        }

        public double getG() {
            return g;
        }

        public int hashCode() {
            return layout.hashCode();
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
            sucs.add(new State(e, n, objective));
        }
        return sucs;
    }

    /**
     * Procura o caminho de menor custo entre a configuracao inicial e a objetivo.
     *
     * <p>Algoritmo A*: {@code abertos} esta ordenada por f = g + h (com f igual, o g maior primeiro).
     * Um sucessor so e adicionado a {@code abertos} se o layout for novo ou se este caminho
     * ate ele for mais barato que o melhor conhecido ({@code melhorG}).
     *
     * @param s configuracao inicial
     * @param goal configuracao objetivo
     * @return iterador sobre os estados do caminho, do inicial ao objetivo
     *         (ambos incluidos), ou {@code null} se nao existir solucao
     */
    final public Iterator<State> solve(Ilayout s, Ilayout goal) {
        objective = goal;

        // Ordenar por f; se for igual, o g maior primeiro (h menor = mais perto do goal)
        abertos = new PriorityQueue<>(1024, (s1, s2) -> {
            int c = Double.compare(s1.f, s2.f);
            return c != 0 ? c : Double.compare(s2.g, s1.g);
        });
        melhorG = new HashMap<>(1 << 16);

        State inicial = new State(s, null, objective);
        abertos.add(inicial);
        melhorG.put(s, inicial.g);
        List<State> sucs;

        while(true){
            // Por verificar vazio = erro. (O erro e processado na main do prof)
            if (abertos.size() == 0){ return null; }

            // Pop no primeiro dos abertos (menor f)
            actual = abertos.poll();

            // A fila nao deixa atualizar um State: quando se encontra um caminho mais barato para um layout
            // adiciona-se um novo e o antigo fica la dentro. Ao sair, ignora-se (melhorG ja tem um g menor)
            if (actual.g > melhorG.get(actual.layout)) continue;

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

            for (State suc : sucs){
                // Se o layout ja foi visto com um g igual ou menor, nao adicionar aos abertos
                Double antigo = melhorG.get(suc.layout);
                if (antigo != null && antigo <= suc.g) continue;
                melhorG.put(suc.layout, suc.g);
                abertos.add(suc);
            }
        }
    }
}
