public class Assignable {

    // That commented @checkers.javari.quals.Assignable is recognized by the extended
    // compiler and supersedes Assignable.fields.
    private /*assignable readonly*/ /*@checkers.javari.quals.Assignable*/ Date d;
    public /*readonly*/ Date foo() /*readonly*/ {
        if (d == null) {
            d = new Date();
        }
        return d;
    }

}
