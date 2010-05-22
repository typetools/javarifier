package javarifier;

import java.util.*;

import javarifier.JrType.VarType;

import javarifier.util.Pair;

/**
 * A type index is an index into a (possibly) parametetric type
 * indicating which certain part of the type, including upper versus
 * lower bounds of a type argument.
 *
 * For example, with the type: List&lt;Set&lt;Date&gt;&gt;, one may wish to
 * specify the List&lt;Set&lt;Date&gt;&gt;, the Set&lt;Date&gt;, or
 * the Date part of the
 * type.  Note in this case, that the upper and lower bounds of the
 * type arguments are identical.  List&lt;? extends Date&gt; is a case where
 * the upper bound and the lower bound differ: the upper bound is Date
 * and the lower bound is the null type (the common subtype of all
 * types).
 *
 * Note that a TypeIndex refers to an entire type, not just the base
 * type.  That is, for List&lt;Date&gt;, there is no way to refer to just
 * List, only List&lt;Date&gt; and Date.  JrType has methods to access the
 * List part of List&lt;Date&gt;, however.
 *
 * The term "top level" is used to describe an entire type.  The top
 * level of List&lt;Date&gt; is List<&lt;Date&gt;.  The term "base type"
 * is used to
 * describe the Java 1.4 part of the type.  For example, the base type
 * of List&lt;Date&gt; is List.  The term "element type" is used to describe
 * the lowest-level element type of an array.  For example, the
 * element type of Date[][] is Date.
 *
 * A type index can also index into a an array.  For example, with
 * Date[][], it can specify the Date, Date[], and Date[][] parts of
 * the type.
 *
 * TypeIndex was designed for use with JrType, but can be used in
 * other contexts.
 *
 * A TypeIndex is 0-based for indexing VarTypes and Bounds.
 */
public class TypeIndex {

    /**
     * A list of VarTypes and Bounds representing the
     * different levels of this type.
     */
    private final List<Pair<VarType, Bound>> indexes;

    /**
     * Constructs a new TypeIndex out of the given indices.
     */
    private TypeIndex(List<Pair<VarType, Bound>> indexes) {
        this.indexes = Collections.unmodifiableList(indexes);
    }

    /**
     * The top level type.
     */
    private static TypeIndex topLevel =
      new TypeIndex(new ArrayList<Pair<VarType, Bound>>(0));

    /**
     * An index refering to the top level of a (possibly) parametric
     * type; for example, for List&lt;Date&gt;, it would refer to
     * List&lt;Date&gt;.
     */
    public static TypeIndex topLevel() {
        return topLevel;
    }

    /**
     * Produces a new TypeIndex equal to this one plus additional
     * index at i, bound.  For example, if this indexes the upper
     * bound of first type argument, this.addIndex(2, Bound.LOWER)
     * would index the lower bound of the second type argument to the
     * upper bound of the first type argument of a type.  If the
     * associated type was List&lt;? super Set&lt;? extends Date&gt;&gt;, this
     * would have refered to Set&lt;? extends Date&gt; and this.addIndex(2,
     * Bound.LOWER) would refer to Date.
     */
    public TypeIndex addIndex(VarType i, Bound bound) {
        List<Pair<VarType, Bound>> newIndexes = new ArrayList<Pair<VarType, Bound>>(this.indexes);
        newIndexes.add(new Pair(i, bound));
        return new TypeIndex(newIndexes);
    }

    /**
     * Returns a type index that is the same as this, without the
     * first type parameter.
     */
    public TypeIndex removeFirst() {
        List<Pair<VarType, Bound>> newIndexes = new ArrayList<Pair<VarType, Bound>>(this.indexes);
        newIndexes.remove(0);
        return new TypeIndex(newIndexes);
    }

    /**
     * Returns a type index that is the same as this, without the
     * last type parameter.
     */
    public TypeIndex removeLast() {
        List<Pair<VarType, Bound>> newIndexes = new ArrayList<Pair<VarType, Bound>>(this.indexes);
        newIndexes.remove(newIndexes.size() - 1);
        return new TypeIndex(newIndexes);
    }

    /**
     * Returns the last parameter in this.
     */
    public VarType getLastParam() {
        return indexes.get(indexes.size() - 1).first();
    }

    /**
     * Returns the last bound in this.
     */
    public Bound getLastBound() {
        return indexes.get(indexes.size() - 1).second();
    }

    /**
     * Returns the type parameter at the given index.
     */
    public VarType getTypeParam(int i) {
        return indexes.get(i).first();
    }

    /**
     * Returns the bound in this at the given index.
     */
    public Bound getBound(int i) {
        return indexes.get(i).second();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (obj instanceof TypeIndex) {
            TypeIndex other = (TypeIndex) obj;
            return other.indexes.equals(this.indexes);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return 1;
    }

    /**
     * Lazily-initialized String representation.
     */
    private String str;
    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (str == null) {
            if (indexes.size() > 0) {
                StringBuilder buf = new StringBuilder();
                buf.append("[");
                for (Pair<VarType, Bound> index : indexes) {
                    buf.append(index.first() + " " + index.second() + ", ");
                }
                // remove trailing ", "
                buf.deleteCharAt(buf.length()-1);
                buf.deleteCharAt(buf.length()-1);
                buf.append("]");
                str = buf.toString();
            } else {
                str = "[]";
            }
        }
        return str;
    }

}
