package javarifier;

/**
 * A ConstraintVar represents a variable in the Javarifier's constraints.
 * A variable is a value, something with a JrType, that has a certain
 * mutability in a certain context.
 */
import java.util.HashMap;
import java.util.Map;

import javarifier.JrType.MutType;

public class ConstraintVar {

    private final JrTyped value;

    private final MutType type;

    private final Context context;

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

    // For debugging
    private ConstraintVar cause;
    public void addCause(ConstraintVar c) {
        if (cause == null) {
            cause = c;
        }
    }
    public ConstraintVar getCause() {
        return cause;
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
