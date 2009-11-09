package javarifier;

import annotations.el.*;
import annotations.util.coll.*;

import soot.*;
import soot.Local;
import soot.AbstractJasminClass;
import soot.tagkit.SignatureTag;

import javarifier.JrType.*;

import java.util.*;


public class TypeInitializer extends SceneVisitor {

    public static void loadSignatures(Scene s) {
        (new TypeInitializer()).visitScene(s);
    }
    public static void loadSignature(SootClass sc) {
      (new TypeInitializer()).visitClass(sc);
    }
    private static boolean hasRun = false;
    public static boolean hasRun() { return hasRun; }


    public void visitScene(Scene s) {
    	// Must process classes from outermost to innermost
    	// for ClassSigature adding code to work.
    	List<SootClass> orderedClasses = new ArrayList<SootClass>();
    	List<SootClass> worklist = new ArrayList<SootClass>(s.getClasses());
      List<SootClass> toRemove = new ArrayList<SootClass>();
//    	while (worklist.size() != 0) {
//    		for (int i = 0; i < worklist.size(); i++) {
//				SootClass clazz = worklist.get(i);
//
//				if (clazz.resolvingLevel() < SootClass.HIERARCHY) {
//					worklist.remove(i);
//					i--;
//					continue;
//				}
//
//				if (clazz.hasOuterClass()
//						&& !orderedClasses.contains(clazz.getOuterClass())) {
//					continue;
//				}
//				orderedClasses.add(clazz);
//				worklist.remove(i);
//				i--;
//			}
//    	}
      while(worklist.size() != 0) {
        toRemove.clear();
        for (SootClass c : worklist) {
          if (c.resolvingLevel() < SootClass.HIERARCHY) {
            toRemove.add(c);
          } else {
            if (c.hasOuterClass() && !orderedClasses.contains(c.getOuterClass())) {
              continue;
            }
            orderedClasses.add(c);
            toRemove.add(c);
          }
        }

        worklist.removeAll(toRemove);
      }

    	for (SootClass clazz : orderedClasses) {
    		visitClass(clazz);
    	}
        hasRun = true;
    }

    @Override
    public void visitClass(SootClass sc) {
        if (Options.v().debugResolver()) {
            System.out.println("Resolving signatures: " + sc);
        }

        // Add class signature to class
        SignatureTag classSigTag = (SignatureTag) sc.getTag("SignatureTag");
        String classSig = null;
        if (classSigTag != null) {
            classSig = classSigTag.getSignature();
        }
        ClassSig classSignature = JVMLSigParser.parseClassSig(sc, classSig);
        classSignature.setOwner(sc); // do this _before_ adding outer params
        if (sc.getOuterMethod() != null) {
            classSignature.add(sc.getOuterMethod().getSig());
        }
        if (sc.hasOuterClass() && ! sc.isStatic()) {
        	// works b/c classes are orded outermost to innermost.  No recursion is needed for outer-outer classes.
        	classSignature.add(sc.getOuterClass().getSig());
        }
        sc.setSig(classSignature);

        asmLocalCreator = null;
        super.visitClass(sc);
    }

    @Override
    public void visitField(SootField field) {
        // Add JrType to fields
        SignatureTag sTag = (SignatureTag) field.getTag("SignatureTag");
        if (sTag == null) {
            field.setJrType(JVMLSigParser.parseJrType(AbstractJasminClass.jasminDescriptorOf(field.getType())));
        } else {
            field.setJrType(JVMLSigParser.parseJrType(sTag.getSignature()));
        }
        // HMMM This won't work if the outer reference isn't called "this$0"
        if (field.getName().equals("this$0") && field.getDeclaringClass().hasOuterClass()) {
        	field.setJrType(field.getDeclaringClass().getOuterClass().getSig().getThisType().copy());
        }
    }

    ASMLocalCreator asmLocalCreator;

    @Override
    public void visitMethod(SootMethod method) {
        // Add method signatures to methods
        SignatureTag sTag = (SignatureTag) method.getTag("SignatureTag");
        String sig = null;
        if (sTag == null) {

            // getBytecodeSignature returns something in the form:
            // <ClassName: MethodName(Ljava.lang.ParamType;)Ljava.util.ReturnType;>
            //
            // But we are only interested in the part:
            // (Ljava.lang.ParamType;)Ljava.util.ReturnType;
            sig = method.getBytecodeSignature();
            sig = sig.substring(sig.indexOf('('), sig.length()-1);


        } else {
        	sig = sTag.getSignature();
        }


        // Instead of always creating a new method signature, check to see
        // if the signature has already been set, and if so, use that one.
        MethodSig methSig = null;
        if (method.hasMethodSigSet()) {
          methSig = method.getSig();
        } else {
          methSig = JVMLSigParser.parseMethodSig(sig, method.getDeclaringClass().getSig().getThisType().copy());
        }
        methSig.setOwner(method);

        if (method.getName().equals("<init>") &&
        	method.getDeclaringClass().hasOuterClass() &&
        	! method.getDeclaringClass().isStatic()) {
        	// Inner class constructors have the form:
        	// <init>(LOuterClass;LOtherArguments;)
        	// The problem is that LOuterClass; is always raw.
        	// We must add in the correct type arguments, which are just the type parameters of the outerclass.

          ClassType outerClassType = null;
          JrType jrtype = null;
          try {
            jrtype = methSig.getParams().get(0);
            if (jrtype instanceof ClassType) {
              outerClassType = (ClassType) jrtype; // The first argument is the outerclass reference
            }
          } catch(Exception e) {
            throw new RuntimeException(
                "\n TypeInitializer.visitMethod: methSig.getParam() returned something other than ClassType: "
                + "\n   from method: " + method.getName() + " with signature: " + methSig.toString()
                + "\n   first parameter has "
                + "class: " + jrtype.getClass().toString() + " value: " + jrtype.toString(), e);
          }
          if (outerClassType != null) {
                ClassType fixedOuterRef = method.getDeclaringClass().getOuterClass().getSig().getThisType().copy();
        	if (! outerClassType.getBaseType().equals(fixedOuterRef.getBaseType())) {
            // jaime: don't throw exception, silently ignore
//            System.out.println("\n TypeInitializer.visitMethod: problem with class: " + jaime_sc);
//        		throw new RuntimeException("\n TypeInitializer.visitMethod: mismatch between class types: "
//                + "\n    outer class type: " + outerClassType
//                + "\n    fixedOuterRef: " + fixedOuterRef);
          }
        	methSig.getParams().set(0, fixedOuterRef);

          }
        }
        if (method.getName().equals("<init>") &&
                method.getDeclaringClass().hasSuperclass() &&
                method.getDeclaringClass().getSuperclass().getName().equals("java.lang.Enum") &&
                methSig.getParams().size() == method.getParameterCount() - 2) {
            // Enum constant constructors have the form:
            // <init>(String name, int ordinal, user-specified arguments)
            // The extended compiler produces a corrupt signature that contains
            // only the user-specified arguments.  Fix the signature.
            methSig.getParams().add(0,
                    new ClassType(Mutability.UNKNOWN, "Ljava/lang/String;",
                            Collections.<TypeArg>emptyList()));
            methSig.getParams().add(1, new JrType.PrimType());
        }

        method.setSig(methSig);
        super.visitMethod(method);
    }

    @Override
    public void visitBody(Body body) {
        SootMethod sm = body.getMethod();
        if (asmLocalCreator == null)
            asmLocalCreator = new ASMLocalCreator(sm.getDeclaringClass().getName());
        KeyedSet<LocalLocation, ASMLocal> asmLocals = asmLocalCreator.getASMLocals(sm);
        for (Local loc : (Collection<Local>) body.getLocals()) {
            if (loc.getName().equals("this")) {
                // InnerClassTest$1.class contained an incorrect signature for
                // the "this" local of the constructor: it was missing the type
                // arguments.  Thus, never trust signatures for "this" from the
                // class file.  It's easy to figure out what the signature
                // ought to be, anyway.
                loc.setJrType(sm.getDeclaringClass().getSig().getThisType().copy());
            } else if (loc.isSourceLocal()) {
                ASMLocal al = asmLocals.lookup(new LocalLocation(
                        loc.getSlotIndex(), loc.getStart_pc(), loc.getLength()));
                if (al != null)
                    loc.setJrType(al.getJrType());
            }
        }
    }

    // Don't process the signature here because:
    // (1) we already did what we need to do in visitClass
    // (2) SceneVisitor will try to recurse into bounds and will crash
    //     because OwnerAdder has not run yet
    @Override
    public void visitClassSig(ClassSig cs) {
    }

    // Don't process the signature here because:
    // (1) we already did what we need to do in visitMethod
    // (2) SceneVisitor will try to recurse into bounds and will crash
    //     because OwnerAdder has not run yet
    @Override
    public void visitMethodSig(MethodSig ms) {
    }


}
