public class CellClient2<S extends /*mutable*/ CellClient2.Day> {

    public static class Cell<T> {
	public T val;
	public T getVal() { return val; }
	public void setVal(T val) { this.val = val; }
    }

    public static class Day {
        public int day;
        public Day() { }
        public Day(int d) { this.day = d; }
        public int getDay() { return day; }
        public void setDay(int d) { this.day = d; }
    }

    private /*this-mutable*/ Cell<S> f;

    public CellClient2(S t) {
        f = new Cell<S>();
        f.setVal(t);
    }

    public /*readonly*/ Cell<S> getF() {
        return f;
    }

    public void foo() {
        /*readonly*/ Cell<S> tmp = getF();
        /*mutable*/ Day d = tmp.getVal();
        d.setDay(2);
    }

    public void setDay(S t) {
        f.setVal(t);
    }
}
