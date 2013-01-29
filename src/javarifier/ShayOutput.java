package javarifier;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javarifier.util.Pair;
import annotations.Annotation;
import annotations.el.AClass;
import annotations.el.AElement;
import annotations.el.AMethod;
import annotations.el.AScene;
import annotations.el.ATypeElement;
import annotations.el.DefException;
import annotations.el.InnerTypeLocation;

import com.sun.tools.javac.code.TypeAnnotationPosition;

/**
 * This class implements the simple output format specified by Shay Artzi
 * for mutability information for his work.  It should not be used for other
 * purposes.  The format is to output which method parameters (including the
 * receiver) are known to be readonly.  You use fully qualified class names,
 * 0-based indexing for parameters, and RC to indicate receiver.  For
 * example:
 *
 * HashEntry.<init>(java.lang.Object,java.lang.Object,HashEntry) RC
 * HashEntry.<init>(java.lang.Object,java.lang.Object,HashEntry) 0
 * HashEntry.<init>(java.lang.Object,java.lang.Object,HashEntry) 1
 * MST.main(java.lang.String[]) 0
 * BlueReturn.vert() RC
 * Cell.walkSubTree(double,Node$HG) RC
 */
public class ShayOutput {

  private Writer out;

  private String className;
  private String methodName;
  private List<String> polyreadStrings;
  private List<String> allClasses;

  public static void write(AScene ascene, Writer out)
  throws IOException, DefException {

    ShayOutput so = new ShayOutput(out);
    so.printScene(ascene);
  }

  private ShayOutput(Writer out) {
    this.out = out;
    this.methodName = "INVALID_METHOD";
    this.className = "INVALID_CLASS";
  }

  private void setClassName(String name) {
    className = replaceSlashes(name);
  }

  private void setMethodName(String name) {
    name = name.substring(0, name.indexOf(")") + 1);
    List<String> params =
      parseParams(name.substring(name.indexOf("(") + 1, name.indexOf(")")));

    name = name.substring(0, name.indexOf("(") + 1);
    boolean first = true;
    for (String p : params) {
      name += first ? "" : ",";
      first = false;

      name += p;
    }
    methodName = className + "." + name + ")";
  }

  private List<String> parseParams(String paramList) {
    List<String> params = new ArrayList<String>();
    paramList = paramList.trim();
    if (paramList.length() == 0) {
      return params;
    }

    while(paramList.length() > 0) {
      Pair<String, Integer> pair = parseParam(paramList);
      params.add(pair.first);
      paramList = paramList.substring(pair.second);
    }

    return params;
  }

//  private List<String> parseParams(String paramList) {
//    List<String> params = new ArrayList<String>();
//    paramList = paramList.trim();
//    if (paramList.length() == 0) {
//      return params;
//    }
//
//    while(paramList.length() > 0) {
//      String firstChar = paramList.substring(0,1);
//      if (firstChar.equals("L")) {
//        // keep the "L" and ";" around
//        String firstParam = paramList.substring(0, paramList.indexOf(";") +1);
//        params.add(parseParam(firstParam));
//        paramList = paramList.substring(paramList.indexOf(";") + 1);
//      } else if (firstChar.equals("[")) {
//        // TODO:
//      }
//      // TODO:
//    }
//
//
//    for (String p : paramList.split(",")) {
//      params.add(parseParam(p));
//    }
//    return params;
//  }

  private Pair<String, Integer> parseParam(String p) {
    p = p.trim();
    String firstChar = p.substring(0,1);
    if (firstChar.equals("B")) {
      return new Pair<String, Integer>("byte", 1);
    } else if (firstChar.equals("C")) {
      return new Pair<String, Integer>("char", 1);
    } else if (firstChar.equals("D")) {
      return new Pair<String, Integer>("double", 1);
    } else if (firstChar.equals("F")) {
      return new Pair<String, Integer>("float", 1);
    } else if (firstChar.equals("I")) {
      return new Pair<String, Integer>("int", 1);
    } else if (firstChar.equals("J")) {
      return new Pair<String, Integer>("long", 1);
    } else if (firstChar.equals("S")) {
      return new Pair<String, Integer>("short", 1);
    } else if (firstChar.equals("Z")) {
      return new Pair<String, Integer>("boolean", 1);
    } else if (firstChar.equals("L")) {
      // object:
      // Ljava/lang/Object;
      String objectSlashed = replaceSlashes(p.substring(1, p.indexOf(";")));
      int length = 1 + objectSlashed.length() + 1;
      return new Pair<String, Integer>(objectSlashed, length);
    } else if (firstChar.equals("[")) {
      // array
      // [java/lang/Object
      // [I
      Pair<String, Integer> pair = parseParam(p.substring(1));
      return new Pair<String, Integer>(pair.first + "[]", pair.second + 1);
    } else {
      throw new RuntimeException("ShayOutput.parseParam: failed on : " + p);
    }
  }

  private String replaceSlashes(String s) {
    return s.replace("/", ".");
  }

  private void printScene(AScene ascene) throws IOException {
    allClasses = new ArrayList<String>();
    polyreadStrings = new ArrayList<String>();
    className = "INVALID_CLASS";
    for (Map.Entry<String, AClass> e : ascene.classes.entrySet()) {
      setClassName(e.getKey());
      printClass(e.getValue());
      allClasses.add(e.getKey());
      className = "INVALID_CLASS";
    }

    out.write("\nPolyRead annotations:");
    out.write("\n");
    for (String s : polyreadStrings) {
      out.write(s);
    }

    out.write("\nAll classes:");
    out.write("\n");
    for (String s : allClasses) {
      out.write(s);
      out.write("\n");
    }
  }

  private void printClass(AClass c) throws IOException {
    methodName = "INVALID_METHOD";
    for (Map.Entry<String, AMethod> e : c.methods.entrySet()) {
      setMethodName(e.getKey());
      printMethod(e.getValue());

      methodName = "INVALID_METHOD";
    }
  }


  private void printMethod(AMethod m) throws IOException {
    printReceiver(m.receiver);

    for (Map.Entry<Integer, AElement> e : m.parameters.entrySet()) {
      printParam(e.getKey(), e.getValue().type);
    }
  }

  private void printReceiver(AElement r) throws IOException {
    String outputString = methodName + " RC\n";
    if (isReadOnly(r, false)) {
      out.write(outputString);
    } else if (isPolyRead(r)) {
      polyreadStrings.add(outputString);
    }
  }

  private void printParam(int index, ATypeElement param) throws IOException {
    String outputString = methodName + " " + String.valueOf(index) + "\n";
    if (isReadOnly(param, isArray(methodName, index))) {
//      out.write(methodName);
//      out.write(" ");
//      out.write(String.valueOf(index));
//      out.write("\n");
      out.write(outputString);
    } else if (isPolyRead(param)) {
      polyreadStrings.add(outputString);
    }
  }

  private boolean isArray(String methodSig, int index) {
    String[] params = methodSig.split(",");
    return params[index].contains("[");
  }

  private boolean isReadOnly(AElement element, boolean checkArray) {
    for (Annotation tla : element.tlAnnotationsHere) {
      String annotationName = tla.def().name;
      if (annotationName.contains("ReadOnly")) {
        return true;
      }
    }

    // Must also check inner types for arrays:
    // double[@ReadOnly] should return true

    if (checkArray && element instanceof ATypeElement) {
      ATypeElement ae = (ATypeElement) element;
      List<Integer> loc = new ArrayList<Integer>();
      loc.add(0);
      loc.add(0);
      InnerTypeLocation firstLoc = new InnerTypeLocation(TypeAnnotationPosition.getTypePathFromBinary(loc));
      AElement innerElement = ae.innerTypes.vivify(firstLoc);

      return isReadOnly(innerElement, false);
    }
    return false;
  }

  private boolean isPolyRead(AElement element) {
    for (Annotation tla : element.tlAnnotationsHere) {
      String annotationName = tla.def().name;
      if (annotationName.contains("PolyRead")) {
        return true;
      }
    }

    if (element instanceof ATypeElement) {
      ATypeElement ae = (ATypeElement) element;
      List<Integer> loc = new ArrayList<Integer>();
      loc.add(0);
      loc.add(0);
      InnerTypeLocation firstLoc = new InnerTypeLocation(TypeAnnotationPosition.getTypePathFromBinary(loc));
      AElement innerElement = ae.innerTypes.vivify(firstLoc);

      return isPolyRead(innerElement);
    }

    return false;
  }
}
