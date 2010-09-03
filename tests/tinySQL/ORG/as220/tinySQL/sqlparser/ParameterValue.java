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

import ORG.as220.tinySQL.tinySQLException;
import ORG.as220.tinySQL.tsRow;
import ORG.as220.tinySQL.util.EmptyEnumeration;

import java.util.Enumeration;

/**
 * A ParameterValue is a placeholder for PreparedStatement-Parameters.
 * such a parameter can replace any String or numeric value, but it
 * cannot parameterize structural data, such as column definitions, tables
 * or operators.
 */
public class ParameterValue implements LValue
{
  private Object value;
  private boolean valueSet = false;

  public ParameterValue()
  {
  }

  /**
   * If no value is set, throw a exception. A value can be set to null,
   * you can check the parameter using the isEmpty() method.
   */
  public Object evaluate(tsRow row) throws tinySQLException
  {
    if (!valueSet)
      throw new tinySQLException("Parameter not set");

    return value;
  }

  public boolean isEmpty()
  {
    return valueSet == false;
  }

  public void clear()
  {
    valueSet = false;
    value = null;
  }

  public void setValue(Object value)
  {
    valueSet = true;
    this.value = value;
  }

  public Object getValue()
  {
    return value;
  }

  public String getName()
  {
    return (hashCode() + "-ParameterValue");
  }

  public Enumeration getChildren()
  {
    return EmptyEnumeration.getEnum();
  }

  public int getChildCount()
  {
    return 0;
  }

  public String toString()
  {
    return "[" + getName() + "]";
  }
}
