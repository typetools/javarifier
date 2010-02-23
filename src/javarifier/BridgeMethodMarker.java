package javarifier;

import java.util.*;

import soot.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public class BridgeMethodMarker extends EmptyVisitor {
    SootClass clazz;
    
    BridgeMethodMarker(SootClass clazz) {
        this.clazz = clazz;
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ((access & Opcodes.ACC_BRIDGE) != 0) {
            SootMethod sm = clazz.getMethodByDescriptor(name, desc);
            sm.setBridge(true);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
    
    public static void mark(Scene s) {
        for (Object clazz1 : s.getClasses()) {
            SootClass clazz = (SootClass) clazz1;
            if (clazz.resolvingLevel() >= SootClass.SIGNATURES) {
                ClassReader cr = ASMClassReaders.v().readerFor(clazz.getName());
                cr.accept(new BridgeMethodMarker(clazz), true);
            }
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        clazz = Scene.v().getSootClass(name.replace('/','.'));
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
}
