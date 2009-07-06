package javarifier;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Local;

import javarifier.JrType.*;

import javarifier.util.Pair;

public class TypeIndexer extends SceneVisitor {

    public static void index(Scene s) {
        (new TypeIndexer()).visitScene(s);
    }

    public void visitType(JrType t) {
        if (t != null) {
            index(t);
        }
    }

    public static void index(JrType type) {
        index(type, TypeIndex.topLevel());
    }
    
    public static void index(JrType type, TypeIndex top) {
        type.accept(new IndexerHelper(top));
    }
    
    private static class IndexerHelper extends EmptyTypeVisitor {

        private IndexerHelper(TypeIndex top) {
            currentIndex = top;
        }

        private TypeIndex currentIndex;

        public void visitClassType(ClassType t) {
            t.setIndex(currentIndex);
            for (int i = 0; i < t.getTypeArgs().size(); i++) {
                TypeArg targ = t.getTypeArgs().get(i);
                // TODO:
                if(t.declaringClassSig() == null) {
                  return;
                }
                VarType param = t.declaringClassSig().getTypeParams().get(i).first();
                currentIndex = currentIndex.addIndex(param, null);
                targ.accept(this);
                currentIndex = currentIndex.removeLast();
            }
            if (t.areSuperTypesInitialized()) {
                for (ClassType superType : t.getSuperTypes()) {
                    superType.accept(this);
                }
            }
        }

        public void visitArrayType(ArrayType t) {
            t.setIndex(currentIndex);
            currentIndex = currentIndex.addIndex(null, null);
            t.getElemType().accept(this);
            currentIndex = currentIndex.removeLast();
        }

        public void visitTypeArg(TypeArg t) {
            t.setIndex(currentIndex);
            VarType param = currentIndex.getLastParam();
            t.setTypeParam(param);
            currentIndex = currentIndex.removeLast();
            currentIndex = currentIndex.addIndex(param, Bound.UPPER);
            t.getUpperBound().accept(this);
            currentIndex = currentIndex.removeLast();
            currentIndex = currentIndex.addIndex(param, Bound.LOWER);
            t.getLowerBound().accept(this);
            currentIndex = currentIndex.removeLast();
            currentIndex = currentIndex.addIndex(param, null);
        }

        public void visitVarType(VarType t) {
            t.setIndex(currentIndex);
            t.getTypeParam().accept(this);
        }

        public void visitTypeParam(TypeParam t) {
            t.setIndex(currentIndex);
        }

        public void visitPrimType(PrimType t) {
            t.setIndex(currentIndex);
        }

        public void visitVoidType(VoidType t) {
        }

        public void visitNullType(NullType t) {
            t.setIndex(currentIndex);
        }

    }


}
