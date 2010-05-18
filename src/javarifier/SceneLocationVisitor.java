package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Body;

class SceneLocationVisitor extends SceneVisitor {
    protected SourceLocation programLocation;

    public void visitClass(SootClass clazz) {
        programLocation = new SourceLocation(clazz);
        super.visitClass(clazz);
    }
    
    public void visitField(SootField f) {
        programLocation = new SourceLocation(f);
        super.visitField(f);
    }

    public void visitMethod(SootMethod meth) {
        programLocation = new SourceLocation(meth);
        super.visitMethod(meth);
    }

    public void visitBody(Body b) {
        programLocation = new SourceLocation(b);
        super.visitBody(b);
    }
}
