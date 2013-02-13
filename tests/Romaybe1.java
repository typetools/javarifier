public class Romaybe1 {

    public static class Day {
        public int day;
        public Day() { }
        public Day(int d) { this.day = d; }
        public int getDay() { return day; }
        public void setDay(int d) { this.day = d; }
    }

    public /*romaybe*/ Day id(Day d) {
        return d;
    }

    // forces id not to have readonly returnt type.
    public void foo(Day x) {
        id(x).setDay(2);
    }
}
