////
// Javarifier changes: added getJrType and setJrType methods.
// Added setMethod and getMethod methods.
////


/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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


package soot;

import javarifier.JrType; // [Javarifier]

import soot.*;
import soot.util.*;
import java.util.*;
import java.io.*;

/** A local variable, used within Body
 * classes.  Intermediate representations must use an implementation
 * of Local for their local variables.
 *  */
public interface Local extends Value, Numberable, Immediate, javarifier.JrTyped
{

    /**
     * Returns the JrType of the current variable; this method was
     * added for Javarifier.
     */
    public JrType getJrType();

    /**
     * Sets the JrType of the current variable; this method was
     * added for Javarifier.
     */
    public void setJrType(JrType jrType);

    /** This method was added for Javarifier. */
    public SootMethod getMethod();

    /** This method was added for Javarifier. */
    public void setMethod(SootMethod meth);

    /** Returns the name of the current Local variable. */
    public String getName();

    /** Sets the name of the current variable. */
    public void setName(String name);

    /** Sets the type of the current variable. */
    public void setType(Type t);

    public boolean isSourceLocal();

    public void setSourceLocal(boolean isSourceLocal);

    public int getLength();

    public void setLength(int length);

    public int getSlotIndex();

    public void setSlotIndex(int slotIndex);

    public int getStart_pc();

    public void setStart_pc(int start_pc);
}
