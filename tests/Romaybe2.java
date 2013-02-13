public class Romaybe2 {

    public static class Day {
        public int day;
        public Day() { }
        public Day(int d) { this.day = d; }
        public int getDay() { return day; }
        public void setDay(int d) { this.day = d; }
    }

    private /*this-mutable*/ Day f;

    public /*romaybe*/ Day getDay() {
        return f;
    }

    // This method forces f to this-mutable instead of readonly.
    public void foo() {
        getDay().setDay(3);
    }
}
