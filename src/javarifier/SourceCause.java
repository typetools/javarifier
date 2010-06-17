package javarifier;

/**
 *  Used to store an location and information relating to the cause of the
 *  existance of a constraint.
 *
 *  This does not store causation for inference steps - see
 *  ConstraintVar for this information (constraintCause field)
 *
 *  SourceCauses are stored by ConstraintVar (sourceCause field) in the
 *  case that that variable is directly inferrable as mutable from the
 *  source code.  They are also recorded by ConstraintTracker, in order to 
 *  record information for debugging the source of constraints.
 */

public class SourceCause {

    // what caused the constraint to form
    // example: "Board.java:86:"
    private SourceLocation location;
    // example:  "$e0 = l0.board"
    private String statement;
    // example:  "means that ...",   "a -> b"
    private String explanation;
    private String subtyping;

    public SourceCause(SourceLocation l, String e) {
        location = l; statement = ""; explanation = e; subtyping = "";
    }

    public SourceCause(SourceLocation l, String s, String e) {
        location = l; statement = s; explanation = e; subtyping = "";
    }

    public String toString() { return prefixedString(""); }

    //  prefix utilized to indent multiple lines efficiently
    public String prefixedString(String prefix) {
        String result = "";
        if(location != null || statement != "") {
            result = prefix + (location != null ? location.toString() : "") +
                              (statement != "" ? statement : "");
        }
        if(subtyping != null && subtyping != "") result += (result != "" ? "\n" : "") + prefix + subtyping;
        else if(explanation != "") result += (result != "" ? "\n" : "") + prefix + explanation;
        return result;
    }

    public void setExplanation(String e) { explanation = e; }
    public void setSubtyping(String s) { subtyping = s; }
}
