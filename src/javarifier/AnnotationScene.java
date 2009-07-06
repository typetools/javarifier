package javarifier;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.objectweb.asm.ClassReader;

import soot.Scene;
import soot.SootClass;
import annotations.SimpleAnnotation;
import annotations.SimpleAnnotationFactory;
import annotations.el.AClass;
import annotations.el.AMethod;
import annotations.el.AScene;
import annotations.el.ATypeElement;
import annotations.el.DefException;
import annotations.io.IndexFileParser;
import annotations.io.IndexFileWriter;
import annotations.io.ParseException;
import annotations.io.classfile.ClassFileReader;

public class AnnotationScene {
    
    /** Singleton */
    private static final AnnotationScene v = new AnnotationScene();
    public static final AnnotationScene v() { return v; }
    private AnnotationScene() {}
    
    private AScene<SimpleAnnotation> scene = null;
    /**
     * Returns the {@link AScene} of the annotations on all the classes being
     * considered by the Javarifier.  Don't mutate the scene except for
     * vivifying things.
     */
    public final AScene<SimpleAnnotation> scene() { return scene; }
    
    public void readAllAnnotations() {

      if (scene == null) {
            AScene<SimpleAnnotation> scene1 =
                new AScene<SimpleAnnotation>(SimpleAnnotationFactory.saf);
            for (Object sc1 : Scene.v().getClasses()) {
                SootClass sc = (SootClass) sc1;
                ClassReader cr = ASMClassReaders.v().readerFor(sc.getName());
                ClassFileReader.read(scene1, cr);
            }
            String extraIndexFile = Options.v().getExtraAnnotationInputFile();
            if (extraIndexFile != null) {
                try {
                    IndexFileParser.parse(new FileReader(extraIndexFile), scene1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            // Success
            scene = scene1;
            //TODO: debug
            AClass<SimpleAnnotation> ac = scene.classes.vivify("java.util.Vector");
            for(Map.Entry<String, AMethod<SimpleAnnotation>> e : ac.methods.entrySet()) {
              if(e.getKey().toString().contains("removeElement(")) {
                //System.out.println("found java.util.Vector.removeElement(Object)");
                AMethod<SimpleAnnotation> am = e.getValue();
                int size = am.parameters.size();
                //System.out.println("size: " + size);
                for(Map.Entry<Integer, ATypeElement<SimpleAnnotation>> em : am.parameters.entrySet()) {
                  Integer i = em.getKey();
                  ATypeElement<SimpleAnnotation> ate = em.getValue();
                  //System.out.println("at param i: " + i);
                  //System.out.println("ate: " + ate.toString());
                }
              }
            }
            AMethod<SimpleAnnotation> am = ac.methods.get("removeElement");
            //System.out.println("found!");
            
            if (Options.v().debugAnnotationLoading()) {
                // Write out the annotations for debugging
                System.out.println("Loaded annotations:");
                try {
                    IndexFileWriter.write(scene1, new OutputStreamWriter(System.out));
                } catch (DefException e) {
                    // too bad
                    System.out.println("Could not print because of a DefException:");
                    e.printStackTrace(System.out);
                }
            }
        }
    }
}
