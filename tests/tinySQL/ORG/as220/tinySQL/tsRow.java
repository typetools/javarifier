/*
 *
 * tsRow.java
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

import java.util.Hashtable;

/**
 * A tsRow is a abstract representation of a table row. At this level, the
 * row is readonly. If you want to bind objects to an row (for Update or
 * insert), you have to use tsPhysicalRows. tinySQLTables use a special
 * tsPhysicalRow, called tsRawRow, wich is responsible for data conversion
 * from and to the native format of the database.
 *
 * Rule: If you have to put values into a row, use PhysicalRow. If you want
 * to read data from a row, use either Physical or Result row and if you want
 * to evaluate expression you have to use ResultRow.
 */
public abstract class tsRow
{
  private Hashtable colcache;

  /**
   * initializes a new tsRow.
   */
  public tsRow()
  {
    colcache = new Hashtable();
  }

  /**
   * initializes a new tsRow by using the rows column cache for
   * name to position conversion.
   */
  public tsRow(tsRow row)
  {
    colcache = row.colcache;
  }

  /**
   * looks up the position of the column with the name <code>name</code>.
   * returns the position of the column or -1 if no columns could be retrieved.
   */
  public int findColumn(String name)
  {
    Integer integ = (Integer) colcache.get(name);
    if (integ != null)
      return integ.intValue();

    int size = size();
    for (int i = 0; i < size; i++)
    {
      tsColumn column = getColumnDefinition(i);
      if (column.isValidName(name))
      {
        colcache.put(name, new Integer(i));
        return i;
      }
    }
    return -1;
  }

  /**
   * returns the columndefinition for column <code>col</code>
   */
  public abstract tsColumn getColumnDefinition(int column);

  /**
   * retrieve the data from the row for the specified column.
   * @throws tinySQLException on errors
   */
  public abstract Object get(int column) throws tinySQLException;

  /**
   * refreshes the row data.
   */
  public abstract void refresh() throws tinySQLException;

  /**
   * Returnes the number of columns in this row.
   */
  public abstract int size();

//  /**
//   * State-function, called before the row is used.
//   */
//  public void rowActivated ()
//  {
//  }
//
//  /**
//   * State-function, called after the row is used.
//   */
//  public void rowDeactivated ()
//  {
//  }
}

