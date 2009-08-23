/*
 *
 * tsPhysicalRow.java
 * Row object for tinySQL.
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

package ORG.as220.tinySQL;

import ORG.as220.tinySQL.util.ArrayEnumeration;
import ORG.as220.tinySQL.util.Log;

import java.util.Enumeration;
import java.util.Vector;

/**
 * The physical row contains a simple columnnumber to object mapping.
 * It does not evaluate expression. tsPhysicalRow and its descends are
 * the only row type that is able to store values.
 */
public class tsPhysicalRow extends tsRow
{
  private Object[] data;
  private tsColumn[] coldefs;

  /**
   * creates a new tsPhysical row using the given tsColumn objects
   * in the Vector.
   */
  public tsPhysicalRow(Vector columns)
  {
    coldefs = new tsColumn[columns.size()];
    data = new Object[columns.size()];

    int size = columns.size();
    for (int i = 0; i < size; i++)
    {
      tsColumn col = (tsColumn) columns.elementAt(i);
      col.setResultPosition(i);
      data[i] = col.getDefaultValue();
      coldefs[i] = col;
    }
  }

  /**
   * creates a new tsPhysical row using the given tsPhysicalRow
   * for the copyconstructor
   */
  public tsPhysicalRow(tsPhysicalRow copycon)
  {
    super(copycon);
    this.data = new Object[copycon.data.length];
    System.arraycopy(copycon.data, 0, data, 0, data.length);

    this.coldefs = copycon.coldefs;
  }

  /**
   * returns the value stored in column <code>col</code>
   *
   * @throws ArrayOutOfBoundsException if col is negative or greater than
   * the column count in this row.
   */
  public Object get(int col) throws tinySQLException
  {
    return data[col];
  }

  /**
   * replaces the value for column col
   *
   * @throws ArrayOutOfBoundsException if col is negative or greater than
   * the column count in this row.
   */
  public void put(int col, Object value) throws tinySQLException
  {
    data[col] = value;
  }

  /**
   * returns the columndefinition for column <code>col</code>
   *
   * @throws ArrayOutOfBoundsException if col is negative or greater than
   * the column count in this row.
   */
  public tsColumn getColumnDefinition(int col)
  {
    return coldefs[col];
  }

  /**
   * returns all column definitions as enumeration.
   */
  public Enumeration getAllDefinitions()
  {
    return new ArrayEnumeration(coldefs);
  }

  /**
   * returns the number of columns in this row.
   */
  public int size()
  {
    return data.length;
  }

  /**
   * returns a string representation of this row.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("[tsPhysicalRow=("); //data.toString ();
    for (int i = 0; i < data.length; i++)
    {
      if (i != 0)
        b.append(", ");
      b.append(data[i]);
    }
    b.append(")]");
    return b.toString();
  }

  /**
   * Reloads the data in this row. The current tablePosition is used
   * and only valid data rows are updated.
   */
  public void refresh()
      throws tinySQLException
  {
    int size = size();
    for (int i = 0; i < size; i++)
    {
      tsColumn currColDef = getColumnDefinition(i);
      if (currColDef.getColumnType() == tsColumn.COL_DATA)
      {
        tinySQLTableView table = currColDef.getTable();
        Object o = table.getColumn(currColDef.getTablePosition());
        put(i, o);
      }
      else
      {
        Log.debug("Refresh: Ignoring row:" + currColDef + " - not a data row");
      }
    }
  }

}

