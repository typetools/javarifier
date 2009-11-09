package javarifier;

import java.util.*;

public class VarTracker {

    public static void printCauses(Set<ConstraintVar> vars) {
        if (Options.v().dumpCauses()) {
            System.out.println("\nCauses:");
            for (ConstraintVar var : vars) {
                System.out.println("\n"+printCauses(var));
            }
            System.out.println("");
        }
    }

    public static String printCauses(ConstraintVar var) {
        StringBuilder buf = new StringBuilder();
        Set<ConstraintVar> seen = new HashSet<ConstraintVar>();
        while (var != null) {
            if (seen.contains(var)) {
                buf.append(var.toString() + " LOOP: This should never happen ");
                return buf.toString();
            }
            seen.add(var);
            buf.append(var.toString() + " <- ");
            var = var.getCause();
        }
        buf.append("?");
        return buf.toString();
    }

}
