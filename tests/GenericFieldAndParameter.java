import java.util.*;

public class GenericFieldAndParameter {

    public Set<Date> set;
    public Set<Integer> ints;

    public GenericFieldAndParameter() {
    }

    // The parameter doesn't get changed; only a field gets changed
    public void foo(Set<Boolean> bools) {
        set.add(new Date());
    }

    public void foo(ArrayList<Red> reds) {
        reds.get(0).set(1);
    }
}
