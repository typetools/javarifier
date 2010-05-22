package javarifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import soot.SootResolver;

/**
 * This class manages all the options available when running Javarifier.
 * It offers a single, through {@link Options#v()}, that can be
 * queried for each individual option.
 * {@link Options#processCmdLine(String[])} sets all the options from the
 * command line arguments.
 */
public class Options {

    /** Singleton */
    private static final Options v = new Options();
    public static final Options v() { return v; }


    private boolean openWorld = false;
    public boolean openWorld() { return openWorld; }

    private String programCPEntries = null;
    public String getProgramCPEntries() { return programCPEntries; }

    // Note that the default StubCPEntries are always used, no matter what
    // the stubCPEntries are.
    private String stubCPEntries = "";
    public String getStubCPEntries() {
      return defaultStubCPEntries +
             System.getProperty("path.separator") +
             stubCPEntries +
             (useWorldAsStubs()
              ? (System.getProperty("path.separator") + getWorldCPEntries())
              : "");
    }

    private String worldCPEntries = "";
    public String getWorldCPEntries() {
      return defaultWorldCPEntries +
             System.getProperty("path.separator") +
             worldCPEntries;
    }

    private String defaultStubCPEntries ="";
    public String getDefaultStubCPEntries() { return defaultStubCPEntries; }

    private String defaultWorldCPEntries = "";
    public String getDefaultWorldCPEntries() { return defaultWorldCPEntries; }

    private String outputFile = null;
    public String getOutputFile() { return outputFile; }

    /**
     * If false (the default), then missing stubs cause Javarifier to halt.
     * If true, then Javarifier uses world classes as stubs, after both the
     * default stub entries and the stub entries.
     */
    private boolean useWorldAsStubs = false;
    public boolean useWorldAsStubs() { return useWorldAsStubs; }

    private OutputFormat outputFormat = OutputFormat.ANNOTATION_INDEX_FILE;
    public OutputFormat outputFormat() { return outputFormat; }

    /**
     * Tells {@link ScenePrinter} to omit a bunch of uninteresting local
     * variables from the output.  These are generally the ones that cannot be
     * annotated in source code, so this option is good for comparing
     * Javarifier results with manual annotations.
     */
    private boolean outputFilterLocals = false;
    public boolean outputFilterLocals() { return outputFilterLocals; }

    /**
     * Tells {@link ScenePrinter} to output "omitted" instead of the type for
     * elements not of the given kind.  The kind can be "field", "local",
     * "parameter", "receiver", or "return".  Useful to study the types
     * assigned to elements of each kind separately.
     */
    private String outputLimitKind = null;
    public String outputLimitKind() { return outputLimitKind; }

    /**
     * Tells {@link ScenePrinter} to output fields and methods sorted by name
     * instead of by order of appearance in the class file.  If several methods
     * have the same name, they are outputted in the order they appear in the
     * class file.
     */
    private boolean outputSortMembers = false;
    public boolean outputSortMembers() { return outputSortMembers; }

    /**
     * Tells {@link ScenePrinter} to output all the classes mentioned on the
     * command line (in the order of first mention on the command line) before
     * all the other classes.
     */
    private boolean outputMainClassFirst = false;
    public boolean outputMainClassFirst() { return outputMainClassFirst; }

    /**
     * If specified, annotations will be loaded from this annotation file as well as
     * the class files for all classes in the Soot scene.
     */
    private String extraAnnotationInputFile = null;
    public String getExtraAnnotationInputFile() { return extraAnnotationInputFile; }

    /**
     * Don't perform inference.  Just read annotations (treating even program
     * classes as fully annotated), expand defaults and &#064;Unmodifiable,
     * and write the results back out.  Use this mode to convert manual
     * annotations to a form in which they can be compared to Javarifier
     * annotations.
     */
    private boolean justPassThrough = false;
    public boolean justPassThrough() { return justPassThrough; }

    private boolean dumpResults = false;
    public boolean dumpResults() { return dumpResults; }

    private boolean dumpConstraints = false;
    public boolean dumpConstraints() { return dumpConstraints; }

    private boolean dumpGeneratorConstraints = false;
    public boolean dumpGeneratorConstraints() { return dumpGeneratorConstraints; }

    private boolean dumpBoundGuards = false;
    public boolean dumpBoundGuards() { return dumpBoundGuards; }

    private boolean dumpParamCons = false;
    public boolean dumpParamCons() { return dumpParamCons; }

    private boolean dumpStubCons = false;
    public boolean dumpStubCons() { return dumpStubCons; }

    private boolean dumpSubtypeCons = false;
    public boolean dumpSubtypeCons() { return dumpSubtypeCons; }

    private boolean dumpOpenWorldCons = false;
    public boolean dumpOpenWorldCons() { return dumpOpenWorldCons; }


    private boolean dumpCauses = false;
    public boolean dumpCauses() { return dumpCauses; }



    private String debugVar = null;
    public String debugVar() { return debugVar; }

    private boolean doNotSplitSourceLocals = true; // Should be True
    public boolean doNotSplitSourceLocals() { return doNotSplitSourceLocals; }

    private boolean debugLocalSplitting = false;
    public boolean debugLocalSplitting() { return debugLocalSplitting; }

    private boolean debugStubs = false;
    public boolean debugStubs() { return debugStubs; }

    private boolean debugASM = false;
    public boolean debugASM() { return debugASM; }

    private boolean debugResolver = false;
    public boolean debugResolver() { return debugResolver; }

    private boolean debugResolve = false;
    public boolean debugResolve() { return debugResolve; }

    private boolean debugTypeInference = false;
    public boolean debugTypeInference() { return debugTypeInference; }

    private boolean debugBoundGuarder = false;
    public boolean debugBoundGuarder() { return debugBoundGuarder; }

    private boolean debugSubtyping = false;
    public boolean debugSubtyping() { return debugSubtyping; }

    private boolean debugMethodTransform = false;
    public boolean debugMethodTransform() { return debugMethodTransform; }

    private boolean debugConstraintGeneration = false;
    public boolean debugConstraintGeneration() { return debugConstraintGeneration; }

    private boolean debugConstraints = false;
    public boolean debugConstraints() { return debugConstraints; }

    private boolean debugSolve = false;
    public boolean debugSolve() { return debugSolve; }

    private boolean debugSigParser = false;
    public boolean debugSigParser() { return debugSigParser; }

    private boolean debugConstraintIncorporating = false;
    public boolean debugConstraintIncorporating() { return debugConstraintIncorporating; }

    private boolean debugAnnotationLoading = false;
    public boolean debugAnnotationLoading() { return debugAnnotationLoading; }

    private boolean debugAnnotationStoring = false;
    public boolean debugAnnotationStoring() { return debugAnnotationStoring; }

    private boolean reallyQuiet = false;
    public boolean reallyQuiet() { return reallyQuiet; }

    private boolean printStubs = false;
    public boolean justPrintStubs() { return printStubs; }

    private boolean printVersion = false;
    public boolean printVersion() { return printVersion; }

    private boolean printUsage = false;
    public boolean printUsage() { return printUsage; }

    private boolean applyHeuristics = false;
    public boolean applyHeuristics() { return applyHeuristics; }

    /**
     * The skipEmpty flag indicates that annotations should not be outputted
     *  for empty code.  That is, there should be no annotations for interfaces
     *  and abstract methods.
     */
    private boolean skipEmpty = false;
    public boolean skipEmpty() { return skipEmpty; }

    /**
     * The includeImmutableClasses indicates that @ReadOnly annotations
     * should be output even for references that are of some type that is known
     * to be @Unmodifiable, such as java.lang.String.
     */
    private boolean includeImmutableClasses = false;
    public boolean includeImmutableClasses() { return includeImmutableClasses; }

    public String[] processCmdLine(String[] args) {

        for (int i = 0; i < args.length && args.length > 0; i++) {
          // System.out.printf("i: %d, args: %s%n", i, Arrays.toString(args));
          i = Math.max(0, i);
          // in case last argument parsed was at the very beginning of the
          // array, in which case it and possibly the next arg have been removed
            if (args[i].equals("-openWorld")) {
                openWorld = true;
                args = removeArgs(args, i, 1);
                i--;
            } else

            if (args[i].equals("-output")) {
                outputFile = args[i+1];
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-outputFormat")) {
                String outputFormatStr = args[i+1];
                if (outputFormatStr.equals("scenePrinter"))
                    outputFormat = OutputFormat.SCENE_PRINTER;
                else if (outputFormatStr.equals("annotationIndexFile")
                      || outputFormatStr.equals("index"))
                    outputFormat = OutputFormat.ANNOTATION_INDEX_FILE;
                else if (outputFormatStr.equals("twoFiles"))
                    outputFormat = OutputFormat.TWO_FILES;
                else if (outputFormatStr.equals("shay"))
                    outputFormat = OutputFormat.SHAY;
                else
                    throw new IllegalArgumentException(
                            "Unrecognized output format " + outputFormatStr);
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-useWorldAsStubs")) {
                useWorldAsStubs = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-outputFilterLocals")) {
                outputFilterLocals = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-outputLimitKind")) {
                outputLimitKind = args[i+1];
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-outputSortMembers")) {
                outputSortMembers = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-outputMainClassFirst")) {
                outputMainClassFirst = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-programCPEntries")) {
              programCPEntries = args[i+1];
              args = removeArgs(args, i, 2);
              i--;
              i--;
            } else
            if (args[i].equals("-stubCPEntries") || args[i].equals("-stubs")) {
                stubCPEntries = args[i+1];
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-worldCPEntries") || args[i].equals("-world")) {
                worldCPEntries = args[i+1];
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-defaultStubCPEntries")) {
              defaultStubCPEntries = args[i+1];
              args = removeArgs(args, i, 2);
              i--;
              i--;
            } else
            if (args[i].equals("-defaultWorldCPEntries")) {
              defaultWorldCPEntries = args[i+1];
              args = removeArgs(args, i, 2);
              i--;
              i--;
            } else
            if (args[i].equals("-extraAnnotationInputFile")) {
                extraAnnotationInputFile = args[i+1];
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-justPassThrough")) {
                justPassThrough = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpResults")) {
                dumpResults = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpConstraints")) {
                dumpConstraints = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpGeneratorConstraints")) {
                dumpGeneratorConstraints = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpBoundGuards")) {
                dumpBoundGuards = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpParamCons")) {
                dumpParamCons = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpStubCons")) {
                dumpStubCons = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpSubtypeCons")) {
                dumpSubtypeCons = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-dumpOpenWorldCons")) {
                dumpOpenWorldCons = true;
                args = removeArgs(args, i, 1);
                i--;
            } else

            if (args[i].equals("-dumpCauses")) {
                dumpCauses = true;
                args = removeArgs(args, i, 1);
                i--;
            } else

            if (args[i].equals("-debugVar")) {
                debugVar = args[i + 1];
                args = removeArgs(args, i, 2);
                i--;
                i--;
            } else
            if (args[i].equals("-debugStubs")) {
                debugStubs = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugLocalSplitting")) {
                debugLocalSplitting = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugResolve")) {
                debugResolve = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugResolver")) {
                debugResolver = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugBoundGuarder")) {
                debugBoundGuarder = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugSubtyping")) {
                debugSubtyping = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugTypeInference")) {
                debugTypeInference = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugConstraintIncorporating")) {
                debugConstraintIncorporating = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugSigParser")) {
                debugSigParser = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugSolve")) {
                debugSolve = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugASM")) {
            	debugASM = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugConstraints")) {
            	debugConstraints = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugMethodTransform")) {
            	debugMethodTransform = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugConstraintGeneration")) {
            	debugConstraintGeneration = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugAll")) {
                debugConstraintGeneration = true;
                debugMethodTransform = true;
                debugASM = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugAnnotationLoading")) {
                debugAnnotationLoading = true;
                args = removeArgs(args, i, 1);
                i--;
            } else
            if (args[i].equals("-debugAnnotationStoring")) {
                debugAnnotationStoring = true;
                args = removeArgs(args, i, 1);
            } else if (args[i].equals("-Q") || args[i].equals("-q") ||
                args[i].equals("--quiet")) {
              reallyQuiet = true;
              args = removeArgs(args, i, 1);
              i--;
            } else
            if (args[i].equals("-printStubs")) {
              printStubs = true;
              args = removeArgs(args, i , 1);
              i--;
            } else
            if (args[i].equals("-version") ||
                args[i].equals("--version")) {
              printVersion = true;
              args = removeArgs(args, i, 1);
              i--;
            } else
            if (args[i].equals("-help") ||
                args[i].equals("--help")) {
              printUsage = true;
              args = removeArgs(args, i, 1);
              i--;
            } else
            if (args[i].equals("-applyHeuristics") ||
                args[i].equals("-assignable")) {
              applyHeuristics = true;
              args = removeArgs(args, i, 1);
              i--;
            } else
            if (args[i].equals("-skipEmpty")) {
              skipEmpty = true;
              args = removeArgs(args, i, 1);
            } else
            if (args[i].equals("-includeImmutableClasses")) {
              includeImmutableClasses = true;
              args = removeArgs(args, i, 1);
            }
        }
        return args;
    }

    private String[] removeArgs(String[] inArgs, int start, int cnt) {
        List<String> argsList = new LinkedList<String>(Arrays.asList(inArgs));
        while (cnt>0) {
            argsList.remove(start);
            cnt--;
        }
        inArgs = argsList.toArray(new String[] {});
        return inArgs;
    }

}
