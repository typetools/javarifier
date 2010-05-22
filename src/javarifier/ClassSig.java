package javarifier;

import java.util.*;

import javarifier.util.Pair;

import javarifier.JrType.*;

/**
 * Represents a class's parametric signature as in Foo<T extends Date>.
 */
public class ClassSig extends Signature {

    class SupertypeDeclaration implements JrTyped {
        final ClassType t;
        public SupertypeDeclaration(ClassType t) {
            this.t = t;
        }
        public ClassType getJrType() {
            return t;
        }
        public String toString() {
            return "supertype declaration of " + ClassSig.this.getThisType().toString();
        }
    }

    private ClassType thisType;
    private List<ClassType> superTypes;
    // For the benefit of SupertypeSubstitutionConstraints
    private List<SupertypeDeclaration> supertypeDeclarations;

    public ClassSig(List<Pair<VarType, JrType>> params, ClassType thisType,
            List<ClassType> superTypes) {
        super(params);
        this.thisType = thisType;
        this.superTypes = Collections.unmodifiableList(
                new ArrayList<ClassType>(superTypes));
        List<SupertypeDeclaration> supertypeDeclarations1
            = new ArrayList<SupertypeDeclaration>();
        for (ClassType t : superTypes)
            supertypeDeclarations1.add(new SupertypeDeclaration(t));
        supertypeDeclarations = Collections.unmodifiableList(supertypeDeclarations1);
    }

    public ClassType getThisType() {
        return thisType;
    }

    public List<ClassType> getSuperTypes() {
        return superTypes;
    }

    public List<SupertypeDeclaration> getSupertypeDeclarations() {
        return supertypeDeclarations;
    }

    /**
     * Prepends the type parameters of other to this.  Used while processing outerclasses.
     * @param other The Signature from which the other parameters are accessed from.
     */
    public void add(Signature other) {
        super.add(other.getTypeParams());
        List<TypeArg> params = new ArrayList<TypeArg>(thisType.getTypeArgs().size() + other.getTypeParams().size());
        for (Pair<VarType, JrType> param : other.getTypeParams()) {
                TypeArg ta = new TypeArg(param.first.copy(), param.first.copy());
                ta.setTypeParam(new VarType(param.first.getTypeParam()));
                params.add(ta);
        }
        for (TypeArg arg : thisType.getTypeArgs()) {
                params.add(arg.copy());
        }
        ClassType newThisType = new ClassType(thisType.getMutability(), thisType.getBaseType(), params);
        newThisType.setIndex(thisType.getIndex());
        thisType = newThisType;
    }

    /*
    public String toString() {
        return super.toString() + ", thisType " + thisType.toString()
            + ", superTypes " + superTypes.toString();
    }
    */
}
