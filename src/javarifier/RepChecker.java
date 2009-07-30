package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Local;

import javarifier.JrType.*;

import javarifier.util.Pair;

public class RepChecker extends SceneVisitor {

    public static void checkRep(Scene s) {
        RepChecker rc = new RepChecker();
        rc.visitScene(s);
    }

    public void visitType(JrType type) {
        RepCheckerHelper.checkRep(type);
    }

    private static class RepCheckerHelper extends EmptyTypeVisitor {

        public static void checkRep(JrType type) {
            type.accept(new RepCheckerHelper());
        }

        public static void checkJrType(JrType type) {
            if (type.getIndex() == null) {
                throw new RuntimeException("No index for type: " + type);
            }
        }

        private boolean wasClassType = false;

        public void visitClassType(ClassType classType) {
            checkJrType(classType);
            // TODO:
            if (classType.getBaseClass() == null) {
              return;
            }
            if (classType.getTypeArgs().size() !=
                classType.getBaseClass().getSig().getTypeParams().size())
                throw new RuntimeException("Wrong number of type arguments: "
                        + classType.toString() + " vs. "
                        + classType.getBaseClass().getSig());
            wasClassType = true;
            super.visitClassType(classType);
        }

        public void visitArrayType(ArrayType t) {
            wasClassType = false;
            checkJrType(t);
            super.visitArrayType(t);
        }

        public void visitTypeArg(TypeArg targ) {
            if (targ.getIndex() == null) {
                throw new RuntimeException("No index for type arg: " + targ);
            }
            if (wasClassType && targ.getTypeParam() == null) {
                throw new RuntimeException("No param for type arg: " + targ);
            }
            if (! targ.getUpperBound().getIndex().getLastBound().equals(Bound.UPPER)) {
                throw new RuntimeException("Incorrect upper bound for: " + targ);
            }
            if (! targ.getLowerBound().getIndex().getLastBound().equals(Bound.LOWER)) {
                throw new RuntimeException("Incorrect lower bound for: " + targ);
            }

            super.visitTypeArg(targ);
        }

        public void visitVarType(VarType t) {
            checkJrType(t);
            super.visitVarType(t);
        }

        public void visitTypeParam(TypeParam t) {
            if (t.getIndex() == null) {
                throw new RuntimeException("No index for type index: " + t);
            }
            super.visitTypeParam(t);
        }

        public void visitPrimType(PrimType t) {
            checkJrType(t);
            super.visitPrimType(t);
        }

        public void visitVoidType(VoidType t) {
            checkJrType(t);
            super.visitVoidType(t);
        }

        public void visitNullType(NullType t) {
            checkJrType(t);
            super.visitNullType(t);
        }

    }

}
