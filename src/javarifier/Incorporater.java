package javarifier;

import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Body;
import soot.Local;

import javarifier.JrType.*;

import javarifier.util.MultiMap;
import javarifier.util.Pair;

import java.util.Set;

public class Incorporater extends SceneVisitor {

//     /** Incorporate the constraints */
//     public static void incorporate(Scene scene, MultiMap<Object, ConstraintVar> results) {
//         Incorporater incorporater = new Incorporater(results);
//         incorporater.visitScene(scene);
//         if (Main.debugConstraintIncorporating) {
//             System.out.println(ScenePrinter.print(scene));
//         }

//     }



//     private MultiMap<Object, ConstraintVar> results;

//     public Incorporater(MultiMap<Object, ConstraintVar> results) {
//         this.results = results;
//     }


//     public void visitClassSig(ClassSig sig) {
//         for (Pair<TypeParam, JrType> param : sig.getParams()) {
//             Set<ConstraintVar> constraints = results.get(new VarType(param.first(), sig));
//             param.second().incorporate(constraints, Mutability.MUTABLE);
//         }
//         super.visitClassSig(sig);
//     }

//     public void visitField(SootField field) {
//         Set<ConstraintVar> constraints = results.get(field);
//         if (field.isStatic()) {
//             field.getJrType().incorporate(constraints, Mutability.MUTABLE);
//         } else {
//             field.getJrType().incorporate(constraints, Mutability.THIS_MUTABLE);
//         }
//         super.visitField(field);
//     }

//     public void visitMethod(SootMethod meth) {
//         if (meth.getJrReturnType() != null) {
//             Set<ConstraintVar> constraints = results.get(meth);
//             meth.getJrReturnType().incorporate(constraints, Mutability.MUTABLE);
//         }
//         super.visitMethod(meth);
//     }

//     public void visitLocal(Local local) {
//         if (local.getJrType() != null) {
//             Set<ConstraintVar> constraints = results.get(local);
//             local.getJrType().incorporate(constraints, Mutability.MUTABLE);
//         }
//         super.visitLocal(local);
//     }

}
