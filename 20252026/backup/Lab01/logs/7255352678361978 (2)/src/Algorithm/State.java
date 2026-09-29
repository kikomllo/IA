package Algorithm;

public class State {
    private Config config = null;
    private State father = null;
    private int count = 0;

    public State(Config config, State father){
        this.config = config;
        this.father = father;
        if (father != null)
            this.count = father.getCount() + 1;
    }

    public boolean matches(Config obj) {
        return config.matches(obj);
    }

    public State getFather() {
        return father;
    }

    public int getCount(){
        return count;
    }
}
