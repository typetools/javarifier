package javarifier;

import javarifier.JrType.ClassType;
import soot.Scene;
import soot.SootField;

public class RawTypeWildcardifier extends SceneVisitor {
    public static void wildcardifyRawTypes(Scene scene) {
        (new RawTypeWildcardifier()).visitScene(scene);
    }

    @Override
    public void visitField(SootField field) {
        super.visitField(field);
    }

    @Override
    public void visitType(JrType type) {
        type.accept(new RTWHelper());
    }

    private static class RTWHelper extends EmptyTypeVisitor {
        RTWHelper() {}

        @Override
        public void visitClassType(ClassType t) {
            t.wildcardifyRawType();
            super.visitClassType(t);
        }
    }
}
