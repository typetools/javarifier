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
 * A column value is a LValue that returns the current value of the
 * column in a row.
 */
public class ColumnValue implements LValue
{
  private String colname;
  private String table;
  private String column;

  /**
   * create a new column value. if the name is fully qualified (contains
   * a "." between the table definition and the column name, split the
   * given name to fill both table and column field.
   */
  public ColumnValue(String colname)
  {
    this.colname = colname;
    int idx = colname.indexOf('.');
    if (idx == -1)
    {
      table = null;
      column = colname;
    }
    else
    {
      table = colname.substring(0, idx);
      column = colname.substring(idx + 1, colname.length());
    }
  }

  /**
   * returns the value of the equal-named column in the row.
   *
   * The result is the same as if you are calling
   * <code>row.get (row.findColumn(ColumnValue.getName()))</code>
   */
  public Object evaluate(tsRow row) throws tinySQLException
  {
    if (row == null)
      throw new tinySQLException("there is no row to check");

    int index = row.findColumn(colname);
    if (index == -1)
      throw new tinySQLException("column " + colname + " was not found");
    Object o = row.get(index);
    return o;
  }

  /**
   * returns the name of the column. If the column had been initialized
   * with a fully qualified name, return that name.
   */
  public String getName()
  {
    return colname;
  }

  /**
   * returns a empty enumeration as this column as no children.
   */
  public Enumeration getChildren()
  {
    return EmptyEnumeration.getEnum();
  }

  /**
   * returns the number of childs for this LValue-element. ColumnValues
   * have no childs, so return 0.
   */
  public int getChildCount()
  {
    return 0;
  }

  /**
   * returns the table name if the initial columnname was fully qualified,
   * otherwise return null
   */
  public String getTable()
  {
    return table;
  }

  /**
   * returns the column name for this column. If the initial column name
   * as fully qualified, return the value after the ".", else return the
   * initial value.
   */
  public String getColumn()
  {
    return column;
  }

  public String toString()
  {
    return "[ColumnValue(" + colname + ")]";
  }
}
