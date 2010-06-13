package javarifier;

/**
 *  Stores both a location in the 
 */
public class SourceCause {

    // what caused the constraint to form
    // example: "Board.java:86:"
    private SourceLocation location;
    // example:  "$e0 = l0.board"
    private String stmt;
    // example:  "means that ...",   "a -> b"
    private String expl;

    public SourceCause(SourceLocation l, String e) {
        location = l; stmt = ""; expl = e;
    }

    public SourceCause(SourceLocation l, String s, String e) {
        location = l; stmt = s; expl = e;
    }

    public String toString() { return prefixedString(""); }

    //  prefix utilized to indent multiple lines properly and efficiently
    public String prefixedString(String prefix) {
        return prefix + (location != null ? location.toString() : "") +
               (stmt != "" ? " <<" + stmt + ">>" : "") +
               (expl != "" ? "\n" + prefix + expl : "");
    }

    public SourceLocation getLocation() { return location; }
    public String getStmt() { return stmt; }
    public String getExpl() { return expl; }

    public void setExpl(String e) { expl = e; }
}
