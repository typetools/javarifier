package javarifier;

import java.util.ArrayList;
import java.util.List;

import soot.AbstractJasminClass;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator.EntryKind;

/**
 * For every class with stub entry kind and resolved to signatures,
 * removes private fields and methods from AnnotationScene.v() and from
 * SootClass.
 */
public class StubCleaner extends SceneVisitor {
    public static void cleanStubs(Scene scene) {
        (new StubCleaner()).visitScene(scene);
    }

    @Override
    public void visitClass(SootClass clazz) {
        try {
            // Only process fields and methods for classes resolved to signatures
            if (clazz.entryKind() == EntryKind.STUB
                    && clazz.resolvingLevel() >= SootClass.SIGNATURES) {
                List<SootField> fields1 = new ArrayList<SootField>(clazz.getFields());
                for (SootField f : fields1) {
                    visitField(f);
                }
                List<SootMethod> methods1 = new ArrayList<SootMethod>(clazz.getMethods());
                for (SootMethod meth : methods1) {
                    visitMethod(meth);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Class: " + clazz, e);
        }
    }

    @Override
    public void visitField(SootField field) {
        if (field.isPrivate()) {
            AnnotationScene.v().scene().classes
                .vivify(field.getDeclaringClass().getName())
                .fields.remove(field.getName());
            field.getDeclaringClass().removeField(field);
        }
    }

    @Override
    public void visitMethod(SootMethod meth) {
        if (meth.isPrivate() || meth.getName().startsWith("access$")) {
            String methodSig = AbstractJasminClass.jasminDescriptorOf(meth.makeRef());
            String methodKey = meth.getName() + methodSig;
            AnnotationScene.v().scene().classes
                .vivify(meth.getDeclaringClass().getName())
                .methods.remove(methodKey);
            meth.getDeclaringClass().removeMethod(meth);
        }
    }

}
