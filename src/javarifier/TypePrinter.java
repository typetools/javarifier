package javarifier;

import javarifier.JrType.*;

/**
 * Prints out an JrType, TypeArg, or TypeParam.  Uses a format
 * inspired by but not equal to the JVML format.
 */
public class TypePrinter extends EmptyTypeVisitor {

    public static String print(JrType t) {
        TypePrinter p = new TypePrinter();
        t.accept(p);
        return p.output();
    }

     public static String print(TypeArg t) {
        TypePrinter p = new TypePrinter();
        t.accept(p);
        return p.output();
    }

     public static String print(TypeParam t) {
        TypePrinter p = new TypePrinter();
        t.accept(p);
        return p.output();
    }



    private StringBuilder output;

    public TypePrinter() {
        output = new StringBuilder();
    }

    public String output() {
        return output.toString();
    }




    public void visitClassType(ClassType t) {
        output.append(t.getMutability() + " ");
        // baseType is sometimes in Java format, sometimes in JVML format
        String baseType = t.getBaseType();
        // output.append(baseType.substring(0, baseType.length()-1));
        output.append(baseType);
        if (t.getTypeArgs().size() != 0) {
            output.append("<");
            for (TypeArg targ : t.getTypeArgs()) {
                targ.accept(this);
            }
            output.append(">");
        }
        output.append(";");
    }

    public void visitArrayType(ArrayType t) {
        output.append("(");
        super.visitArrayType(t);
        output.append(")[" + t.getMutability() + "]");
    }

    public void visitTypeArg(TypeArg t) {
        output.append("+");
        t.getUpperBound().accept(this);
        output.append("-");
        t.getLowerBound().accept(this);
    }

    public void visitVarType(VarType t) {
      output.append(t.getMutability() + " ");
        super.visitVarType(t);
    }

    public void visitTypeParam(TypeParam t) {
        output.append(t.getSym());
    }

    public void visitPrimType(PrimType t) {
        output.append("PrimitiveType");
    }

    public void visitVoidType(VoidType t) {
        output.append("void");
    }

    public void visitNullType(NullType t) {
        output.append("NullType");
    }


}
