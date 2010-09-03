package javarifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;

/**
 * The HeuristicsVisitor visits a Scene and hueristically sets
 * fields that should be excluded from the abstract state of the object
 * to be assignable.  (There are no heuristics yet for inferring
 * the mutable field annotation, but they could be implemented here if
 * they existed.)
 * It will modify the Scene by calling SootField.setAssignable(true)
 * on each SootField that should be assignable.  This class is a visitor
 * that visits the entire scene and can then make fields assignable.
 * To perform both of these steps, simply call applyHeuristics(Scene) on the
 * desired Scene.
 *
 * The current hueristics (from Jaime Quinonez's thesis, see
 * http://groups.csail.mit.edu/pag/pubs/Quinonez2008-abstract.html
 * ) that are implemented are:
 *   - (Only fields that are private and do not have the final keyword
 *      may be inferred to be assignable.  Non-private fields should not be
 *      assignable because they are already exposed outside of their
 *      containing class and thus form part of the specification
 *      of that class.
 *      Fields that are final should not be set to assignable because then
 *      the assignable annotation is meaningless,  this heuristic would
 *      lead to a large number of fields that are supposed to represent
 *      constants to be inferred as assignable
 *      Also, only instance fields may inferred to be assignable;
 *      static fields are not part of the class' abstract state.)
 *   - A field is assignable if it is only set in the constructor or in
 *      equals/hashCode.
 *   - A field is assignable if it is not read in equals/hashCode (and those
 *      methods are implemented in the class).
 *   - A field is assignable if it is written to in a method that overrides
 *      either Iterator.next() or Enumeration.nextElement().
 *
 */
public class HeuristicsVisitor extends SceneVisitor {

  /**
   * This method visits the Scene and sets to assignable every field that the
   * heuristics recommend to be assignable.
   *
   * @param scene the scene on which to apply the heuristics
   */
  public static void applyHeuristics(Scene scene) {
    // Visiting the scene only gathers field read/write information,
    //  then heuristics are applied using this information.
    HeuristicsVisitor fuv = new HeuristicsVisitor();
    fuv.visitScene(scene);

    fuv.applyHeuristicsFromUsage();
  }


  /**
   *  This list keeps track of all classes that were analyzed and whose
   *  field information is in fieldWrittenMap and fieldReadMap.
   */
  private List<SootClass> classesAnalyzed;

  /**
   *  A map from a field to the set of methods in which it is written.
   */
  private Map<SootField, Set<SootMethod>> fieldWrittenMap;

  /**
   * A map from a field to the set of methods in which it is read.
   */
  private Map<SootField, Set<SootMethod>> fieldReadMap;

  /**
   * Private constructor - there shouldn't be a need to initialize elsewhere,
   * applyHeuristics() should be used instead.
   */
  private HeuristicsVisitor() {
    classesAnalyzed = new ArrayList<SootClass>();
    fieldWrittenMap = new HashMap<SootField, Set<SootMethod>>();
    fieldReadMap = new HashMap<SootField, Set<SootMethod>>();
  }

  /**
   * This method should only be called once the Scene has been visited.
   * It will apply the heuristics to all fields in all classes inside
   * classesAnalyzed.
   */
  private void applyHeuristicsFromUsage() {
    for (SootClass sc : classesAnalyzed) {
      // Soot is not generic, so these casts cannot be eliminated.
      for (Object o : sc.getFields()) {
        SootField field = (SootField) o;

        // can't remove non-private, final, or static fields from state
        if (!field.isPrivate() ||
            field.isFinal() ||
            field.isStatic()) {
          continue;
        }

        // If any of these heuristics recommend the field to be assignable,
        //  they should set setAssignable to true, and the end of this
        //  method will set it to assignable.
        boolean setAssignable = false;


        // heuristic:
        // For inferring assignable fields inside Iterator and
        // Enumeration, check that the field is in the right method that
        // extends one of these classes.
        Set<SootMethod> writeMethods = fieldWrittenMap.get(field);
        if (writeMethods != null) {
          for (SootMethod m : writeMethods) {
            // Check that the method has the correct name:
            // m.getName() returns a String of the form: <Iterator: Node next()>
            if (m.getName().toString().contains(" next(") ||
               m.getName().toString().contains(" getNext(")) {

              // To check that this method is in a class that extends
              // one of {Iterator,Enumeration}, go up the class hierarchy
              // until a class with that name is found in either one
              // of the classes or one of the interfaces.
              SootClass clazz = m.getDeclaringClass();
              while(!clazz.getName().toString().contains("Object")) {
                if (clazz.getName().toString().contains("Iterator") ||
                    clazz.getName().toString().contains("Enumeration")) {
                  setAssignable = true;
                }
                for (Object iface : clazz.getInterfaces()) {
                  if (iface.toString().contains("Iterator") ||
                     iface.toString().contains("Enumeration")) {
                    setAssignable = true;
                  }
                }
                clazz = clazz.getSuperclass();
              }
            }
          }
        }

        // heuristic:
        // Field assignable if it is only assigned to in constructor and
        //  equals/hashCode/toString
        Set<SootMethod> writtenMethods = fieldWrittenMap.get(field);
        if (writtenMethods == null) {
          setAssignable = true; // not written to at all
        } else if (onlyContainsObjectROMethods(writtenMethods)) {
          setAssignable = true;
        }

        // heuristic 2:
        // Field assignable if it is not read in equals/hashcode
        if (!setAssignable) {
          Set<SootMethod> readMethods = fieldReadMap.get(field);
          if (readMethods == null) {
            setAssignable = true; // not read at all
          } else if (!readInEqualsHashCode(readMethods)){
            // This heuristic only applies if class actually implements equals()
            // or hashCode
            if (field.getDeclaringClass().declaresMethodByName("equals") ||
               field.getDeclaringClass().declaresMethodByName("hashCode")) {
              setAssignable = true;
            }
          }
        }

        // Even if multiple heuristics recommend the field should be
        // assignable, only set it once.
        if (setAssignable) {
          field.setAssignable(true);
        }
      }
    }
  }

  /**
   * Returns true if and only if the input set of methods only
   * contains a subset of constructor, equals and hashCode
   */
  private boolean onlyContainsObjectROMethods(Set<SootMethod> methods) {
    for (SootMethod method : methods) {
      String name = method.toString();
      if (!(name.contains("<init>")
          || name.contains("equals")
          || name.contains("hashCode"))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if and only if the input set contains either equals
   *  or hashCode methods.
   */
  private boolean readInEqualsHashCode(Set<SootMethod> methods) {
    for (SootMethod method : methods) {
      String name = method.toString();
      if (name.contains("equals") || name.contains("hashCode")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Records the fact that the given field is written to in the given method.
   */
  private void addFieldWritten(SootField field, SootMethod method) {
    // Add the mapping field -> method, vivifying the set of methods field
    //  maps to, if necessary.
    Set<SootMethod> methods = fieldWrittenMap.get(field);
    if (methods == null) {
      methods = new LinkedHashSet<SootMethod>();
      fieldWrittenMap.put(field, methods);
    }
    methods.add(method);
  }

  /**
   * Records the fact that the given field is read in the given method.
   */
  private void addFieldRead(SootField field, SootMethod method) {
    // Add the mapping field -> method, vivifying the set of methods
    //  field maps to, if necessary.
    Set<SootMethod> methods = fieldReadMap.get(field);
    if (methods == null) {
      methods = new LinkedHashSet<SootMethod>();
      fieldReadMap.put(field, methods);
    }
    methods.add(method);
  }

  /**
   * Visits the class if the class is resolved to examine method bodies.
   */
  @Override
  public void visitClass(SootClass clazz) {
    if (clazz.resolvingLevel() >= SootClass.BODIES) {
      classesAnalyzed.add(clazz);
      super.visitClass(clazz);
    }
  }

  /**
   * Visits the body of the method to determine field usage information.
   */
  @Override
  public void visitBody(Body body) {
    // FieldUsageSwitch visits the type each statement it is applied to,
    //  and it records fields/writes.
    FieldUsageSwitch fus = new FieldUsageSwitch(this, body.getMethod());

    for (Unit stmt : body.getUnits()) {
      ((Stmt) stmt).apply(fus);
    }
  }

  /**
   * FieldUsageSwitch extends AbstractStmtSwitch, which is a visitor that
   * can be applied to statements inside method bodies.
   */
  public static class FieldUsageSwitch extends soot.jimple.AbstractStmtSwitch {
    private HeuristicsVisitor fuv;
    private SootMethod enclosingMethod;

    /**
     * Constructs a visitor for Jimple statements.
     *
     * @param fuv - the heuristics visitor keeping track of field reads/writes
     * @param method - the enclosing method to visit
     */
    public FieldUsageSwitch(HeuristicsVisitor fuv, SootMethod method) {
      this.fuv = fuv;
      this.enclosingMethod = method;
    }

    /**
     * Given a soot.Unit, records the fact that all fields inside it are
     * being read.
     *
     * @param unit the unit to examine for field reads
     */
    private void caseUnit(Unit unit) {
      List<ValueBox> boxes = (List<ValueBox>) unit.getUseBoxes();
      for (ValueBox box : boxes) {
        addPossibleFieldRead(box.getValue());
      }
    }

    /**
     * Given a soot.Stmt, records the fact that all fields inside it are
     * being read.
     *
     * @param stmt the statement to examine for field reads
     */
    private void caseStmt(Stmt stmt) {
      caseUnit(stmt);
      if (stmt.containsFieldRef()) {
        FieldRef fieldRef = stmt.getFieldRef();
        addPossibleFieldRead(fieldRef);
      }
    }

    /**
     * Records that the input value is a field read in this method
     * if the value is an InstanceFieldRef.  (So if the value represents a
     * string literal being read, it is ignored.
     *
     * @param v the soot.Value that is being read
     */
    private void addPossibleFieldRead(Value v) {
      if (v instanceof InstanceFieldRef) {
        InstanceFieldRef ifr = (InstanceFieldRef) v;
        SootField field = ifr.getField();
        fuv.addFieldRead(field, enclosingMethod);
      }
    }

    /**
     * Examines an assign statement and records the fact that the left hand side
     * (if it is a field) is being written to, and records the right hand side
     * as a field read.
     */
    @Override
    public void caseAssignStmt(AssignStmt stmt) {
      Value lhs = stmt.getLeftOp();

      if (lhs instanceof InstanceFieldRef) {
        InstanceFieldRef ifr = (InstanceFieldRef) lhs;
        SootField field = ifr.getField();
        fuv.addFieldWritten(field, enclosingMethod);
      }

      addPossibleFieldRead(stmt.getRightOp());
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseBreakpointStmt(BreakpointStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseGotoStmt(GotoStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseIdentityStmt(IdentityStmt stmt) {
      addPossibleFieldRead(stmt.getLeftOp());
      addPossibleFieldRead(stmt.getRightOp());
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseIfStmt(IfStmt stmt) {
      addPossibleFieldRead(stmt.getCondition());
      caseStmt(stmt.getTarget());
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseInvokeStmt(InvokeStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
      caseStmt(stmt);

      List<Unit> units = (List<Unit>) stmt.getTargets();
      for (Unit unit : units) {
        caseUnit(unit);
        if (unit instanceof Stmt) {
          caseStmt((Stmt) unit);
        }
      }
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseNopStmt(NopStmt stmt) {
      // nop
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseRetStmt(RetStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseReturnStmt(ReturnStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseTableSwitchStmt(TableSwitchStmt stmt) {
      caseStmt(stmt);
    }

    /**
     * Examines stmt and records any field reads in this method.
     */
    @Override
    public void caseThrowStmt(ThrowStmt stmt) {
      caseStmt(stmt);
    }
  }
}
