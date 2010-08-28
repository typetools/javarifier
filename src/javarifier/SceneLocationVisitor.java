package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Body;

// Scene visitor which keeps track of the last-visited position in the
// source code 
class SceneLocationVisitor extends SceneVisitor {
    // Stores information regarding the source code location of the
    // outermost class, field, method, or method body.  This is used for
    // cases such as visitJrTyped(...) which does not take a tag carrying 
    // Soot Host (necessary for constructing sourceLocation)
    protected SourceLocation programLocation;
    
    private void setLocation(SourceLocation location) {
        // Only overwrite the program location if it has useful info
        if(location.lineNumber != -1 || programLocation == null) {
            programLocation = location;
        }
    }

    public void visitClass(SootClass clazz) {
        setLocation(new SourceLocation(clazz));
        super.visitClass(clazz);
    }

    public void visitField(SootField field) {
        setLocation(new SourceLocation(field));
        super.visitField(field);
    }

    public void visitMethod(SootMethod method) {
        setLocation(new SourceLocation(method));
        super.visitMethod(method);
    }

    public void visitBody(Body body) {
        setLocation(new SourceLocation(body));
        super.visitBody(body);
    }
}
