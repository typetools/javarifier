package javarifier;

import soot.Local;
import soot.SootField;
import soot.SootMethod;

import javarifier.util.ConstraintSet;
import javarifier.JrType.*;

import javarifier.util.Pair;

import java.util.*;

/**
 * This class includes methods for adding mutability constraints on a program's
 * variables.  Several kinds of constraints can be added.  The first two are
 * applied to pairs of variables and are:
 * (1) subtyping, denoted as "x <: y", which implies that "x" must be a subtype
 * of "y".
 * (2) guards, denoted as "x -> y", which implies "y" must be mutable if "x" is
 * mutable.  <p>
 *
 * The third constraint applied to a single variable:
 * (3) mutable, denoted as "x", which implies that "x" is must be
 * unconditionally mutable.  <p>

 * Additional "guards" can be added to the subtyping and guards constraints
 * above yielding two remaining types of constraints:
 * (4) guarded subtyping, denoted as "z -> x <: y", which implies that if "z" is
 * mutable then "x" must be a subtype of "y".
 * (5) twice guarded, denoted as "z -> x -> y" or "z => x -> y", which implies
 * that if "z" is mutable, then "x" being mutable implies "y" being mutable. <p>
 *
 * Although it is convenient to speak of constraints involving only program
 * variables (as I did above), truthfully, constraints involve pairs of program
 * variables and types.  For example, for the variable "x", declared as
 * "String[] x;", constraints can be placed on the array itself, the lower bound
 * of the array's arguments, and the upper bound on the array's arguments.
 * Thus, one must specify the "part" of the type involved in the constraint in
 * addition to the variables.  See sections 6.1.5 and 6.1.6 of Matthew
 * Tschantz's masters thesis for a more in depth description of "parts" of types
 * and how to determine what is the "relevant" type part for a constraint.  See
 * javarifier.JrType and javarifier.TypeIndex for the details of how a type
 * "part" is represented. <p>
 *
 *
 * When a constraint variable represents a local variables, the constraint
 * variable also has a method context.  The method contexts are usually not
 * passed to this class's methods because the methods are able to determine the
 * correct contexts for the constraint variables.  To denote that a local
 * variable, x, is from a method's mutable context, we write "x^mut".  To denote
 * that a local variable, x, is from a method's readonly context, we write
 * "x^ro". <p>
 *
 * As a shorthand, we write "x^?" to denote the pair of "x^mut" and "x^ro".
 * When more than one "^?" appears in a constraint, only two constraints are
 * denoted, one where all the "^?'s" resolve to "^ro" and one where all the
 * "^?'s" resolve to "^mut".  For example, "x^? -> y^?" represents the pair of
 * constraints "x^ro -> y^ro" and "x^mut -> y^mut".  When "^?" is applied to a
 * field, which do not have method contexts, the "^?" resolves to nothing.
 * Thus, the constraint "f^? -> x^?" where "f" is a field and "x" is a local
 * variable resolves to the pair of constraints "f -> x^ro" and "f -> x^mut".
 * See section 6.2 of Matthew Tschantz's masters thesis for more information on
 * local variable method contexts. <p>
 *
 *
 * Some constraints variables may only be fully resolved with the help of an
 * "environment".  The "environment" of a program variable refers to how that
 * variable was reached.  For example, in "f.x", "f" is "x"'s environment.  Not
 * all program statements will involve environments.  For example, in the
 * statement "x = y", neither "x" or "y" has an environment.  The environment of
 * a variable is used in the case of type parameters.  For example, to know the
 * type of "x.val" in the following code, one must know the type of "x":
 *
 * <pre>
 * class Cell<T> { T val; }
 * Cell<Date> x;
 * x.val; // type of x.val is dependent on the type of x.  In this case x (the
 *        // environment of x.val) has type Cell<Date>, so val has type Date.
 * </pre>
 *
 * In addition to the name of the environment variable, the relevant type part
 * of the environment variable's type must be supplied. <p>
 *
 *
 * To summarize, each part of a constraint has three components: the program
 * variable, the "part" of the type of the program variable being constrained,
 * and the method context of any local variables.  Additionally, in the case of
 * fields, to fully resolve the meaning of a constraint, an environment giving
 * the name of and relevant type part of how the field was reached is needed. <p>
 *
 * The following string representation is used to denote the three components of
 * a constraint variable:
 *
 * <pre>
 * env_variable : env_type |- program_variable^method_context : type_part
 * </pre>
 *
 * where env_variable is the name of the environment variable, env_type is the
 * relevant type part or env_variable, program_variable is the name of the
 * relevant program variable, method_context is the method context of the
 * constraint variable (usually ^?), and type_part is the part of the type being
 * constrained.  For example, in the code below:
 *
 * <pre>
 * class Cell<T> { T val; }
 * Cell<Date> x;
 * x.val.mutate();
 * </pre>
 *
 * A constraint would be created stating that the val field must contain a type
 * that can be mutated.  This constraint can be written as:
 *
 * <pre>
 * x : Cell<Date> |- val : T
 * </pre>
 *
 * This notation is generalized to guarded and subtyping constraints by adding
 * the "->" and "<:" symbols, respectively, between the constraint
 * variables. For example, the following code:
 *
 * <pre>
 * Cell<Date> x, y;
 * x.val = y.val
 * </pre>
 *
 * Produces the constraint
 *
 * <pre>
 * y : Cell<Date> |- val : T  <:  x : Cell<Date> |- val : T
 * </pre>
 *
 * In the case that there is no environment for a constraint, we write the
 * environment variable and type as null, or omit them and the "|-" altogether:
 *
 * <pre>
 * Cell<Date> x, y;
 * x = y;
 * </pre>
 *
 * Yields:
 *
 * <pre>
 * null : null |- y^? : Cell<Date>  <:  null : null |- x^? : Cell<Date>
 * </pre>
 * or, more succinctly:
 * <pre>
 * y^? : Cell<Date>  <:  x^? : Cell<Date>
 * </pre>
 *
 * In this last example, x and y are local variables; thus, their method
 * contexts must be given.  In this case, "^?" is used to denote both the
 * readonly and mutable method contexts. <p>
 *
 *
 * Method parameters follow the following naming conventions:
 * <ul>
 *
 * <li> A parameter's name is prefixed with "lhs" if the relevant program
 *  variable appears on the left hand side of a "subtyping" or "guards"
 *  relationship. For example, "x" appears on the left hand side of "x <: y".
 *  "x" also appears on the left hand side of "x -> y".  Note that the "guard"
 *  of constraints of kind (4) and (5) does not change the naming, thus "x" is
 *  also considered to be on the left hand side of "z -> x -> y" and "z -> x <:
 *  y".  Warning, the program statement "a = b" generates the constraint "b <:
 *  a"; thus, "b" is on the left hand side of the subtyping relationship but the
 *  right hand side of the the program statement.
 *
 * <li> A parameter's name is prefixed with "rhs" if the relevant program
 *  variable appears on the right hand side of the relevant subtyping
 *  relationship.  <li> A parameter's name is prefixed with "guard" if the
 *  relevant program variable appears as the guard on a guarded "subtyping" or
 *  "guards" relationship. For example, "z" in following constraints: "z -> x ->
 *  y" and "z -> x <: y".
 *
 * <li> A parameter's name is suffixed with "Value" if it refers to a program
 *   variable relevant to the constraint.
 *
 * <li> A parameter's name is suffixed with "Type" if it refers to a type
 *  related to a program variable relevant to the constraint.  Warning, this
 *  type may not be the actual type of the corresponding program variable.  For
 *  example, in the case of arrays.
 *
 * <li> A parameter's name is suffixed with "Env" if it refers to the
 *  "environment" of a program variable relevant to the constraint.
 *
 * </ul>
 **/
public class ConstraintManager {

    ConstraintSet<ConstraintVar> cs;

    public ConstraintManager() {
        cs = new ConstraintSet<ConstraintVar>();
    }

    public Set<ConstraintVar> solve() {
        return cs.solve();
    }

    public ConstraintManager combine(ConstraintManager cm) {
        ConstraintManager ret = new ConstraintManager();
        ret.cs = this.cs.combine(cm.cs);
        return ret;
    }

    // These fields are used to determine when to skip the readonly
    // version of constraints, necessary for inferring polyread.
    private boolean skipReadOnly = false;
    private boolean inferringTypeArgs = false;

    private boolean shouldSkipReadonlyContext() {
      return skipReadOnly && inferringTypeArgs;
    }
    private void setSkipReadOnly(boolean skip) {
      skipReadOnly = skip;
    }
    private void setInferringTypeArgs(boolean inferring) {
      inferringTypeArgs = inferring;
    }

    /**
     * Adds the unguarded constraint:
     * null : null |- lhsValue^? : lhsType <: null : null |- rhsValue^? : rhsType.
     *
     * There is not an environment for either the left hand side or the right hand side.
     *
     * This method is a convenience method and is identical to the call:
     * subtype2(null, null, lhsValue, lhsType,
     *                   null, null, rhsValue, rhsType);
     */
    public void subtype(JrTyped lhsValue, JrType lhsType,
                        JrTyped rhsValue, JrType rhsType, SourceCause cause) {

        subtype2(null, null, lhsValue, lhsType,
                 null, null, rhsValue, rhsType, cause);
    }

    /**
     * Adds the unguarded constraint:
     * lhsEnv : lhsEnvType |- lhsValue^? : lhsType <: rhsEnv : rhsEnvType |- rhsValue^? : rhsType
     *
     * lhsEnv and lhsEnvType may be null if there is no environment for the lhs.  Likewise
     * with the rhs.
     *
     * This method is a convenience method and is identical to the call:
     * subtype3(lhsEnv, lhsEnvType, lhsValue, lhsType,
     *                   rhsEnv, rhsEnvType, rhsValue, rhsType,
     *                   null, null);
     */
    public void subtype2(JrTyped lhsEnv, JrType lhsEnvType, JrTyped lhsValue, JrType lhsType,
                         JrTyped rhsEnv, JrType rhsEnvType, JrTyped rhsValue, JrType rhsType, SourceCause cause) {
        subtype3(lhsEnv, lhsEnvType, lhsValue, lhsType,
                 rhsEnv, rhsEnvType, rhsValue, rhsType,
                 null, null, cause);
    }

    /**
     * Adds the same constraints as subtype2, except for the versions of
     * each constraint for the READONLY context on type arguments.
     */
    public void subtype2InvokeWithoutReadOnly(JrTyped lhsEnv, JrType lhsEnvType, JrTyped lhsValue, JrType lhsType,
        JrTyped rhsEnv, JrType rhsEnvType, JrTyped rhsValue, JrType rhsType, SourceCause cause) {
      boolean startSkipReadOnly = shouldSkipReadonlyContext();
      setSkipReadOnly(true);
      subtype2(lhsEnv, lhsEnvType, lhsValue, lhsType,
          rhsEnv, rhsEnvType, rhsValue, rhsType, cause);
      setSkipReadOnly(startSkipReadOnly);
    }

    // Duck typing hack.
    // TODO: replace with interface
    // returns the empty string if there is no getName method, or null
    private String getObjName(JrTyped obj) {
        if (obj == null) return "";
        try {
            return (String)obj.getClass().getMethod("getName").invoke(obj);
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * Adds the constraint:
     * guard : guardType -> (lhsEnv : lhsEnvType |- lhsValue^? : lhsType <: rhsEnv : rhsEnvType |- rhsValue^? : rhsType)
     *
     * guard and guardType may be null if the subtyping constraint is unguarded.
     *
     * lhsEnv and lhsEnvType may be null if there is no environment for the lhs.  Likewise
     * with the rhs.
     */
    // Implementation note: subtype3 does the work of resolving the types of
    // type variables that appear in the constraint then calls the private
    // method subtype4 to actually add the constraints.  There is no strong
    // reason why these two tasks were separated into two methods.
    public void subtype3(JrTyped lhsEnv, JrType lhsEnvType, JrTyped lhsValue, JrType lhsType,
                         JrTyped rhsEnv, JrType rhsEnvType, JrTyped rhsValue, JrType rhsType,
                         JrTyped guard,  JrType guardType, SourceCause cause) {

        //TODO: move to subtype4, and get mutabilities / upper lower bounds
        String subtypeDebug = "";
        if(guard != null) {
            subtypeDebug += getObjName(guard) + " -> ";
        }
        if(lhsEnv != null) {
            subtypeDebug += getObjName(lhsEnv) + " |- ";
        }
        subtypeDebug += getObjName(lhsValue) + " <: ";
        if(rhsEnv != null) {
            subtypeDebug += getObjName(rhsEnv) + " |- ";
        }
        subtypeDebug += getObjName(rhsValue); 
        
        cause.setSubtyping(subtypeDebug);

        if (Options.v().debugSubtyping()) {
            System.out.println("debugSubtyping1>> "
               + lhsEnv + ": " + lhsEnvType + " |- " + lhsValue + ": " + lhsType + " (" + lhsValue.getJrType() + ": " + lhsType.getIndex() + ") <: "
               + rhsEnv + ": " + rhsEnvType + " |- " + rhsValue + ": " + rhsType + " (" + rhsValue.getJrType() + ": " + rhsType.getIndex() + ")" );
        }


        if (! (lhsType instanceof VarType || lhsType instanceof MutType)) {
            return;
        }
        if (! (rhsType instanceof VarType || rhsType instanceof MutType)) {
            return;
        }

        JrTyped newGuard = null;;
        MutType newGuardType = null;
        if (guardType instanceof MutType) {
            newGuard = guard;
            newGuardType = (MutType) guardType;
        } else {
            // Primitive types can never be mutable and a type var can
            // never be the return type of a polyread method.
            newGuard = null;
            newGuardType = null;
        }

        Pair<JrTyped, MutType> lhsEnvResolved = resolveEnv(lhsEnv, lhsEnvType);
        JrTyped newLhsEnv     = lhsEnvResolved.first();
        MutType newLhsEnvType = lhsEnvResolved.second();

        Pair<JrTyped, MutType> rhsEnvResolved = resolveEnv(rhsEnv, rhsEnvType);
        JrTyped newRhsEnv     = rhsEnvResolved.first();
        MutType newRhsEnvType = rhsEnvResolved.second();


        Pair<JrTyped, MutType> lhsResolved = resolveVarType(lhsEnv, lhsEnvType, lhsValue, lhsType);
        if (lhsResolved == null) return; // TEMP
        JrTyped newLhsValue = lhsResolved.first();
        MutType newLhsType  = lhsResolved.second();

        Pair<JrTyped, MutType> rhsResolved = resolveVarType(rhsEnv, rhsEnvType, rhsValue, rhsType);
        if (rhsResolved == null) return; // TEMP
        JrTyped newRhsValue = rhsResolved.first();
        MutType newRhsType  = rhsResolved.second();

        subtype4(newLhsEnv, newLhsEnvType, newLhsValue, newLhsType,
                 newRhsEnv, newRhsEnvType, newRhsValue, newRhsType,
                 newGuard, newGuardType, cause);
    }

    private static Pair<JrTyped, MutType> resolveEnv(JrTyped env, JrType envType) {
        MutType newEnvType;
        if (envType instanceof MutType) {
            newEnvType = (MutType) envType;
        } else if (envType instanceof VarType) {
            env = (VarType) envType;
            newEnvType = ((VarType) envType).bound();
        } else {
            env = null;
            newEnvType = null;
        }
        return new Pair<JrTyped, MutType>(env, newEnvType);
    }

    private static Pair<JrTyped, MutType> resolveVarType(JrTyped env, JrType envType, JrTyped value, JrType valueType) {

        if (!(valueType instanceof VarType))
            return new Pair<JrTyped, MutType>(value, (MutType) valueType);
        VarType valueTypeVT = (VarType) valueType;

        if (env == null || value.equals(env))
            // Can't look up the VarType
            return new Pair<JrTyped, MutType>(value, valueTypeVT.bound());

        Pair<JrTyped, TypeArg> x = null;

        try {
            x = ((ClassType) envType).shallowSearchForTypeArg(env, valueTypeVT);
        } catch (Exception e) {
          //System.out.println("ConstraintManager.resolveVarType: caught exception: " + e);
          return null;
        }
        if (x == null) {
            if (((VarType) valueType).getOwner() instanceof SootMethod) {
                //System.err.println("Can't resolve VarType " + valueType + " belonging to " + ((VarType) valueType).getOwner());
                return null; // TEMP
            } else {
              // TODO: like above, report error, but continue analysis
              //System.err.println("Can't resolve VarType " + valueType + " belonging to " + ((VarType) valueType).getOwner());
              return null;
               // throw new RuntimeException("Did not find type param: " + valueType);
            }
        }

        TypeArg targ = x.second;
        JrType newValueType;

        if (valueType.getIndex().equals(TypeIndex.topLevel())) {
            newValueType = targ.getUpperBound();
        } else {
            newValueType = valueType.getIndex().getLastBound().equals(Bound.UPPER) ? targ.getUpperBound() : targ.getLowerBound();
        }
        JrTyped newValue = x.first;

        // If newValueType is a VarType, resolveVarType is going to forget
        // about newValue.  That's OK because we didn't take any MutTypes from
        // it.
        return resolveVarType(env, envType, newValue, newValueType);
    }


    /**
     * Adds the constraint:
     * guard : guardType -> (lhsEnv : lhsEnvType |- lhsValue^? : lhsType <: rhsEnv : rhsEnvType |- rhsValue^? : rhsType)
     * where no type variables appear in the constraint.
     */
    private void subtype4(JrTyped lhsEnv, MutType lhsEnvType, JrTyped lhsValue, MutType lhsType,
                          JrTyped rhsEnv, MutType rhsEnvType, JrTyped rhsValue, MutType rhsType,
                          JrTyped guard,  MutType guardType, SourceCause cause) {

        if (Options.v().debugSubtyping()) {
            System.out.println("debugSubtyping2>> "
                               + lhsEnv + ": " + lhsEnvType + " |- " + lhsValue + ": " + lhsType + " (" + lhsValue.getJrType() + ": " + lhsType.getIndex() + ") <: "
                               +  rhsEnv + ": " + rhsEnvType + " |- " + rhsValue + ": " + rhsType + "(" + rhsValue.getJrType() + ": " + rhsType.getIndex() + ")" );
        }

        // Some curiously recurring type parameters would cause us to recurse
        // infinitely if it were not for this test.
        // HMMM This is a hack and might not prevent all cases of infinite
        // recursion.  It might be better to remember the sets of arguments
        // with which subtype4 has been called and return immediately if the
        // same arguments (by object identity) are supplied again.

        if (lhsType == rhsType) {
            return;
        }

        // x = y.f
        // y |- f <: x
        // x -> y
            if (lhsEnv != null) {
                if (lhsValue instanceof SootField) {
                    if (! Mutability.MUTABLE.equals(((MutType) lhsType).getMutability())) {
                        guards(rhsValue, rhsType, lhsEnv, lhsEnvType, cause);
                    }
                }
            }

        if (guard == null) {
            if (lhsValue instanceof SootMethod &&
                lhsType.getIndex().equals(TypeIndex.topLevel())) {
                guards4(rhsValue, rhsType, Context.MUTABLE, lhsValue, lhsType, Context.MUTABLE, cause);
            } else {
                // x = y
                // x -> y
                guards(rhsValue, rhsType,
                       lhsValue, lhsType, cause);
            }
        } else {
            // guard = m(x)
            // guard^ro  => m(0)^mut -> x^ro
            // guard^mut => m(0)^mut -> x^mut
            // m(0)^ro -> x^ro
            // m(0)^ro -> x^mut
            guards3(rhsValue, (MutType) rhsType,
                    lhsValue, (MutType) lhsType,
                    guard, (MutType) guardType, cause);

            guards2(rhsValue, (MutType) rhsType,
                    lhsValue, (MutType) lhsType, cause);
        }


        if (lhsType instanceof ClassType) {

            ClassType lhsClassType = (ClassType) lhsType;
            // Downcast Object to array
            if (rhsType instanceof ArrayType && lhsClassType.getBaseType().equals("Ljava/lang/Object;")) return;
            ClassType rhsClassType = (ClassType) rhsType;

            ClassType lhs1 = lhsClassType.
                getSuperParameterizedType(rhsClassType.getBaseType());
            if (lhs1 != null)
                lhsClassType = lhs1;
            else {
                // if that didn't work, we're probably looking at a downcast
                ClassType rhs1 = rhsClassType.
                    getSuperParameterizedType(lhsClassType.getBaseType());
                if (rhs1 != null)
                    rhsClassType = rhs1;
                // otherwise let the test below flag the mismatched base types
            }

            // Check that they have the same structure
            if (!lhsClassType.getBaseType().equals(rhsClassType.getBaseType())
                    || lhsClassType.getTypeArgs().size() != rhsClassType.getTypeArgs().size()) {
              return;
              //throw new RuntimeException("Incompatable types: " + lhsValue + ": " + lhsType + " <: " + rhsValue + ": " + rhsType);
            }

            for (int i = 0; i < lhsClassType.getTypeArgs().size(); i++) {

                // contains lhs_upperBound <: rhs_upperBound AND rhs_lowerBound <: lhs_lowerBound

              setInferringTypeArgs(true);
                subtype3(lhsEnv, lhsEnvType, lhsValue, lhsClassType.getTypeArgs().get(i).getUpperBound(),
                         rhsEnv, rhsEnvType, rhsValue, rhsClassType.getTypeArgs().get(i).getUpperBound(),
                         null, null, cause);

                subtype3(rhsEnv, rhsEnvType, rhsValue, rhsClassType.getTypeArgs().get(i).getLowerBound(),
                         lhsEnv, lhsEnvType, lhsValue, lhsClassType.getTypeArgs().get(i).getLowerBound(),
                         null, null, cause);
              setInferringTypeArgs(false);
            }
        } else if (lhsType instanceof ArrayType) {

            if (rhsType instanceof ClassType && ((ClassType) rhsType).getBaseType().equals("Ljava/lang/Object;")) return;

            if (! (rhsType instanceof ArrayType)) {
                throw new RuntimeException("Incompatable types: " + lhsValue + ": " + lhsType + " <: " + rhsValue + ": " + rhsType);
            }

            ArrayType lhsArrayType = (ArrayType) lhsType;
            ArrayType rhsArrayType = (ArrayType) rhsType;

            subtype3(lhsEnv, lhsEnvType, lhsValue, lhsArrayType.getElemType().getUpperBound(),
                     rhsEnv, rhsEnvType, rhsValue, rhsArrayType.getElemType().getUpperBound(),
                     null, null, cause);

            subtype3(rhsEnv, rhsEnvType, rhsValue, rhsArrayType.getElemType().getLowerBound(),
                     lhsEnv, lhsEnvType, lhsValue, lhsArrayType.getElemType().getLowerBound(),
                     null, null, cause);
        }
    }

    /**
     * Adds the constraint:
     * lhsValue^? : lhsType -> rhsValue^? : rhsType
     *
     * Depending on whether lhsValue and rhsValue are fields or local variables, the
     * "context" of the constraint variables are determined.
     *
     * If both lhs and rhs are local variables then the following constraints are added:
     * lhsValue^ro : lhsType -> rhsValue^ro : rhsType
     * lhsValue^mut : lhsType -> rhsValue^mut : rhsType
     *
     * If lhs is a local variable and rhs is a field, then the following constraints are added:
     * lhsValue^ro : lhsType -> rhsValue : rhsType
     * lhsValue^mut : lhsType -> rhsValue : rhsType
     *
     * If lhs is a field and rhs is a local variable, then the following constraints are added:
     * lhsValue : lhsType -> rhsValue^ro : rhsType
     * lhsValue : lhsType -> rhsValue^mut : rhsType
     *
     *
     * If both lhs and rhs are fields then the following constraint is added:
     * lhsValue : lhsType -> rhsValue : rhsType
     */
    public void guards(JrTyped lhsValue, MutType lhsType,
                        JrTyped rhsValue, MutType rhsType, SourceCause cause) {

        if (Options.v().debugConstraintGeneration() || Options.v().debugSubtyping()) {
            System.out.println("guards():  " + lhsValue + " " + lhsType + " -> " + rhsValue + " " + rhsType);
        }

        ConstraintVar lhsRo;
        ConstraintVar lhsMut;
        if (lhsValue instanceof SootField) {
            lhsRo  = ConstraintVar.create(lhsValue, lhsType, Context.NONE);
            lhsMut = ConstraintVar.create(lhsValue, lhsType, Context.NONE);
        } else {
            lhsRo  = ConstraintVar.create(lhsValue, lhsType, Context.READONLY);
            lhsMut = ConstraintVar.create(lhsValue, lhsType, Context.MUTABLE);
        }

        ConstraintVar rhsRo;
        ConstraintVar rhsMut;
        if (rhsValue instanceof SootField) {
            rhsRo  = ConstraintVar.create(rhsValue, rhsType, Context.NONE);
            rhsMut = ConstraintVar.create(rhsValue, rhsType, Context.NONE);
        } else {
            rhsRo  = ConstraintVar.create(rhsValue, rhsType, Context.READONLY);
            rhsMut = ConstraintVar.create(rhsValue, rhsType, Context.MUTABLE);
        }

        if(cause != null) {
            ConstraintTracker.add(lhsRo,  rhsRo,  cause);
            ConstraintTracker.add(lhsMut, rhsMut, cause);
        }

        if (Options.v().debugConstraintGeneration() || Options.v().debugSubtyping()) {
            System.out.println("  ro : " + lhsRo + " -> " + rhsRo);
            System.out.println("  mut: " + lhsMut + " -> " + rhsMut);
        }

        if (!shouldSkipReadonlyContext()) {
          cs.add(lhsRo, rhsRo);
        } else {
          if (Options.v().debugConstraintGeneration()) {
            System.out.println("guards(): omit constraint: " + lhsRo + " \n -> " + rhsRo);
          }
        }

        cs.add(lhsMut, rhsMut);
    }


    // /**
    //  * lhs Ro -> rhs Ro
    //  * lhs Ro -> rhs Mut
    //  */
    /**
     * Adds the constraints:
     * lhsValue^ro : lhsType  ->  rhsValue^ro : rhsType
     * lhsValue^ro : lhsType  ->  rhsValue^mut : rhsType
     *
     * This private method is used to generate some constraints involved in method calls.
     * In particular, it is used to generate constraints that require that a method call's actual argument is
     * mutable if the method parameter from the method's read-only context is mutable.
     * (Determining whether an actual argument is mutable if the method parameter from the
     * method's mutable context is mutable is a more complicated problem and involves twice
     * guarded constraints generated by guards3.)
     */
    private void guards2(JrTyped lhsValue, MutType lhsType,
                         JrTyped rhsValue, MutType rhsType, SourceCause cause) {

        ConstraintVar lhs    = ConstraintVar.create(lhsValue, lhsType, Context.READONLY);
        ConstraintVar rhsRo  = ConstraintVar.create(rhsValue, rhsType, Context.READONLY);
        ConstraintVar rhsMut = ConstraintVar.create(rhsValue, rhsType, Context.MUTABLE);

        if (!shouldSkipReadonlyContext()) {
            ConstraintTracker.add(lhs,  rhsRo,  cause);
            cs.add(lhs, rhsRo);
        } else {
            if (Options.v().debugConstraintGeneration()) {
                System.out.println("guards2(): omit constraint: " + lhs + " \n -> " + rhsRo);
            }
        }

        ConstraintTracker.add(lhs, rhsMut, cause);
        cs.add(lhs, rhsMut);
    }

    /**
     * Adds the constraints:
     * guard^ro : guardType  -> lhs^mut : lhsType  ->  rhs^ro : rhsType
     * guard^mut : guardType  -> lhs^mut : lhsType  ->  rhs^mut : rhsType
     *
     * This private method is used to generate some constraints involved in method calls.
     * In particular, it is used to generate constraints that require that a method call's actual argument is
     * mutable if the method parameter from the method's mutable context is mutable and the
     * return value of the method is mutable.
     * (Determining whether an actual argument is mutable if the method parameter from the
     * method's read-only context is mutable involves constraints generated by guards2.)
     */
    private void guards3(JrTyped lhsValue, MutType lhsType,
                         JrTyped rhsValue, MutType rhsType,
                         JrTyped guard,    MutType guardType, SourceCause cause) {

        ConstraintVar guardRo  = ConstraintVar.create(guard, guardType, Context.READONLY);
        ConstraintVar guardMut = ConstraintVar.create(guard, guardType, Context.MUTABLE);
        ConstraintVar lhs      = ConstraintVar.create(lhsValue, lhsType, Context.MUTABLE);
        ConstraintVar rhsRo    = ConstraintVar.create(rhsValue, rhsType, Context.READONLY);
        ConstraintVar rhsMut   = ConstraintVar.create(rhsValue, rhsType, Context.MUTABLE);

        if (!shouldSkipReadonlyContext()) {
            ConstraintTracker.add(guardRo, lhs,  rhsRo,  cause);
            cs.add(guardRo, lhs, rhsRo);
        } else {
            if (Options.v().debugConstraintGeneration()) {
              System.out.println("guards3(): omit constraint: " + guardRo + " \n => " + lhs + " \n -> " + rhsRo);
            }
        }

        ConstraintTracker.add(guardMut, lhs, rhsMut, cause);
        cs.add(guardMut, lhs, rhsMut);
    }

    /**
     * Adds the constraint:
     * lhsValue^lhsContext : lhsType  ->  rhsValue^rhsContext : rhsType
     */
    private void guards4(JrTyped lhsValue, MutType lhsType, Context lhsContext,
                         JrTyped rhsValue, MutType rhsType, Context rhsContext, SourceCause cause) {

        ConstraintVar lhs = ConstraintVar.create(lhsValue, lhsType, lhsContext);
        ConstraintVar rhs = ConstraintVar.create(rhsValue, rhsType, rhsContext);
        if(cause != null) ConstraintTracker.add(lhs, rhs, cause);
        cs.add(lhs, rhs);
    }


    /**
     * Adds the constraint:
     * value^? : type
     *
     * Where "type" is not a type variable.
     */
    public void mutable(JrTyped value, MutType type, SourceCause cause) {

        if (value instanceof SootField) {
            ConstraintVar var = ConstraintVar.create(value, type, Context.NONE);
            cs.add(var);
        } else {
            ConstraintVar var1 = ConstraintVar.create(value, type, Context.READONLY);
            ConstraintVar var2 = ConstraintVar.create(value, type, Context.MUTABLE);
            var1.setSource(cause);
            var2.setSource(cause);
            cs.add(var1);
            cs.add(var2);
        }

    }

    /**
     * Adds the constraint:
     * value^? : type
     *
     * Where "type" may be a type variable.
     */
    public void mutable3(JrTyped value, JrType type, SourceCause cause) {

        JrTyped value2 = type instanceof VarType ? (VarType) type : value;
        MutType type2  = (MutType) (value instanceof VarType ? ((VarType) type).bound() : type);

        mutable(value2, type2, cause);
    }


    /**
     * Adds the constraint:
     * value^context : type
     *
     * Where "type" may be a type variable.
     */
    public void mutable4(JrTyped value, JrType type, Context context, SourceCause cause) {

        JrTyped value2 = type instanceof VarType ? (VarType) type : value;
        MutType type2  = (MutType) (value instanceof VarType ? ((VarType) type).bound() : type);

        mutable2(value2, type2, context, cause);
    }

    /**
     * Adds the constraint:
     * value^context : type
     *
     * Where "type" is not a type variable.
     */
    public void mutable2(JrTyped value, MutType type, Context context, SourceCause cause) {

        ConstraintVar var = ConstraintVar.create(value, type, context);
        var.setSource(cause);
        cs.add(var);
    }

//     public void mutable3(ConstraintVar var) {
//         cs.add(var);
//     }


    public String toString() {
        return cs.toString();
    }

}
