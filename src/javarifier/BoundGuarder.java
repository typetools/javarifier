package javarifier;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Local;
import soot.Body;

import javarifier.JrType.*;

import javarifier.util.Pair;

import java.util.Collection;

public class BoundGuarder extends SceneLocationVisitor {

    public static ConstraintManager guards(Scene s) {
        BoundGuarder guarder = new BoundGuarder();
        guarder.visitScene(s);

        if (Options.v().dumpBoundGuards()) {
            System.out.println("\nBound Guards:");
            System.out.println(guarder.getConstraints()+"\n");
        }

        return guarder.getConstraints();
    }


    private ConstraintManager cm;

    public BoundGuarder() {
        cm = new ConstraintManager();
    }

    public ConstraintManager getConstraints() {
        return cm;
    }

    public void visitJrTyped(JrTyped typed) {
        if (typed.getJrType() != null) {
            BoundGuarderHelper.guards(typed, typed.getJrType(), cm, programLocation);
        }
        super.visitJrTyped(typed);
    }

    private static class BoundGuarderHelper extends EmptyTypeVisitor {

        public static void guards(JrTyped val, JrType type, ConstraintManager cm, SourceLocation loc) {
            if (Options.v().debugBoundGuarder()) {
                System.out.println("debugBoundGuarder>> val: " + val + ": " + type);
            }

            type.accept(new BoundGuarderHelper(val, cm, loc));
        }


        private JrTyped val;
        private ConstraintManager cm;
        private SourceLocation loc;

        public BoundGuarderHelper(JrTyped val, ConstraintManager cm, SourceLocation loc) {
            this.val = val;
            this.cm = cm;
            this.loc = loc;
        }

        /**
         * Enforces that a type argument must be a subtype of the
         * bound on the corresponding type parameter.
         *
         * For example,
         *  Foo<T extends Date> { }
         *
         *  Foo<MyDate> x;
         *
         * x[0 UP] <: T[]
         *
         * AND
         *
         * Enforces that a lower bound must be a subtype of an upper
         * bound (by calling visitTypeArg).
         */
        private void checkClassTypeArg(TypeArg targ, ClassType env) {

            if (Options.v().debugBoundGuarder()) {
                System.out.println("debugBoundGuarder>> targ: " + targ);
            }

            // Enforces that the upperbound of a type argument must be
            // a subtype of the bound of the type varable.

            // We must substitute in case the bound for one type parameter
            // depends on another type parameter (or even the same one).
            // bound() never returns a VarType, so substituting is safe.
            try {
              VarType targVarType = targ.getTypeParam();
              MutType targVarTypeMut = targVarType.bound();
              JrType targVarTypeMutType = targVarTypeMut.substitute(env);
              MutType bound = (MutType) targVarTypeMutType;
              //MutType bound = (MutType) targ.getTypeParam().bound().substitute(env);

              SourceCause cause =
                new SourceCause(loc, "Type argument upperbound subtype of type variable");

              cm.subtype(val, targ.getUpperBound(),
                  val, bound, cause);
            } catch (NullPointerException e) {
              // it's ok, either of these can reasonably be null, so ignore
              // the constraint since there's nothing to constrain
            }
            visitTypeArg(targ);
        }

        public void visitTypeArg(TypeArg targ) {
            SourceCause cause =
              new SourceCause(loc, "Type argument lowerbound subtype of upperbound");

            // Enforces that a lowerbound of a type argument must be a
            // subtype of the upperbound of the type argument
                cm.subtype(val, targ.getLowerBound(),
                           val, targ.getUpperBound(), cause);

            super.visitTypeArg(targ);
        }

        public void visitClassType(ClassType t) {
            for (TypeArg targ : t.getTypeArgs()) {
                checkClassTypeArg(targ, t);
            }
        }
    }

}
