package javarifier;

import javarifier.JrType.VarType;
import javarifier.JrType.TypeParam;

import javarifier.util.Pair;

import java.util.*;

/**
 *  A parameter signature.  Extended for methods with
 * return type and parameter types.
 */
public class Signature {
    private List<Pair<VarType, JrType>> typeParams;

    public Signature(List<Pair<VarType, JrType>> typeParams) {
        this.typeParams = Collections.unmodifiableList(typeParams);
    }

    public List<Pair<VarType, JrType>> getTypeParams() {
        return typeParams;
    }

    /**
     * See {@link ClassSig#add(Signature)}
     * @param others
     */
    protected void add(List<Pair<VarType, JrType>> others) {
    	List<Pair<VarType, JrType>> newParams = new ArrayList<Pair<VarType, JrType>>(typeParams.size() + others.size());
    	newParams.addAll(typeParams);
    	newParams.addAll(others);
    	this.typeParams = Collections.unmodifiableList(newParams);
    }
    
    public void setTypeParams(List<Pair<VarType, JrType>> typeParams) {
        this.typeParams = typeParams;
    }

    /**
     * Sets the owners of all the type parameters in the signature.  Do
     * this after creating a signature but before adding type parameters
     * of outer methods and classes because you don't want to overwrite
     * their owners.
     */
    public void setOwner(Signatured owner) {
        for (Pair<VarType, JrType> param : getTypeParams())
            param.first.setOwner(owner);
    }

    /**
     * Does this signature have a type parameter of the given name?  If so,
     * return it; otherwise return null.  Used only by OwnerAdder during
     * initialization of VarType owners.  Once owners are available, use
     * {@link #contains} instead because it compares owners as well as names.
     */
    public VarType maybeGetByName(TypeParam p) {
        for (int i = 0; i < typeParams.size(); i++) {
            VarType vt = typeParams.get(i).first();
            if (vt.getTypeParam().equals(p)) {
                return vt;
            }
        }
        return null;
    }

    public boolean contains(VarType vt) {
        for (int i = 0; i < typeParams.size(); i++) {
            if (typeParams.get(i).first().equals(vt)) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(VarType vt) {
        for (int i = 0; i < typeParams.size(); i++) {
            if (typeParams.get(i).first().equals(vt)) {
                return i;
            }
        }
        throw new RuntimeException("Cannot find type parameter: " + vt + " in " + this);
    }

    public JrType bound(VarType vt) {
        for (int i = 0; i < typeParams.size(); i++) {
            if (typeParams.get(i).first().equals(vt)) {
                return typeParams.get(i).second();
            }
        }
        throw new RuntimeException("Cannot find type parameter: " + vt + " in " + this);
    }

//     public boolean equals(Object obj) {
//         if (obj instanceof ClassSig) {
//             ClassSig other = (ClassSig) obj;
//             return name.equals(other.name) && typeParams.equals(other.typeParams);
//         }
//         return false;
//     }


//     public int hashCode() {
//         return name.hashCode();
//     }

    private String str;
    public String toString() {
        if (str == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(" ");
            buf.append("<");
            for (Pair<VarType, JrType> param : typeParams) {
                buf.append("(");
                buf.append(param.first().toString());
                buf.append(":");
                buf.append(param.second().toString());
                buf.append(")");
            }
            buf.append(">");
            str = buf.toString();
        }
        return str;
    }


}
