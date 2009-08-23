////
// Javarifier changes: added getJrType and setJrType methods.
// Added getMethod and setMethod
// Modified toString to also print enclosing method's name and class name
////


/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


package soot.jimple.internal;

import java.util.Iterator;
import java.util.List;

import javarifier.JrType;
import soot.AbstractUnit;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.SootResolver;
import soot.Type;
import soot.Unit;
import soot.UnitPrinter;
import soot.baf.Baf;
import soot.jimple.ConvertToBaf;
import soot.jimple.JimpleToBafContext;
import soot.jimple.JimpleValueSwitch;
import soot.tagkit.Tag;
import soot.util.Switch;

public class JimpleLocal implements Local, ConvertToBaf
{
    // begin Javarifier added
    private JrType jrType;
      private boolean gone = false;
    /**
     * Returns the JrType of the current variable; this method was
     * added for Javarifier.
     */
    public JrType getJrType() {
         if (! gone) {
             // Not all local variables can be resolved.  soot
             // introduced ones for example.
             SootResolver.v().reResolve(getMethod().getDeclaringClass());
             gone = true;
         }
        return jrType;
    }
    /**
     * Sets the JrType of the current variable; this method was
     * added for Javarifier.
     */
    public void setJrType(JrType jrType) {
      // TODO: debug info
//      if(name.contains("$e14")) {
//        if(this.toString().contains("internal.compiler.lookup")) {
//          if(this.toString().contains("getTypeFromType")) {
//        System.out.println("About to set $e14's type!");
//          }
//        }
//      }
        this.jrType = jrType;
    }
    private SootMethod meth;
    /** This method was added for Javarifier. */
    public SootMethod getMethod() {
        return meth;
    }
    /** This method was added for Javarifier. */
    public void setMethod(SootMethod meth) {
        this.meth = meth;
    }
    
    private boolean isSourceLocal = false;
    private int slotIndex = -1;
    private int start_pc = -1;
    private int length = -1;
    
    public boolean isSourceLocal() { return isSourceLocal; }
    public void setSourceLocal(boolean isSourceLocal) { this.isSourceLocal = isSourceLocal; }
    public int getLength() { return length; }
    public void setLength(int length) {this.length = length;}
    public int getSlotIndex() { return slotIndex; }
    public void setSlotIndex(int slotIndex) { this.slotIndex = slotIndex; }
    public int getStart_pc() { return start_pc; }
    public void setStart_pc(int start_pc) { this.start_pc = start_pc; }
    
    // end Javarifier added


    String name;
    Type type;

    int fixedHashCode;
    boolean isHashCodeChosen;

    /** Constructs a JimpleLocal of the given name and type. */
    public JimpleLocal(String name, Type t)
    {
        this.name = name;
        this.type = t;
        Scene.v().getLocalNumberer().add( this );
    }

    /** Returns true if the given object is structurally equal to this one. */
    public boolean equivTo(Object o)
    {
        return this.equals( o );
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    public int equivHashCode()
    {
        return name.hashCode() * 101 + type.hashCode() * 17;
    }

    /** Returns a clone of the current JimpleLocal. */
    public Object clone()
    {
        JimpleLocal jl = new JimpleLocal(name, type);
        jl.isSourceLocal = isSourceLocal;
        jl.slotIndex = slotIndex;
        jl.start_pc = start_pc;
        jl.length = length;
        return jl;
    }

    /** Returns the name of this object. */
    public String getName()
    {
        return name;
    }

    /** Sets the name of this object as given. */
    public void setName(String name)
    {
        this.name = name;
    }

    /** Returns a hashCode consistent with object equality. */
    public int hashCode()
    {
        if(!isHashCodeChosen)
        {
            // Set the hash code for this object

            if(name != null & type != null)
                fixedHashCode = name.hashCode() + 19 * type.hashCode();
            else if(name != null)
                fixedHashCode = name.hashCode();
            else if(type != null)
                fixedHashCode = type.hashCode();
            else
                fixedHashCode = 1;

            isHashCodeChosen = true;
        }

        return fixedHashCode;
    }

    /** Returns the type of this local. */
    public Type getType()
    {
        return type;
    }

    /** Sets the type of this local. */
    public void setType(Type t)
    {
        this.type = t;
    }

    public String toString()
    {
        return  meth != null ? meth.getDeclaringClass().getName()+"."+meth.getName()+"."+getName() : getName();
    }

    public void toString(UnitPrinter up) {
        up.local(this);
    }

    public List getUseBoxes()
    {
        return AbstractUnit.emptyList;
    }

    public void apply(Switch sw)
    {
        ((JimpleValueSwitch) sw).caseLocal(this);
    }

    public void convertToBaf(JimpleToBafContext context, List out)
    {
	Unit u = Baf.v().newLoadInst(getType(),context.getBafLocalOfJimpleLocal(this));
        out.add(u);
	Iterator it = context.getCurrentUnit().getTags().iterator();
	while(it.hasNext()) {
	    u.addTag((Tag) it.next());
	}
    }
    public final int getNumber() { return number; }
    public final void setNumber( int number ) { this.number = number; }

    private int number = 0;
}
