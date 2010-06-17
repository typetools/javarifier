package javarifier;

import soot.Scene;
import soot.SootMethod;
import soot.Body;
import soot.Local;

// Adds constraints which connect up the variables which represent function
// parameter recievers with the local variables associated.
// (the parameter must be a subtype of the local)
public class ParameterConstraints extends SceneVisitor {

    public static ConstraintManager constraints(Scene s) {
        ParameterConstraints pc = new ParameterConstraints();
        pc.visitScene(s);

        if (Options.v().dumpParamCons()) {
            System.out.println("Parameter Cons:");
            System.out.println(pc.getConstraints());
        }

        return pc.getConstraints();
    }


    private ConstraintManager cm = new ConstraintManager();

    public ConstraintManager getConstraints() {
        return cm;
    }


    public void visitBody(Body body) {
        SootMethod sm = body.getMethod();
        String name = sm.getName();

        if (! sm.isStatic() && (! sm.getName().equals("<init>"))) {
            Local thisLocal = body.getThisLocal();
            JrType thisLocalType = thisLocal.getJrType();

            Param receiver = sm.getReceiver();
            JrType receiverType = receiver.getJrType();


            SourceCause cause = new SourceCause(new SourceLocation(sm),
                name + ".this <: " + thisLocal.getName() + ",    where " +
                name + ".this indicates the type of the receiver");
            cm.subtype2(null, null, receiver,  receiverType,
                        null, null, thisLocal, thisLocalType, cause);
        }
        for (int i = 0; i < sm.getParameters().size(); i++) {
            Local paramLocal = body.getParameterLocal(i);
            JrType paramLocalType = paramLocal.getJrType();
            
            SourceCause cause = new SourceCause(new SourceLocation(sm),
                name+".args[" + i + "] <: " + paramLocal.getName() + ",    where " +
                name+".args[n] indicates the type of the parameter");
            cm.subtype2(null, null, sm.getParameter(i), sm.getParameter(i).getJrType(),
                        null, null, paramLocal, paramLocalType, cause);
        }
    }
}
