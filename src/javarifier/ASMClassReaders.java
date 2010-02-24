package javarifier;

import java.io.*;
import java.util.*;

import org.objectweb.asm.*;

import soot.*;
import soot.SourceLocator.*;

public class ASMClassReaders {
    
    /** Singleton */
    private static final ASMClassReaders v = new ASMClassReaders();
    public static final ASMClassReaders v() { return v; }
    private ASMClassReaders() {}
    
    private final HashMap<String, ClassReader> classReaders
        = new HashMap<String, ClassReader>();
    
    public ClassReader readerFor(String className) {
        ClassReader cr;
        if (classReaders.containsKey(className)) {
            cr = classReaders.get(className);
            if (cr == null)
                throw new RuntimeException("Already failed to read class " + cr);
        } else {
            try {
                // Use the soot classpath to find files.
                String name = className.replace('.', '/') + ".class";
                FoundFile ff = SourceLocator.v().lookupInClassPath(name);
                
                cr = new ClassReader(new BufferedInputStream(ff.inputStream()));
                classReaders.put(className, cr);
            } catch (Exception e) {
                classReaders.put(className, null);
                throw new RuntimeException("Failed to read class " + className, e);
            }
        }
        return cr;
    }
}
