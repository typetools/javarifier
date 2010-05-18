package javarifier;

import javarifier.util.*;
import java.util.*;

// Keeps track of the source causes of constraints
public class ConstraintTracker {

    // indexed on the rhs so that they can be traversed from result to cause
    private static Map<Pair<ConstraintVar, ConstraintVar>, SourceCause> singleCauses = 
               new HashMap<Pair<ConstraintVar, ConstraintVar>, SourceCause>();
    private static Map<Pair<Pair<ConstraintVar, ConstraintVar>, ConstraintVar>, SourceCause> doubleCauses =
               new HashMap<Pair<Pair<ConstraintVar, ConstraintVar>, ConstraintVar>, SourceCause>();

    // records the source cause of an unguarded constraint
    public static void add(ConstraintVar lhs, ConstraintVar rhs, SourceCause cause) {
        singleCauses.put(new Pair<ConstraintVar,ConstraintVar>(lhs, rhs), cause);
    }

    // records the source cause of a guarded constraint
    public static void add(ConstraintVar guard, ConstraintVar lhs, ConstraintVar rhs, SourceCause cause) {
        doubleCauses.put(new Pair<Pair<ConstraintVar, ConstraintVar>,ConstraintVar>(
                         new Pair<ConstraintVar, ConstraintVar>(guard, lhs), rhs), cause);
    }

    // looks up the source cause of a given unguarded constraint
    public static SourceCause lookupCause(ConstraintVar lhs, ConstraintVar rhs) {
        return singleCauses.get(new Pair<ConstraintVar,ConstraintVar>(lhs, rhs));
    }
    // looks up the source cause of the given guarded constraint
    public static SourceCause lookupCause(Pair<ConstraintVar,ConstraintVar> lhs, ConstraintVar rhs) {
        return doubleCauses.get(new Pair<Pair<ConstraintVar, ConstraintVar>, ConstraintVar>(lhs, rhs));
    }

    /*TODO: remove old code
    
    public static class ConstraintCause {
        public ConstraintVar guard, left;
        public SourceCause cause;
        public ConstraintCause(ConstraintVar g, ConstraintVar l, SourceCause c) {
            guard = g; left = l; cause = c;
        }
    }
    
    public static void add(ConstraintVar guard, ConstraintVar lhs, ConstraintVar rhs, SourceCause cause, RuntimeException e) {
        if (Options.v().debugConstraints()) {
            //TODO: do something better with trace or don't use it
            StringBuilder buf = new StringBuilder();
            buf.append(cause.getInfo());
            buf.append("\n");
            for (StackTraceElement se : e.getStackTrace()) {
                buf.append("  " + se.toString()+"\n");
            }
            cause.setExpl(buf.toString());
        }
        add(guard, lhs, rhs, cause);
    } */

}
