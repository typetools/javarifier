package javarifier;

import soot.SootMethod;

public class Param implements JrTyped {

  // TODO: back to private
    public SootMethod meth;
    private JrType type;
    private int index;

    public Param(SootMethod meth, JrType type, int index) {
      //if(meth.toString().contains("Vector") && meth.toString().contains("removeElement(")) {
      //  System.out.println("forming bad param");
      //}
        this.meth = meth;
        this.type = type;
        this.index = index;
    }

    public JrType getJrType() {
        return type;
    }

    public String toString() {
        return meth + "." + index;
    }
}
