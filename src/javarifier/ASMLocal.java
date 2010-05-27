package javarifier;

import org.objectweb.asm.Label;

/**
 * A simple data type containing information gained from ASM about
 * locals.  The inportant fact is that it contains the TypeStructure
 * of the local, information not present in soot.
 */
public class ASMLocal {

    private String name;
    private String desc;
    private String signature;
    private int start;
    private int end;
    private int index;
    private JrType jrType;

    public ASMLocal(String name, String desc, String signature, int start, int end, int index){
        this.name = name;
        this.desc = desc;
        this.signature = signature == null ? desc : signature;
        this.start = start;
        this.end = end;
        this.index = index;
        this.jrType = JVMLSigParser.parseJrType(this.signature); // do not remove "this."
    }

    public String getName() { return name; }
    public String getDesc() { return desc; }
    public String getSignature() { return signature; }
    public int getIndex() { return index; }
    public JrType getJrType() { return jrType; }
    public int getStart() { return start; }
    public int getEnd() { return end; }

    public String toString() {
        return "<" + name + " " + desc + " " + signature + " " + start + " " + end + " " + index + ">";
    }

}
