////
// Javarifier changes: added getJrType and setJrType methods.
// Added getMethod and setMethod
////


/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam, Patrick Pominville and Raja Vallee-Rai
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


package soot.baf.internal;

import javarifier.JrType; // [Javarifier]

import soot.*;
import soot.util.*;
import java.util.*;

public class BafLocal implements Local
{

    // begin Javarifier additions
    // This code is not used by Javarifier but is included so that
    // BafLocal and JimpleLocal may have the same interface.
    // Javarifer only used JimpleLocals.
    private JrType jrType;
    /**
     * Returns the JrType of the current variable; this method was
     * added for Javarifier.
     */
    public JrType getJrType() {
        return jrType;
    }
    /**
     * Sets the JrType of the current variable; this method was
     * added for Javarifier.
     */
    public void setJrType(JrType jrType) {
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
    // end Javarifier additions

    String name;
    Type type;

    int fixedHashCode;
    boolean isHashCodeChosen;

    public BafLocal(String name, Type t)
    {
        this.name = name;
        this.type = t;
    }

    /* JimpleLocals are *NOT* equivalent to Baf Locals! */
    public boolean equivTo(Object o)
    {
        return this.equals( o );
    }

    /** Returns a hash code for this object, consistent with structural equality. */
    public int equivHashCode()
    {
        return name.hashCode() * 101 + type.hashCode() * 17;
    }

    public Object clone()
    {
        return new BafLocal(name, type);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type t)
    {
        this.type = t;
    }

    public String toString()
    {
        return getName();
    }

    public void toString( UnitPrinter up ) {
        up.local( this );
    }

    public List getUseBoxes()
    {
        return AbstractUnit.emptyList;
    }

    public void apply(Switch s)
    {
        throw new RuntimeException("invalid case switch");
    }
    public final int getNumber() { return number; }
    public final void setNumber( int number ) { this.number = number; }

    private int number = 0;
}
