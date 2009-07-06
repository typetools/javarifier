package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.*;

import javarifier.util.Pair;



/**
 * Repersents a Javari type including the features of generics and
 * mutability.  Can be used to represent Java types by assigning
 * mutability to Mutability.UNKNOWN.
 *
 * JrTypes are semi-mutable: the only part that may change is the
 * mutability of a type.  This allows the mutabilities of types
 * to be filled in after the initial Java type was created.
 */
public abstract class JrType {

    /**
     * The index of this.
     */
    private TypeIndex index;

    /**
     * Returns the index of this.
     */
    public TypeIndex getIndex() {
      return index;
    }

    /**
     * Sets the index of this.
     */
    public void setIndex(TypeIndex index) {
      this.index = index;
    }

    /**
     * Returns the type contained in this at the given index.
     */
    public JrType getType(TypeIndex index) {
        if (index.equals(TypeIndex.topLevel())) {
            return this;
        } else {
            throw new RuntimeException("Invalid index: " + 
                index + " for " + this);
        }
    }


    /**
     * Returns a ddep copy of this.  Classes with state that needs to be
     * copied must override this.
     */
    public JrType copy() {
        return this;
    }
    
    /**
     * Returns a deep copy of this, resetting all mutabilities contained
     * by this to {@link Mutability#UNKNOWN}.
     */
    public JrType copyResettingMutabilities() {
        return copy();
    }
    
    /**
     * Replaces all references in this JrType to type parameters in env by the
     * corresponding type arguments in env; returns the result.  For example,
     * given "class List&lt;T&gt;", "Set&lt;T&gt;".substitute("List&lt;Integer&gt;") 
     * gives
     * "Set&lt;Integer&gt;".
     * 
     * The resulting JrType is newly allocated except that, where this
     * contains a VarType, the resulting JrType contains an alias into env.
     * Newly allocated portions of the resulting JrType have unknown
     * mutabilities. FIX
     * 
     * If this is a VarType, the upper bound of the type argument is
     * substituted.
     */
    public JrType substituteUpperBound(ClassType env) {
        return substitute(env);
    }
    /**
     * Replaces all references in this JrType to type parameters in env by the
     * corresponding type arguments in env; returns the result.  For example,
     * given "class List&lt;T&gt;", "Set&lt;T&gt;".substitute("List&lt;Integer&gt;") gives
     * "Set&lt;Integer&gt;".
     * 
     * The resulting JrType is newly allocated except that, where this
     * contains a VarType, the resulting JrType contains an alias into env.
     * Newly allocated portions of the resulting JrType have unknown
     * mutabilities.
     * 
     * If this is a VarType, the lower bound of the type argument is
     * substituted.
     */
    public JrType substituteLowerBound(ClassType env) {
        return substitute(env);
    }
    /**
     * Replaces all references in this JrType to type parameters in env by the
     * corresponding type arguments in env; returns the result.  For example,
     * given "class List&lt;T&gt;", "Set&lt;T&gt;".substitute("List&lt;Integer&gt;") gives
     * "Set&lt;Integer&gt;".
     * 
     * The resulting JrType is newly allocated except that, where this
     * contains a VarType, the resulting JrType contains an alias into env.
     * Newly allocated portions of the resulting JrType have unknown
     * mutabilities.
     * 
     * If this is a VarType, an exception is thrown.
     */
    public JrType substitute(ClassType env) {
        return copyResettingMutabilities();
    }

    /**
     * Calls the appropriate visit* method, passing in this.
     */
    public abstract void accept(TypeVisitor v);

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return TypePrinter.print(this);
    }

    /**
     * Returns whether the given JrTyped is the ownder of this.
     */
    public boolean owned(JrTyped maybeOwner) {
        return maybeOwner.getJrType().getType(getIndex()) == this;
    }

    /**
     * A MutType is the base type for all JrTypes that represent a mutability.
     * (VoidType, for example, does not have a mutability.)
     */
    public static abstract class MutType extends JrType {

      /**
       * The mutability of this type.
       */
      private Mutability mutability;

      /**
       * Constructs a new MutType with the given mutability.
       */
      public MutType(Mutability mutability) {
        this.mutability = mutability;
      }

      /**
       * Returns the mutability of this.
       */
      public Mutability getMutability() {
        return mutability;
      }

      /**
       * Sets the mutability of this.
       */
      public void setMutability(Mutability mutability) {
        this.mutability = mutability;
      }

      /**
       * Returns the JrType at the given index inside this class.
       */
      public abstract JrType getType(TypeIndex index);

    }

    /**
     * A TypeArg represents a generic type argument for a class type.
     */
    public static class TypeArg {
        /**
         * The upper bound.
         */
        private final JrType upperBound;
        
        /**
         * The lower bound.
         */
        private final JrType lowerBound;

        /**
         * The index of this in the class type this is an argument of.
         */
        private TypeIndex index;
        
        /**
         * The corresponding type parameter this is an argument of.
         */
        private VarType param;

        /**
         * Constructs a new TypeArg with the given bounds.
         */
        public TypeArg(JrType upperBound, JrType lowerBound) {
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
       }

        /**
         * Returns the index of this.
         */
        public TypeIndex getIndex() {
            return index;
        }

        /**
         * Sets the index of this.
         */
        public void setIndex(TypeIndex index) {
            this.index = index;
        }

        /**
         * Returns the type parameter this is an argument of.
         */
        public VarType getTypeParam() {
            return param;
        }
        
        /**
         * Sets the type parameter of this.
         */
        public void setTypeParam(VarType param) {
            this.param = param;
        }

        /**
         * Returns a deep copy of this.
         */
        public TypeArg copy() {
           TypeArg ret = new TypeArg(upperBound.copy(), lowerBound.copy());
           ret.setIndex(this.getIndex());
           ret.setTypeParam(this.getTypeParam());
           return ret;
        }
        
        /**
         * Returns a copy of this, transitively setting all mutabilites
         * to {@link Mutability#UNKNOWN}.
         */
        public TypeArg copyResettingMutabilities() {
            TypeArg ret = new TypeArg(upperBound.copyResettingMutabilities(), lowerBound.copyResettingMutabilities());
            ret.setIndex(this.getIndex());
            ret.setTypeParam(this.getTypeParam());
            return ret;
        }
        
        /** @see JrType#substitute */
        public TypeArg substitute(ClassType env) {
            TypeArg sta = new TypeArg(getUpperBound().substituteUpperBound(env),
                    getLowerBound().substituteLowerBound(env));
            //System.out.println("Subs old: " + this);
            //System.out.println("Subs new: " + sta);
            sta.setIndex(this.getIndex());
            sta.setTypeParam(this.getTypeParam());
            return sta;
        }

        /**
         * Returns the upper bound of this.
         */
        public JrType getUpperBound() {
            return upperBound;
        }

        /**
         * Returns the lower bound of this.
         */
        public JrType getLowerBound() {
            return lowerBound;
        }

        /** 
         * {@inheritDoc}
         */
        public String toString() {
            return TypePrinter.print(this);
        }

        /**
         * Calls {@link TypeVisitor#visitTypeArg(javarifier.JrType.TypeArg)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitTypeArg(this);
        }
    }

    /**
     * A ClassType represents a (possibly parameterized) Java class type
     * with a Javari mutability.
     * 
     * Notes on handling innerclasses: The following type (given in JVML
     * signature format) 
     * LOuterClass&lt;LDate;&gt;.InnerClass&lt;TS;LInteger;&gt;; 
     * produces
     * a ClassType which "LOuterClass$InnerClass" as its name; this works well
     * with soot.Scene.v().getSootClass(String classname) and args 
     * [+LDate;-LDate;+TS;-TS;+LInteger;-LInteger;]
     * 
     * @author tschantz
     * 
     */
    public static class ClassType extends MutType {

        /**
         * The underlying Java base type, in JVML format.
         */
        private final String baseType;
        
        /**
         * Soot's representation of the given Java base class.
         * This is set by looking it up in {@link #getBaseClass()}.
         */
        private SootClass baseClass;
        
        /**
         * Any type arguments in this class type.
         */
        private List<TypeArg> args;

        /** 
         * Parameterized supertypes obtained by substituting our own type
         * arguments into our class signature's supertypes.  Null until needed.
         */
        private List<ClassType> superTypes;

        /**
         * Constructs a new ClassType.
         * 
         * @param mutability - the mutability of this Javari type
         * @param baseType - the class type in JVML format
         * @param args - any type arguments of this class type
         */
        public ClassType(Mutability mutability, 
                         String baseType, 
                         List<TypeArg> args) {
            super(mutability);     
            this.baseType = baseType;
            this.args = Collections.unmodifiableList(args);
        }

        /**
         * Returns a deep copy of this.
         */
        public ClassType copy() {
            List<TypeArg> newArgs = new ArrayList<TypeArg>(args.size());
            for (TypeArg arg : args) {
                newArgs.add(arg.copy());
            }
            ClassType ret = new ClassType(getMutability(), baseType, newArgs);
            ret.setIndex(this.getIndex());
            return ret;
        }
        
        /**
         * Returns a deep copy of this, setting (transitively) all 
         * mutabilities to {@link Mutability#UNKNOWN}
         */
        public ClassType copyResettingMutabilities() {
            List<TypeArg> newArgs = new ArrayList<TypeArg>(args.size());
            for (TypeArg arg : args) {
                newArgs.add(arg.copyResettingMutabilities());
            }
            ClassType ret = new ClassType(Mutability.UNKNOWN, baseType, newArgs);
            ret.setIndex(this.getIndex());
            return ret;
        }
        
        /**
         * Returns a copy of this, substituting arguments from the given
         * environments into the type arguments of this.
         */
        public ClassType substitute(ClassType env) {
            List<TypeArg> newArgs = new ArrayList<TypeArg>(args.size());
            for (TypeArg arg : args) {
                TypeArg newArg = arg.substitute(env);
                newArgs.add(newArg);
            }
            ClassType nct = new ClassType(Mutability.UNKNOWN, baseType, newArgs);
            nct.setIndex(this.getIndex());
            return nct;
        }
        
        /**
         * Returns the SootClass representing the underlying class.=
         */
        public SootClass getBaseClass() {
            if (baseClass == null) {
                String name = baseType.substring(1, baseType.length()-1).
                  replace('.', '$').replace('/', '.');
                SootClass sc = Scene.v().getSootClass(name);
                baseClass = sc;
            }
            return baseClass;
        }

        /**
         * Returns a new ClassType representing Object.
         * The ClassType has no index; set one if TypeIndexer has already run.
         */
        public static ClassType readonlyObject() {
            // must return a new object each time.
            ClassType obj = new ClassType(Mutability.READONLY, "Ljava/lang/Object;", new ArrayList<TypeArg>(0));
            return obj;
        }

        /**
         * Returns the type arguments of this.
         */
        public List<TypeArg> getTypeArgs() {
            return args;
        }

        /**
         * Returns the base type of this class, in JVML format.
         * i.e. Ljava/lang/String;
         * 
         * @return the base type of this class
         */
        public String getBaseType() {
            return baseType;
        }

        /**
         *
         *  class Foo&lt;U, T, S&gt; {
         *    Map&lt;S, T&gt; bar;
         *  }
         *
         * (Map&lt;S, T&gt;).indexOf(S) -&gt; 0
         */
        public int indexOf(VarType vt) {
           return declaringClassSig().indexOf(vt);
        }

        /**
         * Returns the class signature of the base type of this type.
         */
        public ClassSig declaringClassSig() {
          if(getBaseClass() == null) {
            return null;
          }
          
            ClassSig sig = getBaseClass().getSig();
            if (sig == null) {
                throw new RuntimeException("No class sig for: " + this);
            }
            return sig;
        }
        

        /**
         * Initializes the parameterized supertypes obtained by substituting 
         * our own type
         * arguments into our class signature's supertypes. 
         */
        private void initSuperTypes() {
            if (superTypes != null)
                return;
            ClassSig mySig = declaringClassSig();
            List<ClassType> sts1 = new ArrayList<ClassType>();
            
            // In order to avoid circular initialization, don't wait until
            // all superTypes have been intialized in order to store them
            // in this.superTypes.  Instead of keeping an isolated temporary 
            // list sts1, have superTypes also point to this list while
            // initializing superTypes, and afterwards, set the list to
            // be immutable.  (effectively x = unmodifiable(x))
            // TODO: this seems to always propagate types as soon as they are
            // found, but can it cause any problems?
            
            superTypes = sts1;
            
            // TODO: why necessary?
            if(mySig != null) {
            for (ClassType templateST : mySig.getSuperTypes()) {
                ClassType st = templateST.substitute(this);
                if (getIndex() != null)
                    TypeIndexer.index(st, getIndex());
                sts1.add(st);
            }
            }
            superTypes = Collections.unmodifiableList(sts1);
        }
        
        /**
         * Returns whether super types have been initialized.
         */
        public boolean areSuperTypesInitialized() {
            return superTypes != null;
        }
        
        /**
         * Returns the parameterized super types of this.
         */
        public List<ClassType> getSuperTypes() {
            initSuperTypes();
            return superTypes;
        }

        /**
         * Returns true if and only if this contains the given VarType
         * as a type parameter.
         */
        public boolean containsTypeArg(VarType tparam) {
            List<Pair<VarType, JrType>> ourParams
                = getBaseClass().getSig().getTypeParams();
            for (int i = 0; i < ourParams.size(); i++) {
                if (ourParams.get(i).first().equals(tparam)) {
                    return true;
                }
            }
         return false;
        }

        /**
         * Returns the type parameter in this that matches the given 
         * VarType.
         */
        public TypeArg getTypeArg(VarType tparam) {
            List<Pair<VarType, JrType>> ourParams
                = getBaseClass().getSig().getTypeParams();
            for (int i = 0; i < ourParams.size(); i++) {
                if (ourParams.get(i).first().equals(tparam)) {
                    return getTypeArgs().get(i);
                }
            }
            throw new RuntimeException("Did not find type param: " + 
                tparam + " " + this);
        }
        
        /**
         * Adds unrestricted type arguments (NullType) to the type
         * parameters in this class type.
         */
        public void wildcardifyRawType() {
          SootClass baseClass = getBaseClass();
          if(baseClass == null) {
            return;
          }
          
            List<Pair<VarType, JrType>> ourParams
                = getBaseClass().getSig().getTypeParams();
            if (getTypeArgs().isEmpty() && !ourParams.isEmpty()) {
                //System.err.println("Warning!  Got raw type " + toString()
                //        + ", saving wildcards for type arguments");
                List<TypeArg> newArgs = new ArrayList<TypeArg>();
                for (int i = 0; i < ourParams.size(); i++) {
                    TypeArg newArg = new TypeArg(readonlyObject(), new NullType());
                    newArg.setTypeParam(ourParams.get(i).first);
                    newArgs.add(newArg);
                }
                args = newArgs;
            }
        }

        /**
         * Like getTypeArg but searches supertypes and, on failure, returns null
         * instead of throwing an exception. 
         */
        public TypeArg searchForTypeArg(VarType tparam) {
            if (containsTypeArg(tparam))
                return getTypeArg(tparam);
            for (ClassType superType : getSuperTypes()) {
                TypeArg arg = superType.searchForTypeArg(tparam);
                if (arg != null)
                    return arg;
            }
            return null;
        }

        /**
         * Like searchForTypeArg but operates on unsubstituted versions of
         * this class's supertypes, meaning that all MutType layers of the
         * result come from the same supertype declaration.  Returns this
         * declaration along with the type arg.
         * 
         * Used by ConstraintManager.resolveVarType.
         */
        public Pair<JrTyped, TypeArg> shallowSearchForTypeArg(JrTyped origOwner, VarType tparam) {
            if (containsTypeArg(tparam))
                return new Pair<JrTyped, TypeArg>(origOwner, getTypeArg(tparam));
            for (ClassSig.SupertypeDeclaration superTypeDecl :
                    getBaseClass().getSig().getSupertypeDeclarations()) {
                Pair<JrTyped, TypeArg> ret =
                    superTypeDecl.getJrType().shallowSearchForTypeArg(superTypeDecl, tparam);
                if (ret != null)
                    return ret;
            }
            return null;
        }
        
        /**
         * Returns the parameterization of the given class that is a supertype
         * of this type, or null if there is none.  For example, if Foo extends
         * Cell&lt;Date&gt;, then "Foo".getSuperParameterizedType("LCell;") 
         * returns
         * "Cell&lt;Date&gt;".
         */
        public ClassType getSuperParameterizedType(String superBaseType) {
            if (superBaseType.equals(getBaseType()))
                return this;
            for (ClassType mySuperType : getSuperTypes()) {
                ClassType spt = mySuperType.
                    getSuperParameterizedType(superBaseType);
                if (spt != null)
                    return spt;
            }
            return null;
        }

        /**
         * Returns the JrType that is in this class type at the given index.
         */
        public JrType getType(TypeIndex index) {
            if (index.equals(TypeIndex.topLevel())) {
                return this;
            } else {
                ClassSig sig = declaringClassSig();
                VarType param = index.getTypeParam(0);

                int num = sig.indexOf(param);
                Bound bound = index.getBound(0);

                if (num < 0 || num >= args.size() || bound == Bound.NONE) {
                    throw new RuntimeException("Invalid index: " + this + " " + index);
                }

                TypeArg arg = args.get(num);

                JrType type =
                    bound == Bound.UPPER ?
                    arg.getUpperBound() :
                    arg.getLowerBound();

                return type.getType(index.removeFirst());
            }
        }

        /**
         * Calls {@link TypeVisitor#visitClassType(javarifier.JrType.ClassType)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitClassType(this);
        }

    }

    /**
     * A VarType represents the Javari type of a type parameter.
     */
    public static class VarType extends MutType implements JrTyped {

        /**
         * The type parameters this represents.
         */
        private final TypeParam param;

        /**
         * The owner that declared this type parameter.
         */
        private Signatured owner;
        
        /**
         * Constructs a new VarType out of the given parameter.
         */
        public VarType(TypeParam param) {
            super(Mutability.UNKNOWN); // initialize all parameters to have
            // unknown mutability
            this.param = param;
        }

        /**
         * Returns the underlying type parameter.
         */
        public TypeParam getTypeParam() {
            return param;
        }

        /**
         * Returns the owner of the underlying type parameter.
         */
        public Signatured getOwner() {
            return owner;
        }

        /**
         * Sets the owner of the underlying type parameter.
         */
        public void setOwner(Signatured owner) {
            this.owner = owner;
        }

        /**
         * Returns the bound on the underlying type parameter.
         */
        public MutType bound() {
            JrType type = owner.getSig().bound(this);
            if (type instanceof MutType) {
                return (MutType) type;
            } else if (type instanceof VarType) {
                return ((VarType) type).bound();
            }
            throw new RuntimeException();
        }

        /**
         * Returns the type of this, which is the same as its
         * {@link #bound()}
         */
        public JrType getJrType() {
            return bound();
        }
        
        /** 
         * Returns a shallow copy of this.
         */
        public VarType copy() {
            VarType copy = new VarType(this.param);
            copy.setIndex(this.getIndex());
            copy.setOwner(this.getOwner());
            return copy;
        }
        
        /**
         * Returns the JrType representing the upper of this found
         * in the given environment.
         */
        public JrType substituteUpperBound(ClassType env) {
            // If we're trying to substitute an (unavailable) type parameter of
            // a generic method, return the parameter unchanged.  TypeInferencer
            // will detect the unavailable parameter and will not use the
            // resulting type.
            if (getOwner() instanceof SootMethod) {
                SootMethod oMeth = (SootMethod) getOwner();
                if (oMeth.getDeclaringClass() == env.getBaseClass())
                    return this;
            }
            // Otherwise, do an ordinary search.
            // If type argument not found, return this as its own upper bound.
            // TODO: does this break anywhere?
            TypeArg ta = env.searchForTypeArg(this);
            if(ta == null) {
              return this;
            }
            
            return ta
                .getUpperBound().copyResettingMutabilities();
        }
        /**
         * Returns the JrType representing the upper of this found
         * in the given environment.
         */
        public JrType substituteLowerBound(ClassType env) {
            // If we're trying to substitute an (unavailable) type parameter of
            // a generic method, return the parameter unchanged.  TypeInferencer
            // will detect the unavailable parameter and will not use the
            // resulting type.
            if (getOwner() instanceof SootMethod) {
                SootMethod oMeth = (SootMethod) getOwner();
                if (oMeth.getDeclaringClass() == env.getBaseClass())
                    return this;
            }
            // Otherwise, do an ordinary search.            
            // If type argument not found, return this as its own upper bound.
            // TODO: does this break anywhere?
            TypeArg ta = env.searchForTypeArg(this);
            if(ta == null) {
              return this;
            }
            
            return ta
                .getLowerBound().copyResettingMutabilities();
        }
        
        /**
         * Throws an exception, since either an upper or lower bound must
         * be substituted.
         */
        public JrType substitute(ClassType env) {
            throw new RuntimeException("Cannot substitute a VarType; must " +
            		"substitute either the upper or the lower bound");
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object obj) {
            if (obj instanceof VarType) {
                VarType other = (VarType) obj;

                if (other.param.equals(this.param)) {
                    if (other.owner == null && owner == null) {
                        return true;
                    } else if (owner == null) {
                        return false;
                    } else {
                        return owner.equals(other.owner);
                    }
                }
                return false;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return param.hashCode();
        }

        /**
         * Calls {@link TypeVisitor#visitVarType(javarifier.JrType.VarType)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitVarType(this);
        }

        /**
         * Returns this.
         * This method needed in order to be able to extend MutType
         */
        public JrType getType(TypeIndex index) {
          return this;
        }
        
    }

    /**
     * A TypeParam represents a type parameter.
     */
    public static class TypeParam {
        // The symbol for this type parameter, in JVML format
        private final String sym;

        private TypeIndex index;

        /**
         * Creates a new type parameter represented by the given symbol.
         */
        public TypeParam(String sym) {
            this.sym = sym;
            checkRep();
        }

        /**
         * Checks that the symbol for this type parameter
         * does not start with "T" and ends with ";"
         */
        private void checkRep() {
            if (! sym.startsWith("T")) {
                throw new RuntimeException("Illegal symbol: " +sym.toString());
            }
            if (! (sym.charAt(sym.length()-1) == ';')) {
                throw new RuntimeException("Illegal symbol: " +sym.toString());
            }
        }

        /**
         * Returns the symbol representing the type parameter.
         */
        public String getSym() {
            return sym;
        }

        /**
         * Returns the type index of this.
         */
        public TypeIndex getIndex() {
            return index;
        }

        /**
         * Sets the type index of this to the given index.
         */
        public void setIndex(TypeIndex index) {
            this.index = index;
        }

        /**
         * Calls {@link TypeVisitor#visitTypeParam(javarifier.JrType.TypeParam)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitTypeParam(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TypeParam) {
                TypeParam other = (TypeParam) obj;
                return other.sym.equals(this.sym);
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return sym.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return TypePrinter.print(this);
        }
    }


    /**
     * An ArrayType represents an array whose base type is another JrType.
     */
    public static class ArrayType extends MutType {

        // Javari allows limited upper and lower bound on array types.
        /** The element type of the array. */
        private final TypeArg elemType;

        /**
         * Constructurs a new Array type with the given mutability and
         * element type.
         */
        public ArrayType(Mutability mutability, TypeArg elemType) {
            super(mutability);
            this.elemType = elemType;
        }

        /** 
         * Returns the element type of this.
         */
        public TypeArg getElemType() {
            return elemType;
        }

        /**
         * Returns the type at the given index in this type.
         */
        public JrType getType(TypeIndex index) {
            if (index.equals(TypeIndex.topLevel())) {
                return this;
            } else {
                VarType param = index.getTypeParam(0);
                Bound bound = index.getBound(0);

                if (param != null || bound == Bound.NONE) {
                    throw new RuntimeException("Invalid index: " + this + " " + index);
                }

                JrType type =
                    bound == Bound.UPPER ?
                    elemType.getUpperBound() :
                    elemType.getLowerBound();

                return type.getType(index.removeFirst());
            }
        }

        /**
         * Returns a fresh copy of this, copying the element type as well.
         */
        public ArrayType copy() {
            ArrayType ret = new ArrayType(getMutability(), getElemType().copy());
            ret.setIndex(this.getIndex());
            return ret;
        }
        
        /**
         * Returns a fresh copy of this, transitively setting all
         * mutabilities to unknown.
         */
        public ArrayType copyResettingMutabilities() {
            ArrayType ret = new ArrayType(Mutability.UNKNOWN, 
                getElemType().copyResettingMutabilities());
            ret.setIndex(this.getIndex());
            return ret;
        }
        
        /**
         * Returns a copy of this, substituting in the type in
         * the given environment.
         */
        public JrType substitute(ClassType env) {
            ArrayType sat = new ArrayType(Mutability.UNKNOWN,
                    getElemType().substitute(env));
            sat.setIndex(this.getIndex());
            return sat;
        }

        /**
         * Calls {@link TypeVisitor#visitArrayType(javarifier.JrType.ArrayType)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitArrayType(this);
        }
    }

    /**
     * A PrimType represents a primitive Java type.
     */
    public static class PrimType extends JrType {

        /**
         * Default constructor.
         */
        public PrimType() {
        }

        /**
         * Returns a fresh copy of this.
         */
        public PrimType copy() {
            PrimType ret = new PrimType();
            ret.setIndex(this.getIndex());
            return ret;
        }

        /**
         * Calls {@link TypeVisitor#visitPrimType(javarifier.JrType.PrimType)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitPrimType(this);
        }
    }

    /**
     * A VoidType represents Java's void type.  
     * (For example, the return type of the method: "void foo()"
     * 
     * This class implements the singleton pattern.
     */
    public static class VoidType extends JrType {
        
        /** The singleton. */
        private static VoidType v = new VoidType();

        /**
         * Creates the singleton void type.
         */
        private VoidType() {
             setIndex(TypeIndex.topLevel());
        }

        /**
         * Returns the singleton void type.
         */
        public static VoidType v() {
            return v;
        }

        /**
         * Calls {@link TypeVisitor#visitVoidType(javarifier.JrType.VoidType)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitVoidType(this);
        }
    }

    /**
     * A NullType is a JrType used to indicate that lower bounds on type 
     * arguments are unrestricted.  In other words, there is no
     * lower bound, such as in the lower bound of T in 
     * Foo&lt;T extends Object&gt;
     */
    public static class NullType extends JrType {
      
        /**
         * Returns a deep copy of this.
         */
        public NullType copy() {
            NullType ret = new NullType();
            ret.setIndex(this.getIndex());
            return ret;
        }
        
        /**
         * Calls {@link TypeVisitor#visitNullType(javarifier.JrType.NullType)}
         * passing in this.
         */
        public void accept(TypeVisitor v) {
            v.visitNullType(this);
        }

    }

}
