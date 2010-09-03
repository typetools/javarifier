package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Body;
import soot.SourceLocator.EntryKind;

import javarifier.JrType.*;

// Generates constraints from Stub classes, if passed a non-stub class,
// does nothing.
public class StubConstraints extends SceneLocationVisitor {

    public static ConstraintManager generate(Scene s) {
        StubConstraints g = new StubConstraints();
        g.visitScene(s);
        if (Main.dumpStubCons) {
            System.out.println("Stub Constraints");
            System.out.println(g.getConstraints());
        }
        return g.getConstraints();
    }


    private ConstraintManager cm = new ConstraintManager();
    public ConstraintManager getConstraints() {
        return cm;
    }

    public void visitClass(SootClass sc) {
      if (sc.entryKind() == null) {
        throw new RuntimeException("Unknown entry kind for class: " + sc);
      }
        if (sc.entryKind() == EntryKind.STUB) {
            super.visitClass(sc);
        }
    }

    public void visitJrTyped(JrTyped typed) {
        if (typed.getJrType() != null) {
            StubConstraintsHelper.generate(typed, cm, this.programLocation);
        }
    }

    public static class StubConstraintsHelper extends EmptyTypeVisitor {

        public static void generate(JrTyped typed, ConstraintManager cm, SourceLocation loc) {
          JrType type = typed.getJrType();
          if (type instanceof MutType) {
            MutType mutType = (MutType) type;
            if (mutType.getMutability().equals(Mutability.UNKNOWN)) {
              if (Main.debugStubs) {
                System.out.println("recognized unknown mutability constraint, converting to mutable: " + typed + " with type: " + typed.getJrType());
              }
              mutType.setMutability(Mutability.MUTABLE);
            }
          }
          typed.getJrType().accept(new StubConstraintsHelper(typed, cm, loc));
        }

        private JrTyped val;
        private ConstraintManager cm;
        private SourceLocation loc;

        public StubConstraintsHelper(JrTyped val, ConstraintManager cm, SourceLocation loc) {
            this.val = val;
            this.cm = cm;
            this.loc = loc;
        }

        public void visitClassType(ClassType type) {
            generateConstraints(type);
        }

        public void visitArrayType(ArrayType type) {
            generateConstraints(type);
        }

        private void generateConstraints(MutType type) {
            Mutability mut = type.getMutability();
            SourceCause cause = new SourceCause(loc, "Stub");
            switch (mut) {
            case MUTABLE :
            case THIS_MUTABLE :
                cm.mutable2(val, type, Context.READONLY, cause);
                cm.mutable2(val, type, Context.MUTABLE, cause);
                break;
            case POLYREAD :
                cm.mutable2(val, type, Context.MUTABLE, cause);
                break;
            case READONLY :
                break;
            default :
              // TODO: (?) :
                // throw new RuntimeException("StubConstraints: unknown MutType");
            }

        }
    }

}
