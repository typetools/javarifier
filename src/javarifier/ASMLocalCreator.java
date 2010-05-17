package javarifier;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.Method;

import annotations.el.*;
import annotations.util.coll.*;

import soot.SootMethod;


import java.util.*;

public class ASMLocalCreator extends EmptyVisitor {

    private Map<Method, KeyedSet<LocalLocation, ASMLocal>> asmLocals
        = new HashMap<Method, KeyedSet<LocalLocation, ASMLocal>>();

    /** The current method being visited. */
    private Method currentMethod = null;
    private int currentPosition = -1;
    private Map<Label, Integer> labelPositions = null;

    @Override
    public MethodVisitor visitMethod(
        int access,
        String name,
        String desc,
        String signature,
        String[] exceptions) {

        currentMethod = new Method(name, desc);
        labelPositions = new HashMap<Label, Integer>();
        return this;
    }

    @Override
    public void visitCurrentPosition(int position) {
        currentPosition = position;
    }
    @Override
    public void visitLabel(Label label) {
        labelPositions.put(label, currentPosition);
    }

    /**
     * visits a local variable declaration.
     *
     * @param name the name of a local variable.
     * @param desc the type descriptor of this local variable.
     * @param signature the type signature of this local variable. May be
     *        <tt>null</tt> if the local variable type does not use generic
     *        types.
     * @param start the first instruction corresponding to the scope of this
     *        local variable (inclusive).
     * @param end the last instruction corresponding to the scope of this local
     *        variable (exclusive).
     * @param index the local variable's index.
     * @throws IllegalArgumentException if one of the labels has not already
     *         been visited by this visitor (by the
     *         {@link #visitLabel visitLabel} method).
     */
    public void visitLocalVariable(
        String name,
        String desc,
        String signature,
        Label start,
        Label end,
        int index){
        add(currentMethod, new ASMLocal(name, desc, signature, labelPositions.get(start), labelPositions.get(end), index));
    }

    private KeyedSet<LocalLocation, ASMLocal> newLocalSet() {
        return new LinkedHashKeyedSet<LocalLocation, ASMLocal>(
                    new Keyer<LocalLocation, ASMLocal>() {
                        public LocalLocation getKeyFor(ASMLocal v) {
                            return new LocalLocation(v.getIndex(), v.getStart(), v.getEnd() - v.getStart());
                        }
                    });
    }

    private void add(Method meth, ASMLocal loc) {
        KeyedSet<LocalLocation, ASMLocal> localList = asmLocals.get(meth);
        if (localList == null) {
            localList = newLocalSet();
            asmLocals.put(meth, localList);
        }
        localList.add(loc);
    }


    public KeyedSet<LocalLocation, ASMLocal> getASMLocals(Method meth) {
        if (asmLocals.get(meth) == null) {
            return newLocalSet();
        } else {
            KeyedSet<LocalLocation, ASMLocal> tmp = newLocalSet();
            tmp.addAll(asmLocals.get(meth));
            return tmp;
        }
    }


    public KeyedSet<LocalLocation, ASMLocal> getASMLocals(SootMethod sootMethod) {
        Method meth = convertFromSootMethod(sootMethod);
        return getASMLocals(meth);
    }

    private static Method convertFromSootMethod(SootMethod sootMethod) {
        String methodName = sootMethod.getName();

        // getBytecodeSignature returns something in the form:
        // <ClassName: MethodName(Ljava.lang.ParamType;)Ljava.util.ReturnType;>
        //
        // But we are only interested in the part:
        // (Ljava.lang.ParamType;)Ljava.util.ReturnType;
        String md = sootMethod.getBytecodeSignature();
        md = md.substring(md.indexOf("("), md.length() - 1);

        Method meth = new Method(methodName, md);
        return meth;
    }

    public ASMLocalCreator(String className) {
        ASMClassReaders.v().readerFor(className).accept(this, false);
    }
}
