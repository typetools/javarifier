package javarifier;

import javarifier.JrType.*;

public class EmptyTypeVisitor implements TypeVisitor {

  
    public void visitClassType(ClassType t) {
        for (TypeArg targ : t.getTypeArgs()) {
            targ.accept(this);
        }
        if (t.areSuperTypesInitialized()) {
            for (ClassType superType : t.getSuperTypes()) {
                superType.accept(this);
            }
        }
    }

    public void visitArrayType(ArrayType t) {
        t.getElemType().accept(this);
    }

    public void visitTypeArg(TypeArg t) {
        t.getUpperBound().accept(this);
        t.getLowerBound().accept(this);
    }

    public void visitVarType(VarType t) {
        t.getTypeParam().accept(this);
    }

    public void visitTypeParam(TypeParam t) {
    }

    public void visitPrimType(PrimType t) {
    }

    public void visitVoidType(VoidType t) {
    }

    public void visitNullType(NullType t) {
    }

}
