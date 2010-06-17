package javarifier;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Body;
import soot.Local;

import javarifier.util.Pair;
import javarifier.JrType.*;

import java.util.*;

public class ScenePrinter extends SceneVisitor {

    public static String print(Scene s) {
        ScenePrinter p = new ScenePrinter();
        p.visitScene(s);
        return p.getString();
    }



    private StringBuilder buf;

    public ScenePrinter() {
        buf = new StringBuilder();
    }

    public String getString() {
        return buf.toString();
    }

    public void visitScene(Scene scene) {
        Collection<SootClass> classes = OutputFormat.collectClasses(scene);

        for (SootClass clazz : classes) {
            visitClass(clazz);
        }
    }

    public void visitClass(SootClass clazz) {
        buf.append(clazz.getJavaStyleName());
        buf.append("<");
        ClassSig sig = clazz.getSig();
        for (Pair<VarType, JrType> param : sig.getTypeParams()) {
            buf.append(param.first() + " extends " + param.second() + ", ");
        }
        if (sig.getTypeParams().size() != 0) {
            buf.deleteCharAt(buf.length()-1);
            buf.deleteCharAt(buf.length()-1);
        }
        buf.append("> {\n");
        // Only process fields and methods for classes resolved to signatures
        if (clazz.resolvingLevel() >= SootClass.SIGNATURES) {
            // Sort members if appropriate.
            SootField[] fields = ((Collection<SootField>) clazz.getFields())
                .toArray(new SootField[clazz.getFields().size()]);
            if (Main.outputSortMembers) {
                Arrays.sort(fields, new Comparator<SootField>() {
                    public int compare(SootField m1, SootField m2) {
                        return m1.getName().compareTo(m2.getName());
                    }
                });
            }
            for (SootField f : fields) {
                visitField(f);
            }
            SootMethod[] meths = ((Collection<SootMethod>) clazz.getMethods())
                .toArray(new SootMethod[clazz.getMethods().size()]);
            if (Main.outputSortMembers) {
                Arrays.sort(meths, new Comparator<SootMethod>() {
                    public int compare(SootMethod m1, SootMethod m2) {
                        return m1.getName().compareTo(m2.getName());
                    }
                });
            }
            for (SootMethod meth : meths) {
                visitMethod(meth);
            }
        }
        buf.append("}\n");
    }

    /**
     * Returns the string representation of the given type that should be
     * printed, considering {@link Options#outputLimitKind()}.
     */
    private String typeAsKind(JrType type, String kind) {
        if (Main.outputLimitKind == null
                || Main.outputLimitKind.equals(kind)) {
          if (type == null)  {
            return "omitted";
          }
            return type.toString();
        }
        else
            return "omitted";
    }

    public void visitField(SootField field) {

        buf.append("  " + field.getName() + ": ");
        if (field.assignable()) {
            buf.append("assignable ");
        }
//         if (field.mutable()) {
//             buf.append("mutable ");
//         }
        buf.append(typeAsKind(field.getJrType(), "field") + "\n");
    }


    public void visitMethod(SootMethod meth) {
        if (meth.isBridge())
            return;
        buf.append("  " + typeAsKind(meth.getJrReturnType(), "return") + " " + meth.getName() + "(");
        if (meth.getBody() != null) {

            Body body = meth.getBody();

            List<Local> locals = new ArrayList<Local>(body.getLocals());

            for (int i = 0; i < meth.getParameterCount(); i++) {
                Local argLocal = body.getParameterLocal(i);
                if (Main.outputFilterLocals)
                    locals.remove(argLocal); // Only print once
                Param param = meth.getParameters().get(i);
                String paramTypeString = typeAsKind(param.getJrType(), "parameter");
                buf.append(typeAsKind(param.getJrType(), "parameter") + " " + argLocal.getName());
                if (i != meth.getParameterCount() - 1) {
                    buf.append(", ");
                }
            }
            buf.append(") ");
            if (! meth.isStatic()) {
                Local thisArg = body.getThisLocal();
                if (Main.outputFilterLocals)
                    locals.remove(thisArg); // Only print once
                Param thisParam = meth.getReceiver();
                if (! meth.getName().equals("<init>"))
                    buf.append(typeAsKind(thisParam.getJrType(), "receiver") + " ");
            }
            buf.append("{\n");
            for (Local loc : locals) {
                if (!Main.outputFilterLocals
                        || loc.isSourceLocal() && loc.getName().indexOf('$') == -1) {
                //if (loc.getJrType() != null) {
                    buf.append("    " + loc.getName() + ": " + typeAsKind(loc.getJrType(), "local") + "\n");
                    // }
                }
            }
        } else {
            for (int i = 0; i < meth.getParameters().size(); i++) {
                buf.append(typeAsKind(meth.getParameters().get(i).getJrType(), "parameter"));
                if (i != meth.getParameterCount() - 1) {
                    buf.append(", ");
                }
            }
            buf.append(")");
            if (! meth.isStatic()) {
                Param thisParam = meth.getReceiver();
                buf.append(" " + typeAsKind(thisParam.getJrType(), "receiver") + " ");
            }
            buf.append(";");

        }
        buf.append("  }\n");
    }


}
