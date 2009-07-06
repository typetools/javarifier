package javarifier;

import java.util.AbstractMap;
import java.util.List;

import javarifier.JrType.VarType;
import javarifier.util.Pair;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

/**
 * Visits each VarType setting its owner, that is, which class or
 * method defined the VarType's type parameter.  For example,
 *
 * Class bar&lt;S&gt; {
 *  &lt;T&gt; foo(T x) {
 *    S y;
 *   }
 * }
 *
 *  The owner of the type of x is foo and the owner of type of y is bar.
 */
public class OwnerAdder extends SceneVisitor {

    public static void addOwners(Scene s) {
        (new OwnerAdder()).visitScene(s);
    }

    private SootClass enclosingClass;
    private SootMethod enclosingMethod;


    public void visitClass(SootClass sc) {
      // TODO: In the case of doubly-nested classes, i.e. AbstractMap$2$1, you don't
      // want to override AbstractMap$2 with AbstractMap$2$1.  Instead, keep
      // a list of enclosing classes.
        enclosingClass = sc;
        super.visitClass(sc);
        enclosingClass = null;
    }

    public void visitMethod(SootMethod meth) {
        enclosingMethod = meth;
        super.visitMethod(meth);
        enclosingMethod = null;
    }

    // Owners of type parameters in signatures were set when the signatures
    // were created.  If we were to set them here, we might incorrectly make an
    // inner class the owner of a type parameter aliased from its outer class.
    // But we do need to visit bounds of type parameters.
    @Override
    public void visitTypeParam(Pair<VarType, JrType> param) {
        visitType(param.second());   // Here param.second is the VarType's direct bound.
    }

    public void visitType(JrType t) {
        if (t != null) {
            t.accept(new OwnerAdderHelper());
        }
        super.visitType(t);
    }

    private class OwnerAdderHelper extends EmptyTypeVisitor {
        
      @Override
    	public void visitVarType(VarType t) {
        SootClass originalEnclosingClass = enclosingClass;
        SootMethod originalEnclosingMethod = enclosingMethod;
        try {
          
          // Every loop either returns, throws exception, or walks further up
          // the class hierarchy, so you will always get termination.
          while(true) {
                VarType param;
                if (enclosingMethod != null) {
                    MethodSig enclosingMethodSig = enclosingMethod.getSig();
                    if(enclosingMethodSig == null) {
                      throw new RuntimeException("OwnerAdderHelper.visitVarType: enclosing method has null sig: " + enclosingMethod);
                    }
                    param = enclosingMethodSig.maybeGetByName(t.getTypeParam());
                    if (param != null) {
                        t.setOwner(param.getOwner());
                        return;
                    }
                }
                // We can't just make the enclosing class the owner because the
                // type parameter may have been aliased into it from an outer
                // class.
                if(enclosingClass == null) {
                  throw new RuntimeException("OwnerAdderHelper.visitVarType: enclosingClass is null");
                }
                ClassSig enclosingClassSig = enclosingClass.getSig();
                if(enclosingClassSig == null) {
                  throw new RuntimeException("OwnerAdderHelper.visitVarType: enclosing class has null sig: " + enclosingClassSig);
                }
                param = enclosingClassSig.maybeGetByName(t.getTypeParam());
                if (param != null) {
                    t.setOwner(param.getOwner());
                    return;
                }
                
                // In case of doubly-nested classes, i.e. AbstractMap$2$1, the
                // class AbstractMap$2$1 won't have type parameters, but the class
                // AbstractMap will.  Need to keep checking up the chain of classes:
                // use SootClass.outerClass
                if(enclosingClass != null) {
                  //System.out.println("Failed with: " + enclosingClass);
                  //System.out.println("Trying: " + enclosingClass.getOuterClass());
                  enclosingClass = enclosingClass.getOuterClass();
                  enclosingMethod = enclosingClass.getOuterMethod();
                  continue;
                }
                
               throw new RuntimeException(
                   "OwnerAdderHelper.visitVarType: Could not find owner for: "
                   + t.getTypeParam() + "\n in enclosingClass: "
                   + enclosingClass + " or enclosingMethod: " 
                   + ((enclosingMethod == null) ? "null" : enclosingMethod));
          }
        } catch(NullPointerException e) {
          throw new RuntimeException("OwnerAdderHelper.visitVarType: " +
          		"caught null pointer exception: " + e.getMessage(), e);
        } finally {
          enclosingClass = originalEnclosingClass;
          enclosingMethod = originalEnclosingMethod;
        }
        }
    }

}
