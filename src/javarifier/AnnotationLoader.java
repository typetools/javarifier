package javarifier;

import java.util.*;

import soot.AbstractJasminClass;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SourceLocator.EntryKind;

import javarifier.JrType.*;
import javarifier.util.*;

import annotations.*;
import annotations.el.*;
import annotations.util.coll.*;
import annotations.field.AnnotationFieldType;
import static annotations.Annotations.typeQualifierMetaAnnotations;

/**
 * {@link AnnotationLoader} provides a static method,
 * {@link #loadAnnotations}, to fill the mutabilities in a class based
 * on extended annotations from {@link AnnotationScene}.
 */
public class AnnotationLoader {
  private AnnotationLoader() {}

  /**
   * The annotation for the Javari assignable keyword.
   */
  public static final String assignableAnnotationName =
    "checkers.javari.quals.Assignable";

  public static final AnnotationDef assignableAnnotationDef =
    new AnnotationDef(assignableAnnotationName,
                      typeQualifierMetaAnnotations,
                      Collections.<String,AnnotationFieldType>emptyMap());

  /**
   * The annotation for syntactic sugar on a class indicating that all
   * methods have a readonly receiver.
   */
  public static final String unmodifiableAnnotation =
    // "checkers.javari.quals.Unmodifiable";
    "checkers.javari.quals.ReadOnly";

  /**
   * Returns whether the class is always an unmodifiable class.
   *
   * @param className - the fully-qualified name of the class to examine
   */
  static boolean classIsUnmodifiable(String className) {
    AClass clazz =
      AnnotationScene.v().scene().classes.vivify(className);
    boolean unmodifiable = false;
    if (clazz.lookup(unmodifiableAnnotation) != null) {
      unmodifiable = true;
    } else if (clazz.lookup(Mutability.READONLY.annotationName) != null) {
      unmodifiable = true;
    }

    return unmodifiable;
  }

  /**
   * Given a JVML representation of a class, returns the fully-qualified
   * name for that class.
   */
  private static String classDescToName(String desc) {
    return desc.substring(1, desc.length() - 1).replace('/','.');
  }

  /**
   * Loads all annotations for program and stub classes in the scene by
   * looking in the corresponding class files.
   */
  public static void loadAllAnnotations(Scene scene) {
    // Now we want to run this for all classes, stub or not.
    // That way a user can specify assignable or mutable fields using
    // annotations.
    for (SootClass sc : (Collection<SootClass>) scene.getClasses())
      if (sc.entryKind() == EntryKind.PROGRAM
          || sc.entryKind() == EntryKind.STUB)
        loadAnnotations(sc);
  }
  /**
   * Fills all the mutabilities in the class <code>sc</code> based on
   * the extended annotations in the class file.  All explicit annotations
   * in the class file are trusted.  A default is used for unspecified
   * mutabilities in stub classes, while unspecified mutabilities in
   * non-stub classes are left as {@link Mutability#UNKNOWN} so that
   * the Javarifier can infer them.
   *
   * <p>
   * This method now considers all mutabilities of all bounds and desugars
   * {@link Mutability#QUESTION_RO}.
   */
  private static void loadAnnotations(SootClass sc) {
    try {
      AClass ac = AnnotationScene.v().scene().classes.vivify(sc.getName());
      loadClass(sc, ac);
    } catch (RuntimeException e) {
      throw wrap(e, "class " + sc.getName());
    }
  }

  /**
   * Loads all the annotations from the given annotation scene library class
   * into the given SootClass.
   */
  private static void loadClass(SootClass sc, AClass ac) {
    boolean isStub = ((sc.entryKind() == EntryKind.STUB)
                      || Main.justPassThrough);

    if (sc.resolvingLevel() == SootClass.DANGLING) {
      if (Main.debugStubs) {
        System.out.println("AnnotationLoader skipping dangling class: " + sc);
      }
      return;
    }

    loadTypeParameters(sc.getSig().getTypeParams(), ac.bounds, isStub,
        Mutability.READONLY);

    // Only do fields and methods for classes resolved to signatures
    if (sc.resolvingLevel() >= SootClass.SIGNATURES) {
      Set<String> annoFieldsLeft = new LinkedHashSet<String>(ac.fields.keySet());
      for (Object sf1 : sc.getFields()) {
        SootField sf = (SootField) sf1; // emulate an erasure-change cast
        annoFieldsLeft.remove(sf.getName());
        AElement af = ac.fields.vivify(sf.getName());
        try {
          loadField(sf, af, isStub);
        } catch (RuntimeException e) {
          throw wrap(e, "field " + sf.getName());
        }
      }
      if (!annoFieldsLeft.isEmpty())
        throw notThere("field " + arbElement(annoFieldsLeft));
      Set<String> annoMethodsLeft = new LinkedHashSet<String>(ac.methods.keySet());
      for (Object sm1 : sc.getMethods()) {
        SootMethod sm = (SootMethod) sm1; // emulate an erasure-change cast
        String methodSig = AbstractJasminClass.jasminDescriptorOf(sm.makeRef());
        String methodKey = sm.getName() + methodSig;
        annoMethodsLeft.remove(methodKey);
        AMethod am = ac.methods.vivify(methodKey);
        try {
          loadMethod(sm, am, isStub);
        } catch (RuntimeException e) {
          throw wrap(e, "method " + methodKey);
        }
      }
      if (!annoMethodsLeft.isEmpty())
        throw notThere("method " + arbElement(annoMethodsLeft));
    }
  }

  /**
   * Loads all the annotations (in the signature and in the body) from the
   * method in the annotation scene library into the given SootMethod.
   *
   * @param sm - the method to load annotations into
   * @param am - the annotation scene library method to load annotations from
   * @param isStub - whether this method is in a stub class
   */
  private static void loadMethod(SootMethod sm, AMethod am, boolean isStub) {
    MethodSig ms = sm.getSig();
    loadTypeParameters(ms.getTypeParams(), am.bounds, isStub,
        Mutability.READONLY);
    try {
      loadType(ms.getReturnType(), am.returnType, isStub, Mutability.MUTABLE, false);
    } catch (RuntimeException e) {
      throw wrap(e, "return type");
    }
    if (sm.isStatic()) {
      if (!am.receiver.prune()) {
        // Do not throw exception if receiver of static method is
        // @ReadOnly.  That should be a valid, although useless,
        // annotation.
        // throw notThere("receiver");
      }
    } else {
      try {
        loadTypeLayer(ms.getReceiverType(), am.receiver,
            isStub, Mutability.MUTABLE, false);
      } catch (RuntimeException e) {
        throw wrap(e, "receiver");
      }
    }
    List<JrType> dParams = ms.getParams();
    Set<Integer> annoParamsLeft = new LinkedHashSet<Integer>(am.parameters.keySet());
    for (int i = 0; i < dParams.size(); i++) {
      JrType dParam = dParams.get(i);
      annoParamsLeft.remove(i);
      ATypeElement sParam = am.parameters.vivify(i).type;
      try {
        loadType(dParam, sParam, isStub, Mutability.MUTABLE, false);
      } catch (RuntimeException e) {
        throw wrap(e, "parameter #" + i);
      }
    }
    if (!annoParamsLeft.isEmpty())
      throw notThere("parameter #" + arbElement(annoParamsLeft));
    // Only do locals for methods with meaningful bodies
    if (!sm.isBridge() && sm.getBody() != null) {
      Body body = sm.getBody();
      Collection<Local> locals = body.getLocals();
      Set<LocalLocation> annoLocalsLeft = new LinkedHashSet<LocalLocation>(am.body.locals.keySet());
      for (Local l : locals) {
        if (l.isSourceLocal()) {
          LocalLocation loc = new LocalLocation(l.getSlotIndex(),
              l.getStart_pc(), l.getLength());
          annoLocalsLeft.remove(loc);
          try {
            loadType(l.getJrType(), am.body.locals.vivify(loc).type,
                isStub, Mutability.MUTABLE, false);
          } catch (RuntimeException e) {
            throw wrap(e, "local " + loc.index + " #"
                + loc.scopeStart + "+" + loc.scopeLength);
          }
        }
      }
      if (!annoLocalsLeft.isEmpty()) {
        LocalLocation notThereLoc = arbElement(annoLocalsLeft);
        throw notThere("local " + notThereLoc.index + " #"
            + notThereLoc.scopeStart + "+" + notThereLoc.scopeLength);
      }
    }
  }

  /**
   * Loads all the annotations from the given annotation scene element
   * into the given SootField.
   * Note that assignable annotations are also loaded by calling
   * SootField.setAssignable(boolean) if appropriate.
   */
  // Argument is not ATypeElement because we need to find @Assignable
  // annotations on it as well as annotations on its type.
  private static void loadField(SootField sf, AElement af, boolean isStub) {
    Mutability defmut;
    defmut = (classIsUnmodifiable(sf.getDeclaringClass().getName())
              ? Mutability.READONLY
              : (sf.isStatic()
                 ? Mutability.MUTABLE
                 : Mutability.THIS_MUTABLE));
    loadType(sf.getJrType(), af.type, isStub, defmut, true);
    if (af.lookup(assignableAnnotationName) != null)
      sf.setAssignable(true);
  }


  /**
   * Loads all the annotations from the given element in the scene library
   * into the given JrType.
   *
   * @param dest - the JrType to write elements to
   * @param source - the element to load annotations from
   * @param isStub - whether this is in a stub class
   * @param defmut - the default mutability
   * @param isField - whether the given element represents a field
   */
  private static
  void loadType(JrType dest, ATypeElement source,
      boolean isStub, Mutability defmut, boolean isField) {
    new JrTypeASTMapper(isStub, defmut, isField).traverse(dest, source);
    expandType(dest);
    desugarQuestionRo(dest);
  }

  /**
   * Loads all the annotations from the given bound map from the annotation
   * scene library into <code>jtp</code> to be made into a TypeIndex later.
   *
   * @param jtp - the types to write to
   * @param atp - the map containing annotations on bounds
   * @param isStub - whether this is in a stub class
   * @param defmut - the default mutability
   */
  private static
  void loadTypeParameters(List<Pair<VarType, JrType>> jtp,
      VivifyingMap<BoundLocation, ATypeElement> atp,
      boolean isStub, Mutability defmut) {
    Set<BoundLocation> annoBLsLeft = new LinkedHashSet<BoundLocation>(atp.keySet());
    for (int i = 0; i < jtp.size(); i++) {
      // HMMM Java and the ASL support more than one bound per
      // type parameter, but evidently the Javarifier does not.
      // For now, we just load the first bound.
      BoundLocation bl = new BoundLocation(i, 0);
      annoBLsLeft.remove(bl);
      ATypeElement bound = atp.vivify(bl);
      try {
        loadType(jtp.get(i).second(), bound,
            isStub, defmut, false);
      } catch (RuntimeException e) {
        throw wrap(e, "bound #0 of type parameter #" + i);
      }
    }
    if (!annoBLsLeft.isEmpty()) {
      // TODO: FIXME annotation bound handling.
      // This requires finalizing the JSR 308 classfile format so
      // that these annotations are in fact in the class file.
      // Good Test case: plume.Intern$SequenceAndIndices
      // (Ignoring doesn't affect Javarifier yet, because they are
      //  just @Interned annotations.)
      annoBLsLeft.clear();

      //  BoundLocation bl = arbElement(annoBLsLeft);
      //  throw notThere("bound #" + bl.boundIndex +
      //          " of type parameter #" + bl.paramIndex);
    }
  }

  /**
   * Loads all the annotations from a given element from the annotation
   * scene library into the given JrType.
   *
   * @param dest - the JrType to write elements to
   * @param source - the element to load annotations from
   * @param isStub - whether this is in a stub class
   * @param defmut - the default mutability
   * @param isField - whether the given element represents a field
   */
  static void loadTypeLayer(JrType dest, AElement source,  // [dbro: was ATypeElement]
      boolean isStub, Mutability defmut, boolean isField) {
    Mutability m = null;

    // Iterate through each possible Mutability and lookup each one
    // to make sure source only has one mutability annotation on it.
    for (Mutability mut : Mutability.values()) {
      if (source.lookup(mut.annotationName) != null) {
        if (m == null)
          m = mut;
        else {
          throw new RuntimeException("Two different mutability annotations " +
          	        "on the same element: dest: " + dest + "  source: " + source +
                        "  isStub: " + isStub + "  defmut: " + defmut +
                        "  isField: " + isField);
        }
      }
    }
    if (m == Mutability.THIS_MUTABLE && !isField)
      throw new RuntimeException("@ThisMutable on or inside something other than a field!");
    if (dest instanceof MutType) {
      MutType mdest = (MutType) dest;
      if (m != null)
        mdest.setMutability(m);
      else if (mdest.getMutability() == Mutability.UNKNOWN && isStub) {
        if (dest instanceof ClassType &&
            classIsUnmodifiable(classDescToName(
                ((ClassType) dest).getBaseType())))
          defmut = Mutability.READONLY;
        mdest.setMutability(defmut);
      }
    } else if (m != null) {
      // TODO: figure out why <java.lang.String: void <init>(char[],int,int)>
      // causes this problem
      // System.err.println("Error: AnnotationLoader.loadTypeLayer(): got mutability annotation on something that isn't a MutType");
      //  throw new RuntimeException("Mutability annotation on something that isn't a MutType");
    }
  }

  /**
   * If the lower bound of the given type argument
   * is unrestricted (a null type), this returns
   * the upper bound.  Else, returns the lower bound.
   */
  static JrType getLowestRestrictedBound(TypeArg targ) {
    if (targ.getLowerBound() instanceof NullType)
      return targ.getUpperBound();
    else
      return targ.getLowerBound();
  }

  /**
   * This class is a mapper that loads the annotations anywhere in a
   * type from an element in the annotation scene library into
   * a JrType.
   */
  static class JrTypeASTMapper extends TypeASTMapper<JrType> {

    /**
     * Whether the class being loaded is a stub.
     */
    final boolean isStub;

    /**
     * The default mutability for unannotated types.
     */
    final Mutability defmut;

    /**
     * Whether it is a field that is being loaded.
     */
    final boolean isField;

    /**
     * Constructs a new mapper to load the annotations on a new type.
     *
     * @param isStub - whether the class being loaded is a stub
     * @param defmut - the default mutability for unannotated types
     * @param isField - whether it is a field that is being loaded
     */
    JrTypeASTMapper(boolean isStub, Mutability defmut, boolean isField) {
      this.isStub = isStub;
      this.defmut = defmut;
      this.isField = isField;
    }

    /**
     * Returns the lowest restricted type of the element type if
     * the given type is an array type.  Else, returns null.
     */
    @Override
    protected JrType getElementType(JrType n) {
      if (n instanceof ArrayType)
        return getLowestRestrictedBound(((ArrayType) n).getElemType());
      else
        return null;
    }

    /**
     * Returns the lowest restricted type of the type argument
     * at the given index into the given type.
     */
    @Override
    protected JrType getTypeArgument(JrType n, int index) {
      return getLowestRestrictedBound(((ClassType) n).getTypeArgs().get(index));
    }

    /**
     * Loads all the types from the given ATypeElement to the given JrType.
     */
    @Override
    protected void map(JrType type, ATypeElement element) {
      loadTypeLayer(type, element, isStub, defmut, isField);
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

  /**
   * Performs a deep copy of the mutabilities in <code>src</code> to the
   * mutabilities in <code>dest</code>
   */
  private static void copyMutabilities(JrType src, JrType dest) {
    if (src instanceof MutType && dest instanceof MutType) {
      MutType msrc = (MutType) src, mdest = (MutType) dest;
      mdest.setMutability(msrc.getMutability());
      if (msrc instanceof ArrayType && mdest instanceof ArrayType) {
        ArrayType asrc = (ArrayType) msrc, adest = (ArrayType) mdest;
        TypeArg tasrc = asrc.getElemType(), tadest = adest.getElemType();
        copyMutabilities(tasrc.getUpperBound(), tadest.getUpperBound());
        copyMutabilities(tasrc.getLowerBound(), tadest.getLowerBound());
      } else if (msrc instanceof ClassType && mdest instanceof ClassType) {
        ClassType csrc = (ClassType) msrc, cdest = (ClassType) mdest;
        List<TypeArg> tassrc = csrc.getTypeArgs(), tasdest = cdest.getTypeArgs();
        for (int i = 0; i < tassrc.size() && i < tasdest.size(); i++) {
          TypeArg tasrc = tassrc.get(i), tadest = tasdest.get(i);
          copyMutabilities(tasrc.getUpperBound(), tadest.getUpperBound());
          copyMutabilities(tasrc.getLowerBound(), tadest.getLowerBound());
        }
      }
    }
  }

  /**
   * Returns true if and only if the given type represents a
   * {@link Mutability#READONLY} object.
   */
  public static boolean typeIsReadOnlyObject(JrType t) {
    if (t instanceof ClassType) {
      ClassType ct = (ClassType) t;
      return (ct.getBaseType().equals("Ljava/lang/Object;")
              && ct.getMutability() == Mutability.READONLY);
    } else
      return false;
  }

  /**
   * Expands the given type argument by copying a restricted non-readonly
   * mutability from the lower bound to the upper bound.
   */
  private static void expandTypeArg(TypeArg targ) {
    JrType upper = targ.getUpperBound();
    JrType lower = targ.getLowerBound();
    if (lower instanceof NullType) {
      expandType(upper);
    } else {
      expandType(lower);
      if (!typeIsReadOnlyObject(upper))
        copyMutabilities(lower, upper);
    }
  }

  /**
   * Expands the given type by expanding any base types (if it is an array)
   * and any type arguments (if it is a class type).
   * See expandTypeArg(TypeArg).
   */
  private static void expandType(JrType t) {
    if (t instanceof ArrayType) {
      ArrayType at = (ArrayType) t;
      expandTypeArg(at.getElemType());
    } else if (t instanceof ClassType) {
      ClassType ct = (ClassType) t;
      List<TypeArg> targs = ct.getTypeArgs();
      for (TypeArg targ : targs)
        expandTypeArg(targ);
    }
  }

  /**
   * Replaces any {@link Mutability#QUESTION_RO} mutabilities in the
   * upper or lower bounds in the given type argument with the
   * {@link Mutability#MUTABLE} mutability.
   */
  private static void replaceQuestionRo(TypeArg targ) {
    JrType upper = targ.getUpperBound();
    JrType lower = targ.getLowerBound();
    replaceQuestionRo(upper);
    replaceQuestionRo(lower);
  }

  /**
   * Replaces any {@link Mutability#QUESTION_RO} mutabilities in the
   * given type with the {@link Mutability#MUTABLE} mutability.
   */
  public static void replaceQuestionRo(JrType t) {
    if (t instanceof MutType) {
      MutType mt = (MutType) t;
      if (mt.getMutability() == Mutability.QUESTION_RO)
        mt.setMutability(Mutability.MUTABLE);
    }
    if (t instanceof ArrayType) {
      ArrayType at = (ArrayType) t;
      TypeArg eta = at.getElemType();
      replaceQuestionRo(eta);
    } else if (t instanceof ClassType) {
      ClassType ct = (ClassType) t;
      List<TypeArg> targs = ct.getTypeArgs();
      for (TypeArg targ : targs)
        replaceQuestionRo(targ);
    }
  }

  /**
   * Desugars the given type argument.  If the type argument has
   * the {@link Mutability#QUESTION_RO} mutability, it sets the upper
   * mutability to {@link Mutability#READONLY} and the lower
   * mutability to {@link Mutability#MUTABLE}.  It also
   * transitively desugars the upper and lower bounds.
   */
  private static void desugarQuestionRo(TypeArg targ) {
    JrType upper = targ.getUpperBound();
    JrType lower = targ.getLowerBound();
    if (lower instanceof MutType &&
        ((MutType) lower).getMutability() == Mutability.QUESTION_RO) {
      MutType mupper = (MutType) upper;
      MutType mlower = (MutType) lower;
      mupper.setMutability(Mutability.READONLY);
      desugarQuestionRo(mupper);
      mlower.setMutability(Mutability.MUTABLE);
      replaceQuestionRo(mlower);
    } else {
      desugarQuestionRo(upper);
      desugarQuestionRo(lower);
    }
  }

  /**
   * Desugars the input type.
   * See desugarQuestionRo(TypeArg).
   */
  private static void desugarQuestionRo(JrType t) {
    if (t instanceof ArrayType) {
      ArrayType at = (ArrayType) t;
      TypeArg eta = at.getElemType();
      desugarQuestionRo(eta);
    } else if (t instanceof ClassType) {
      ClassType ct = (ClassType) t;
      List<TypeArg> targs = ct.getTypeArgs();
      for (TypeArg targ : targs)
        desugarQuestionRo(targ);
    }
  }

  /**
   * Returns an exception stating that the object in the given description
   * does not exist.
   */
  private static RuntimeException notThere(String desc) {
    return new RuntimeException("Annotation on nonexistent " + desc);
  }

  /**
   * Returns an exception stating that the object in the given description
   * does not exist, and the cause of the returned exception is the input
   * exception.
   */
  public static RuntimeException wrap(RuntimeException e1, String desc) {
    return new RuntimeException("While processing " + desc, e1);
  }

  /**
   * Returns an arbitrary element from a Set, which must not be empty.
   */
  private static <E> E arbElement(Set<E> m) {
    return m.iterator().next();
  }
}
