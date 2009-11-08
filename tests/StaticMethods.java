public class StaticMethods {

    public static /*romaybe*/ Date id(/*romaybe*/ Date d) {
        return d;
    }

    public static void foo(/*mutable*/ Date x) {
        id(x).setDay(3);
    }

    public static /*mutable*/ Cell</*mutable*/ Date> baz() {
        return new Cell<Date>();
    }

    public static void quax() {
        baz().setVal(new Date());
        baz().getVal().setDay(3);
    }


}
