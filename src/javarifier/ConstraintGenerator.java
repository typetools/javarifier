package javarifier;

import java.util.Collection;
import java.util.List;

import javarifier.JrType.ArrayType;
import javarifier.JrType.MutType;
import javarifier.util.Pair;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

// This handles assignments (e.g., "a = b") and pseudo-assignments (e.g.,
// of an actual argument to a formal parameter, or a result to a return
// value).
public class ConstraintGenerator extends SceneVisitor {

    public static ConstraintManager generate(Scene s) {
        ConstraintGenerator cg = new ConstraintGenerator();
        cg.visitScene(s);

        if (Options.v().dumpGeneratorConstraints()) {
            System.out.println("Generator Constraints:");
            System.out.println(cg.getConstraints());
        }

        return cg.getConstraints();
    }

    private ConstraintManager cm;

    public ConstraintGenerator() {
        cm = new ConstraintManager();
    }

    public ConstraintManager getConstraints() {
        return cm;
    }

    @Override
    public void visitClass(SootClass sc) {
      if(Options.v().debugConstraintGeneration()) {
        System.out.println("Generating constraints for class: " + sc.getName());
      }
      
      // Skip generating constraints for internals of stub classes.
      if(sc.resolvingLevel() >= SootClass.BODIES) {
        super.visitClass(sc);
      } else if(Options.v().debugConstraintGeneration()) {
        System.out.println("ConstraintGenerator skipping stub class: " + sc.getName());
      }
    }
    
    public void visitBody(Body b) {

        ConstraintGenerationSwitch conGen =
            new ConstraintGenerationSwitch(b.getMethod());

        for (Stmt stmt : (Collection<Stmt>) b.getUnits()) {
            stmt.apply(conGen);
        }

        cm =  cm.combine(conGen.getConstraintManager());
    }


public static class ConstraintGenerationSwitch extends soot.jimple.AbstractStmtSwitch {

    private ConstraintManager cm;

    /**
     * The method in which this stmt appears.
     */
    private SootMethod enclosingMethod;

    public ConstraintGenerationSwitch(SootMethod enclosingMethod) {
        this.enclosingMethod = enclosingMethod;
        cm = new ConstraintManager();
    }

    public ConstraintManager getConstraintManager() {
        return cm;
    }

    public void caseAssignStmt(AssignStmt stmt) {
        try {

            if (Options.v().debugConstraintGeneration()) {
                System.out.println("Stmt: " + stmt);
            }

            defaultCase(stmt);

            Value lhs = stmt.getLeftOp();
            Value rhs = stmt.getRightOp();


            boolean missingLhsBaseType = false;
            String missingLhsBaseTypeMessage = "";
            // If base of lhs has a null type, then debug information is
            // missing from classfile, so signal this error to the user.
            // Since the Javarifier cannot continue, throw an exception.
            if(lhs instanceof InstanceFieldRef) {
              Local lhsBase = (Local) ((InstanceFieldRef) lhs).getBase();
              JrType lhsBaseType = lhsBase.getJrType();
              if(lhsBaseType == null) {
                SootClass badClass = 
                  ((InstanceFieldRef) lhs).getField().getDeclaringClass();
                String errorMessage = "Class compiled without debug information: " 
                    + badClass.getName();
                System.err.println(errorMessage);
                //TODO: 
                missingLhsBaseType = true;
                missingLhsBaseTypeMessage = errorMessage;
                //throw new RuntimeException(errorMessage);
              }  
            }
            
            if (false) {
            } else if (lhs instanceof Local &&
                       rhs instanceof Constant) {
                // x = 1 : none

            } else if (lhs instanceof StaticFieldRef &&
                       rhs instanceof Constant) {
                // T.sf = 1 : none

            } else if (lhs instanceof InstanceFieldRef &&
                       rhs instanceof Constant) {
                // x.f = 1 : !assignable(f) => x?

                Local x = (Local) ((InstanceFieldRef) lhs).getBase();

                JrType xType = x.getJrType();

                // TODO: missing lhs?
                if(xType != null) {
                
                if (! ((InstanceFieldRef) lhs).getField().assignable()) {
                    cm.mutable3(x, xType);
                }
                }

            } else if (lhs instanceof Local &&
                       rhs instanceof Local) {
                // x = y : y? <: x?

                Local x = (Local) lhs;
                Local y = (Local) rhs;

                JrType xType = x.getJrType();
                JrType yType = y.getJrType();

                cm.subtype(y, yType,
                           x, xType);

            } else if (lhs instanceof StaticFieldRef &&
                       rhs instanceof Local) {
                // T.sf = y : y? <: sf

                SootField sf = ((StaticFieldRef) lhs).getField();
                Local y = (Local) rhs;

                JrType sfType = sf.getJrType();
                JrType yType = y.getJrType();

                cm.subtype(y, yType,
                           sf, sfType);

            } else if (lhs instanceof Local &&
                       rhs instanceof StaticFieldRef) {
                // x = T.sf : sf <: x?

                Local x = (Local) lhs;
                SootField sf = ((StaticFieldRef) rhs).getField();

                JrType xType = x.getJrType();
                JrType sfType = sf.getJrType();

                cm.subtype(sf, sfType,
                            x,  xType);


        } else if (lhs instanceof InstanceFieldRef &&
                   rhs instanceof Local) {
            // x.f = y : (!assignable(f) => x?), (y? <: f)


            Local x = (Local) ((InstanceFieldRef) lhs).getBase();
            SootField f = ((InstanceFieldRef) lhs).getField();
            Local y = (Local) rhs;

            JrType xType = x.getJrType();
            JrType fType = f.getJrType();
            JrType yType = y.getJrType();

            // TODO: missing lhs
            // TODO: assert that if anyone is null, you don't have to do anything
            if(xType != null) {
              if (! f.assignable()) {
                cm.mutable3(x, xType);
              }
              
              cm.subtype2(null,  null, y, yType,
                          x,    xType, f, fType);
            }


        } else if (lhs instanceof Local &&
                   rhs instanceof InstanceFieldRef) {
            // x = y.f : (!mutable(f) => (x? -> y?)), f <: x?

            Local x = (Local) lhs;
            Local y = (Local) ((InstanceFieldRef) rhs).getBase();
            SootField f = ((InstanceFieldRef) rhs).getField();

            JrType xType = x.getJrType();
            JrType yType = y.getJrType();
            JrType fType = f.getJrType();

            cm.subtype2(   y, yType, f, fType,
                        null,  null, x, xType);


        } else if (lhs instanceof Local &&
                   rhs instanceof CastExpr) {
            // x = (Type) y : y? < x?

        	if (lhs instanceof Local && ((CastExpr) rhs).getOp() instanceof Local) {
        		Local x = (Local) lhs;
        		Local y = (Local) ((CastExpr) rhs).getOp();

        		JrType xType = x.getJrType();
        		JrType yType = y.getJrType();

        		cm.subtype(y, yType,
        				   x, xType);
        	}


        } else if (lhs instanceof ArrayRef &&
                   rhs instanceof Local) {
            // array a[x] = y : y <: a [0 LOW] AND a

            Local a = (Local) ((ArrayRef) lhs).getBase();
            Local y = (Local) rhs;

            ArrayType aType = (ArrayType) a.getJrType();
            JrType yType = y.getJrType();

            cm.mutable(a, aType);
            cm.mutable(a, aType);

            cm.subtype(y, yType,
                       a, aType.getElemType().getLowerBound());


        } else if (lhs instanceof Local &&
                   rhs instanceof ArrayRef) {
            // x = a[y] : a [0 UP] <: x

            Local x = (Local) lhs;
            Local a = (Local) ((ArrayRef) rhs).getBase();

            JrType xType = x.getJrType();
            ArrayType aType = (ArrayType) a.getJrType();

            cm.subtype(a, aType.getElemType().getUpperBound(),
                       x, xType);


        } else if (lhs instanceof Local &&
                   rhs instanceof InvokeExpr) { // method call
            // Handled in default case below

        } else if (lhs instanceof Local &&
                   rhs instanceof NewExpr) {
            // Do nothing, let default case handle the arguments.
        } else {
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
        // return x : x <: m_ret
        defaultCase(stmt);
        Value val = stmt.getOp();

        if (val instanceof Local) {
            Local x = (Local) val;
            SootMethod m = enclosingMethod;

            JrType xType = x.getJrType();
            JrType retType = m.getJrReturnType();

            cm.subtype(x, xType,
                       m, retType);

        }
    }

    public void defaultCase(Object obj) {
        Stmt stmt = (Stmt) obj;
        try {
        if (stmt instanceof AssignStmt &&
            ((AssignStmt) stmt).getLeftOp() instanceof Local &&
            ((AssignStmt) stmt).getRightOp() instanceof InvokeExpr) { 
                // method invocation statement (where the return value is
                // assigned to a variable):
                // x = y.m(z)
                // OR x = m(z)

            InvokeExpr methInvk = (InvokeExpr) ((AssignStmt) stmt).getRightOp();

            // Handle return value
            Local x = (Local) ((AssignStmt) stmt).getLeftOp();
            JrType xType = x.getJrType();

            SootMethod m = methInvk.getMethod();
            JrType retType = m.getJrReturnType();

            if (xType == null) {
//              System.err.println("JW: ConstraintGenerator.defaultCase() null pointer for xType");
//              throw new RuntimeException("ConstraintGenerator.deafultCase() null pointer for xType");
//                System.err.println("Null pointer: " + xType + " " + retType);
                //throw new RuntimeException("Null pointer: " + xType + " " + retType);
            }
            

            Local  y     = (methInvk instanceof InstanceInvokeExpr) ? (Local) ((InstanceInvokeExpr) methInvk).getBase() : null;
            JrType yType = (methInvk instanceof InstanceInvokeExpr) ? y.getJrType() : null;
            
            
            // To fix polyread bug, here you don't want to just generally subtype
            // m <: x, because this applies  x -> m (return value) for both
            // contexts.  Instead, you want x -> m^mut.            
            cm.subtype2InvokeWithoutReadOnly(y,   yType, m, retType,
                                             null, null, x, xType);
            handleMethodInvk(methInvk, x, xType);
        } else if (stmt.containsInvokeExpr()) {
            // Method invocation expressions (where the return values are
            // not assigned to a variable, such as void methods).
            InvokeExpr invokeExpr = stmt.getInvokeExpr();
            handleMethodInvk(invokeExpr, null, null);
        }
        
        } catch (RuntimeException e) {
        	throw new RuntimeException(stmt.toString(), e);
        }
    }

    private void handleMethodInvk(InvokeExpr methInvk, Local x, JrType xType) {
        // y.m(z) or m(z)

        SootMethod m = methInvk.getMethod();

        List<Value> zs = methInvk.getArgs();

        if (methInvk instanceof InstanceInvokeExpr &&
            (! (methInvk instanceof SpecialInvokeExpr &&
                methInvk.getMethod().getName().equals("<init>")))) { // Don't operate on constructors

            InstanceInvokeExpr iMethInvk =
                (InstanceInvokeExpr) methInvk;

            Local y = (Local) iMethInvk.getBase();
            JrType yType = y.getJrType();

            Param  thisParam = m.getReceiver();
            JrType thisParamType = thisParam.getJrType();

            cm.subtype3(null, null,  y,         yType,
                        y,    yType, thisParam, thisParamType,
                        x,    xType);
        }
        for (int i = 0; i < zs.size(); i++) {
            if (zs.get(i) instanceof Local) { // Some args can be int constants and not locals

                Local z = (Local) zs.get(i);
                JrType zType = z.getJrType();

                Param param = m.getParameter(i);
                JrType paramType = param.getJrType();

                Local y =
                    (methInvk instanceof InstanceInvokeExpr) ?
                    (Local) ((InstanceInvokeExpr) methInvk).getBase() : null;

                JrType yType =
                    (methInvk instanceof InstanceInvokeExpr) ?
                    ((Local) ((InstanceInvokeExpr) methInvk).getBase()).getJrType() : null;

//                    cm.setSkipReadOnly(false);
                cm.subtype3(null, null,  z,     zType,
                            y,    yType, param, paramType,
                            x,    xType);
//                cm.setSkipReadOnly(true);

            }
        }

    }
}
}
