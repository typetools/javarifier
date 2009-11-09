public class Romaybe1 {

    public /*romaybe*/ Date id(/*romaybe*/ Date d) /*readonly*/ {
        return d;
    }

    // forces id not to have readonly returnt type.
    public void foo(/*mutable*/ Date x) /*readonly*/ {
        id(x).setDay(2);
    }
}
