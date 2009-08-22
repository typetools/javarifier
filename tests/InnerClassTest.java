
public class InnerClassTest<S> {
    Date d;

    // $AIn
    public class AIn<T> {
        public AIn() {

        }
        Cell<T> f;
        public S sf;
    }

    AIn<Date> in;

    public AIn<Date> foo() {
        //InnerClassTest<Date> i = new InnerClassTest<Date>();
        InnerClassTest<S>.AIn<Date> x = this.in;
        return x;
    }

    // $Inner
    public class Inner {
        Cell<Date> c;
    }

    // $Nested static
    public static class Nested {
        Cell<Date> c;
    }


    // $1
    Cell<Date> initializedField = new Cell<Date>() {
        public void initializedFieldFoo() {}
    };



    // $2 static
    static Cell<Date> staticInitializedField = new Cell<Date>() {
        public void staticInitializedFieldFoo() {}
    };

    // $3
    Cell<Date> factory() {
        return new Cell<Date>() {
            public void factoryFoo() {}
            public S var() { return null; }
        };
    }

    // $4 static
    static Cell<Date> staticFactory() {
        return new Cell<Date>() {
            public void factoryFoo() {}
        };
    }
}
