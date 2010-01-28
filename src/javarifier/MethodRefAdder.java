package javarifier;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;

/**
 * Adds a method reference to each Local in a Scene.
 */
public class MethodRefAdder extends SceneVisitor {

    public static void add(Scene s) {
        (new MethodRefAdder()).visitScene(s);
    }

    private SootMethod currentMethod;

    public void visitMethod(SootMethod meth) {
      // NOTE: do not call super.visitMethod(meth), as it leads to some
      // error checking which will intentionally fail until after the entire
      // scene has been set.
        currentMethod = meth;
        try {
          Body methodBody = meth.getBody();
            if (!meth.isBridge() && methodBody != null) {
                visitBody(methodBody);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                "MethodRefAdder.visitMethod: exception with: " + meth, e);
        }
    }

    public void visitLocal(Local loc) {
      // Want to set method before super class visit's Local in case
      // it assumes that the method has already been set.
        loc.setMethod(currentMethod);
        super.visitLocal(loc);
    }

}
