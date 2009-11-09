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
 * A JokerColumn Value is a placeholder for "*" or "tablename.*"
 * values. The JokerColumnValue has to be resolved to real columns.
 * If you try to evaluate such a column, a exception is raised.
 *
 */
public class JokerColumnValue implements LValue
{
  private String colname;
  private String table;

  /**
   * creates a jokercolumn.
   */
  public JokerColumnValue(String name)
  {
    this.colname = name.trim();
    if (colname.equals("*"))
      table = null;
    else
    {
      table = this.colname.substring(0, colname.length() - 2);
    }
  }

  /**
   * Throws an exception as JokerColumn cannot be evaluated.
   */
  public Object evaluate(tsRow row)
  {
    throw new IllegalStateException("A JokerColumn cannot be evaluated");
  }

  /**
   * returns the name of the column.
   */
  public String getName()
  {
    return colname;
  }

  /**
   * returns the table of the column. A Global Joker ("*") has no
   * assigned table and collects all columns from all tables.
   */
  public String getTable()
  {
    return table;
  }

  /**
   * returns an empty enumeration as a joker column has no children.
   */
  public Enumeration getChildren()
  {
    return EmptyEnumeration.getEnum();
  }

  /**
   * returns 0 as a joker column has no children.
   */
  public int getChildCount()
  {
    return 0;
  }

  public String toString()
  {
    return "[JokerColumnValue(" + colname + ")]";
  }

}
