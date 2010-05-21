package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator.EntryKind;

import java.util.*;

public class SubtypeConstraints extends SceneVisitor {
    
    public void visitClass(SootClass sc) {
        if (sc.entryKind() == EntryKind.PROGRAM)
            super.visitClass(sc);
    }

    public static ConstraintManager generate(Scene s) {
        SubtypeConstraints generator = new SubtypeConstraints(s);
        generator.visitScene(s);
        if (Options.v().dumpSubtypeCons()) {
            System.out.println("Subtype Constraints");
            System.out.println(generator.getConstraints());
        }
        return generator.getConstraints();
    }

    private ConstraintManager cm = new ConstraintManager();
    public ConstraintManager getConstraints() {
        return cm;
    }

    private Scene s;

    public SubtypeConstraints(Scene s) {
        this.s = s;
    }

    public void visitMethod(SootMethod meth) {
        if (! (meth.getName().equals("<init>") || meth.getName().equals("<clinit>"))) {
            Set<SootMethod> subtypers =
                s.getOrMakeFastHierarchy().resolveAbstractDispatch(meth.getDeclaringClass(), meth);
            for (SootMethod subtyper : subtypers) {
                if (subtyper.equals(meth)) continue;
                subtype(subtyper, meth);
            }
        }
        super.visitMethod(meth);
    }

    // mSub <: mSuper
    // Each parameter must have identical type
    // Return type of mSub must be a subtype of mSuper
    public void subtype(SootMethod mSub, SootMethod mSuper) {
        SourceLocation loc = new SourceLocation(mSub);

        String subName = mSub.getDeclaringClass().getName();
        String superName = mSuper.getDeclaringClass().getName();

        String fstr = "function " + mSub.getName() + " in " + subName + " <: " + superName;
    
        Param r1 = mSub.getReceiver();
        Param r2 = mSuper.getReceiver();
        // TODO: uncomment in order to make subtype method a forced subtype
        // (mutable if super type is mutable) of supertype method, and thus
        // have methods be invariant in their parameters.
        // cm.subtype(r1, r1.getJrType(),
        //            r2, r2.getJrType());
        //TODO: Check that the explanation makes sense
        cm.subtype(r2, r2.getJrType(),
                   r1, r1.getJrType(),
          new SourceCause(loc, fstr, "Covariant return types"));

        for (int i = 0; i < mSub.getParameters().size(); i++) {
            Param p1 = mSub.getParameters().get(i);
            Param p2 = mSuper.getParameters().get(i);
            // TODO: uncomment in order to make subtype method a forced subtype
            // (mutable if super type is mutable) of supertype method, and thus
            // have methods be invariant in their parameters.
            // cm.subtype(p1, p1.getJrType(),
            //           p2, p2.getJrType());
            //TODO: Check that the explanation makes sense
            cm.subtype(p2, p2.getJrType(),
                       p1, p1.getJrType(),
              new SourceCause(loc, "parameter " + Integer.toString(i) + " of " + fstr, "Contravariant parameter types"));
        }

        
        cm.subtype(mSub, mSub.getJrType(),
                   mSuper, mSuper.getJrType(),
          new SourceCause(loc, fstr, "this-mutability subtype"));
    }

}
