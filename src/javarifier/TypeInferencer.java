package javarifier;

import soot.Scene;
import soot.Body;
import soot.Type;
import soot.Value;
import soot.Local;
import soot.SootField;
import soot.SootMethod;

import soot.util.Chain;

import soot.jimple.ClassConstant;
import soot.jimple.NewArrayExpr;
import soot.jimple.Stmt;
import soot.jimple.ReturnStmt;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.ThisRef;
import soot.jimple.ParameterRef;

import javarifier.JrType.*;
import javarifier.util.Pair;

import java.util.*;

public class TypeInferencer extends SceneVisitor {

    public static void infer(Scene s) {
        (new TypeInferencer()).visitScene(s);
        if (Main.debugTypeInference) {
            System.out.println(ScenePrinter.print(s));
        }
    }

    public void visitBody(Body b) {

        TypeInferenceSwitch inferencer =
            new TypeInferenceSwitch(b.getMethod());

        Chain units = b.getUnits();

        for (Stmt stmt : (Collection<Stmt>) units) {
            stmt.apply(inferencer);
        }
    }

public static class TypeInferenceSwitch extends soot.jimple.AbstractStmtSwitch {


    private SootMethod enclosingMethod;

    public TypeInferenceSwitch(SootMethod enclosingMethod) {
        this.enclosingMethod = enclosingMethod;
    }

    private JrType createJrTypeFromSootBaseType(Type elementType) {
        if (elementType instanceof soot.PrimType)
            return new PrimType();
        else if (elementType instanceof soot.RefType)
            return createWildcardClassType(
                    ((soot.RefType) elementType).getClassName());
        else if (elementType instanceof soot.ArrayType) {
          soot.ArrayType at = (soot.ArrayType) elementType;
          JrType arrayElType = createJrTypeFromSootBaseType(at.baseType);
          TypeArg typeArg = new JrType.TypeArg(arrayElType.copy(), arrayElType.copy());
          return new JrType.ArrayType(Mutability.UNKNOWN, typeArg);
        } else {
            throw new RuntimeException("Unrecognized array element type " + elementType);
        }
    }

    // If "class Foo<A,B,C>", then
    // createWildcardClassType("Foo") --> "Foo<?,?,?>"
    private ClassType createWildcardClassType(String className) {
        String classDesc = "L" + className.replace('.','/') + ";";
        List<Pair<VarType, JrType>> params =
            Scene.v().getSootClass(className).getSig().getTypeParams();
        List<TypeArg> wildcards = new ArrayList<TypeArg>();
        for (Pair<VarType, JrType> param : params) {
            TypeArg arg = new TypeArg(ClassType.readonlyObject(), new NullType());
            arg.setTypeParam(param.first);
            wildcards.add(arg);
        }
        ClassType wildcardClassType = new ClassType(Mutability.UNKNOWN,
                classDesc, wildcards);
        return wildcardClassType;
    }

    public void caseAssignStmt(AssignStmt stmt) {
        try {
            defaultCase(stmt);
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();

        if (lhs instanceof InstanceFieldRef) {
          SootField f = ((InstanceFieldRef) lhs).getField();
          if (f.getName().toString().contains("defaultValue")) {
            //TODO: System.out.println("Did not infer type for: " + lhs + " = " + rhs);
          }
        }

        if (false) {
        } else if (lhs instanceof Local &&
                   rhs instanceof Constant) {
            if (rhs instanceof ClassConstant) {
                // x = Foo.class
                Local x = (Local) lhs;
                if (x.getJrType() == null) {
                    // Create the type of the class token: Class<Foo<?,?,?>>
                    String classNameSlash = ((ClassConstant) rhs).getValue();
                    String className = classNameSlash.replace('/', '.');
                    ClassType wildcardClassType = createWildcardClassType(className);
                    VarType clazzT = Scene.v().getSootClass("java.lang.Class")
                        .getSig().getTypeParams().get(0).first;
                    TypeArg clazzTArg = new TypeArg(wildcardClassType,
                            wildcardClassType.copy());
                    clazzTArg.setTypeParam(clazzT);
                    ClassType classTokenType = new ClassType(Mutability.UNKNOWN,
                            "Ljava/lang/Class;", Collections.singletonList(clazzTArg));
                    TypeIndexer.index(classTokenType);
                    x.setJrType(classTokenType);
                }
            }
        } else if (lhs instanceof StaticFieldRef &&
                   rhs instanceof Constant) {
        } else if (lhs instanceof InstanceFieldRef &&
                   rhs instanceof Constant) {
        } else if (lhs instanceof Local &&
                   rhs instanceof Local) {

            Local x = (Local) lhs;
            Local y = (Local) rhs;

            JrType xType = x.getJrType();
            JrType yType = y.getJrType();

            if (xType == null && yType == null) {
            } else if (xType == null) {
                xType = yType.copyResettingMutabilities();
                x.setJrType(xType);
            } else if (yType == null) {
                yType = xType.copyResettingMutabilities();
                y.setJrType(yType);
            }

        } else if (lhs instanceof StaticFieldRef &&
                   rhs instanceof Local) {

            SootField sf = ((StaticFieldRef) lhs).getField();
            Local y = (Local) rhs;

            JrType sfType = sf.getJrType();
            JrType yType = y.getJrType();

            if (yType == null) {
                yType = sfType.copyResettingMutabilities();
                y.setJrType(yType);
            }
        } else if (lhs instanceof Local &&
                   rhs instanceof StaticFieldRef) {
            // x = T.sf

            Local x = (Local) lhs;
            SootField sf = ((StaticFieldRef) rhs).getField();

            JrType xType = x.getJrType();
            JrType sfType = sf.getJrType();

            if (xType == null) {
                xType = sfType.copyResettingMutabilities();
                x.setJrType(xType);
            }

        } else if (lhs instanceof InstanceFieldRef &&
                   rhs instanceof Local) {
            // x.f = y
// TODO: here is where AnnotationMethodDeclaration $e14.defaultValue = $e17 gets taken care of
            Local x = (Local) ((InstanceFieldRef) lhs).getBase();
            SootField f = ((InstanceFieldRef) lhs).getField();
            Local y = (Local) rhs;

            JrType xType = x.getJrType();
            JrType fType = f.getJrType();
            JrType yType = y.getJrType();

            if (xType == null) {
              if (fType instanceof ClassType) {
                xType = createJrTypeFromSootBaseType(f.getDeclaringClass().getType());
                // TODO: should you ever not use the top level type, which is the
                // true type for field access.
                xType.setIndex(TypeIndex.topLevel());
                x.setJrType(xType);
              }
            }

            if (yType == null) {
                yType = fType.substituteLowerBound((ClassType) xType);
                y.setJrType(yType);
            }

        } else if (lhs instanceof Local &&
                   rhs instanceof InstanceFieldRef) {
            // x = y.f

            Local x = (Local) lhs;
            Local y = (Local) ((InstanceFieldRef) rhs).getBase();
            SootField f = ((InstanceFieldRef) rhs).getField();

            JrType xType = x.getJrType();
            JrType yType = y.getJrType();
            JrType fType = f.getJrType();

            if (xType == null) {
                // class Cell<T> { T f }
                // Cell<Date> y;
                // Date x = y.f;
                // "T".substituteUpperBound("Cell<Date>") --> "Date"
                xType = fType.substituteUpperBound((ClassType) yType);
                x.setJrType(xType);
            }

        } else if (lhs instanceof Local &&
                   rhs instanceof CastExpr) {
            // x = (Type) y

            if (lhs instanceof Local && ((CastExpr) rhs).getOp() instanceof Local) {

                Local x = (Local) lhs;
                Local y = (Local) ((CastExpr) rhs).getOp();

                JrType xType = x.getJrType();
                JrType yType = y.getJrType();

                    // HMMM The point of a cast is to change the type, so why
                    // are we inferring the same type?!  For now, comment this
                    // out so we don't infer a type from the cast at all, and
                    // hope that the correct type is inferred another way.
//                  if (! (xType == null && yType == null)) {
//
//                      if (xType == null) {
//                              xType = yType.copyResettingMutabilities();
//                              x.setJrType(xType);
//
//                      }
//                      if (yType == null) {
//                              yType = xType.copyResettingMutabilities();
//                              y.setJrType(yType);
//
//                      }
//                  }
                }
        } else if (lhs instanceof ArrayRef &&
                   rhs instanceof Local) {
            // array a[x] = y

            Local a = (Local) ((ArrayRef) lhs).getBase();
            Local y = (Local) rhs;

            if (!(a.getJrType() instanceof ArrayType)) {
              //TODO: System.out.println("About to cast prim to object");
            }
            ArrayType aType = (ArrayType) a.getJrType();
            JrType yType = y.getJrType();

            if (yType == null) {
                yType = aType.getElemType().getLowerBound().copyResettingMutabilities();
                y.setJrType(yType);
            }

            if (aType == null) {
                aType = new ArrayType(Mutability.UNKNOWN, new TypeArg(yType.copyResettingMutabilities(), yType.copyResettingMutabilities()));
                TypeIndexer.index(aType);
                a.setJrType(aType);
            }

        } else if (lhs instanceof Local &&
                   rhs instanceof ArrayRef) {
            // x = a[y] : a [0 UP] <: x

            Local x = (Local) lhs;
            Local a = (Local) ((ArrayRef) rhs).getBase();

            JrType xType = x.getJrType();
            ArrayType aType = (ArrayType) a.getJrType();

            if (aType == null) {
              //System.out.println("Null aType: " + aType);

            }

            // TODO: if both aType and xType are null, don't set one from the other
            // Does this make sense because there is no necessary constraint between unknown values?

            if (xType == null && aType != null) {
              //TODO: Decompose to see where null comes from:
              //xType = aType.getElemType().getUpperBound().copyResettingMutabilities();

              // TODO: this was removed in moving from eclipsec to htmlparser:
              TypeArg et = aType.getElemType();
              JrType jr = et.getUpperBound();
              JrType ta = jr.copyResettingMutabilities();
              xType = ta;

              x.setJrType(xType);
            }

            if (aType == null && xType != null) {
                aType = new ArrayType(Mutability.UNKNOWN, new TypeArg(xType.copyResettingMutabilities(), xType.copyResettingMutabilities()));
                TypeIndexer.index(aType);
                a.setJrType(aType);
            }

        } else if (lhs instanceof Local &&
                   rhs instanceof InvokeExpr) { // method call
            // x = y.m(z)
            // OR x = m(z)

            InvokeExpr methInvk = (InvokeExpr) rhs;

            // handles parameters and receiver
            handleMethodInvk(methInvk);

            // Handle return value
            Local x = (Local) lhs;
            SootMethod m = methInvk.getMethod();

            JrType xType = x.getJrType();
            JrType retType = m.getJrReturnType();
            if (retType == null) {
                throw new RuntimeException("Null return type for " + m + " in: " + stmt);
            }

            if (xType == null) {
                if (methInvk instanceof InstanceInvokeExpr) {
                    Local y = (Local) ((InstanceInvokeExpr) methInvk).getBase();
                    JrType yType = y.getJrType();
                    if (yType instanceof VarType)
                        yType = ((VarType) yType).bound();
                    if (yType instanceof ArrayType)
                        xType = retType.copyResettingMutabilities();
                    else if (yType instanceof ClassType)
                        xType = retType.substituteUpperBound((ClassType) yType);
                    else if (yType != null) {
                        String message = String.format(
                          "Javarifier bug:  receiver type has already been set, to an inconsistent value.%n  Receiver = %s%n  Type (%s) = %s%n  Statement = %s%n  A cause is a bytecode file that re-uses a local variable, first to hold a%n  primitive and later to hold an object; processing of an earlier statement%n  sets the local variable's type, which causes this error when processing a%n  statement that uses the second live range as as object.",
                          y,
                          yType.getClass(), yType,
                          stmt);
                        // throw new RuntimeException(message);
                        System.out.println(message);
                        System.out.println("Reset and hope for the best.  But, the same bug is likely to show up elsewhere%nin a manner that can't be worked around.%nA better fix would be to convert to SSA form.");
                        y.setJrType(null);
                    }
                    // if yType is null, we just set the same type back into x
                } else
                    xType = retType.copyResettingMutabilities();
                if (!typeUsesUnavailableParameters(xType))
                    x.setJrType(xType);
            }
        } else if (lhs instanceof Local &&
                   rhs instanceof NewExpr) {
                // x = new Foo<Date>();
                // Do nothing.  This case is handled in the method invk code.

        } else if (lhs instanceof Local &&
                   rhs instanceof NewArrayExpr) {
            Local x = (Local) lhs;
            if (x.getJrType() == null) {
                Type elementType1 = ((NewArrayExpr) rhs).getBaseType();
                JrType elementType = createJrTypeFromSootBaseType(elementType1);
                TypeArg elTypeArg = new TypeArg(elementType, elementType.copy());
                // leave elTypeArg.param null, representing an array element type
                ArrayType arrayType = new ArrayType(Mutability.UNKNOWN,
                        elTypeArg);
                TypeIndexer.index(arrayType);
                x.setJrType(arrayType);
            }
        } else {
          // TODO: remove specific debugging case:
          if (lhs instanceof InstanceFieldRef) {
            SootField f = ((InstanceFieldRef) lhs).getField();
            if (f.getName().toString().contains("defaultValue")) {
              //TODO: System.out.println("Did not infer type for: " + lhs + " = " + rhs);
            }
          }
//             System.out.println("ConstraintGenerationSwitch: WARNING\n" +
//                                lhs + " " + rhs + "\n " +
//                                lhs.getClass() +  " " + rhs.getClass());
        }
        } catch (Exception e) {
            throw new RuntimeException("\nException caused from " + stmt +
                                       "\n in " + enclosingMethod.getName() +
                                       "\n" + e, e);
        }
    }


    public void caseReturnStmt(ReturnStmt stmt) {
        // return x
        Value val = stmt.getOp();

        if (val instanceof Local) {
            Local x = (Local) val;
            SootMethod m = enclosingMethod;

            JrType xType = x.getJrType();
            JrType retType = m.getJrReturnType();
            // This can be incorrect for anno classes
            if (xType == null) {
                xType = retType.copyResettingMutabilities();
                x.setJrType(xType);
            }
        }
    }
    @Override
    public void caseIdentityStmt(IdentityStmt stmt) {

        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (lhs instanceof Local) {
                        Local x = (Local) lhs;
                        SootMethod meth = x.getMethod();
                        JrType xType = x.getJrType();

                        JrType yType = null;
                        if (rhs instanceof ThisRef) {
                        Param y = meth.getReceiver();
                        yType = y.getJrType();
                } else if (rhs instanceof ParameterRef){
                        Param y = meth.getParameter(((ParameterRef) rhs).getIndex());
                        yType = y.getJrType();
                }
                        if (xType == null && yType != null) {
                                xType = yType.copyResettingMutabilities();
                                x.setJrType(xType);
                        }

        }
    }


    public void defaultCase(Object obj) {
        Stmt stmt = (Stmt) obj;
        if (stmt.containsInvokeExpr()) {
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            handleMethodInvk(invokeExpr);
        }
    }

    /**
     * Does the type refer to any type parameters that are not in the context
     * of the enclosing method?  If so, we don't want to assign a broken type
     * to a local of the enclosing method; we should try to infer the local's
     * type another way.
     */
    private boolean typeUsesUnavailableParameters(JrType type) {
        if (type == null)
            return true;
        // hack alert: signal that we found an unavailable parameter and
        // break out of visiting by throwing NoSuchElementException.
        try {
            type.accept(new EmptyTypeVisitor() {
                @Override
                public void visitVarType(VarType t) {
                    if (!enclosingMethod.getSig().contains(t)
                            && !enclosingMethod.getDeclaringClass()
                                .getSig().contains(t))
                        throw new NoSuchElementException();
                }
                @Override
                public void visitClassType(ClassType t) {
                    // don't visit supertypes
                    for (TypeArg targ : t.getTypeArgs()) {
                        targ.accept(this);
                    }
                }
            });
            return false;
        } catch (NoSuchElementException e) {
            return true;
        }
    }

    private void handleMethodInvk(InvokeExpr methInvk) {
        // y.m(zs)
        SootMethod m = methInvk.getMethod();

        List<Value> zs = methInvk.getArgs(); // Warning, some args can be int constants and not locals


        // Handle reciever
        if (methInvk instanceof InstanceInvokeExpr) {

            InstanceInvokeExpr iMethInvk =
                (InstanceInvokeExpr) methInvk;

            Local y = (Local) iMethInvk.getBase();
            JrType yType = y.getJrType();

            Param thisParam = m.getReceiver();
            JrType thisParamType = thisParam.getJrType();

            if (yType == null) {
                yType = thisParamType.copyResettingMutabilities();
                if (!typeUsesUnavailableParameters(yType))
                    y.setJrType(yType);
            }
        }

//            // Handle constructor "recievers" specially
//            if (methInvk instanceof SpecialInvokeExpr &&
//              methInvk.getMethod().getName().equals("<init>")) {
//
//              SpecialInvokeExpr constructorInvk = (SpecialInvokeExpr) methInvk;
//
//            }

        // Handle arguments
        for (int i = 0; i < zs.size(); i++) {
            if (zs.get(i) instanceof Local) {
                Local z = (Local) zs.get(i);
                Param param = m.getParameter(i);

                JrType zType = z.getJrType();
                JrType paramType = param.getJrType();
                if (zType == null) {

                    if (methInvk instanceof InstanceInvokeExpr) {
                        Local y = (Local) ((InstanceInvokeExpr) methInvk).getBase();
                        ClassType yType = (ClassType) y.getJrType();
                        if (yType != null)
                            zType = paramType.substituteLowerBound(yType);
                        // if yType is null, we just set the same type back into z
                    } else
                        zType = paramType.copyResettingMutabilities();

                    if (!typeUsesUnavailableParameters(zType))
                        z.setJrType(zType);
                }
            }
        }
    }
}


}
