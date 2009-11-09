
/**
 * Tests things such as Object o = Date[] and Object[] a = Date[][].
 */
public class SpecialObjectCases {

    public static void foo() {
        Object o = new Date[3];
        o = new Cell<Date>();
        Object[] a = new Date[3][1];
    }

    public static String bar(Object o) {
        if (o.hashCode() < 0) {
            o = new Cell<Date>();
        } else if (o.hashCode() > 10) {
            o = new Date[3];
        }
        String s = o.toString();
        return s;
    }
}
