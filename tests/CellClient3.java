public class CellClient3 {

    public static class Day {
        public int day;
        public Day() { }
        public Day(int d) { this.day = d; }
        public int getDay() /*readonly*/ { return day; }
        public void setDay(int d) /*mutable*/ { this.day = d; }
    }

    /*readonly*/ Cell</*this-mutable*/ Day> f;

    public void foo() /*mutable*/ {
        /*mutable*/ Day d = f.getVal();
        d.setDay(3);
    }

    public void bar() /*readonly*/ {
        /*readonly*/ Day rd = f.getVal();
        int x = rd.getDay();
    }

}
