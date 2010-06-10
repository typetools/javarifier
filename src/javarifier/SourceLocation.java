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
    private String className, memberName;

    // unless debugging information is available, these will stay this way
    public String filePath = "";
    public int lineNumber = -1;

    public SourceLocation(ClassMember member) {
        className = member.getDeclaringClass().getName();
        if (member instanceof SootMethod) {
            memberName = ((SootMethod)member).getName();
        } else if (member instanceof SootField) {
            memberName = ((SootField)member).getName();
        }
        getDebugTags((Host) member);
    }

    public SourceLocation(SootMethod member, Host h) {
        className = member.getDeclaringClass().getName();
        memberName = member.getName();
        getDebugTags(h);
    }

    public SourceLocation(Body body) {
        this(body.getMethod());
    }

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

    public String toString() {
        // it seems that filePath is nearly never set, so it's left out here.
        if (lineNumber != -1) {
            return className + ".java:" + Integer.toString(lineNumber) + ":";
        } else {
            return memberName == "" ? className + ".java::" : className + ":1: " + memberName;
        }
    }
}
