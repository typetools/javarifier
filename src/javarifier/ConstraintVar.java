package javarifier;

/**
 * A ConstraintVar represents a variable in the Javarifier's constraints.
 * A variable is a value, something with a JrType, that has a certain
 * mutability in a certain context.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import javarifier.JrType.MutType;
import javarifier.util.Pair;

public class ConstraintVar {

    private final JrTyped value;

    private final MutType type;

    private final Context context;

    //cause-tracking fields
    private SourceCause sourceCause;
    private Pair<ConstraintVar, ConstraintVar> constraintCause;
    private Set<Pair<ConstraintVar, ConstraintVar>> guarded;

    private ConstraintVar(JrTyped value, MutType type, Context context) {
        this.value = value;
        this.type = type;
        this.context = context;
        checkRep();
    }

    private static Map<ConstraintVar, ConstraintVar> interned =
      new HashMap<ConstraintVar, ConstraintVar>();

    public static ConstraintVar create(JrTyped val,
                                       MutType type,
                                       Context context) {
      // Create temporary ConstraintVar, then use it to look up interned
      //  version.  If it is already interned, return interned copy, else
      //  add temporary variable to interned set.
      ConstraintVar tmp = new ConstraintVar(val, type, context);
        if (interned.containsKey(tmp)) {
            return interned.get(tmp);
        }
        interned.put(tmp, tmp);
        return tmp;
    }

    private boolean checkRep() {
      if (type == null || context == null) {
         throw new RuntimeException("Null pointer: " + this);
      }
        return true;
    }

    public JrTyped getValue() {
        return value;
    }

    public MutType getType() {
        return type;
    }

    public Context getContext() {
        return context;
    }

    // sets the direct cause
    public void addCause(ConstraintVar v) {
        if (constraintCause == null && sourceCause == null) {
            if (guarded != null) {
                //checks to see if this was originally from a double guard
                for (Pair<ConstraintVar, ConstraintVar> guard : guarded) {
                    if (guard.second == v) {
                        constraintCause = guard;
                        guarded = null;
                        return;
                    }
                }
            }
            //just a regular single
            constraintCause = new Pair<ConstraintVar, ConstraintVar>(null, v);
            guarded = null;
        }
    }
    // adds a potential double guard cause
    public void addCause(Pair<ConstraintVar, ConstraintVar> v) {
        //If we don't have a concrete cause yet, keep information regarding double guards
        if (constraintCause == null && sourceCause == null) {
            if (guarded == null) guarded = new HashSet<Pair<ConstraintVar,ConstraintVar>>();
            guarded.add(v);
        }
    }
    //sets a direct source cause for the variable (usually related directly to the nature of its declaration)
    public void setSource(SourceCause x) {
        sourceCause = x;
        guarded = null;
    }
    public SourceCause getSource() {
        return sourceCause;
    }

    //returns a string which represents the shortest cause
    public String causeString() {
        StringBuilder buf = new StringBuilder();
        causeRec("", buf);
        return buf.toString();
    }

    //recursive helper for causeString
    private void causeRec(String prefix, StringBuilder buf) {
        buf.append(prefix);
        buf.append(this.toString());
        buf.append("\n");

        if (constraintCause == null) {
            if (sourceCause == null) {
                buf.append("ERROR: NO CAUSE\n");
            } else {
                buf.append(prefix);
                buf.append(sourceCause.toString());
                buf.append("\n");
            }
        } else {
            SourceCause cause = constraintCause.first != null ?
                ConstraintTracker.lookupCause(constraintCause,        this)
              : ConstraintTracker.lookupCause(constraintCause.second, this);
            if (cause != null) {
                buf.append(prefix);
                buf.append(cause.toString());
                buf.append("\n");
            } else {
                buf.append("ERROR: NO CONSTRAINT CAUSE\n");
            }
            if (constraintCause.first != null) {
                buf.append(prefix);
                buf.append("GUARD:\n");
                constraintCause.first.causeRec(prefix+"    ", buf);
            }
            constraintCause.second.causeRec(prefix, buf);
        }
    }

    private static boolean eq(Object o1, Object o2) {
        return (o1 == o2) || (o1 != null && o1.equals(o2));
    }
    private static int hc(Object o1) {
        return (o1 == null) ? 0 : o1.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ConstraintVar) {
            ConstraintVar other = (ConstraintVar) obj;
            return
                eq(value, other.value) &&
                type.equals(other.type) &&
                context.equals(other.context);
        } else {
            return false;
        }
    }

    public static String contextString(Context context) {
        switch (context) {
        case MUTABLE:
            return "ctx:m";
        case READONLY:
            return "ctx:r";
        case NONE:
            return "ctx:n";
        default:
            throw new Error("this can't happen");
        }
    }

    public String toString() {
        return "<" + value + ": " +
            (value == null ? type : value.getJrType())
            + " " + type.getIndex() + " " + contextString(context) + ">";
    }

    public int hashCode() {
        return 7*hc(value) + type.hashCode();
    }
}
