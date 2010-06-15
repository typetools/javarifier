package javarifier;

import soot.SootClass;

import javarifier.JrType.*;
import javarifier.util.Pair;

import java.util.*;


public class JVMLSigParser {

    /**
     * Creates a new ClassSig from sc and classSig, which is in ASM's string format.
     * If the class has no parameterized supertypes, supertypes are loaded from sc.
     *
     * @param signature Format: <TypeParamName:JVMTypeSignatureForBound*>JVMTypeSignatureForSuperType.
     *   Example: <T:Ljava/util/HashSet<Ljava/util/Date;>;S:Foo;>Ljava/util/Object;
     *
     *   In the case that a class has no type parameters, the entire
     *   list of type parameters can be dropped.
     *   For example, "Ljava/util/AbstractList<Ljava/lang/Number;>;"
     *
     *   If a class has no type parameters and extends a
     *   non-parameteric class, then the signature may also be null.
     *   (Wierd choice by the * designers of ASM).
     *
     *   Finally, if the Type is an interface, two ":" are place
     *   between the type parameter name and the type parameter's
     *   type. For example, "T::Ljava/util/Set<Ljava/util/Date;>;.
     *   I do not know why this is the case, a bug? My code is robust
     *   in case this "bug" is fixed.
     */
    public static ClassSig parseClassSig(SootClass sc, String signature) {
        String name = sc.getName();
        try {
            if (Options.v().debugSigParser()) {
                System.out.println("parseClassSig: " + signature);
            }

            if (signature == null  || signature.equals("")) {
                List<ClassType> superTypes = new ArrayList<ClassType>();
                if (sc.hasSuperclass())
                    superTypes.add(new ClassType(Mutability.UNKNOWN,
                            "L" + sc.getSuperclass().getName().replace('.','/') + ";",
                            Collections.<TypeArg>emptyList()));
                for (SootClass iface :
                    (Collection<SootClass>) sc.getInterfaces())
                    superTypes.add(new ClassType(Mutability.UNKNOWN,
                            "L" + iface.getName().replace('.','/') + ";",
                            Collections.<TypeArg>emptyList()));
                return new ClassSig(new ArrayList<Pair<VarType, JrType>>(0),
                        (ClassType) parseJrType("L"+name.replace('.', '/')+";"),
                        superTypes);
            }

            ClassSig sig = (new JVMLSigParser(signature, true)).parseClassSig(name);
            return sig;
        } catch (Exception e) {
            throw new RuntimeException(name + " " + signature, e);
        }
    }


    /**
     * Parses signature, which is in ASM's format, into a MethodSig.
     *
     * Format: <TypeParam*>(ParamType*)ReturnType
     * TypeParam is the type parameters of the method.  It is given
     * in the format: TypeParamName:Bound
     * ParamType are the types of parameters of the method.
     * ReturnType is the return type of the method.
     * Bound, ParamType, and Return type are on in the JVML format.
     */
    public static MethodSig parseMethodSig(String signature, ClassType receiverType) {
        try {
            if (Options.v().debugSigParser()) {
                System.out.printf("parseMethodSig: \"%s\" \"%s\"%n", signature, receiverType);
            }
            MethodSig sig = (new JVMLSigParser(signature, true)).parseMethodSig(receiverType);
            if (Options.v().debugSigParser()) {
                System.out.println("parseMethodSig => " + sig);
            }
            return  sig;
        } catch (Exception e) {
            throw new RuntimeException(signature + " " + receiverType, e);
        }
    }


    public static JrType parseJrType(String signature) {
        try {
            JrType type = (new JVMLSigParser(signature, true)).parseJrType();
            return type;
        } catch (Exception e) {
            throw new RuntimeException(signature, e);
        }
    }


    private final String signature;

    /** Should types be created or only create TypeParams */
    private boolean resolveTypes;

    /** current char being parsed. */
    private int index;

    private JVMLSigParser(String signature, boolean resolveTypes) {
        signature = signature.replace("::", ":"); // :: are used instead of : for interfaces.
        this.signature = signature;
        this.index = 0;
        this.resolveTypes = resolveTypes;
    }

    private ClassSig parseClassSig(String name) {

        if (Options.v().debugSigParser()) {
            System.out.println("parseClassSig: " + signature + " " + index);
        }

        if (signature.charAt(0) != '<') {
            // class has no type parameters but a parametric supertype.
            List<ClassType> superTypes = parseSuperTypes();
            return new ClassSig(new ArrayList<Pair<VarType, JrType>>(0),
                    (ClassType) parseJrType("L"+name.replace('.','/').replace('$', '.')+";"),
                    superTypes);
        }

        List<Pair<VarType, JrType>> params = parseTypeParamList();

        List<ClassType> superTypes = parseSuperTypes();

        List<TypeArg> thisArgs = new ArrayList<TypeArg>(params.size());
        for (Pair<VarType, JrType> param : params) {
            thisArgs.add(new TypeArg(param.first().copy(), param.first().copy()));
        }

        ClassType thisType = new ClassType(Mutability.UNKNOWN, "L"+name.replace('.','/')+";", thisArgs);
        ClassSig classSig = new ClassSig(params, thisType, superTypes);

        index = signature.length();

        return classSig;
    }

    // Format: "<T:LSet<LDate;>;S:Foo;>"
    private List<Pair<VarType, JrType>> parseTypeParamList() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseTypeParamList: " + signature + " " + index);
        }

        if (signature.charAt(0) != '<') {
            return new ArrayList<Pair<VarType, JrType>>(0);
        }

        index++;

        List<Pair<VarType, JrType>> typeParams = new ArrayList<Pair<VarType, JrType>>();
        while (true) {

            if (signature.charAt(index) == '>') {
                index++;
                break;
            } else {
                typeParams.add(parseTypeParamDecl());
            }
        }
        return typeParams;
    }

    private List<ClassType> parseSuperTypes() {
        List<ClassType> superTypes = new ArrayList<ClassType>();
        while (index < signature.length())
            superTypes.add(parseClassType());
        return superTypes;
    }

    private Pair<VarType, JrType> parseTypeParamDecl() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseTypeParamDecl: " + signature + " " + index);
        }

        VarType tp = parseTypeParam();
        index++; // Move past ':'
        JrType paramType = parseJrType();
        return new Pair(tp, paramType);
    }

    private MethodSig parseMethodSig(ClassType receiverType) {

        List<Pair<VarType, JrType>> typeParams = parseTypeParamList();

        if (signature.charAt(index) != '(') {
            throw new RuntimeException();
        }

        List<JrType> params = parseParamList();

        Mutability mut = null;
        if (signature.charAt(index) == '{') {
            mut = parseMutability();
        }
        if (mut != null) {
            receiverType.setMutability(mut);
        }

        JrType returnType = parseJrType();

        MethodSig result = new MethodSig(typeParams, receiverType, params, returnType);

        return result;
    }


    private List<JrType> parseParamList() {
        if (signature.charAt(index) != '(') {
            throw new RuntimeException();
        }

        index++;

        List<JrType> params = new ArrayList<JrType>();
        while (true) {
            if (signature.charAt(index) == ')') {
                index++;
                break;
            } else {
                params.add(parseJrType());
            }
        }
        return params;
    }

    private JrType parseJrType() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseJrType: " + signature + " " + index);
        }

        char first = signature.charAt(index);
        JrType ret;

        if (first == 'L') {
            ret = parseClassType();

        } else if (first == '[') {
            ret = parseArrayType();

        } else if (first == 'T') {
            ret = parseVarType();

        } else if (first == 'Z' ||
                   first == 'B' ||
                   first == 'C' ||
                   first == 'D' ||
                   first == 'F' ||
                   first == 'I' ||
                   first == 'J' ||
                   first == 'S') {

                return parsePrimType();
        } else if (first == 'V') {
            ret = parseVoidType();
        } else {
            throw new RuntimeException("Invalid signature: " + signature);
        }
        if (Options.v().debugSigParser()) {
            System.out.println("parseJrType => " + ret);
        }
        return ret;
    }

    private TypeArg parseTypeArg() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseTypeArg: " + signature + " " + index);
        }

        char curr = signature.charAt(index);

        JrType upperBound = null;
        JrType lowerBound = null;
        if (curr == '+') {
            // extends
            // lowerBound = null type

            index++;

            upperBound = parseJrType();
            lowerBound = new NullType();


        } else if (curr == '-') {
            // super
            // upperBound = Ljava/lang/Object;

            index++;

            upperBound = ClassType.readonlyObject();
            lowerBound = parseJrType();

        } else if (curr == '*') {
            index++;
            upperBound = ClassType.readonlyObject();
            lowerBound = new NullType();
        } else {
            // non-wildcard

            // Must create different types for upper and lower bounds
            // since JrTypes are mutable.
            upperBound = parseJrType();
            if (resolveTypes) {
                lowerBound = upperBound.copy();

                // desugar ? readonly into its upper and lower bounds
                if (upperBound instanceof MutType &&
                    ((MutType) upperBound).getMutability().equals(Mutability.QUESTION_RO)) {
                    ((MutType) upperBound).setMutability(Mutability.READONLY);
                    ((MutType) lowerBound).setMutability(Mutability.MUTABLE);
                }
            }
        }

        if (resolveTypes) {
            return new TypeArg(upperBound, lowerBound);
        } else {
            return null;
        }
    }

    private ArrayType parseArrayType() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseArrayType: " + signature + " " + index);
        }

        if (signature.charAt(index) != '[') {
            throw new RuntimeException();
        }

        index++;
        JrType elemType = parseJrType();
        if (resolveTypes) {
            return new ArrayType(Mutability.UNKNOWN, new TypeArg(elemType, elemType.copy()));
        } else {
            return null;
        }
    }

    private List<TypeArg> parseTypeArgList() {

        if (Options.v().debugSigParser()) {
            System.out.println("parseTypeArgList: " + signature + " " + index);
        }


        if (signature.charAt(index) != '<') {
            throw new RuntimeException();
        }

        index++;

        List<TypeArg> args = new ArrayList<TypeArg>();
        while (true) {
            char curr = signature.charAt(index);
            if (curr == '>') {
                index++;
                break;
            } else {
                args.add(parseTypeArg());
            }
        }
       return args;
    }


    /** signature is in the form LMap<LFoo;LBaz;>; */
    private ClassType parseClassType() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseClassType: " + signature + " " + index);
        }

        if (signature.charAt(index) != 'L') {
            throw new RuntimeException();
        }

        index++;
        Mutability mut = Mutability.UNKNOWN;
        if (signature.charAt(index) == '{') {
            mut = parseMutability();
        }


        StringBuilder baseType = new StringBuilder();
        baseType.append("L");
        List<TypeArg> args = new ArrayList<TypeArg>();
        while (true) {
            char curr = signature.charAt(index);
            if (curr == ';') {
                baseType.append(curr);
                index++;
                break;
            } else if (curr == '<') {
                args.addAll(parseTypeArgList());
            } else if (curr == '.') {
                // entering inner class
                // JVML syntax: LOuterClass<TS;>.InnerClass<LDate;>;
                baseType.append('$'); // soot's syntax for inner class
                index++;
            } else {
                baseType.append(curr);
                index++;
            }
        }
        if (resolveTypes) {
            return new ClassType(mut, baseType.toString(), args);
        } else {
            return null;
        }
    }

    private Mutability parseMutability() {
        if (signature.charAt(index) != '{') {
            throw new RuntimeException(signature + " " + index + " " +signature.charAt(index));
        }
        index++;
        char curr = signature.charAt(index);
        Mutability mut = null;
        if (curr == 'r') {
            mut = Mutability.READONLY;
        } else if (curr == 'y') {
            mut = Mutability.POLYREAD;
        } else if (curr == 'q') {
            mut = Mutability.QUESTION_RO;
        } else if (curr == 'm') {
            mut = Mutability.MUTABLE;
        } else if (curr == 'f') {
            mut = Mutability.MUTABLE;
        } else if (curr == 't') {
            mut = Mutability.THIS_MUTABLE;
        } else {
            throw new RuntimeException(signature + " " + index + " " + curr);
        }

        index++;

        if (signature.charAt(index) != '}') {
            throw new RuntimeException(signature + " " + index + " " +signature.charAt(index));
        }

        index++;

        return mut;
    }


    private VarType parseVarType() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseVarType: " + signature + " " + index);
        }


        if (signature.charAt(index) != 'T') {
            throw new RuntimeException(signature.toString());
        }

        StringBuilder var = new StringBuilder();
        for ( ; index < signature.length(); index++) {
            if (signature.charAt(index) == ';') {
                var.append(signature.charAt(index));
                index++;
                break;
            }
            var.append(signature.charAt(index));
        }
        if (resolveTypes) {
            TypeParam param = new TypeParam(var.toString());
            return new VarType(param);
        } else {
            return null;
        }
    }


    private VarType parseTypeParam() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseTypeParam: " + signature + " " + index);
        }


        // Stop when ":" is reached or end of signature is reached.
        StringBuilder name = new StringBuilder();
        for ( ; true ; index++) {
            if (signature.charAt(index) == ':') {
                break;
            }
            name.append(signature.charAt(index));
        }
        return new VarType(new TypeParam("T" + name.toString() + ";"));
    }

    private PrimType parsePrimType() {
        if (Options.v().debugSigParser()) {
            System.out.println("parsePrimType: " + signature + " " + index);
        }

        index++;
        if (resolveTypes) {
            return new PrimType();
        } else {
            return null;
        }
    }

    private VoidType parseVoidType() {
        if (Options.v().debugSigParser()) {
            System.out.println("parseVoidType: " + signature + " " + index);
        }

        index++;
        if (resolveTypes) {
            return VoidType.v();
        } else {
            return null;
        }
    }


}
