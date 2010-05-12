package javarifier;

import javarifier.util.Pair;

import java.util.*;

public class ConstraintTracker {

    private static List<Pair<Pair<ConstraintVar, ConstraintVar>, String>> causes = new ArrayList<Pair<Pair<ConstraintVar, ConstraintVar>, String>>();

    public static void add(ConstraintVar lhs, ConstraintVar rhs, RuntimeException e) {
        if (Options.v().debugConstraints()) {
            StringBuilder buf = new StringBuilder();
            for (StackTraceElement se : e.getStackTrace()) {
                buf.append("  " + se.toString()+"\n");
            }
            add(lhs, rhs, buf.toString());
        }
    }

    public static void add(ConstraintVar lhs, ConstraintVar rhs, String cause) {
        Pair<ConstraintVar, ConstraintVar> constraint = new Pair<ConstraintVar, ConstraintVar>(lhs, rhs);
        causes.add(new Pair<Pair<ConstraintVar, ConstraintVar>, String>(constraint, cause));
    }

    public static void printCauses() {
        if (Options.v().debugConstraints()) {
            System.out.println("Constraint Causes");
            for (Pair<Pair<ConstraintVar, ConstraintVar>, String> cause : causes) {
                System.out.println(cause.first().first() + " -> " + cause.first().second());
                System.out.println("  " + cause.second());
            }
        }
    }

}
