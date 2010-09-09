/*
 * Copyright (c) 1998, 1999 by Free Software Foundation, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by the Free Software Foundation, version 2. (see COPYING.LIB)
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package ORG.as220.tinySQL.util;

import java.util.Enumeration;

/**
 * Eine Enumeration über ein Array von Objekten.
 *
 * Abhängikeiten: Keine
 */
public class ArrayEnumeration implements Enumeration
{
  private Object[] objectarray = null;
  private int counter = -1;

  public ArrayEnumeration(Object[] objectarray)
  {
    if (objectarray == null)
      throw new NullPointerException("ObjektArray kann nicht null sein");

    this.objectarray = objectarray;
  }

  /**
   * Returns true if this enumeration has at least one more Element.
   *
   */
  public boolean hasMoreElements()
  {
    return (counter < objectarray.length);
  }

  /**
   * Returns the next element in the Array.
   */
  public Object nextElement()
  {
    counter += 1;
    Object retval = objectarray[counter];
    return retval;
  }
}