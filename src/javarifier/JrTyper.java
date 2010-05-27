package javarifier;

import soot.Scene;

/**
 * JrTyper is a class that centralizes methods from other classes that are
 * used to add Java 1.5 types to all references.
 */
public class JrTyper {
    private JrTyper() {} // Don't instantiate.  This class is just a collection
    // of static methods from other classes.

    public static void type(Scene s) {
        // Pre processing steps of adding Java 1.5 types to all
        // references
        TypeInitializer.loadSignatures(s);
        printNonQuiet("TypeInitializer finished.");

        OwnerAdder.addOwners(Scene.v());
        printNonQuiet("OwnerAdder finished.");

        RawTypeWildcardifier.wildcardifyRawTypes(s);
        printNonQuiet("RawTypeWildcardifier finished.");

        TypeIndexer.index(Scene.v());
        printNonQuiet("TypeIndexer finished.");

        TypeInferencer.infer(Scene.v());
        printNonQuiet("TypeInferencer finished.");

        RepChecker.checkRep(Scene.v());
        printNonQuiet("RepChecker finished.");
    }

    private static void printNonQuiet(String s) {
      if (!Options.v().reallyQuiet()) {
        System.out.println(s);
      }
    }
}
