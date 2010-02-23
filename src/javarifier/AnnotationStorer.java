package javarifier;

import java.util.*;
import java.lang.annotation.RetentionPolicy;

import javarifier.JrType.*;
import javarifier.JrType.ArrayType;
import javarifier.JrType.NullType;
import javarifier.util.*;
import soot.*;
import annotations.*;
import annotations.el.*;
import annotations.util.coll.*;
import static javarifier.AnnotationLoader.classIsUnmodifiable;
import static javarifier.AnnotationLoader.replaceQuestionRo;
import static javarifier.AnnotationLoader.typeIsReadOnlyObject;
import static javarifier.AnnotationLoader.wrap;

/**
 * {@link AnnotationStorer} stores annotations from Soot classes into the
 * annotation scene library.  It provides a static method,
 * {@link #storeAnnotations}, to store the mutabilities of
 * references inside a class into a
 * given {@link AScene} as annotations.
 */
public class AnnotationStorer {

  /**
   * The annotation to use for storing immutable classes (such as String).
   */
  // This would be useful if we decided to use Unmodifiable as syntactic
  // sugar on a class to indicate all methods should be readonly.
  public static final String unmodifiableAnnotationName =
    // "checkers.javari.quals.Unmodifiable";
    "checkers.javari.quals.ReadOnly";

  public static final AnnotationDef unmodifiableAnnotationDef =
    new AnnotationDef(unmodifiableAnnotationName);


  /**
   * Converts all the mutabilities in the class <code>sc</code> into
   * JSR308 annotations and writes them into <code>scene</code>.
   *
   * <p>
   * This method resugars {@link Mutability#QUESTION_RO QUESTION_RO} and
   * coalesces bounds, the inverse of the process followed by
   * {@link AnnotationLoader#loadAnnotations}.  If a {@link JrType} in
   * the class is too unusual to be resugared and coalesced, a
   * {@link RuntimeException} will probably be thrown
   * (such as if the lower bound is mutable and the upper bound is readonly).
   *
   * <p>
   * Caution: This method mutates the mutabilities in <code>sc</code> (ha ha)
   * by changing some of them to QUESTION_RO during resugaring.
   */
  public static void storeAnnotations(SootClass sc, AScene scene) {
    try {
      // The AnnotationStorer class is a visitor over the Soot
      // representation of various program elements.  The visitor
      // stores the annotation on an element into the scene when it
      // visits that element.
      AnnotationStorer storer = new AnnotationStorer(scene);
      AClass ac = scene.classes.vivify(sc.getName());
      storer.storeClass(sc, ac);
    } catch (RuntimeException e) {
      throw wrap(e, "class " + sc.getName());
    }
  }

  /**
   * Store the scene in which to place all new annotations.
   */
  private final AScene scene;

  /**
   * Private constructor, should not be initialized elsewhere.  Call
   * storeAnnotations instead.
   *
   * @param scene - the scene in which to store all annotations this visits.
   */
  private AnnotationStorer(AScene scene) {
    this.scene = scene;
  }

  /**
   * The default retention policy to use for inserting annotations into the
   * AScene.  (Javarifier only infers that something is, say "@ReadOnly".
   * A retention policy is needed by the scene library in order to
   * add annotations to a scene.  Even though the JVM doesn't do anything
   * with these annotations, we reserve this capability to, for example,
   * be able to later check casts.)
   */
  public static final RetentionPolicy DEFAULT_RETENTION =
    RetentionPolicy.RUNTIME;


  /**
   * Stores the mutabilities of all references in the given class into the
   * annotation scene library's representation of a class.
   *
   * @param sc - the class whose mutabilities should be stored
   * @param ac - the annotation scene library element to store the
   *             annotations in
   */
  private void storeClass(SootClass sc, AClass ac) {
    if (classIsUnmodifiable(sc.getName()))
      annotate(ac.type, unmodifiableAnnotationDef);

    // An interface has all "empty" methods, so don't output the
    // inferred annotations if user doesn't want empty methods/classes
    // annotated.
    if (Options.v().skipEmpty() && sc.isInterface()) {
      if (Options.v().debugAnnotationStoring()) {
        System.out.println("AnnotationStorer skipping interface: " +
            sc.getName());
      }
      return;
    }

    storeTypeParameters(sc.getSig().getTypeParams(), ac.bounds);

    // Only do fields and methods for classes resolved to signatures
    if (sc.resolvingLevel() >= SootClass.SIGNATURES) {
      for (Object sf1 : sc.getFields()) {
        SootField sf = (SootField) sf1; // emulate an erasure-change cast
        AElement af = ac.fields.vivify(sf.getName());
        try {
          storeField(sf, af);
        } catch (RuntimeException e) {
          throw wrap(e, "field " + sf.getName());
        }
      }
      for (Object sm1 : sc.getMethods()) {
        SootMethod sm = (SootMethod) sm1; // emulate an erasure-change cast
        String methodSig = AbstractJasminClass.jasminDescriptorOf(sm.makeRef());
        String methodKey = sm.getName() + methodSig;
        AMethod am = ac.methods.vivify(methodKey);
        try {
          storeMethod(sm, am);
        } catch (RuntimeException e) {
          throw wrap(e, "method " + methodKey);
        }
      }
    }
  }

  /**
   * Stores the mutabilities of all references in the given method
   * into the corresponding annotation scene library representation of a method.
   *
   * @param sm - the method whose Javari types should be stored
   * @param am - the annotation scene library to store all the annotations in
   */
  private void storeMethod(SootMethod sm, AMethod am) {
    // If a method is abstract, don't output any annotations for it.
    // This is useful when comparing Javarifier results to results that
    // do not annotate abstract methods.
    if (Options.v().skipEmpty() && sm.isAbstract()) {
      if (Options.v().debugAnnotationStoring()) {
        System.out.println("AnnotationStorer skipping abstract method: " + sm);
      }
      return;
    }

    // In the following, there is a wrapper around methods for storing each
    // reference type in order to improve error reporting.

    // Store mutability on type parameters on methods
    MethodSig ms = sm.getSig();
    try {
      storeTypeParameters(ms.getTypeParams(), am.bounds);
    } catch(RuntimeException e) {
      throw wrap(e, "method signature");
    }

    // Store mutability on return type.
    try {
      storeType(ms.getReturnType(), am.returnType);
    } catch (RuntimeException e) {
      throw wrap(e, "return type");
    }

    // Store the mutability of the receiver only for object methods.
    if (!sm.isStatic()) {
      try {
        storeTypeLayer(ms.getReceiverType(), am.receiver);
      } catch (RuntimeException e) {
        throw wrap(e, "receiver");
      }
    }

    // Store the mutability on each parameter.
    List<JrType> sParams = ms.getParams();
    for (int i = 0; i < sParams.size(); i++) {
      JrType sParam = sParams.get(i);
      ATypeElement dParam = am.parameters.vivify(i).type;
      // Don't include annotations on parameters whose type is a
      //  known immtuable class.
      boolean isImmutable = false;
      if (sParam instanceof ClassType) {
        ClassType ctParam = (ClassType) sParam;
        isImmutable = AnnotationLoader.classIsUnmodifiable(
            ctParam.getBaseType());
      }
      if (!Options.v().includeImmutableClasses() &&
          isImmutable) {
        if (Options.v().debugAnnotationStoring()) {
          System.out.println("AnnotationStorer skipping parameter " +
              " (with type of immutable class): " + sParam);
        }
        continue;
      }

      try {
        storeType(sParam, dParam);
      } catch (RuntimeException e) {
        throw wrap(e, "parameter #" + i);
      }
    }

    // Store the mutability on local variables if the method has a body.
    if (!sm.isBridge() && sm.getBody() != null) {
      Body body = sm.getBody();
      Collection<Local> locals = body.getLocals();
      for (Local l : locals) {
        if (l.isSourceLocal()) {
          LocalLocation loc = new LocalLocation(l.getSlotIndex(),
              l.getStart_pc(), l.getLength());
          try {
            storeType(l.getJrType(), am.locals.vivify(loc).type);
          } catch (RuntimeException e) {
            throw wrap(e, "local " + loc.index + " #"
                + loc.scopeStart + "+" + loc.scopeLength);
          }
        }
      }
    }
  }


  /**
   * Stores the mutability of the given field into the given annotation scene
   * library element as the corresponding annotation.  This method also
   * writes out the "@Assignable" annotation if the field is assignable.
   * (Recall that "@Assignable" is not a mutability, it is a field annotation.)
   * (If the field is explicitly mutable, this writes out the "@Mutable"
   * annotation because "@Mutable" is both a field annotation and mutability
   * type qualifier when it is on a field.)
   *
   * @param sf - the field whose mutability and assignability should be stored
   * @param af - the annotation scene library element to store the
   *             annotations in
   */
  private void storeField(SootField sf, AElement af) {
    if (sf.assignable())
      annotate(af, AnnotationLoader.assignableAnnotationDef);
    storeType(sf.getJrType(), af.type);
  }

  /**
   * Stores the mutability of the Javari type into the given annotation scene
   * library element as the corresponding annotation.
   *
   * @param source - the Javari type whose mutability should be stored
   * @param dest - the annotation scene library element to store the
   *          annotations in
   */
  private void storeType(JrType source, ATypeElement dest) {
    // Copy the type because the resugarer may change it.
    // Mutate the copy and store the copy.
    JrType sourceCopy = source.copy();
    try {
      resugarQuestionRo(sourceCopy);
    } catch (RuntimeException e) {
      throw wrap(e, "resugarQuestionRo " + source.toString());
    }
    new JrTypeASTMapper().traverse(sourceCopy, dest);
  }

  /**
   * This stores all the mutabilities of the given type parameters
   * into the map of type parameters in the scene library that maps the
   * type parameters of the corresponding element.
   *
   * @param jtp - a list of type parameters with mutabilities to store
   * @param atp - a map to store the annotations into
   */
  private void storeTypeParameters(List<Pair<VarType, JrType>> jtp,
      VivifyingMap<BoundLocation, ATypeElement> atp) {
    for (int i = 0; i < jtp.size(); i++) {
      // Java and the ASL support more than one bound per
      // type parameter, but evidently the Javarifier does not.
      // For now, we just store the first bound.
      BoundLocation bl = new BoundLocation(i, 0);
      ATypeElement bound = atp.vivify(bl);
      try {
        storeType(jtp.get(i).second(), bound);
      } catch (RuntimeException e) {
        throw wrap(e, "bound #0 of type parameter #" + i);
      }
    }
  }

  /**
   * Stores any mutability annotations in <code>source</code> at this particular
   * type layer into <code>dest</code>.  For example, when traversing
   * <code>@ReadOnly List&lt;@Mutable Date&gt;</code> in {@link JrTypeASTMapper},
   * this will be called:
   *
   *  <ul>
   *  <li>
   *  once with <code>source</code> :
   *  <code>@ReadOnly List&lt;@Mutable Date&gt;</code>, in which case this stores
   *  <code>checkers.javari.quals.ReadOnly</code> into <code>dest</code>
   *  </li>
   *  <li>
   *  once with <code>source</code> : <code>@Mutable Date</code>,
   *  in which case this stores <code>checkers.javari.quals.Mutable</code>
   *  into <code>dest</code>.
   *  </li>
   *  </ul>
   *  (Of course, dest must be the corresponding element in the scene library.)
   *
   * @param source - the type containing mutability annotations
   * @param dest - the element to store the mutabilities into
   */
  private void storeTypeLayer(JrType source, ATypeElement dest) {
    // If source has any mutabilities, simply retrieve them, look up
    // the annotation from mutabilityAnnos and then use annotate().
    if (source instanceof MutType) {
      MutType msource = (MutType) source;
      Mutability m = msource.getMutability();
      if (m == Mutability.UNKNOWN) {
        // If mutability is unknown, it was never marked as anything
        // mutable, so it must be readonly.
        m = Mutability.READONLY;
      }

      AnnotationDef adef = m.annotationDef;
      if (adef == null) {
        throw new RuntimeException("AnnotationStorer.storeTypeLayer: " +
                                   "Bad mutability in: " + source);
      }
      annotate(dest, adef);
    }
  }

  /**
   * Adds the annotation described by <code>annoTypeName</code> to the
   * element <code>target</code>.
   *
   * @param target - the element onto which to add the annotation
   * @param annoTypeName - the name of the annotation to add
   */
  private void annotate(AElement target, AnnotationDef adef) {
    if (target instanceof ATypeElement) {
      if (adef.name.indexOf("Assignable") != -1) {
        throw new Error(String.format("annotation %s belongs on ATypeElement, not AElement %s", adef, target));
      }
    } else {
      if (adef.name.indexOf("ReadOnly") != -1
          || adef.name.indexOf("Mutable") != -1) {
        throw new Error(String.format("annotation %s belongs on ATypeElement, not AElement %s", adef, target));
      }
    }
    Annotation ann = AnnotationFactory.saf.beginAnnotation(adef).finish();
    target.tlAnnotationsHere.add(ann);
  }

  /**
   * This class traverses the AST of some {@link ATypeElement} (see
   * {@link TypeASTMapper}), and "applies" (stores the mutability of)
   * a {@link JrType} into the appropriate elements in the AST.
   *
   * {@link TypeASTMapper#traverse(Object, ATypeElement)} will call
   * {@link #map(JrType, AElement)} on the appropriate Javari type/
   * scene library element pairs.
   */
  private class JrTypeASTMapper extends TypeASTMapper<JrType> {
    /**
     * Private constructor, should not be initialized elsewhere.
     */
    private JrTypeASTMapper() {
    }

    /**
     * Returns the element type of <code>type</code> if it is an array type.
     * Else, if <code>type</code> is not an array type, returns null.
     */
    @Override
    protected JrType getElementType(JrType type) {
      if (type instanceof ArrayType)
        return getHighestReadOnlyBound(((ArrayType) type).getElemType());
      else
        return null;
    }

    /**
     * Returns the type argument of the type at the given index.
     */
    @Override
    protected JrType getTypeArgument(JrType type, int index) {
      return getHighestReadOnlyBound(((ClassType) type).getTypeArgs().get(index));
    }

    /**
     * Stores the mutability in the type <code>type</code> into the scene
     * element <code>element</code>.
     *
     * @param type - the Javari type whose mutability should be stored
     * @param element - the element of the scene library to store the mutability
     * into
     */
    @Override
    protected void map(JrType type, ATypeElement element) {
      storeTypeLayer(type, element);
    }

    /**
     * Returns the number of type arguments in the given type.
     */
    @Override
    protected int numTypeArguments(JrType n) {
      if (n instanceof ClassType)
        return ((ClassType) n).getTypeArgs().size();
      else
        return 0;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  // Code to deal with sugaring types.
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Returns true if and only if the two input types are equal.
   */
  private static boolean typesEqual(JrType t1, JrType t2) {
    return t1.toString().equals(t2.toString());
  }

  /**
   * If there is no lower bound, or if the upper bound represents
   * a non-readonly type, it returns the upper bound.  Else, it returns
   * the lower bound.
   *
   * @param targ - the type argument whose interesting bound should be returned
   * @return the highest readonly bound for the type argument.
   */
  private static JrType getHighestReadOnlyBound(TypeArg targ) {
    if (targ.getLowerBound() instanceof NullType)
      return targ.getUpperBound();
    else if (typeIsReadOnlyObject(targ.getUpperBound()))
      return targ.getLowerBound();
    else
      return targ.getUpperBound();
  }

  /**
   * Returns true if and only if <code>maybeReplaced</code> is the
   * Javari type of <code>orig</code> with the mutabilities on
   * upper and lower bounds sugared to be {@link Mutability#QUESTION_RO}
   * if possible, and otherwise is the same as the unsugared version.
   * If <code>sugared/code> is the sugared version
   * of <code>orig</code> but it, correctly, does not contain any
   * question ro types, this returns true.
   *
   * This method does not mutate either parameter.
   */
  private static boolean isQuestionRoReplacement(
      MutType sugared, MutType orig) {
    JrType checkReplaced = orig.copy();
    replaceQuestionRo(checkReplaced);
    return typesEqual(checkReplaced, sugared);
  }

  /**
   * Resugars the type given type argument to have its mutability set to
   * {@link Mutability#QUESTION_RO} if necessary (or
   * {@link Mutability#POLYREAD} in some cases).  This method mutates
   * the bounds in <code>targ</code> to have the right mutability.
   * (See Section 4.3 on resugaring mutabilities in Jaime Quinonez's thesis.)
   *
   * If the lower bound is mutable (or this-mutable)
   * and the upper bound is readonly, then the upper bound is set to
   * question-readonly.
   *
   * If the lower bound is mutable (or this-mutable)
   * and the upper bound is polyread, then the lower bound is set to polyread.
   *
   * This recursively resugars the bounds in order to handle types such
   * as List&lt;? readonly List&lt;? readonly Date&gt;&gt;.
   *
   * @param targ - the type argument to resugar
   */
  private static void sugarQuestionRo(TypeArg targ) {
    JrType upper = targ.getUpperBound();
    JrType lower = targ.getLowerBound();
    if (lower instanceof NullType) {
      // If lower bound is null type, upper bound is only interesting
      // mutability.
      resugarQuestionRo(upper);
    } else if (typeIsReadOnlyObject(upper)) {
      // If the upper bound is a reaonly type, only the lower bound needs
      // to be resugared.
      resugarQuestionRo(lower);
    } else {
      if (lower instanceof MutType != upper instanceof MutType)
        throw new RuntimeException("Mismatched MutTypes");
      if (lower instanceof MutType) {
        MutType mupper = (MutType) upper;
        MutType mlower = (MutType) lower;

        // If the lower bound is this-mutable, there is nothing to do.
        if (mlower.getMutability() != Mutability.THIS_MUTABLE) {

          // If lower bound == upper bound, there is nothing to do.
          if (mupper.getMutability() != mlower.getMutability()) {
            if (!(mupper.getMutability() == Mutability.READONLY
                && mlower.getMutability() == Mutability.MUTABLE)) {
              if (mupper.getMutability() == Mutability.POLYREAD &&
                  mlower.getMutability() == Mutability.MUTABLE) {
                // Having polyread upper type takes cares of the
                // upper/lower bound requirements of question_ro
                mlower.setMutability(Mutability.POLYREAD);
              } else {
                throw new RuntimeException("Wrong differing mutabilities");
              }
            } else {
              // upper = readonly, lower = mutable
              mupper.setMutability(Mutability.QUESTION_RO);
            }
          }

          // unlike the two top cases, now need to sugar both bounds
          resugarQuestionRo(mlower);
          resugarQuestionRo(mupper);
        }
      }
    }
  }

  /**
   * Resugars the mutability bounds in the given type to be set to
   * {@link Mutability#QUESTION_RO} or {@link Mutability#POLYREAD} if
   * it contains inner types and those inner types have different
   * mutabilities on their upper/lower bounds.
   */
  private static void resugarQuestionRo(JrType type) {
    // Only arrays and class types
    // have inner types (Date[? readonly] and List<? readonly Date>)
    if (type instanceof ArrayType) {
      ArrayType at = (ArrayType) type;
      TypeArg eta = at.getElemType();
      sugarQuestionRo(eta);
    } else if (type instanceof ClassType) {
      ClassType ct = (ClassType) type;
      List<TypeArg> targs = ct.getTypeArgs();
      for (TypeArg targ : targs)
        sugarQuestionRo(targ);
    }
  }


}
