public class CellClient {

    public void foo() {
        /*readonly*/ Cell</*readonly*/ Date> c = new Cell<Date>();
        /*readonly*/ Date d = c.getVal();
        int day = d.getDay();
    }

    public void bar() {
        /*mutable*/ Cell</*readonly*/ Date> c = new Cell<Date>();
        /*readonly*/ Date d = c.getVal();
        int day = d.getDay();
        /*readonly*/ Date e = new Date();
        c.setVal(e);
    }

    public void baz() {
        /*readonly*/ Cell</*mutable*/ Date> c = new Cell<Date>();
        /*mutable*/ Date d = c.getVal();
        d.setDay(3);
    }

    public void quax() {
        /*mutable*/ Cell</*mutable*/ Date> c = new Cell<Date>();
        /*mutable*/ Date d = c.getVal();
        d.setDay(5);
        /*mutable*/ Date e = new Date();
        c.setVal(e);
    }
}
