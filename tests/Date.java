public class Date {

    public int day;

    public Date() {
    }

    public Date(int d) {
        this.day = d;
    }

    public int getDay() /*readonly*/ {
        return day;
    }

    public void setDay(int d) /*mutable*/ {
        this.day = d;
    }

}
