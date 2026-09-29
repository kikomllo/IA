package Algorithm;

public class Config {
    private final String config;

    public Config(String config){
        this.config = config;
    }

    public boolean matches(Config test){
        return this.toString().matches(test.toString());
    }

    @Override
    public String toString() {
        return config;
    }
}
