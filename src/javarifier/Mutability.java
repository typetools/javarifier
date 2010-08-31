package javarifier;
import annotations.el.AnnotationDef;
import annotations.field.AnnotationFieldType;
import java.util.Collections;
import static annotations.Annotations.typeQualifierMetaAnnotations;

/**
 * The Mutability enum enumerates all the Javari mutability keywords.
 */
public enum Mutability {
    READONLY("checkers.javari.quals.ReadOnly"),
    MUTABLE("checkers.javari.quals.Mutable"),
    THIS_MUTABLE("checkers.javari.quals.ThisMutable"),
    QUESTION_RO("checkers.javari.quals.QReadOnly"),
    UNKNOWN("checkers.javari.quals.UNKNOWN"),
    POLYREAD("checkers.javari.quals.PolyRead");

    /**
     * The fully qualified name of the annotation describing this
     * mutability, excluding the at-sign. (E.g. "checkers.javari.quals.ReadOnly")
     */
    final String annotationName;

    final AnnotationDef annotationDef;

    /**
     * Constructs a Mutability from a given annotation.
     */
    private Mutability(String annotationName) {
      this.annotationName = annotationName;
      this.annotationDef = new AnnotationDef(annotationName,
                                             typeQualifierMetaAnnotations);
      this.annotationDef.fieldTypes = Collections.<String, AnnotationFieldType> emptyMap();
    }

    /**
     * Looks up the Mutability that corresponds to the given annotation.
     * The annotation should be the fully-qualified class name, without
     * the at-sign, e.g. "checkers.javari.quals.ReadOnly"
     *
     * @param annotationName - the annotation to look up
     * @return the mutability corresponding to <code>annotationName</code>
     */
    public static Mutability lookup(String annotationName) {
      // Don't want to look up by name() "checkers.javari.quals.ReadOnly"
      // Actually want to look up by annotation():
      for (Mutability m : Mutability.values()) {
        if (m.annotationName.equals(annotationName)) {
          return m;
        }
      }
      throw new RuntimeException("Mutability: unknown Mutability for" +
                "annotation: " + annotationName);
    }
}
