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
import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Unpublicized;

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
 * <a href="http://types.cs.washington.edu/javarifier">
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
//in all cases.
//- The main representation for containing classes is a Scene, which is
//Soot's format.  It contains a set of SootClass objects, one for
//each class in the scene.
public class Main {

  /**
   * The current Javarifier version.
   */
  public static final String versionString = "Javarifier version 0.1.4";

  /**
   * Print the short usage message.  This does not include verbosity or
   * debugging options.
   */
  @OptionGroup ("General Options")
  @Option (value="-h Print short usage message", aliases={"-help"})
  public static boolean help = false;

  /**
   * Print the extended usage message.
   * This includes verbosity and debugging options but not internal options.
   */
  @Option ("-H Print extended usage message (includes debugging options)")
  public static boolean allHelp = false;

  /**
   * Print the current Javarifier version.
   */
  @Option (value="-v Print program version", aliases={"-version"})
  public static boolean version = false;
  // end option group "General Options"
  
  /**
   * Be quiet, do not print much information.
   */
  @OptionGroup("Execution Options")
  @Option ("-Q Be quiet, do not print much information")
  public static boolean reallyQuiet = false;

  /**
   * Use heuristics to set fields that should be excluded from the abstract
   * state of the object to be assignable.
   * @see javarifier.HeuristicsVisitor
   */
  @Option ("Use heuristics to determine @Assignable fields")
  public static boolean applyHeuristics = false;

  /**
   * Forces all public method return types to have mutable (or polyread) types
   * and all public fields to have this-mutable types.
   * @see javarifier.OpenWorld
   */
  @Option ("Force all public method return types to be @Mutable and public fields @ThisMutable")
  public static boolean openWorld = false;

  /**
   * Do not split source locals.
   */
  // This option does not appear to be used anywhere.
  @Unpublicized
  @Option ("Do not split source locals")
  public static boolean doNotSplitSourceLocals = true; // Should be True
  // end option group "Execution Options"

  /**
   * Specify a colon-delimited classpath other than <b>$CLASSPATH</b> for
   * Javarifier to use to look up classes being analyzed.
   */
  @OptionGroup ("Input Options")
  @Option ("Specify a classpath to use to look up classes being analyzed")
  public static String programCPEntries = null;

  public static String getProgramCPEntries() {
    if (programCPEntries == null)
      return "";
    else
      return programCPEntries;
  }
  
  /**
   * Specify a colon-delimited classpath to look for stub classes. All
   * classes found in this path will be treated as stub classes.
   */
  @Option (value="Specify a classpath for stub classes", aliases={"--stubs"})
  public static String stubCPEntries = null;

  // Note that the default StubCPEntries are always used, no matter what
  // the stubCPEntries are.
  public static String getStubCPEntries() { 
    return defaultStubCPEntries + 
           (stubCPEntries == null ? ""
            : (System.getProperty("path.separator") + stubCPEntries)) +
           (useWorldAsStubs
            ? (System.getProperty("path.separator") + getWorldCPEntries())
            : ""); 
  }

  /**
   * By default, Javarifier assumes that the JDK classes required by the
   * program classes on which you are running Javarifier are in your
   * <b>$CLASSPATH</b>. If this is not the case, or if you wish to reference
   * a different JDK, you may use this followed by the colon-delimited
   * classpath of your JDK jarfiles.
   */
  @Option (value="Specify a classpath for JDK jarfiles", aliases={"--world"})
  public static String worldCPEntries = null;

  public static String getWorldCPEntries() { 
    return defaultWorldCPEntries +
           (worldCPEntries == null ? ""
            : (System.getProperty("path.separator") + worldCPEntries)); 
  }

  /**
   * If false (the default), then missing stubs cause Javarifier to halt.
   * If true, then Javarifier uses world classes as stubs, after both the
   * default stub entries and the stub entries.
   */
  @Option ("Do not halt on missing stubs")
  public static boolean useWorldAsStubs = false;

  /**
   * If specified, annotations will be loaded from this annotation file as well as
   * the class files for all classes in the Soot scene.
   */
  @Option ("<filename> Specify extra file from which to load annotations")
  public static String extraAnnotationInputFile = null;
  // end option group "Input Options"

  /**
   * By default, all output, including the results, is printed to standard
   * out. Use this option to output the results to an annotation file. Note
   * that Javarifier outputs the results for all classes into a single file.
   */
  @OptionGroup("Output Options")
  @Option (value="<filename> Output annotations to a file", aliases={"--output"})
  public static String outputFile = null;


  /**
   * Specify the output annotation format. Valid options are:
   * <ul>
   * <li><b>annotationIndexFile</b>, alias <b>index</b>
   * <li><b>scenePrinter</b>
   * <li><b>twoFiles</b>
   * <li><b>shay</b>
   * </ul>
   * See {@link javarifier.OutputFormat} for a description of these formats.
   */
  @Option (value="<format> Specify the output annotation format")
  public static String outputFormat = "index";

  public static OutputFormat outputFormat() {
      if (outputFormat.equals("annotationIndexFile")
          || outputFormat.equals("index"))
          return OutputFormat.ANNOTATION_INDEX_FILE;
      else if (outputFormat.equals("scenePrinter"))
          return OutputFormat.SCENE_PRINTER;
      else if (outputFormat.equals("twoFiles"))
          return OutputFormat.TWO_FILES;
      else if (outputFormat.equals("shay"))
          return OutputFormat.SHAY;
      else
          throw new IllegalArgumentException(
              "Unrecognized output format " + outputFormat);
  }

  /**
   * Tells {@link ScenePrinter} to omit a bunch of uninteresting local
   * variables from the output.  These are generally the ones that cannot be
   * annotated in source code, so this option is good for comparing
   * Javarifier results with manual annotations.
   */
  @Option ("Omit local variables from output")
  public static boolean outputFilterLocals = false;

  /**
   * Tells {@link ScenePrinter} to output "omitted" instead of the type for
   * elements not of the given construct.  The construct can be "field",
   * "local", "parameter", "receiver", or "return".  Useful to study the
   * types assigned to elements of each kind separately.
   */
  @Option ("<construct> Only output annotations for the given construct.")
  public static String outputLimitKind = null;

  /**
   * Tells {@link ScenePrinter} to output fields and methods sorted by name
   * instead of by order of appearance in the class file.  If several methods
   * have the same name, they are outputted in the order they appear in the
   * class file.
   */
  @Option ("Output fields and methods sorted by name")
  public static boolean outputSortMembers = false;

  /**
   * Tells {@link ScenePrinter} to output all the classes mentioned on the
   * command line (in the order of first mention on the command line) before
   * all the other classes.
   */
  @Option ("Output annotations for command-line specified classes first")
  public static boolean outputMainClassFirst = false;

  /**
   * The skipEmpty flag indicates that annotations should not be outputted 
   * for empty code.  That is, there should be no annotations for interfaces
   * and abstract methods.
   */
  @Option ("Do not output annotations for interfaces or abstract methods")
  public static boolean skipEmpty = false;
  
  /**
   * The includeImmutableClasses flag indicates that @ReadOnly annotations
   * should be output even for references that are of some type that is known
   * to be @Unmodifiable, such as java.lang.String.
   */
  @Option ("Output annotations for immutable classes")
  public static boolean includeImmutableClasses = false;
  // end option group "Output Options"

  /**
   * Do not perform inference.  Just read annotations (treating even program
   * classes as fully annotated), expand defaults and @Unmodifiable, and write
   * the results back out.  Use this mode to convert manual annotations to a
   * form in which they can be compared to Javarifier annotations.
   */
  @OptionGroup ("Utility Options (no inference)")
  @Option ("Read annotations, expand defaults, and write the results")
  public static boolean justPassThrough = false;

  /**
   * Do not perform inference.  Just output a list of classes for which stubs
   * are needed to perform inference.  In the future, an option such as
   * <tt>--missingStubs</tt> may be added, which only outputs a list of stubs
   * that are needed but missing.
   */
  @Option ("Output a list of stub classes needed to perform inference")
  public static boolean printStubs = false;
  // end option group "Utility Options (no inference)"

  /**
   * Enumerates the causes for each type
   * qualifier Javarifier infers.  For each annotation this outputs the
   * shortest-length inference chain.  See the manual section on
   * "Printing the cause tree".
   */
  @OptionGroup (value="Verbosity Options", unpublicized=true)
  @Option ("Dump shortest-length inference chain for each annotation")
  public static boolean dumpCauses = false;

  /**
   * Dump the raw constraints generated by Javarifier during its inference
   * process.  See the section on <code>-dumpConstraints</code> in the manual
   * for information.
   */
  @Option ("Dump raw constraints generated by Javarifier")
  public static boolean dumpConstraints = false;

  /**
   * Dump results.
   */
  @Option ("Dump results")
  public static boolean dumpResults = false;

  /**
   * Dump generator constraints.
   * @see javarifier.ConstraintGenerator
   */
  @Option ("Dump generator constraints")
  public static boolean dumpGeneratorConstraints = false;

  /**
   * Dump bound guards.
   * @see javarifier.BoundGuarder
   */
  @Option ("Dump bound guards")
  public static boolean dumpBoundGuards = false;

  /**
   * Dump parameter constraints.
   * @see javarifier.ParameterConstraints
   */
  @Option ("Dump parameter constraints")
  public static boolean dumpParamCons = false;

  /**
   * Dump stub constraints.
   * @see javarifier.StubConstraints
   */
  @Option ("Dump stub constraints")
  public static boolean dumpStubCons = false;

  /**
   * Dump subtype constraints.
   * @see javarifier.SubtypeConstraints
   */
  @Option ("Dump subtype constraints")
  public static boolean dumpSubtypeCons = false;

  /**
   * Dump open world constraints.
   * @see javarifier.OpenWorld
   */
  @Option ("Dump open world constraints")
  public static boolean dumpOpenWorldCons = false;

  /**
   * Debug local splitting.
   * @see soot.toolkits.scalar.LocalSplitter
   */
  @OptionGroup (value="Debugging Options", unpublicized=true)
  @Option ("Debug local splitting")
  public static boolean debugLocalSplitting = false;

  /**
   * Debug stubs.
   */
  @Option ("Debug stubs")
  public static boolean debugStubs = false;

  /**
   * Debug signature resolver.
   * @see javarifier.TypeInitializer
   */
  @Option ("Debug signature resolver")
  public static boolean debugResolver = false;

  /**
   * Debug Soot resolver.
   * @see soot.SootResolver
   */
  @Option ("Debug Soot resolver")
  public static boolean debugResolve = false;

  /**
   * Debug type inference.
   * @see javarifier.TypeInferencer
   */
  @Option ("Debug type inference")
  public static boolean debugTypeInference = false;

  /**
   * Debug bound guarder.
   * @see javarifier.BoundGuarder
   */
  @Option ("Debug bound guarder")
  public static boolean debugBoundGuarder = false;

  /**
   * Debug subtyping.
   * @see javarifier.ConstraintManager
   */
  @Option ("Debug subtyping")
  public static boolean debugSubtyping = false;

  /**
   * Debug constraint generation.
   * @see javarifier.ConstraintGenerator
   * @see javarifier.ConstraintManager
   */
  @Option ("Debug constraint generation")
  public static boolean debugConstraintGeneration = false;

  /**
   * Debug constraints.
   * @see javarifier.ConstraintTracker#printCauses
   */
  @Option ("Debug constaints")
  public static boolean debugConstraints = false;

  /**
   * Debug signature parser.
   * @see javarifier.JVMLSigParser
   */
  @Option ("Debug signature parser")
  public static boolean debugSigParser = false;

  /**
   * Debug constraint incorporating.
   * @see javarifier.Incorporater
   */
  @Option ("Debug constraint incorporating")
  public static boolean debugConstraintIncorporating = false;

  /**
   * Debug annotation loading.
   * @see javarifier.AnnotationScene
   */
  @Option ("Debug annotation loading")
  public static boolean debugAnnotationLoading = false;
  
  /**
   * Debug annotation storing.
   * @see javarifier.AnnotationStorer
   */
  @Option ("Debug annotation storing")
  public static boolean debugAnnotationStoring = false;

  // The following options don't appear to be used anywhere.
  @Unpublicized
  @Option ("debugVar")
  public static String debugVar = null;

  @Unpublicized
  @Option ("Debug ASM")
  public static boolean debugASM = false;

  @Unpublicized
  @Option ("Debug method transform")
  public static boolean debugMethodTransform = false;

  @Unpublicized
  @Option ("Debug solve")
  public static boolean debugSolve = false;
  // end option group "Debugging Options"

  /**
   * Set the default classpath for stub classes.
   */
  @OptionGroup(value="Internal Options", unpublicized=true)
  @Option ("Set the default classpath for stub classes")
  public static String defaultStubCPEntries = null;
  
  /**
   * Set the default classpath for JDK jarfiles.
   */
  @Option ("Set the default classpath for JDK jarfiles")
  public static String defaultWorldCPEntries = null;
  // end option group "Internal Options"
  
  /**
   * An array of all the default arguments that must be passed to Soot
   * for it to load all the class files.
   */
  private static final String[] sootDefaultArgs = {
    "-w",
    "-whole-program",
    "-keep-line-number",
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

  /** One line synopsis of usage */
  private static String usage_string
    = "javarifier [options] <program-classpath> "
    + "<space-seperated-list-of-classes-to-analyze>";

  /**
   * The main method to perform the inference algorithm.  See user
   * documentation an explanation of the options.
   *
   * @param args - command-line options
   */
  public static void main(String[] args) {
    Options options = new Options (usage_string, Main.class);
    String[] commandLineArgs = null;
    commandLineArgs = options.parse_or_usage (args);

    if (allHelp) {
      System.out.println(
        options.usage("General Options",
                      "Execution Options",
                      "Input Options",
                      "Output Options",
                      "Utility Options (no inference)",
                      "Verbosity Options",
                      "Debugging Options"));
      return;
    }

    if (help) {
      options.print_usage();
      return;
    }

    if (version) {
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
      getProgramCPEntries() + sep +
      getStubCPEntries() + sep +
      getWorldCPEntries();
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
    } catch (Exception e) {
      System.out.println("Soot args: " + Arrays.toString(args));
      System.out.println("Javarifier exception: " + e);
      e.printStackTrace();
      System.exit(1);
    } catch (Error e) {
      System.out.println("Soot args: " + Arrays.toString(args));
      System.out.println("Javarifier Error: " + e);
      e.printStackTrace();
      System.exit(1);
    }
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
      if (Main.printStubs) {
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
     * @param scene
     */
    private void javarifyScene(Scene scene) {
      if (Main.printStubs) {
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

      if (Main.applyHeuristics) {
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

      if (Main.openWorld) {
        ConstraintManager openWorldCons = OpenWorld.generate(Scene.v());
        cm = cm.combine(openWorldCons);
        printNonQuiet("OpenWorld finished.");
      }

      if (Main.debugConstraints) {
          ConstraintTracker.printCauses();
      }

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

      if (Main.dumpConstraints) {
        System.out.println("cm:\n" + cm + "\n");
        System.out.println("Solved:");
        for (ConstraintVar var : solved) {
          System.out.println(var);
        }
      }

      if (Main.dumpCauses) {
        System.out.println("Causes:");
        for (ConstraintVar var : solved) {
            System.out.println(var.causeString());
        }
        System.out.println("");
      }

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
      if (Main.printStubs) {
        return;
      }

      AnnotationScene.v().readAllAnnotations();

      prepareScene(Scene.v());

      if (!Main.justPassThrough)
        javarifyScene(Scene.v());

      if (Main.dumpResults) {
        String results = ScenePrinter.print(Scene.v());
        System.out.println(results);
      }

      String outFile = Main.outputFile;
      printNonQuiet("");
      printNonQuiet("Writing Javari's results to " +
          (outFile == null ? "standard output" : outFile));

      try {
        Writer out = ((outFile == null)
                      ? new OutputStreamWriter(System.out)
                      : new FileWriter(outFile));
          Scene scene = Scene.v();
          Main.outputFormat().write(Scene.v(), out);
          out.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

    }

    /**
     * Prints the given String if the command-line option permits it.
     */
    private void printNonQuiet(String s) {
      if (!Main.reallyQuiet) {
        System.out.println(s);
      }
    }
  }
}
