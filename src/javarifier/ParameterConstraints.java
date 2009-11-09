package javarifier;

import soot.Scene;
import soot.SootMethod;
import soot.Body;
import soot.Local;

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

        if (! sm.isStatic() && (! sm.getName().equals("<init>"))) {
            Local thisLocal = body.getThisLocal();
            JrType thisLocalType = thisLocal.getJrType();

            Param receiver = sm.getReceiver();
            JrType receiverType = receiver.getJrType();

            cm.subtype2(null, null, receiver,  receiverType,
                        null, null, thisLocal, thisLocalType);

        }
        for (int i = 0; i < sm.getParameters().size(); i++) {
            cm.subtype2(null, null, sm.getParameter(i), sm.getParameter(i).getJrType(),
                        null, null, body.getParameterLocal(i), body.getParameterLocal(i).getJrType());
        }
    }
}
