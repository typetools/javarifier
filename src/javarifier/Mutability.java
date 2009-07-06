package javarifier;

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
     * The annotation describing this mutability.
     */
    private String annotationName;
    
    /**
     * Constructs a Mutability from a given annotation.
     */
    private Mutability(String annotationName) {
      this.annotationName = annotationName;
    }
    
    /**
     * Returns the fully qualified name of the annotation describing this
     * mutability, excluding the at-sign. (E.g. "checkers.javari.quals.ReadOnly")
     * @return the name of the annotation describing this mutability
     */
    public String annotation() {
      return annotationName;
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
      // Don't want to lookup by name() "checkers.javari.quals.ReadOnly"
      // Actually want to lookup by annotation():
      for(Mutability m : Mutability.values()) {
        if(m.annotation().equals(annotationName)) {
          return m;
        }
      }
      throw new RuntimeException("Mutability: unknown Mutability for" +
      		"annotation: " + annotationName);
    }
}
