/**
 * This class tests static inner classes.
 */
public class StaticInnerClass {

    /*this-mutable*/ Foo f;


    public void bar() /*mutable*/ {
        /*readonly*/ Date d = new Date();
        /*mutable*/ Foo x = new Foo(d);
        f = x;
        f.d = new Date();

        /*mutable*/ Foo y = new Foo(new Date()); //assigned to this-mut field
        f = y;
        int i = f.d.getDay();

        /*readonly*/ Foo z = new Foo(new Date());

        int j = f.d.getDay();

    }

    public static class Foo {
        /*readonly*/ Date d;
        public Foo(/*readonly*/ Date d) {
            this.d = d;
        }
    }


}
