package Algorithm;

import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class Algorithm {
    private Comparator<State> comparator = (s1, s2) -> Integer.compare(s2.getCount(), s1.getCount()); 

    private PriorityQueue<State> open = new PriorityQueue<>(comparator);
    private Queue<State> closed = new LinkedList<>();

    private Config getInput(InputStream stream){
        Scanner scanner = new Scanner(stream);
        Config initial = new Config(scanner.nextLine());

        open.add(new State(initial, null));
        return new Config(scanner.nextLine());
    }

    private void newStates(State current){
        
    }

    private State mainLoop(Config goal){
        State current = null;
        while (true) {
            if (open.isEmpty())
                return null;
        
            current = open.remove();

            if (current.matches(goal))
                return current;

            else newStates(current);

            closed.add(current);
        }
        

        //TODO
    }

    private void showResult(State result){
        //TODO
    }

    public void run(){
        Config goal = getInput(System.in);
        State result = mainLoop(goal);

        if (result != null)
            showResult(result);
    }
}
