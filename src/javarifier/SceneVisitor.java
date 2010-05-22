package javarifier;

/**
 * SceneVisitor defines a default visitor class that will visit every
 * program element in a Scene.  Overriding classes can simply choose to perform
 * a particular action for each visited program element, or may choose to
 * visit only a subset of some program elements, since typically, only
 * visitScene(Scene) is called directly.
 */
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Body;
import soot.Local;

import soot.SourceLocator.EntryKind;
import soot.util.Chain;

import javarifier.JrType.*;
import javarifier.util.Pair;

import java.util.*;

public class SceneVisitor {

    public void visitScene(Scene scene) {
        Chain classes = scene.getClasses();

        for (SootClass clazz : (Collection<SootClass>) classes) {
            if (clazz.resolvingLevel() >= SootClass.HIERARCHY)
                visitClass(clazz);
        }
    }


    public void visitClass(SootClass clazz) {
        try {
            visitClassSig(clazz.getSig());
            // Only process fields and methods for classes resolved to signatures
            if (clazz.resolvingLevel() >= SootClass.SIGNATURES) {
                for (SootField f : (Collection<SootField>) clazz.getFields()) {
                  try {
                    visitField(f);
                  } catch (Exception e) {
                    throw e;
                  }
                }
                for (SootMethod meth : (Collection<SootMethod>) clazz.getMethods()) {
                    visitMethod(meth);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SceneVisitor.visitClass: Class: " + clazz + ": " + e.getMessage(), e);
        }
    }

    public void visitClassSig(ClassSig sig) {
        if (sig != null) {
            for (Pair<VarType, JrType> param : sig.getTypeParams()) {
                visitTypeParam(param);
            }
            visitThisType(sig.getThisType());
            for (ClassType templateST : sig.getSuperTypes())
                visitType(templateST);
        }
    }

    public void visitThisType(JrType type) {
        visitType(type);
    }

    public void visitTypeParam(Pair<VarType, JrType> param) {
        visitType(param.first());
        visitJrTyped(param.first()); // Here the VarType's type is its transitive bound.
        visitType(param.second());   // Here param.second is the VarType's direct bound.
    }

    public void visitField(SootField f) {
        visitJrTyped(f);
    }

    public void visitMethod(SootMethod meth) {
        try {

            visitMethodSig(meth.getSig());

            if (!meth.isStatic())
                visitJrTyped(meth.getReceiver());
            for (Param p : meth.getParameters()) {
                visitJrTyped(p);
            }
            visitJrTyped(meth);

            if (!meth.isBridge() && meth.getBody() != null) {
                visitBody(meth.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Method: " + meth, e);
        }
    }

    public void visitMethodSig(MethodSig sig) {
        for (Pair<VarType, JrType> tParam : sig.getTypeParams()) {
            visitTypeParam(tParam);
        }
    }

    public void visitBody(Body b) {
        for (Local l : (Collection<Local>) b.getLocals()) {
            visitLocal(l);
        }
    }

    public void visitLocal(Local l) {
            visitJrTyped(l);
    }

    public void visitJrTyped(JrTyped typed) {
        if (typed.getJrType() != null) {
            visitType(typed.getJrType());
        }
    }

    public void visitType(JrType type) {}

}
