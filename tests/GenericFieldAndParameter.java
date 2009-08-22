import java.util.*;

public class GenericFieldAndParameter {

    public Set<Date> set;
    public Set<Integer> ints;

    public GenericFieldAndParameter() {
    }

    public void foo(Set<Boolean> bools) {
        set.add(new Date());
    }

    public void foo(ArrayList<Red> reds) {
        reds.get(0).set(1);
    }
}
