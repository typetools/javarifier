public class WildCards<T extends /*readonly*/ Object> {

    private T f;

    public static /*readonly*/ Date getDate(/*readonly*/ Cell<? extends /*readonly*/ Date> c) {
        return c.getVal();
    }

    public void setF(/*readonly*/ Cell<? extends T> x) /*mutable*/ {
        f = x.getVal();
    }

    public void addF(/*mutable*/ Cell<? super T> y) /*readonly*/ {
        y.setVal(f);
    }

    public static void setDay(/*readonly*/ Cell<? extends /*mutable*/ Date> z) {
        z.getVal().setDay(3);
    }

    public static /*mutable*/ Cell<? super /*readonly*/ Date> baz() {
        return new Cell<Date>();
    }

    public static void quax() {
        /*mutable*/ Date d = new Date();
        d.setDay(3);
        baz().setVal(d);
    }

    public static String printVal(/*readonly*/ Cell<?> c) {
        String s = c.toString();
        return s;
    }
}
