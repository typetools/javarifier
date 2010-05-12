package javarifier;

import soot.Scene;
import soot.SootMethod;

import javarifier.JrType.*;

/**
 * This class provides a static method
 * {@link UnneededPolyreadRemover#remove(Scene)} that removes
 * unneeded polyread's from method
 * return types. If a method has a polyread return type, but none
 * of the method's parameters or receiver are polyread, then that polyread is
 * useless.
 *
 * For example, polyread Date foo(readonly Date, mutable Date);
 *
 * Is translated to: mutable Date foo(readonly Date, mutable Date);
 */
public class UnneededPolyreadRemover extends SceneVisitor {

  /**
   * Visits all classes in the given scene and removes all
   * unnecessary instances of polyread.
   */
  public static void remove(Scene s) {
    (new UnneededPolyreadRemover()).visitScene(s);
  }

  /**
   * Visits the method and removes all unnecessary instances of polyread.
   */
  @Override
  public void visitMethod(SootMethod meth) {
    // If return type is not polyread, there is nothing to remove.
    if (! containsPolyread(meth.getJrType())) {
      // meth.getJrType() is return type
      return;
    }

    // If the receiver or any parameter is polyread, there is nothing left to
    //  do.
    if (!meth.isStatic()) {
      if (containsPolyread(meth.getReceiver().getJrType())) {
        return;
      }
    }
    for (Param p : meth.getParameters()) {
      if (containsPolyread(p.getJrType())) {
        return;
      }
    }

    // The return type contains polyread but the all parameters
    // (including receiver) do not.  Must remove this
    // extra unneeded polyread (including any inner types).
    // meth.getJrType() is return type
    removePolyreads(meth.getJrType());
  }

  /**
   * Returns true iff the given type contains any polyread mutability.
   */
  private static boolean containsPolyread(JrType type) {
    PolyreadSeeker seeker = new PolyreadSeeker();
    type.accept(seeker);
    return seeker.containsPolyread();
  }

  /**
   * This class visits a type and records whether there were any
   * polyread mutabilities.
   */
  private static class PolyreadSeeker extends EmptyTypeVisitor {

    private boolean containsPolyread = false;
    /**
     * Returns true if there was any polyread mutability found in the
     * visited type.
     */
    public boolean containsPolyread() {
      return containsPolyread;
    }

    @Override
    public void visitClassType(ClassType type) {
      if (type.getMutability().equals(Mutability.POLYREAD)) {
        containsPolyread = true;
      }
      super.visitClassType(type);
    }

    @Override
    public void visitArrayType(ArrayType type) {
      if (type.getMutability().equals(Mutability.POLYREAD)) {
        containsPolyread = true;
      }
      super.visitArrayType(type);
    }
  }

  /**
   * Removes all unneeded polyreads from the given type.
   */
  private static void removePolyreads(JrType type) {
    type.accept(new PolyreadRemover());
  }

  /**
   * This class visits a type and changes every polyread
   * mutability to mutable.
   */
  private static class PolyreadRemover extends EmptyTypeVisitor {

    public void visitClassType(ClassType type) {
      if (type.getMutability().equals(Mutability.POLYREAD)) {
        type.setMutability(Mutability.MUTABLE);
      }
      super.visitClassType(type);
    }

    public void visitArrayType(ArrayType type) {
      if (type.getMutability().equals(Mutability.POLYREAD)) {
        type.setMutability(Mutability.MUTABLE);
      }
      super.visitArrayType(type);
    }
  }
}
