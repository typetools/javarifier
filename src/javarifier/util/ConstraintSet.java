package javarifier.util;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Set;

import javarifier.ConstraintVar;
import javarifier.Main;
import javarifier.Options;

/**
 * ConstraintSet models a set that contains unguarded, guarded and 
 * double-guarded constraints.
 * 
 * For example, an unguarded constraint is the simple constraint variable "a".  
 * This type of constraint means that the reference "a" is mutable. 
 * 
 * This class never modifies any of the constraints that are inserted into it.
 * 
 * Refer to Jaime Quinonez's thesis (javari/design/thesis-jaime or 
 * available online at:
 * <a href="http://pag.csail.mit.edu/javari">http://pag.csail.mit.edu/javari</a>
 * )
 * for a description of what these constraints represent in the program.
 * A "constraint variable" in the documentation (which is equivalent to 
 * an "unguarded constraint") is represented by javarifier.ConstraintVar
 * 
 * Guarded constraints represent the fact that a reference is mutable if 
 * another reference happens to be mutable.  A single guarded constraint, of the
 * form "a -&gt; b", means that if the reference "a" is mutable, then "b" is
 * mutable.  A double-guarded constraint, of the form "a -&gt; (b -&gt; c)", means
 * that if "a" is mutable and "b" is mutable, then you have that "c" is mutable.
 * The double guarded constraint should be thought of as there being two 
 * conditions that need to be met for the last reference to be mutable.
 * The constraints: "a -&gt; (b -&gt; c)" and "b -&gt; (a -&gt; c)" have exactly the same
 * meaning.
 * 
 * A constraint set solution is the set of variables known to be true
 * according to the constraints added to the set.  For example, given "a",
 * "a -&gt; b", "c -&gt; d", the solution is "a, b".  Another example is "e -&gt; (f
 * -&gt; g)", "e", "f", whose solution is "e", f, g".
 *
 * This class is designed to only handle
 * constraints of the types "a", "a -&gt; b" and "a -&gt; (b -&gt; c)".  Other
 * constraints such as "(a -&gt; b) -&gt; c" or "a -&gt; (b -&gt; (c -&gt; d))" are
 * not supported.  The Javarifier algorithm uses only these three types.
 *
 * This class (and the rest of Javarifier) represents guarded
 * and guarded constraints by using a map to store the relationships over
 * all constraint variables.  That is, mapping a constraint variable "a" to
 * the constraint variable "b" represents the guarded constraint "a-&gt;b".
 * There are no classes such as "GuardedConstraint" or 
 * "DoubleGuardedConstraint."
 * This representation is necessary because the algorithm specifies that one
 * should be able to look up all guarded constraints with a certain constraint
 * variable as the guard in constant time.
 * 
 * For single-guarded constraints, the guarded map stores a mapping from
 * the guard to the set of constraint variables it guards.  The
 * constraints "a-&gt;b", "a-&gt;c", "c-&gt;d" are represented as the map:
 * { a-&gt;{b,c}, c-&gt;{d} }
 * 
 * For double-guarded constraint, the twiceGuarded map stores a mapping from
 * the guard to the single-guarded constraints it guards.  The 
 * constraints "a-&gt;b-&gt;c", "a-&gt;b-&gt;d", "b-&gt;d-&gt;c" are represented as the map:
 * { a-&gt;{b-&gt;c,b-&gt;d}, b-&gt;{d-&gt;c} }
 */
public class ConstraintSet<T extends javarifier.ConstraintVar> 
  implements Iterable<T> {

    /** Unguarded constraints. */
    private Set<T> unguarded = new LinkedHashSet<T>();

    /**
     * Guarded constraints.  A MultiMap maps a key (the guard) to a set of
     * values (the consequents in guarded constraints with that guard).
     */
    private MultiMap<T, T> guarded = new HashMultiMap<T, T>();

    /**
     * Double-guarded constraints.  A MultiMap maps a key (the guard) to a set
     * of values (the consequents in double-guarded constraints with that 
     * guard).
     * The representation of the single-guarded constraints here is as a pair.
     * They are converted to the map representation when (and if) they are
     * added to the guarded constraint set (and inserted into the guarded map).
     */
    private MultiMap<T, Pair<T, T>> twiceGuarded
        = new HashMultiMap<T, Pair<T, T>>();

    /** 
     * Adds the unguarded constraint c to the unguarded constraint set. 
     */
    public void add(T c) {
        unguarded.add(c);
    }

    /** 
     * Adds the guarded constraint c1 -&gt; c2 to the unguarded constraint set. 
     */
    public void add(T c1, T c2) {
        guarded.put(c1, c2);
    }

    /** 
     * Adds the guarded constraint c1 -&gt; (c2 -&gt; c3) to the double-guarded
     * constraint set. 
     */
    public void add(T c1, T c2, T c3) {
        twiceGuarded.put(c1, new Pair<T, T>(c2, c3));
    }

    /**
     * Returns a new ConstraintSet that contains the unguarded, guarded, 
     * and double-guarded constraints from both 
     * the input constraint set and this constraint set.
     * It does not modify either the input constraint set or this constraint 
     * set.  It adds the constraints directly, without copying them, because
     * constraints are immutable.
     * 
     * @param cs the constraint set to combine with this
     * @return a new ConstraintSet containing the union of constraints from
     *  this and cs
     */
    public ConstraintSet<T> combine(ConstraintSet<T> cs) {
        ConstraintSet<T> ret = new ConstraintSet<T>();

        ret.unguarded.addAll(this.unguarded);
        ret.unguarded.addAll(cs.unguarded);

        ret.guarded.putAll(this.guarded);
        ret.guarded.putAll(cs.guarded);

        ret.twiceGuarded.putAll(this.twiceGuarded);
        ret.twiceGuarded.putAll(cs.twiceGuarded);

        return ret;
    }

    /**
     * Solves the unguarded, guarded and double-guarded constraints in this
     * and returns the resulting set of unguarded constraints.  These are
     * all the variables known to be mutable in a program.
     *
     * Solving the constraints does not modify this. 
     * 
     * This algorithm takes linear time in the total number of constraints
     * (in expectation, reliant on hash
     * maps taking constant time for lookups and additions in expectation).
     * 
     * The algorithm for solving constraints takes linear time in the total 
     * number of constraints.  The basic algorithm is to use a work-list
     * of all the unguarded constraints left to propagate.  The work-list
     * is initialized with all the unguarded constraints.  For each unguarded
     * constraint in the work-list, that constraint is used to possibly
     * satisfy the guards for some single-guarded and double-guarded 
     * constraints.
     * For single-guarded constraints, the algorithm looks up all consequents
     * guarded by a given guard, and adds those to the unguarded constraint set
     * and to the work-list if they were not already in the unguarded constraint
     * set.
     * For double-guarded constraints, the algorithm looks up all consequents
     * guarded by a given guard.  Each of these consequents is a single-guarded
     * constraint of the form "a-&gt;b".  If "a" is already in the unguarded 
     * constraint set, the algorithm adds "b" to the unguarded constraint set,
     * and to the work-list if it was not already in the unguarded constraint 
     * set.  If "a" is not in the unguarded constraint set, the algorithm adds
     * "a-&gt;b" to the single-guarded constraint set.
     *
     * @return the set of unguarded constraints after solving the constraint
     * sets in this
     */
    public Set<T> solve() {
      // Here is pseudo-code for the algorithm, taken from Jaime's thesis:
      // - Let U be the set of unguarded constraints, of the form "a"
      // - Let G be the set of guarded constraints, of the form "a->b"
      // - Let D be the set of double-guarded constraints, of the form "a->b->c"
      //     
      // initialize W with all the constraints from U
      // while W is not empty
      //   pop a constraint "a" from W
      //   for each constraint "g" in G that has "a" as its guard
      //     let "c" be the consequent of "g"
      //     if "c" is not in U, add "c" to W and to U
      //
      //   for each double-guarded constraint "d" in D that has "a" as \
      //     its first guard
      //     let "b -> c" be the consequent of "d"
      //     if "b" is in U
      //       if "c" is not in U, add "c" to W and to U
      //     else, add "b -> c" to G

      // Note that extracting the commonality of adding to workList if
      // not already in unguarded shouldn't be extracted out because
      // you would have to pass around these worklists, and I think 
      // it obstructs the explicit description of the algorithm
      
      // Calling ConstraintVar.addCause() is not a mutation because this
      // is debug information.
      
      // Shadow fields, this method is not supposed to mutate this.
      Set<T> unguarded = new LinkedHashSet<T>(this.unguarded);
      MultiMap<T, T> guarded = new HashMultiMap<T, T>(this.guarded);
      MultiMap<T, Pair<T, T>> twiceGuarded = new HashMultiMap<T, Pair<T, T>>(
          this.twiceGuarded);

      // Initialize W with contents of U
      Set<T> workList = new LinkedHashSet<T>();
      workList.addAll(unguarded);

      while (!workList.isEmpty()) {
        // Get (and remove) the next constraint to propagate from W.
        // (called "a" in pseudocode)
        T a = workList.iterator().next();
        workList.remove(a);

        // For single-guarded constraints, add the consequents to U and W.
        // Looking for constraints of the form: a->b
        for (T b : guarded.get(a)) {
          // Only add to work-list if it was not already in work-list.
          if (unguarded.add(b)) {
            workList.add(b);
          }
          // For each constraint variable that is added to the unguarded
          // constraint set, record the guard that caused it to be satisfied.
          b.addCause(a);
        }

        // For double-guarded constraints, add the consequents either to G,
        // or to U and W.
        // Looking for constraints of the form: a->(b->c)
        for (Pair<T, T> consequent : twiceGuarded.get(a)) {
          // Consequent is of the form: b->c
          T b = consequent.first;
          T c = consequent.second;

          // If single-guarded constraint has guard already known to be mutable,
          // act just like when satisfying guards from guarded constraint set.
          if (unguarded.contains(b)) {
            if (unguarded.add(c)) {
              workList.add(c);
            }
            c.addCause(b);
          } else {
            // Constraint not already satisfied, add to single guarded
            // constraint set for future consideration.
            guarded.put(b, c);
          }
        }
      }
      return unguarded;
    }
    
    /** 
     * Returns an iterator over the unguarded constraint set of this, after
     * solving.
     * Same as this.solve().iterator() 
     */
    public Iterator<T> iterator() {
      return solve().iterator();
    }
    
    /**
     * Returns a string representation of this, with each constraint on its
     * own line.  Given constraint variables "a", "b" and "c",
     * unguarded constraints have the form: "U: a"
     * guarded constraints have the form: "G: a -&gt; b"
     * and double-guarded constraints have the form: "GG: a =&gt; b -&gt; c"
     */
    public String toString() {
      StringBuilder ret = new StringBuilder();
      for (T t : unguarded) {
        ret.append("U: " + t);
        ret.append(String.format("/%n"));
      }
      for (T key : guarded.keySet()) {
        for (T val : guarded.get(key)) {
          ret.append("G: " + key + " -> " + val);
          ret.append(String.format("/%n"));
        }
      }
      for (T key : twiceGuarded.keySet()) {
        for (Pair<T, T> val : twiceGuarded.get(key)) {
          ret.append("GG: " + key + " => " + val.first() + 
                     " -> " + val.second());
          ret.append(String.format("/%n"));
        }
      }
      
      return ret.toString();
    }
}
