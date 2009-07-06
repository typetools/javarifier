package javarifier;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import annotations.SimpleAnnotation;
import annotations.SimpleAnnotationFactory;
import annotations.el.AScene;
import annotations.el.DefException;
import annotations.io.IndexFileWriter;

import soot.Scene;
import soot.SootClass;
import soot.SourceLocator.EntryKind;

public enum OutputFormat {
    /**
     * The traditional format showing full JrTypes.
     */
    SCENE_PRINTER {
        @Override
        public void write(Scene scene, Writer out) throws IOException {
            out.write(ScenePrinter.print(scene));
        }
    },
    /**
     * An annotation file showing types resugared as annotations.
     */
    ANNOTATION_INDEX_FILE {
        @Override
        public void write(Scene scene, Writer out) throws IOException {
            Collection<SootClass> classes = collectClasses(scene);
            AScene<SimpleAnnotation> ascene =
                new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf);
            for (SootClass clazz : classes)
                AnnotationStorer.storeAnnotations(clazz, ascene);
            try {
                IndexFileWriter.write(ascene, out);
            } catch (DefException e) {
                throw new RuntimeException(e);
            }
        }
    },
    /**
     * Two files, one listing readonly method parameters and receivers and
     * another listing mutable ones.  -outputFormat is ignored and both
     * output files are created in the current directory.
     */
    TWO_FILES {
        @Override
        public void write(Scene scene, Writer out) throws IOException {
            // ignore given out and open own files
            FileWriter readOnlysOut = new FileWriter("javarifier-ro.out");
            FileWriter mutablesOut = new FileWriter("javarifier-mut.out");
            new TwoFilePrinter(readOnlysOut, mutablesOut).visitScene(scene);
            readOnlysOut.close();
            mutablesOut.close();
        }
    },
    
    SHAY {        
      @Override
      public void write(Scene scene, Writer out) throws IOException {
      Collection<SootClass> classes = collectClasses(scene);
      AScene<SimpleAnnotation> ascene =
          new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf);
      for (SootClass clazz : classes)
          AnnotationStorer.storeAnnotations(clazz, ascene);
      try {
          ShayOutput.write(ascene, out);
      } catch (DefException e) {
          throw new RuntimeException(e);
      }
  }
    }
    ;
    public abstract void write(Scene scene, Writer out) throws IOException;

    public static Collection<SootClass> collectClasses(Scene scene) {
        // Reorder the classes to (1) a deterministic order and (2) a
        // nice order for visual inspection.

        // Sort classes by name.
        SortedSet<SootClass> classes = new TreeSet<SootClass>(
                new Comparator<SootClass>() {
                    public int compare(SootClass o1, SootClass o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

        // Remove stub classes
        for (SootClass sc : (Collection<SootClass>) scene.getClasses())
            if (sc.entryKind() == EntryKind.PROGRAM)
                classes.add(sc);

        if (Options.v().outputMainClassFirst()) {
            // Move main classes (from the command line) to the beginning
            // The result consists of:
            // - all program classes listed in the command line, ordered by
            //   first appearance on the command line, followed by
            // - all analyzed program classes not listed on the command line,
            //   ordered by name.
            List<SootClass> result = new ArrayList<SootClass>();
            LinkedList<String> mainClassNames
                = (LinkedList<String>) soot.options.Options.v().classes();
            for (String mainName : mainClassNames) {
                SootClass main = scene.getSootClass(mainName);
                if (classes.contains(main)) {
                    classes.remove(main);
                    result.add(main);
                }
            }
            result.addAll(classes);
            return result;
        } else
            return classes;
   }
}
