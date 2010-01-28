package javarifier;

import javarifier.JrType.*;

public interface TypeVisitor {

    public void visitClassType(ClassType t);

    public void visitArrayType(ArrayType t);

    public void visitTypeArg(TypeArg t);

    public void visitVarType(VarType t);

    public void visitTypeParam(TypeParam t);

    public void visitPrimType(PrimType t);

    public void visitVoidType(VoidType t);

    public void visitNullType(NullType t);
}
