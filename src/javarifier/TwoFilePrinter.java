package javarifier;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javarifier.JrType.MutType;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

/**
 * A <code>TwoFilePrinter</code> prints a scene in the format required by
 * Adam's group.
 */
public class TwoFilePrinter extends SceneVisitor {
    final Writer readOnlysOut;
    final Writer mutablesOut;
    
    public TwoFilePrinter(Writer readOnlysOut, Writer mutablesOut) {
        this.readOnlysOut = readOnlysOut;
        this.mutablesOut = mutablesOut;
    }

    @Override
    public void visitScene(Scene scene) {
        Collection<SootClass> classes = OutputFormat.collectClasses(scene);

        for (SootClass clazz : classes) {
            visitClass(clazz);
        }
    }

    @Override
    public void visitMethod(SootMethod meth) {
        String displaySig = meth.getSignature();
        displaySig = displaySig.substring(displaySig.lastIndexOf(' ') + 1,
                displaySig.length() - 1);
        displaySig = meth.getDeclaringClass().getName() + "." + displaySig;
        check(displaySig, meth, Mutability.READONLY, readOnlysOut);
        check(displaySig, meth, Mutability.MUTABLE, mutablesOut);
    }
    
    private void check(String line, SootMethod meth,
            Mutability mut, Writer out) {
        boolean gotOne = false;
        if (meth.getReceiver().getJrType() instanceof MutType)
        if (hasMut(meth.getReceiver(), mut)) {
            gotOne = true;
            line += " RC";
        }
        for (int i = 0; i < meth.getParameterCount(); i++) {
            if (hasMut(meth.getParameter(i), mut)) {
                gotOne = true;
                line += " " + i;
            }
        }
        if (gotOne) {
            try {
                out.write(line + "\n");
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private boolean hasMut(Param param, Mutability mut) {
        JrType type = param.getJrType();
        return type instanceof MutType && ((MutType) type).getMutability() == mut;
    }
}
