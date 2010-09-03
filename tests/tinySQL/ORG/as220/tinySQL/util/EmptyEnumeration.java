/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package ORG.as220.tinySQL.util;

import java.util.Enumeration;

/**
 * A empty enumeration that will always return false on hasMoreElements().
 */
public class EmptyEnumeration implements Enumeration
{
  private static EmptyEnumeration singleton;

  /**
   * returns always false
   */
  public boolean hasMoreElements()
  {
    return false;
  }

  /**
   * should not be calles as hasMoreElements returns false.
   * returns null.
   */
  public Object nextElement()
  {
    return null;
  }

  /**
   * returns a reference to a singleton empty enumeration.
   * This is usefull for avoiding object creation as this object
   * has no state.
   */
  public static Enumeration getEnum()
  {
    if (singleton == null)
      singleton = new EmptyEnumeration();

    return singleton;
  }
}
