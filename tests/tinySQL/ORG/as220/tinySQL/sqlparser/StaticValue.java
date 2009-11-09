/*
 *
 * Copyright 1996, Brian C. Jepson
 *                 (bjepson@ids.net)
 *
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
 *
 */
package ORG.as220.tinySQL.sqlparser;

import ORG.as220.tinySQL.tsRow;
import ORG.as220.tinySQL.util.EmptyEnumeration;

import java.util.Enumeration;

/**
 * A Static value encapsulates a java-Object and returns this object
 * on evaluate.
 */
public class StaticValue implements LValue
{
  private Object value;

  public StaticValue(Object value)
  {
    this.value = value;
  }

  public Object evaluate(tsRow rowdata)
  {
    return value;
  }

  public String getName()
  {
    if (value == null)
      return "NULL";

    return value.toString();
  }

  public Enumeration getChildren()
  {
    return EmptyEnumeration.getEnum();
  }

  public int getChildCount()
  {
    return 0;
  }
}
