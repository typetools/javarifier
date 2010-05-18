package javarifier;

import java.util.*;
import javarifier.util.Pair;

public class VarTracker {
    // for each solved variable, prints out the shortest justification for each.
    public static void printCauses(Set<ConstraintVar> vars) {
        System.out.println("Causes:");
        for (ConstraintVar var : vars) {
            System.out.println(causeToString(var));
        }
        System.out.println("");
    }

    public static String causeToString(ConstraintVar var) {
        Set<ConstraintVar> seen = new LinkedHashSet<ConstraintVar>();
        LinkedList<String> stack = new LinkedList<String>();

        int causeCount = shortestCause(var, stack, seen, 0);
        if(causeCount > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("Cause count: " + Integer.toString(causeCount) + "\n");
            for(String s : stack) {
                buf.append(s);
                buf.append("\n");
            }
            return buf.toString();
        } else {
            return "ERROR: 0 cause count for " + var.toString();
        }
    }

    // given an integer, returns a string which indents by that amount
    private static String indentString(int indent) {
        String ret = "";
        for(int i = 0; i < indent; i++) ret += "    ";
        return ret;
    }

    // mutates the passed linked list, adding the causes
    private static int shortestCause(ConstraintVar rhs, LinkedList<String> result, Set<ConstraintVar> seen, int indent) {    
        seen.add(rhs);
        VarEvidence evidence = rhs.getEvidence();

        //if there is a direct source cause, then that's the shortest explanation
        SourceCause source = evidence.getCause();
        if(source != null) {
            result.add(indentString(indent) + rhs.toString());
            result.add(indentString(indent+1) + source.toString());
            seen.remove(rhs);
            return 1;
        }
        
        LinkedList<String> best = null;
        int bestSize = Integer.MAX_VALUE;
        int causeCount = 0;
        for(ConstraintVar lhs : evidence.getSingleGuards()) {
            SourceCause cause = ConstraintTracker.lookupCause(lhs, rhs);
            if(cause != null) {
                if(!seen.contains(lhs)) {
                    LinkedList<String> trial = new LinkedList<String>();
                    int rCount = shortestCause(lhs, trial, seen, indent); 
                    
                    if(rCount > 0 && trial.size() < bestSize) {
                        best = trial;
                        best.addFirst(indentString(indent+1) + cause.toString());
                    }
                    
                    causeCount += rCount;
                }
            }
        }
        for(Pair<ConstraintVar, ConstraintVar> p : evidence.getDoubleGuards()) {
            SourceCause cause = ConstraintTracker.lookupCause(p, rhs);
            if(cause != null) {
                if(!seen.contains(p.first) && !seen.contains(p.second)) {
                    LinkedList<String> trial = new LinkedList<String>();
                    
                    int gCount = shortestCause(p.first, trial, seen, indent+2);
                    trial.add(indentString(indent) + "GUARD:");
                    int rCount = shortestCause(p.second, trial, seen, indent+2);
                    
                    if(gCount > 0 && rCount > 0 && trial.size() < bestSize) {
                        best = trial;
                        best.addFirst(indentString(indent) + cause.toString());
                    }
                    
                    causeCount += rCount + gCount;
                }
            }
        }
        if(best != null) {
            best.addFirst(indentString(indent) + rhs.toString());
            result.addAll(best);
        }
        seen.remove(rhs);
        return causeCount;
    }
}
