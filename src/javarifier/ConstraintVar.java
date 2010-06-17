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

    // cause-tracking fields

    // Rep invariant:
    // this is known to be mutable <=> ((sourceCause != null) || (constraintCause != null))
    // this is not known to be mutable <=> ((sourceCause == null) && (constraintCause == null))
    // this is known to be mutable <=> (partialGuardCauses == null)
    /**
     * Non-null if this was mutable to start with -- no constraint had to
     * be fired to make this mutable.
     */
    private SourceCause sourceCause;
    /**
     * Null if this is not yet known to be mutable.
     * Non-null if this is known to be mutable:
     *  * If a normal constraint was fired to make this mutable, then the Pair is <non-null, null>.
     *  * If a double-partialGuardCauses constraint was fired to make this mutable, then the Pair is <non-null, non-null>.
     */
    private Pair<ConstraintVar, ConstraintVar> constraintCause;
    /** Records partial firing of double-guards (their transformation into
     * single guards).  Multiple double-guards may be turned into
     * single-guards before finally this is known to be mutable.
     * This is stored as a map from the unresolved constraint
     * variable to the resolved constraint variable, as this is the
     * information needed to determine if the double-guard is necessary.
     */
    private Map<ConstraintVar, ConstraintVar> partialGuardCauses;


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

    /** Sets a direct, single cause of the constraint variable.
     *
     * If this was the inner guard of a previous, partially fired
     * constraint, then that fact is stored as a pair of <a, b> in
     * constraintCause.  Otherwise, a pair of <null, b> is stored.
     *
     * If a constraintCause is already known, it is presumed to be simpler,
     * and preserved.
     */
    public void addCause(ConstraintVar v) {
        // If a direct cause was already found, or there's a source cause,
        // we don't need to record it.
        if (constraintCause != null || sourceCause != null) return;
        ConstraintVar guard = null;
        if (partialGuardCauses != null) {
            //checks to see if this was potentially from a double guard
            guard = partialGuardCauses.get(v);
        }
        constraintCause = new Pair<ConstraintVar, ConstraintVar>(guard, v);
        partialGuardCauses = null;
    }

    /** Adds a partially fired double guard of the form a -> (b -> c).
     *    v.first  Represents a, which is known mutable 
     *    v.second Represents b, which may be mutable
     *    this     Represents c
     *
     * If b has already been associated, this is ignored.
     */
    public void addCause(Pair<ConstraintVar, ConstraintVar> v) {
        // If we don't have a concrete cause yet, keep information regarding double guards
        if (constraintCause != null || sourceCause != null) return;
        if (partialGuardCauses == null) {
            // Initialize map storing partially-completed guards.
            partialGuardCauses = new HashMap<ConstraintVar,ConstraintVar>();
        }
        if(partialGuardCauses.get(v.second) == null) {
            // Since this key does not yet exist, there isn't a prior
            // double-cause association for this variable.  We assume that
            // earlier causes are more succinct
            partialGuardCauses.put(v.second, v.first);
        }
    }

    /** Sets a direct source cause for the variable.
     * After this happens, addCause will cease to have any effect.
     * Subsequent calls will overwrite the source cause.
     */
    public void setSource(SourceCause x) {
        sourceCause = x;
        partialGuardCauses = null;
    }

    /** Retrieves the direct source cause for a variable.
     * This is null if none exists.
     */
    public SourceCause getSource() {
        return sourceCause;
    }

    // returns a string which represents the shortest cause
    public String causeString() {
        StringBuilder buf = new StringBuilder();
        causeRec("", buf);
        return buf.toString();
    }

    // utilized in the following function
    protected void prefixedLine(StringBuilder buf, String a, String b) {
        buf.append(a); buf.append(b); buf.append("\n");
    }

    /** Recursive helper for causeString, recursively prints the stored
     * causes, attempting to yield the shortest possible complete cause
     * chain for the variable.
     */
    protected void causeRec(String prefix, StringBuilder buf) {
        prefixedLine(buf, prefix, this.toString());

        if (constraintCause == null) {
                buf.append(sourceCause == null ? "ERROR: NO CAUSE\n" :
                           sourceCause.prefixedString(prefix + "  ") + "\n");
        } else {
            //  Caveat: Rather than just assuming that a non-null
            // constraintCause.first indicates a double-guard, we
            // check if there is a direct cause stored in the tracker, as
            // this is preferable.
            SourceCause cause = ConstraintTracker.lookupCause(constraintCause.second, this);
            Boolean useDouble = false;
            if(cause == null && constraintCause.first != null) {
                cause = ConstraintTracker.lookupCause(constraintCause, this);
                useDouble = true;
            }

            // If there's a cause for this constraint, print it, indented
            buf.append(cause == null ? "ERROR: NO CONSTRAINT CAUSE\n" :
                       cause.prefixedString(prefix + "  ") + "\n");

            // If this is a double guard, print that.
            if (useDouble) {
                prefixedLine(buf, prefix, "GUARD:");
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
