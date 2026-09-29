import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Ilayout {
    /**
        @return the children of the receiver.
    */
    List<Ilayout> children();
    
    /**
        @return true if the receiver equals the argument l;
        return false otherwise.
    */
    boolean isGoal(Ilayout l);
    
    /**
        @return the cost for moving from the input config to the receiver.
    */
    double getK();

    /**
        @return the heuristic estimated to goal.
     */
    double getH();

    /**
         Calculates the heuristic estimated to goal.
     */
    void setH(Map<?, ?> goalIndexes);

    Map<?, ?> getDataKeyValues();
}
