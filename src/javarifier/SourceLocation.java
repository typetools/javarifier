package javarifier;

import soot.tagkit.Tag;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.jimple.Stmt;
import soot.ClassMember;
import soot.SootMethod;
import soot.SootClass;
import soot.SootField;
import soot.Body;

import java.util.Collection;

/**
 *   Stores which class and member a particular sooty thing belongs to, and
 * attempts to extract line number information
 * Used for debugging purposes / communicating with the user.
 */
public class SourceLocation {
    private String className;
    private String memberName;

    // If debugging information is available, these will be assigned
    public String filePath = "";
    public int lineNumber = -1;
    
    // Return the location of a ClassMember
    public SourceLocation(ClassMember member) {
        className = member.getDeclaringClass().getName();
        if (member instanceof SootMethod) {
            memberName = ((SootMethod)member).getName();
        } else if (member instanceof SootField) {
            memberName = ((SootField)member).getName();
        }
        getDebugTags((Host) member);
    }

    // Determine the line location of a particular Host, and glean className /
    // memberName information from the passed SootMember.
    public SourceLocation(SootMethod member, Host h) {
        className = member.getDeclaringClass().getName();
        memberName = member.getName();
        getDebugTags(h);
    }

    // Determine's the location of a particular Host
    public SourceLocation(Body body) {
        this(body.getMethod());
    }

    // Determine the location of a particular class, leaving memberName=""
    public SourceLocation(SootClass clazz) {
        className = clazz.getName();
        getDebugTags((Host) clazz);
    }

    /* Collects any line-number and file information
     *   primarily for cause-tracking purposes.
     */
    private void getDebugTags(Host h) {
        for (Tag tag : (Collection<Tag>) h.getTags()) {
            if (tag instanceof LineNumberTag) {
                byte[] value = tag.getValue();
                //TODO: this assumes LOC < 65k
                lineNumber = ((value[0] & 0xff) << 8) | (value[1] & 0xff);
            } else if (tag instanceof SourceLnPosTag) {
                //TODO: also keep track of endLn() ?
                lineNumber = ((SourceLnPosTag) tag).startLn();
            } else if (tag instanceof SourceLineNumberTag) {
                lineNumber = ((SourceLineNumberTag) tag).getLineNumber();
            } else if (tag instanceof SourceFileTag) {
                filePath = ((SourceFileTag) tag).getSourceFile();
            }
        }
    }

    // toString in a format akin to compiler output, eg:
    //    Board.java:86:
    //    Plank.java:93: methodName    -- also yielding method information
    //    Plank.java::                 -- no debug information available
    //
    // TODO: Note that the outputted filename is just the class name with
    // ".java" appended, so inner classes do not work properly.
    // eg, Parent.Inner.java
    public String toString() {
        // it seems that filePath is nearly never set, so it's left out here.
        if (lineNumber != -1) {
            return className + ".java:" + Integer.toString(lineNumber) + ":";
        } else {
            return memberName == "" ? className + ".java::" : className + " " + memberName;
        }
    }
}
