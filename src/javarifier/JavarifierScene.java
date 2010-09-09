package javarifier;

import soot.SootClass;
import soot.SootMethod;
import soot.Body;

import java.util.Map;
import java.util.HashMap;

/** Soot nulls out method bodies within soot.Scene, so I am resorting to this.  Very sad. */
public class JavarifierScene {

    //    private MultiMap<SootClass, SootField> fields;
    // private MultiMap<SootClass, SootMethod> methods;
    private Map<SootMethod, Body> bodies;


    public JavarifierScene() {
        bodies = new HashMap<SootMethod, Body>();
    }

   public Body getBody(SootMethod meth) {
        return bodies.get(meth);
    }

    public void put(SootMethod meth, Body body) {
        bodies.put(meth, body);
    }

}
