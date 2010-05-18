package javarifier;

import soot.Scene;
import soot.SootMethod;
import soot.SootField;

import javarifier.JrType.*;

/**
 * Forces all public method return types to have mutable (or polyread)
 * types and all public fields have this-mutable types.
 */
public class OpenWorld extends SceneVisitor {


    public static ConstraintManager generate(Scene s) {
        OpenWorld generator = new OpenWorld();
        generator.visitScene(s);
        if (Options.v().dumpOpenWorldCons()) {
            System.out.println("Open world constraints:");
            System.out.println(generator.getConstraints());
        }
        return generator.getConstraints();
    }


    private ConstraintManager cm = new ConstraintManager();
    public ConstraintManager getConstraints() {
        return cm;
    }


    public void visitMethod(SootMethod meth) {
        if (meth.isPublic()) {
            meth.getJrType().accept(new MakeMutableReturnType(meth, cm));
        }
    }

    public void visitField(SootField field) {
        if (field.isPublic()) {
            field.getJrType().accept(new MakeMutableField(field, cm));
        }
    }


    private static class MakeMutableReturnType extends EmptyTypeVisitor {

        private SootMethod meth;
        private ConstraintManager cm;

        public MakeMutableReturnType(SootMethod val, ConstraintManager cm) {
            this.meth = val;
            this.cm = cm;
        }


        public void visitClassType(ClassType type) {
            cm.mutable4(meth, type, Context.MUTABLE, new SourceCause(new SourceLocation(meth), "Public return in open world"));
            for (TypeArg targ : type.getTypeArgs()) {
                targ.accept(this);
            }
        }

        public void visitArrayType(ArrayType type) {
            cm.mutable4(meth, type, Context.MUTABLE, new SourceCause(new SourceLocation(meth), "Public return in open world"));
            super.visitArrayType(type);
        }


    }


    private static class MakeMutableField extends EmptyTypeVisitor {

        private SootField field;
        private ConstraintManager cm;

        public MakeMutableField(SootField val, ConstraintManager cm) {
            this.field = val;
            this.cm = cm;
        }


        public void visitClassType(ClassType type) {
            cm.mutable4(field, type, Context.NONE, new SourceCause(new SourceLocation(field), "Public field in open world"));
            super.visitClassType(type);
        }

        public void visitArrayType(ArrayType type) {
            cm.mutable4(field, type, Context.NONE, new SourceCause(new SourceLocation(field), "Public field in open world"));
            super.visitArrayType(type);
        }


    }


}
