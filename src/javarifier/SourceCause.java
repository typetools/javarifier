package javarifier;

/**
 *  Stores a location in the program, and information explaining why this
 * location caused a constraint or variable to be inferred.
 */
public class SourceCause {

    // what caused the constraint to form
    private SourceLocation location;
    private String stmt;
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
