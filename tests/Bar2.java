public class Bar2 {
    /*readonly*/ Integer x;

    public Bar2() {
        x = null;
    }

    public int foo() {
        return x.intValue();
    }

    // readonly because nowhere is the return type of fooChange used in a
    // mutable manner.
    public /*readonly*/ Integer fooChange(Integer i) {
        x = i;
        return i;
    }

    public /*readonly*/ Integer fooReadOnly() {
        return x;
    }

    public /*readonly*/ Integer fooNull() {
        x = null;
        return x;
    }
}
