public class SimpleCellClient {

    public void foo() {
        /*readonly*/ SimpleCell</*readonly*/ Date> c = new SimpleCell<Date>();
        /*readonly*/ Date d = c.val;
        int day = d.getDay();
    }

    public void bar() {
        /*mutable*/ SimpleCell</*readonly*/ Date> c = new SimpleCell<Date>();
        /*readonly*/ Date d = c.val;
        int day = d.getDay();
        /*readonly*/ Date e = new Date();
        c.val = e;
    }

    public void baz() {
        /*readonly*/ SimpleCell</*mutable*/ Date> c = new SimpleCell<Date>();
        /*mutable*/ Date d = c.val;
        d.setDay(3);
    }

    public void quax() {
        /*mutable*/ SimpleCell</*mutable*/ Date> c = new SimpleCell<Date>();
        /*mutable*/ Date d = c.val;
        d.setDay(5);
        Date e = new Date();
        c.val = e;
    }
}
