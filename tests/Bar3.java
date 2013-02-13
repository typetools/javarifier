public class Bar3 {
    public /*readonly*/ String[/*this-mutable*/] x;
    int i;

    public Bar3() {
        x = new String[10];
        i = 0;
    }

    public /*readonly*/ String get(int i) {
        return x[i];
    }

    public void add(String s) {
        x[i] = s;
        i++;
    }

    public void clear() {
        x = new String[10];
        i = 0;
    }
}
