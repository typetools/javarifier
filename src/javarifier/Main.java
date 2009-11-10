package javarifier;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javarifier.JrType.VarType;
import soot.Scene;
import soot.SootField;

/**
 * Javarifier implements the refrence immutability inference algorithm for
 * Java explained in Jaime Quinonez's thesis, available at:
 * <a href="http://types.cs.washington.edu/javarifier">
 * http://types.cs.washington.edu/javarifier </a>
 *
 * Given as input a set of Java class files to {@link #main(String[])},
 * Javarifier will infer the reference immutability of all references in
 * those classes and in classes those classes depend upon.
 *
 * Consult the documentation at:
 * <a href="http://types.cs.washington.edu/javarifier"
 * http://types.cs.washington.edu/javarifier </a>
 * for more information about running Javarifier.
 *
 * All arguments
 * passed in are also passed to the underlying Soot process.  Javarifier
 * is implemented via a callback-style transformer where Soot calls
 * {@link JrTransformer} to perform the various inference algorithm steps.
 */
//Some key notes about all of Javarifier's implementation:
//- 'Jr' is a common prefix, short for 'Javari,' that is used to indicate
//that something is Javari-specific.  For example, JrType is a type
//that has mutability information.
//- Soot uses the singleton design pattern, and much of Javarifier has
//taken the same format.  The method v() is used to extract the singleton
//in all cases.  For example, javarifier.Options implements the singleton
//design pattern, and the singleton is obtained by calling
//javarifier.Options.v()
//- The main representation for containing classes is a Scene, which is
//Soot's format.  It contains a set of SootClass objects, one for
//each class in the scene.
public class Main {

  /**
   * The current Javarifier version.
   */
  public static final String versionString = "Javarifier version 0.1.3";

  /**
   * An array of all the default arguments that must be passed to Soot
   * for it to load all the class files.
   */
  private static final String[] sootDefaultArgs = {
    "-w",
    "-whole-program",
    "-f",
    "jimple",
    "-print-tags",
    "-p",
    "jb",
    "use-original-names:true",
    "-p",
    "jb.dae",
    "enabled:false",
    "-p",
    "jb.ne",
    "enabled:false",
    "-p",
    "jb.uce",
    "enabled:false",
    "-p",
    "wjop.smb",
    "enabled:false",
    "-p",
    "wjop.si",
    "enabled:false",
    "-p",
    "wjap",
    "enabled:false",
    "-p",
    "cg",
    "enabled:false",
    "-p",
    "jb.tr",
    "enabled:false",
    "-p",
    "wjtp",
    "enabled:true",
    "-p",
    "jb.ulp",
    "enabled:false",
    "-p",
    "jb.ls",
    "enabled:true",
    "-soot-classpath"
    // Now need soot-classpath, followed by any number of program classes
  };

  /** All the classes in the command-line to run inference over. */
  static String[] args;

  /**
   * The main method to perform the inference algorithm.  See user
   * documentation an explanation of the options.
   *
   * @param args - command-line options
   */
  public static void main(String[] args) {
    String[] commandLineArgs = null;

    try {
      // processes javarifier options and removes them from args
      commandLineArgs = Options.v().processCmdLine(args);
    } catch(Exception e) {
      System.out.println("Bad arguments:");
      for (String arg : args) {
        System.out.println("  " + arg);
      }
      e.printStackTrace(System.out);
      printUsage();
      return;
    }

    if (Options.v().printUsage()) {
      printUsage();
      return;
    }

    if (Options.v().printVersion()) {
      System.out.println(versionString);
      return;
    }

    // After Javarifier args have been taken out, append the whole
    // soot-classpath (program,stub and world classpath args), then the
    // rest of the command-line args (the program classes)
    List<String> argsList =
      new LinkedList<String>(Arrays.asList(sootDefaultArgs));

    // Note that there is no default for program classpath - user
    // must always supply it
    String sep = System.getProperty("path.separator");
    String sootCP =
      Options.v().getProgramCPEntries() + sep +
      Options.v().getStubCPEntries() + sep +
      Options.v().getWorldCPEntries();
    argsList.add(sootCP);

    // now the rest of the command-line args
    argsList.addAll(Arrays.asList(commandLineArgs));

    args = argsList.toArray(new String[] {});
    Main.args = args;

    // This line simply specifies that JrTransformer
    // should be called-back by Soot.  (See {@link JrTransformer})
    soot.PackManager.v().getPack("wjtp").add(
        new soot.Transform("wjtp.javarifier", new JrTransformer()));

    try {
      soot.Main.main(args);
    } catch(Exception e) {
      System.out.println("Javarifier exception: " + e);
      e.printStackTrace();
    } catch(Error e) {
      System.out.println("Javarifier Error: " + e);
      e.printStackTrace();
    }
  }

  /** Prints usage information. */
  private static void printUsage() {
    System.out.println("Javarifier usage: ");
    System.out.println("  javarifier <program-classpath> " +
    "<space-separated list of fully-qualified classes to analyze>");
  }



  /**
   * Javarifier is implemented using a callback-style transformer.
   * {{@link Main#Main()} calls Soot and passes in a JrTransformer.
   * Soot calls the following methods on a JrTransformer:
   *
   * {@link #prepareScene(Scene)} - Adds extra information to the
   * global scene by calling the static methods of several other classes
   * that each add a different type of information.  For example,
   * {@link MethodRefAdder#add(Scene)} adds a reference
   * to every soot.Local that points to the method that contains it.
   *
   * {@link #internalTransform(String, Map)} - Runs Javarifier
   * inference algorithm by generating the constraints, solving them,
   * and printing the results.
   */
  public static class JrTransformer extends soot.SceneTransformer {

    /**
     * Adds extra information to the given scene by loading
     * information from class files.  See:
     * {@link StubCleaner#cleanStubs(Scene)},
     * {@link BridgeMethodMarker#mark(Scene)},
     * {@link MethodRefAdder#add(Scene)},
     * {@link JrTyper#type(Scene)},
     * {@link AnnotationLoader#loadAllAnnotations(Scene)}.
     */
    private void prepareScene(Scene scene) {
      if (Options.v().justPrintStubs()) {
        return;
      }

      printNonQuiet("To disable progess messages, use -Q option.");

      printNonQuiet("Javarifier preparations started on: "
          + scene.getClasses().size() + " classes.");
      StubCleaner.cleanStubs(scene);
      printNonQuiet("StubCleaner finished.");

      BridgeMethodMarker.mark(scene);
      printNonQuiet("BridgeMethodMarker finished.");

      // Add a reference from each Local to its enclosing
      // method.
      MethodRefAdder.add(scene);
      printNonQuiet("MethodRefAdder finished.");

      JrTyper.type(scene);
      printNonQuiet("JrTyper finished.");

      AnnotationLoader.loadAllAnnotations(scene);
      printNonQuiet("AnnotationLoader finished.");
    }

    /**
     * Generates a set of Javarifier constraints over the given scene,
     * solves those constraints and then applies them.
     * This should
     * @param scene
     */
    private void javarifyScene(Scene scene) {
      if (Options.v().justPrintStubs()) {
        return;
      }
      // Constraint generation
      ConstraintManager cm = new ConstraintManager();
      cm = generateConstraints(cm);
      solveConstraints(cm);
    }

    /**
     * Generates all the Javarifier constraints and returns a new
     * {@link ConstraintManager} containing all the generated constraints
     * and the constraints in the input ConstraintManager.
     */
    private ConstraintManager generateConstraints(ConstraintManager cm) {
      ConstraintManager stubCons = StubConstraints.generate(Scene.v());
      cm = cm.combine(stubCons);
      printNonQuiet("StubConstraints finished.");

      if (Options.v().applyHeuristics()) {
        HeuristicsVisitor.applyHeuristics(Scene.v());
      }

      ConstraintManager fromCode =
        ConstraintGenerator.generate(Scene.v());
      cm = cm.combine(fromCode);
      printNonQuiet("ConstraintGenerator finished.");

      ConstraintManager boundGuards = BoundGuarder.guards(Scene.v());
      cm = cm.combine(boundGuards);
      printNonQuiet("BoundGuarder finished.");

      ConstraintManager paramCons =
        ParameterConstraints.constraints(Scene.v());
      cm = cm.combine(paramCons);
      printNonQuiet("ParameterConstraints finished.");

      ConstraintManager subtypeCons =
        SubtypeConstraints.generate(Scene.v());
      cm = cm.combine(subtypeCons);
      printNonQuiet("SubtypeConstraints finished.");

      if (Options.v().openWorld()) {
        ConstraintManager openWorldCons = OpenWorld.generate(Scene.v());
        cm = cm.combine(openWorldCons);
        printNonQuiet("OpenWorld finished.");
      }

      ConstraintTracker.printCauses();

      return cm;
    }

    /**
     * Solves all the constraints contained in the given
     * {@link ConstraintManager} and modifies the references that
     * are found to be mutable to have a {@link Mutability#MUTABLE}
     * mutability.
     *
     * See {@link javarifier.util.ConstraintSet#solve()} for a description of the
     * constraint solving algorithm.
     */
    private void solveConstraints(ConstraintManager cm) {
      // Matching the notation used in the javarifier paper,
      // the solved constraint set is the set of all references that
      // are known to be mutable.  Either zero, one, or two versions
      // of a variable may appear in the constraint set, since for each
      // variable there is a version of that variable in a readonly
      // context, and another in a mutable context.  Below, you use
      // these different context to infer polyread.

      Set<ConstraintVar> solved = cm.solve();
      printNonQuiet("Solved constraints.");

      if (Options.v().dumpConstraints()) {
        System.out.println("cm:\n" + cm + "\n");
        System.out.println("Solved:");
        for (ConstraintVar var : solved) {
          System.out.println(var);
        }
      }

      VarTracker.printCauses(solved);

      // Apply constraints to the references by marking non-readonly
      // references as either mutable, polyread or this-mutable.
      // UnknownEliminator converts the rest to readonly.
      for (ConstraintVar var : solved) {
        String s = var.toString();
        if (var.getValue() instanceof SootField &&
              (! ((SootField) var.getValue()).isStatic())) {
          // If instances fields are ever read as mutable, they must be
          // this-mutable.
          if (! var.getType().getMutability().equals(Mutability.MUTABLE)) {
            var.getType().setMutability(Mutability.THIS_MUTABLE);
          }
        } else if (var.getValue() instanceof SootField) {
          var.getType().setMutability(Mutability.MUTABLE);
        } else {
          if (var.getContext().equals(Context.READONLY)) {
            var.getType().setMutability(Mutability.MUTABLE);

            // var.getType().setMutability() changes the mutability in var.type,
            // and also changes the mutability in var.value.type in order to
            // account for change where VarType is mutable.
            JrType jr = var.getValue().getJrType();
            if (jr instanceof VarType) {
              VarType mu = (VarType) jr;
              mu.setMutability(Mutability.MUTABLE);
            }
          } else {
            if (! var.getType().getMutability().equals(
                Mutability.MUTABLE)) {
              var.getType().setMutability(Mutability.POLYREAD);
            }
          }
        }
      }
      printNonQuiet("Applied solutions to types.");

      // Unknowns are converted to readonly
      UnknownEliminator.eliminateUnknowns(Scene.v());
      printNonQuiet("UnknownEliminator finished.");

      // A return type shouldn't be polyread if none of the
      // parameters are.
      UnneededPolyreadRemover.remove(Scene.v());
      printNonQuiet("UnneededPolyreadRemover finished.");
    }

    /**
     * Applies the Javarifier inference algorithm by generating
     * the constraints for the Scene, solving them, and printing
     * the results.  This is the method Soot calls after
     * preparing the Scene with {{@link #prepareScene(Scene)}.
     *
     * The arguments are not used, but must be included to
     * match Soot's signature.
     */
    protected void internalTransform(String name, Map options) {
      // All Soot resolving will be done when we reach this point.
      if (Options.v().justPrintStubs()) {
        return;
      }

      AnnotationScene.v().readAllAnnotations();

      prepareScene(Scene.v());

      if (!Options.v().justPassThrough())
        javarifyScene(Scene.v());

      if (Options.v().dumpResults()) {
        String results = ScenePrinter.print(Scene.v());
        System.out.println(results);
      }

      String outFile = Options.v().getOutputFile();
      printNonQuiet("");
      printNonQuiet("Writing Javari's results to " +
          (outFile == null ? "standard output" : outFile));

      try {
        Writer out =
          (outFile == null) ? new OutputStreamWriter(System.out)
        : new FileWriter(outFile);
          Scene scene = Scene.v();
          Options.v().outputFormat().write(Scene.v(), out);
          out.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }

    /**
     * Prints the given String if the command-line option permits it.
     */
    private void printNonQuiet(String s) {
      if (!Options.v().reallyQuiet()) {
        System.out.println(s);
      }
    }
  }
}
