package javarifier.util;

/** Works correctly with null entries. */
public class Pair<F, S> {
    public final F first;
    public final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F first() { return first; }
    public S second() { return second; }

    public String toString() {
        return "(" + first + ", " + second + ")"; // too many "<" floating around, use "("
    }

    public boolean equals(Object obj) {
        if (! (obj instanceof Pair))  {
            return false;
        }
        Pair other = (Pair) obj;

        if (first != other.first) { // this check handles when both are null
            if (! first.equals(other.first)) {
                return false;
            }
        }

        if (second != other.second) { // this check handles when both are null
            if (! second.equals(other.second)) {
                return false;
            }
        }
        return true;

    }

    public int hashCode() {
        if (first == null && second == null) {
            return 1;
        }
        if (first == null) {
            return second.hashCode();
        }

        if (second == null) {
            return first.hashCode();
        }

        return first.hashCode() + second.hashCode()*7;
    }
}
