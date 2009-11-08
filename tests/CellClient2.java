public class CellClient2<S extends /*mutable*/ Date> {

    private /*this-mutable*/Cell<S> f;

    public CellClient2(S t) {
        f = new Cell<S>();
        f.setVal(t);
    }

    public /*readonly*/ Cell<S> getF() /*readonly*/ {
        return f;
    }

    public void foo() {
        /*readonly*/ Cell<S> tmp = getF();
        /*mutable*/ Date d = tmp.getVal();
        d.setDay(2);
    }

    public void setDate(S t) /*mutable*/ {
        f.setVal(t);
    }
}
