package javarifier;

import java.util.*;

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
import soot.BriefUnitPrinter;
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

    private static Set<SootClass> missingDebugInfo = new LinkedHashSet<SootClass>();

    private ConstraintManager cm;

    public ConstraintGenerator() {
        cm = new ConstraintManager();
    }

    public ConstraintManager getConstraints() {
        return cm;
    }

    @Override
        public void visitClass(SootClass sc) {
        if (Options.v().debugConstraintGeneration()) {
            System.out.println("Generating constraints for class: " + sc.getName());
        }

        // Skip generating constraints for internals of stub classes.
        if (sc.resolvingLevel() >= SootClass.BODIES) {
            super.visitClass(sc);
        } else if (Options.v().debugConstraintGeneration()) {
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
                    System.out.println("caseAssignStmt: " + stmt);
                }

                SourceLocation loc = new SourceLocation(enclosingMethod, stmt);
                defaultCase(stmt, loc);

                Value lhs = stmt.getLeftOp();
                Value rhs = stmt.getRightOp();

                boolean missingLhsBaseType = false;
                String missingLhsBaseTypeMessage = "";
                // If base of lhs has a null type, then debug information is
                // missing from classfile, so signal this error to the user.
                // Since the Javarifier cannot continue, throw an exception.
                if (lhs instanceof InstanceFieldRef) {
                    Local lhsBase = (Local) ((InstanceFieldRef) lhs).getBase();
                    JrType lhsBaseType = lhsBase.getJrType();
                    if (lhsBaseType == null) {
                        SootClass noDebugClass =
                            ((InstanceFieldRef) lhs).getField().getDeclaringClass();
                        String errorMessage = "Class compiled without debug information: "
                            + noDebugClass.getName();
                        if (missingDebugInfo.add(noDebugClass)) {
                            System.err.println(errorMessage);
                        }
                        missingLhsBaseType = true;
                        missingLhsBaseTypeMessage = errorMessage;
                        //throw new RuntimeException(errorMessage);
                    }
                }
            
                if (lhs instanceof Local &&
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

                    String xName = x.getName();

                    // TODO: missing lhs?
                    if (xType != null) {
                        SootField field = ((InstanceFieldRef) lhs).getField();
                        String tName = field.getDeclaringClass().getShortName();
                        if (!field.assignable()) {
                            // TODO: Check that message makes sense in the wild
                            String fName = field.getName();
                            SourceCause cause = new SourceCause(loc, xName + "." + fName + " = " + "CONSTANT;",
                                 xName + " is mutable, since " + tName + "." +  fName + " isn't assignable" );
                            cm.mutable3(x, xType, cause);
                        }
                    }

                } else if (lhs instanceof Local &&
                           rhs instanceof Local) {
                    // x = y : y? <: x?

                    Local x = (Local) lhs;
                    Local y = (Local) rhs;

                    JrType xType = x.getJrType();
                    JrType yType = y.getJrType();

                    String xName = x.getName();
                    String yName = y.getName();

                    // TODO: Check that message makes sense in the wild
                    SourceCause cause = new SourceCause(loc, xName + " = " + yName,
                        "\"" + yName + "\" must be mutable because \"" + xName + "\" is");
                
                    cm.subtype(y, yType,
                           x, xType, cause);

                } else if (lhs instanceof StaticFieldRef &&
                           rhs instanceof Local) {
                    // T.sf = y : y? <: sf

                    SootField sf = ((StaticFieldRef) lhs).getField();
                    Local y = (Local) rhs;

                    JrType sfType = sf.getJrType();
                    JrType yType = y.getJrType();

                    String tName = sf.getDeclaringClass().getShortName();
                    String sfName = sf.getName();
                    String yName = y.getName();

                    // TODO: Check that message makes sense in the wild
                    SourceCause cause = new SourceCause(loc, tName + "." + sfName + " = " + yName,
                        "\"" + yName + "\" must be mutable because \"" + sfName + "\" is");
                
                    cm.subtype(y, yType,
                           sf, sfType, cause);

                } else if (lhs instanceof Local &&
                           rhs instanceof StaticFieldRef) {
                    // x = T.sf : sf <: x?

                    Local x = (Local) lhs;
                    SootField sf = ((StaticFieldRef) rhs).getField();

                    JrType xType = x.getJrType();
                    JrType sfType = sf.getJrType();

                    String xName = x.getName();
                    String tName = sf.getDeclaringClass().getShortName();
                    String sfName = sf.getName();
                    
                    // TODO: Check that message makes sense in the wild
                    SourceCause cause = new SourceCause(loc, xName + " = " + tName + "." + sfName,
                        "\"" + sfName + "\" must be mutable because \"" + xName + "\" is");
                
                    cm.subtype(sf, sfType,
                            x,  xType, cause);


                } else if (lhs instanceof InstanceFieldRef &&
                           rhs instanceof Local) {
                    // x.f = y : (!assignable(f) => x?), (y? <: f)


                    Local x = (Local) ((InstanceFieldRef) lhs).getBase();
                    SootField f = ((InstanceFieldRef) lhs).getField();
                    Local y = (Local) rhs;

                    JrType xType = x.getJrType();
                    JrType fType = f.getJrType();
                    JrType yType = y.getJrType();

                    String xName = x.getName();
                    String fName = f.getName();
                    String tName = f.getDeclaringClass().getShortName();
                    String yName = y.getName();

                    String st = xName + "." + fName + " = " + yName + ";";

                    // TODO: missing lhs
                    // TODO: assert that if anyone is null, you don't have to do anything
                    if (xType != null) {
                        if (! f.assignable()) {
                            cm.mutable3(x, xType, new SourceCause(loc, st,
                                    "\"" + xName + "\" is mutable, since \"" + tName + "." + fName + "\" isn't assignable"));
                        }

                        cm.subtype2(null,  null, y, yType,
                                    x,    xType, f, fType, new SourceCause(loc, st,
                                    "\"" + yName + "\" is mutable if \"" + tName + "." + fName + "\" is"));
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

                    String xName = x.getName();
                    String yName = y.getName();
                    String tName = f.getDeclaringClass().getShortName();
                    String fName = f.getName();
                    
                    // TODO: Check that message makes sense in the wild
                    SourceCause cause = new SourceCause(loc, xName + " = " + yName + "." + fName,
                        "means that \"" + tName + "." + fName + "\" must be mutable because \"" + xName + "\" is, " +
                        " and if \"" + tName + "." + fName + "\" isn't mutable, then \"" + yName + "\" must be mutable because \"" + xName + "\" is.");
                
                    cm.subtype2(   y, yType, f, fType,
                        null,  null, x, xType, cause);


                } else if (lhs instanceof Local &&
                           rhs instanceof CastExpr) {
                    // x = (Type) y : y? < x?

                    if (lhs instanceof Local && ((CastExpr) rhs).getOp() instanceof Local) {
                		Local x = (Local) lhs;
                		Local y = (Local) ((CastExpr) rhs).getOp();

                		JrType xType = x.getJrType();
                		JrType yType = y.getJrType();

                        String xName = x.getName();
                        String yName = y.getName();
                        String tName = ((CastExpr) rhs).getCastType().toString();
                		
                        SourceCause cause = new SourceCause(loc, xName + " = (" + tName + ") " + yName, 
                            "\"" + yName + "\" must be mutable because \"" + xName + "\" is");
                        
                		cm.subtype(y, yType,
                				   x, xType, cause);
                    }
                } else if (lhs instanceof ArrayRef) {
                    // array a[x] = ...

                    Local a = (Local) ((ArrayRef) lhs).getBase();
                    ArrayType aType = (ArrayType) a.getJrType();
                    String aName = a.getName();

                    cm.mutable(a, aType, new SourceCause(loc, aName + "[...] = ...;",
                        "means that \"" + aName + "\" must be mutable"));

                    if (rhs instanceof Local) {
                        // array a[x] = y : y <: a [0 LOW] AND a

                        Local y = (Local) rhs;
                        JrType yType = y.getJrType();

                        String yName = y.getName();
                                                
                        cm.subtype(y, yType,
                            a, aType.getElemType().getLowerBound(), new SourceCause(loc, aName + "[...] = " + yName,
                            "means that \"" + yName + "\" must be mutable because the elements of \"" + aName + "\" are mutable."));
                    }
                } else if (lhs instanceof Local &&
                           rhs instanceof ArrayRef) {
                    // x = a[y] : a [0 UP] <: x

                    Local x = (Local) lhs;
                    Local a = (Local) ((ArrayRef) rhs).getBase();

                    JrType xType = x.getJrType();
                    ArrayType aType = (ArrayType) a.getJrType();

                    String xName = x.getName();
                    String aName = a.getName();
                
                    // TODO: Check that message makes sense in the wild
                    SourceCause cause = new SourceCause(loc, xName + " = " + aName + "[...];",
                        "The elements of \"" + aName + "\" must be mutable because \"" + xName + "\" is mutable.");
            
                    cm.subtype(a, aType.getElemType().getUpperBound(),
                       x, xType, cause);


                } else if (lhs instanceof Local &&
                           rhs instanceof InvokeExpr) { // method call
                    // Handled in default case below

                } else if (lhs instanceof Local &&
                           rhs instanceof NewExpr) {
                    // Do nothing, let default case handle the arguments.
                } else {
                    System.out.printf("ConstraintGenerationSwitch: WARNING, unhandled except by defaultCase:%n" +
                                      "  lhs(%s)=%s%n  rhs(%s)=%s%n",
                                      lhs.getClass(), lhs, rhs.getClass(), rhs);
                }

            } catch (Exception e) {
                throw new RuntimeException("\nException caused from " + stmt +
                                           "\n in " + enclosingMethod.getName() +
                                           "\n" + e, e);
            }

        }


        public void caseReturnStmt(ReturnStmt stmt) {
            // return x : x <: m_ret
            SourceLocation loc = new SourceLocation(enclosingMethod, stmt);
            defaultCase(stmt, loc);
            Value val = stmt.getOp();

            if (val instanceof Local) {
                Local x = (Local) val;
                SootMethod m = enclosingMethod;

                JrType xType = x.getJrType();
                JrType retType = m.getJrReturnType();

                // TODO: better cause here
                cm.subtype(x, xType, m, retType, new SourceCause(loc, "Mutable return value"));

            }
        }

        public void defaultCase(Object obj, SourceLocation loc) {
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

                    SourceCause cause = new SourceCause(loc, "Assignment of " + x.getName() + " to mutable result of " + m.getName());

                    // To fix polyread bug, here you don't want to just generally subtype
                    // m <: x, because this applies  x -> m (return value) for both
                    // contexts.  Instead, you want x -> m^mut.
                    cm.subtype2InvokeWithoutReadOnly(y,    yType, m, retType,
                                             null, null, x, xType, cause);
                    handleMethodInvk(methInvk, x, xType, loc);
                } else if (stmt.containsInvokeExpr()) {
                    // Method invocation expressions (where the return values are
                    // not assigned to a variable, such as void methods).
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    handleMethodInvk(invokeExpr, null, null, loc);
                }

            } catch (RuntimeException e) {
        	throw new RuntimeException(stmt.toString(), e);
            }
        }

    private void handleMethodInvk(InvokeExpr methInvk, Local x, JrType xType, SourceLocation loc) {
            // y.m(z) or m(z)

            SootMethod m = methInvk.getMethod();
        String name = m.getName();
            List<Value> zs = methInvk.getArgs();

            if (methInvk instanceof InstanceInvokeExpr &&
                (! (methInvk instanceof SpecialInvokeExpr &&
                name.equals("<init>")))) { // Don't operate on constructors

                InstanceInvokeExpr iMethInvk =
                    (InstanceInvokeExpr) methInvk;

                Local y = (Local) iMethInvk.getBase();
                JrType yType = y.getJrType();

                Param  thisParam = m.getReceiver();
                JrType thisParamType = thisParam.getJrType();

                SourceCause cause = new SourceCause(loc, "Receiver of call to " + name + " must be mutable");

                cm.subtype3(null, null,  y,         yType,
                            y,    yType, thisParam, thisParamType,
                        x,    xType, cause);
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


                    SourceCause cause = new SourceCause(loc, "Argument " + i + " of call to " + name + " must be mutable");

                    //                    cm.setSkipReadOnly(false);
                    cm.subtype3(null, null,  z,     zType,
                                y,    yType, param, paramType,
                                x,    xType, cause);
                    //                cm.setSkipReadOnly(true);

                }
            }

        }
    }
}
