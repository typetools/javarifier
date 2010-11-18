/* Soot - a J*va Optimization Framework
 * Copyright (C) 2000 Patrice Pominville
 * Copyright (C) 2004 Ondrej Lhotak, Ganesh Sittampalam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


package soot;
import soot.javaToJimple.IInitialResolver.Dependencies;
import soot.options.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import polyglot.util.StdErrorQueue;

import soot.JastAddJ.ASTNode;
import soot.JastAddJ.BytecodeParser;
import soot.JastAddJ.CompilationUnit;
import soot.JastAddJ.JavaParser;
import soot.JastAddJ.JastAddJavaParser;
import soot.JastAddJ.Program;

// Begin javarifier changes
import javarifier.ClassSig;
import javarifier.EmptyTypeVisitor;
import javarifier.JVMLSigParser;
import javarifier.JrType;
import javarifier.MethodSig;
import javarifier.Mutability;
import javarifier.SceneVisitor;
import javarifier.JrType.ClassType;
import javarifier.JrType.TypeArg;
import javarifier.JrType.VarType;
import javarifier.util.Pair;
import soot.SourceLocator.EntryKind;
import soot.options.Options;
import soot.tagkit.SignatureTag;
import soot.util.Chain;
// End javarifier changes

/** Loads symbols for SootClasses from either class files or jimple files. */
public class SootResolver 
{
    /** Maps each resolved class to a list of all references in it. */
    private final Map<SootClass, ArrayList> classToTypesSignature = new HashMap<SootClass, ArrayList>();

    /** Maps each resolved class to a list of all references in it. */
    private final Map<SootClass, ArrayList> classToTypesHierarchy = new HashMap<SootClass, ArrayList>();

    /** SootClasses waiting to be resolved. */
    private final LinkedList/*SootClass*/[] worklist = new LinkedList[4];

	protected Program program;

    public SootResolver (Singletons.Global g) {
        worklist[SootClass.HIERARCHY] = new LinkedList();
        worklist[SootClass.SIGNATURES] = new LinkedList();
        worklist[SootClass.BODIES] = new LinkedList();
        
        
        program = new Program();
	program.state().reset();

        program.initBytecodeReader(new BytecodeParser());
        program.initJavaParser(
          new JavaParser() {
            public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
              return new JastAddJavaParser().parse(is, fileName);
            }
          }
        );

        program.options().initOptions();
        program.options().addKeyValueOption("-classpath");
        program.options().setValueForOption(Scene.v().getSootClassPath(), "-classpath");
	if(Options.v().src_prec() == Options.src_prec_java)
        	program.setSrcPrec(Program.SRC_PREC_JAVA);
        else if(Options.v().src_prec() == Options.src_prec_class)
        	program.setSrcPrec(Program.SRC_PREC_CLASS);
        else if(Options.v().src_prec() == Options.src_prec_only_class)
        	program.setSrcPrec(Program.SRC_PREC_CLASS);
        program.initPaths();
    }

    public static SootResolver v() { return G.v().soot_SootResolver();}
    
    /** Returns true if we are resolving all class refs recursively. */
    private boolean resolveEverything() {
        return( Options.v().whole_program() || Options.v().whole_shimple()
	|| Options.v().full_resolver() 
	|| Options.v().output_format() == Options.output_format_dava );
    }

    /** Returns a (possibly not yet resolved) SootClass to be used in references
     * to a class. If/when the class is resolved, it will be resolved into this
     * SootClass.
     * */
    public SootClass makeClassRef(String className)
    {
        if(Scene.v().containsClass(className))
            return Scene.v().getSootClass(className);

        SootClass newClass;
        newClass = new SootClass(className);
        newClass.setResolvingLevel(SootClass.DANGLING);
        Scene.v().addClass(newClass);

        return newClass;
    }


    /**
     * Resolves the given class. Depending on the resolver settings, may
     * decide to resolve other classes as well. If the class has already
     * been resolved, just returns the class that was already resolved.
     * */
    public SootClass resolveClass(String className, int desiredLevel) {
        SootClass resolvedClass = makeClassRef(className);
        addToResolveWorklist(resolvedClass, desiredLevel);
        processResolveWorklist();
        return resolvedClass;
    }

    /** Resolve all classes on toResolveWorklist. */
    private void processResolveWorklist() {
        for( int i = SootClass.BODIES; i >= SootClass.HIERARCHY; i-- ) {
            while( !worklist[i].isEmpty() ) {
                SootClass sc = (SootClass) worklist[i].removeFirst();
                // Begin javarifier changes
                if (javarifier.Main.debugResolve) {
                    System.out.println("debugResolve>> processResolveWorklist " + sc + " level " + i);
                }
                // End javarifier changes
                if( resolveEverything() ) {
                    // Begin javarifier changes
                    if (i >= SootClass.SIGNATURES) {
                        bringToSignatures(sc);
                        if ( !sc.isPhantom() &&
                                sc.entryKind() == EntryKind.PROGRAM )
                            bringToBodies(sc);
                    } else
                        bringToHierarchy(sc);
                    // End javarifier changes
                } else {
                    switch(i) {
                        case SootClass.BODIES: bringToBodies(sc); break;
                        case SootClass.SIGNATURES: bringToSignatures(sc); break;
                        case SootClass.HIERARCHY: bringToHierarchy(sc); break;
                    }
                }
            }
        }
    }

    private void addToResolveWorklist(Type type, int level) {
        if( type instanceof RefType )
            addToResolveWorklist(((RefType) type).getClassName(), level);
        else if( type instanceof ArrayType )
            addToResolveWorklist(((ArrayType) type).baseType, level);
    }
    private void addToResolveWorklist(String className, int level) {
        addToResolveWorklist(makeClassRef(className), level);
    }
    // Begin javarifier changes
    // For debugging
    private SootClass currentClass;
    // End javarifier changes
    private void addToResolveWorklist(SootClass sc, int desiredLevel) {
        if( sc.resolvingLevel() >= desiredLevel ) return;
        worklist[desiredLevel].add(sc);
        // Begin javarifier changes
        if (javarifier.Main.debugResolve) {
            System.out.println("debugResolve>> addToResolveWorklist " + currentClass + " -> " + sc + " level " + desiredLevel);
        }
        // End javarifier changes
    }

    /** Hierarchy - we know the hierarchy of the class and that's it
     * requires at least Hierarchy for all supertypes and enclosing types.
     * */
    private void bringToHierarchy(SootClass sc) {
        if(sc.resolvingLevel() >= SootClass.HIERARCHY ) return;
        if(Options.v().debug_resolver())
            G.v().out.println("bringing to HIERARCHY: "+sc);
        sc.setResolvingLevel(SootClass.HIERARCHY);

        String className = sc.getName();
        ClassSource is = SourceLocator.v().getClassSource(className);
        if( is == null ) {
            if(!Scene.v().allowsPhantomRefs()) {
            	String suffix="";
            	if(className.equals("java.lang.Object")) {
            		suffix = " Try adding rt.jar to Soot's classpath, e.g.:\n" +
            				"java -cp sootclasses.jar soot.Main -cp " +
            				".:/path/to/jdk/jre/lib/rt.jar <other options>";
            	} else if(className.equals("javax.crypto.Cipher")) {
            		suffix = " Try adding jce.jar to Soot's classpath, e.g.:\n" +
            				"java -cp sootclasses.jar soot.Main -cp " +
            				".:/path/to/jdk/jre/lib/rt.jar:/path/to/jdk/jre/lib/jce.jar <other options>";
            	}
                throw new RuntimeException("couldn't find class: " +
                    className + " (is your soot-class-path set properly?)"+suffix);
            } else {
                G.v().out.println(
                        "Warning: " + className + " is a phantom class!");
                sc.setPhantomClass();
                classToTypesSignature.put( sc, new ArrayList() );
                classToTypesHierarchy.put( sc, new ArrayList() );
            }
        } else {
            Dependencies dependencies = is.resolve(sc);
            // Begin javarifier changes
            if (sc.entryKind() == EntryKind.STUB) {
                if (javarifier.Main.debugStubs) {
                    System.out.println("bringToHierarchy Stub: " + sc.getName());
                }
            } else {
                classToTypesSignature.put( sc, new ArrayList(dependencies.typesToSignature) );
                classToTypesHierarchy.put( sc, new ArrayList(dependencies.typesToHierarchy) );
            }
            // End javarifier changes
        }
        reResolveHierarchy(sc);
    }

    public void reResolveHierarchy(SootClass sc) {
        // Bring superclasses to hierarchy
        if(sc.hasSuperclass()) 
            addToResolveWorklist(sc.getSuperclass(), SootClass.HIERARCHY);
        if(sc.hasOuterClass()) 
            addToResolveWorklist(sc.getOuterClass(), SootClass.HIERARCHY);
        for( Iterator ifaceIt = sc.getInterfaces().iterator(); ifaceIt.hasNext(); ) {
            final SootClass iface = (SootClass) ifaceIt.next();
            addToResolveWorklist(iface, SootClass.HIERARCHY);
        }
        // Begin javarifier changes
        List<SootClass> genericDeps = getGenericDependencies(sc);
        for (SootClass gdep : genericDeps)
            addToResolveWorklist(gdep, SootClass.HIERARCHY);
        // End javarifier changes
    }

    // Begin javarifier changes
    // Returns a list of SootClasses at DANGLING resolve level
    // based on the generic types found in the class sc.
    // Field declarations and parameter declarations have been added so that
    // given:
    //
    //   private Set<Integer> set;
    //   private void foo(Set<Boolean> set);
    //
    // this method returns a set that contains Integer and Boolean, amongst the 
    // rest of the generic dependencies in the class.
    private List<SootClass> getGenericDependencies(SootClass sc) {
      // regardless of what the exit path is, gdeps is exactly what will 
      // be returned by this method
      final List<SootClass> gdeps = new ArrayList<SootClass>();
      
      // first go through each field, get it's signature tage, create a 
      // JrType for the field, then look at the lower and upper bounds
      // of any type arguments, and if they represent ClassType, they 
      // should be added to gdeps (else, they would be VarType, such as when
      // you have Set<T> data;
      for(Object o : sc.getFields().toArray()) {
        SootField f = (SootField) o;
        SignatureTag sig = (SignatureTag) f.getTag("SignatureTag");
        if(sig != null) {
          JrType jrType = JVMLSigParser.parseJrType(sig.getSignature());
          if(jrType instanceof ClassType) {
            ClassType ct = (ClassType) jrType;
            for(TypeArg arg : ct.getTypeArgs()) {
              String lowerName = "";
              if(arg.getLowerBound() instanceof ClassType) {
                String lower = ((ClassType) arg.getLowerBound()).getBaseType();
                lowerName = lower.substring(1, lower.length()-1).replace(
                      '.', '$').replace('/', '.');
                if(true) {
                  SootClass sclass = RefType.v(lowerName).getSootClass();
                  if(sclass.resolvingLevel() == SootClass.DANGLING) {
                    gdeps.add(sclass);
                  }
                }
              }
              if(arg.getUpperBound() instanceof ClassType) {
                String upper = ((ClassType) arg.getUpperBound()).getBaseType();                 
                String upperName = upper.substring(1, upper.length()-1).replace(
                      '.', '$').replace('/', '.');
                if(!upperName.equals(lowerName)) {
                  SootClass sclass = RefType.v(upperName).getSootClass();
                  if(sclass.resolvingLevel() == SootClass.DANGLING) {
                    gdeps.add(sclass);
                  }
                }
              }
            }
          }
        }
      }
      
      // now do the same process for all methods, getting their signature tag, 
      // then looking at the parameters to see if any are templated by generics,
      // and if so, and if these types are ClassTypes, then add them to gdeps.
      // else, they would be VarTypes, like when you have foo(Set<T> s)
      List<SootMethod> allMethods = sc.getMethods();
      for(SootMethod method : allMethods) {
        SignatureTag sTag = (SignatureTag) method.getTag("SignatureTag");
        String sig = null;
        if (sTag == null) {
          sig = method.getBytecodeSignature();
          sig = sig.substring(sig.indexOf('('), sig.length()-1);       
        } else {
          sig = sTag.getSignature();
        }
        
        MethodSig methSig = JVMLSigParser.parseMethodSig(sig, 
            new JrType.ClassType(Mutability.UNKNOWN, sc.getName(), 
                new ArrayList<TypeArg>()));
        if(methSig != null) {
          methSig.setOwner(method);
          List<JrType> params = methSig.getParams();
          for(JrType p : params) {
            if(p instanceof ClassType) {
              ClassType ct = (ClassType) p;
              for(TypeArg arg : ct.getTypeArgs()) {
                String lowerName = "";
                if(arg.getLowerBound() instanceof ClassType) {
                  String lower = 
                    ((ClassType) arg.getLowerBound()).getBaseType();
                  lowerName = lower.substring(1, lower.length()-1).replace(
                      '.', '$').replace('/', '.');
                  if(true) {
                    SootClass sclass = RefType.v(lowerName).getSootClass();
                    if(sclass.resolvingLevel() == SootClass.DANGLING) {
                      gdeps.add(sclass);
                    }
                  }
                }
                if(arg.getUpperBound() instanceof ClassType) {
                  String upper = 
                    ((ClassType) arg.getUpperBound()).getBaseType();                 
                  String upperName = 
                    upper.substring(1, upper.length()-1).replace(
                      '.', '$').replace('/', '.');
                  if(!upperName.equals(lowerName)) {
                    SootClass sclass = RefType.v(upperName).getSootClass();
                    if(sclass.resolvingLevel() == SootClass.DANGLING) {
                      gdeps.add(sclass);
                    }
                  }
                }
              }
            }
          }
        }
      }
      
      
      // now the field and method parameter generic types have been added,
      // proceed as normal to find the generic types inside the method bodies
        SignatureTag classSigTag = (SignatureTag) sc.getTag("SignatureTag");
        if (classSigTag == null)
            return gdeps;
        String classSig = classSigTag.getSignature();
        ClassSig classSignature = JVMLSigParser.parseClassSig(sc, classSig);
        new SceneVisitor() {
            @Override
            public void visitType(JrType type) {
                type.accept(new EmptyTypeVisitor() {
                    @Override
                    public void visitClassType(ClassType t) {
                        String desc = t.getBaseType();
                        String name = desc.substring(1, desc.length() - 1)
                            .replace('.','$').replace('/', '.');
                        gdeps.add(RefType.v(name).getSootClass());
                        super.visitClassType(t);
                    }
                });
            }
            @Override
            public void visitTypeParam(Pair<VarType, JrType> param) {
                visitType(param.second());
            }
        }.visitClassSig(classSignature);
        return gdeps;
    }
    // End javarifier changes

    /** Signatures - we know the signatures of all methods and fields
    * requires at least Hierarchy for all referred to types in these signatures.
    * */
    private void bringToSignatures(SootClass sc) {
        if(sc.resolvingLevel() >= SootClass.SIGNATURES ) return;
        // Begin javarifier changes
        if(javarifier.Main.printStubs) {
          System.out.println("bringToSignatures Stub: " + sc.getName());
        } else
        if (SourceLocator.v().entryKindForClass(sc.getName())
                == EntryKind.WORLD) {
          if (javarifier.Main.printMissingStubs) {
            System.out.println("Missing stub for class " + sc.getName());
          } else {
            throw new RuntimeException("Missing stub for class " + sc.getName());
          }
        }
        // End javarifier changes
        bringToHierarchy(sc);
        if(Options.v().debug_resolver()) 
            G.v().out.println("bringing to SIGNATURES: "+sc);
        sc.setResolvingLevel(SootClass.SIGNATURES);

        for( Iterator fIt = sc.getFields().iterator(); fIt.hasNext(); ) {

            final SootField f = (SootField) fIt.next();
            addToResolveWorklist( f.getType(), SootClass.HIERARCHY );
        }
        for( Iterator mIt = sc.getMethods().iterator(); mIt.hasNext(); ) {
            final SootMethod m = (SootMethod) mIt.next();
            addToResolveWorklist( m.getReturnType(), SootClass.HIERARCHY );
            for( Iterator ptypeIt = m.getParameterTypes().iterator(); ptypeIt.hasNext(); ) {
                final Type ptype = (Type) ptypeIt.next();
                addToResolveWorklist( ptype, SootClass.HIERARCHY );
            }
            for (SootClass exception : m.getExceptions()) {
                addToResolveWorklist( exception, SootClass.HIERARCHY );
            }
        }

        // Bring superclasses to signatures
        if(sc.hasSuperclass()) 
            addToResolveWorklist(sc.getSuperclass(), SootClass.SIGNATURES);
        for( Iterator ifaceIt = sc.getInterfaces().iterator(); ifaceIt.hasNext(); ) {
            final SootClass iface = (SootClass) ifaceIt.next();
            addToResolveWorklist(iface, SootClass.SIGNATURES);
        }
    }

    /** Bodies - we can now start loading the bodies of methods
    * for all referred to methods and fields in the bodies, requires
    * signatures for the method receiver and field container, and
    * hierarchy for all other classes referenced in method references.
    * Current implementation does not distinguish between the receiver
    * and other references. Therefore, it is conservative and brings all
    * of them to signatures. But this could/should be improved.
    * */
    private void bringToBodies(SootClass sc) {
        if(sc.resolvingLevel() >= SootClass.BODIES ) return;
        bringToSignatures(sc);
        if(Options.v().debug_resolver()) 
            G.v().out.println("bringing to BODIES: "+sc);
        sc.setResolvingLevel(SootClass.BODIES);

        {
        	Collection references = classToTypesHierarchy.get(sc);
            if( references == null ) return;

            Iterator it = references.iterator();
            while( it.hasNext() ) {
                final Object o = it.next();

                if( o instanceof String ) {
                    addToResolveWorklist((String) o, SootClass.HIERARCHY);
                } else if( o instanceof Type ) {
                    addToResolveWorklist((Type) o, SootClass.HIERARCHY);
                } else throw new RuntimeException(o.toString());
            }
        }

        {
        	Collection references = classToTypesSignature.get(sc);
            if( references == null ) return;

            Iterator it = references.iterator();
            while( it.hasNext() ) {
                final Object o = it.next();

                if( o instanceof String ) {
                    addToResolveWorklist((String) o, SootClass.SIGNATURES);
                } else if( o instanceof Type ) {
                    addToResolveWorklist((Type) o, SootClass.SIGNATURES);
                } else throw new RuntimeException(o.toString());
            }
        }
    }

    public void reResolve(SootClass cl) {
        int resolvingLevel = cl.resolvingLevel();
        if( resolvingLevel < SootClass.HIERARCHY ) return;
        reResolveHierarchy(cl);
        cl.setResolvingLevel(SootClass.HIERARCHY);
        addToResolveWorklist(cl, resolvingLevel);
        processResolveWorklist();
    }

	public Program getProgram() {
		return program;
	}
}


