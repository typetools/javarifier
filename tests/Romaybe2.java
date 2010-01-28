public class Romaybe2 {

    private /*this-mutable*/ Date f;

    public /*romaybe*/ Date getDate() /*romaybe*/ {
        return f;
    }

    // This method forces f to this-mutable instead of readonly.
    public void foo() /*mutable*/ {
        getDate().setDay(3);
    }
}
