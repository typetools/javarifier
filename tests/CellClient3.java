public class CellClient3 {

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

    /*readonly*/ Cell</*this-mutable*/ Day> f;

    public void foo() {
        /*mutable*/ Day d = f.getVal();
        d.setDay(3);
    }

    public void bar() {
        /*readonly*/ Day rd = f.getVal();
        int x = rd.getDay();
    }

}
