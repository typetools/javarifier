package javarifier;

/**
 * Stores a location in the program, and information explaining why this
 * location caused a constraint or variable to be inferred.
 */
public class SourceCause {

    // what caused the constraint to form
    // example: "(line 86) Board.foobar"
    // better would be:  "Board.java:86: " which can be prepended and then interpreted by tools just as compiler error messages are
    private SourceLocation location;
    // example:  "$e0 = l0.board"
    private String stmt;
    // example:  "means that ..."  (? is that correct?)
    private String expl;

    public SourceCause(SourceLocation l, String s) {
        location = l; stmt = s; expl = "";
    }

    public SourceCause(SourceLocation l, String s, String e) {
        location = l; stmt = s; expl = e;
    }

    public String toString() {
        return stmt + " " + expl + " " + (location != null ? location.toString() : "");
    }

    public SourceLocation getLocation() { return location; }
    public String getStmt() { return stmt; }
    public String getExpl() { return expl; }

    public void setExpl(String e) { expl = e; }
}
