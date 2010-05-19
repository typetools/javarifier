package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Local;

import javarifier.JrType.*;

import javarifier.util.Pair;

import java.util.*;

public class UnknownEliminator extends SceneVisitor {

    public static void eliminateUnknowns(Scene s) {
        UnknownEliminator elim = new UnknownEliminator();
        elim.visitScene(s);
    }

    public void visitType(JrType type) {
        UnknownEliminatorHelper.eliminate(type);
    }


    private static class UnknownEliminatorHelper extends EmptyTypeVisitor {


        public static void eliminate(JrType t) {
            UnknownEliminatorHelper elim = new UnknownEliminatorHelper();
            t.accept(elim);
        }

        public void visitClassType(ClassType t) {
            if (t.getMutability() == Mutability.UNKNOWN) {
                t.setMutability(Mutability.READONLY);
            }

            super.visitClassType(t);
        }

        public void visitArrayType(ArrayType t) {

            if (t.getMutability() == Mutability.UNKNOWN) {
                t.setMutability(Mutability.READONLY);
            }

            super.visitArrayType(t);
        }


        @Override
        public void visitVarType(VarType t) {
          if (t.getMutability() == Mutability.UNKNOWN) {
            t.setMutability(Mutability.READONLY);
          }

          super.visitVarType(t);
        }
    }
}
