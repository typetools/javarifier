/*
 *
 * tsResultRow.java
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

import ORG.as220.tinySQL.sqlparser.LValue;
import ORG.as220.tinySQL.util.Log;

import java.util.Arrays;
import java.util.Vector;

/**
 *
 * tsResultRow - Hold a tsRow with all Data needed to display the data in
 * a resultSet. This row evaluates expressions, but is unable to bind data.
 * Therefore this row is readonly. If you want to update data use a tsPhysicalRow.
 *
 * Requests for data rows are passed to an other assigned tsRow, which will
 * be usually a tsPhysicalRow.
 *
 * This implementation detects dead locks and will throw a tinySQLException
 * in that case.
 */
public class tsResultRow extends tsRow
{
  private tsRow prototype;
  private tsRow row;
  private tsColumn[] myColumns;
  private Object[] cache;
  private int[] translationTable;
  private boolean[] evalStack;

  /**
   * creates a new tsResultRow given the vector of tsColumn objects.
   * This physical row will not use a underlying data row, even if
   * a row is set by setParent.
   */
  public tsResultRow(Vector cols)
  {
    tsPhysicalRow empty = new tsPhysicalRow(new Vector());
    init(cols, empty);
  }

  /**
   * creates a new tsResultRow given the vector of tsColumn objects and
   * using the given row as prototype for the underlying data.
   *
   * If cols contains references to data columns, the prototype is
   * queried for the value of the object.
   */
  public tsResultRow(Vector cols, tsRow prototype)
  {
    init(cols, prototype);
  }

  /**
   * initializes this object using cols and the given prototype.
   */
  private void init(Vector cols, tsRow prototype)
  {
    int size = cols.size();
    myColumns = new tsColumn[size];
    translationTable = new int[size];

    for (int i = 0; i < size; i++)
    {
      tsColumn column = (tsColumn) cols.elementAt(i);
      myColumns[i] = column;
      translationTable[i] = prototype.findColumn(column.getPhysicalName());
    }

    this.prototype = prototype;
    this.cache = new Object[size];
    evalStack = new boolean[size];
    row = prototype;
  }

  /**
   * create a tsResultRow by using all column definitions of the
   * given row and using it as prototype for the columns translation
   * table.
   */
  public tsResultRow(tsRow prototype)
  {
    Vector cols = new Vector();
    for (int i = 0; i < prototype.size(); i++)
    {
      cols.add(prototype.getColumnDefinition(i));
    }
    init(cols, prototype);
  }

  /**
   * creates a new tsResultRow by copying the state of the given
   * resultRow and discarding the stack and the cache. The parent
   * will be set to null. You have to set a parent using the setParent()
   * function before using this object.
   */
  public tsResultRow(tsResultRow copy)
  {
    int size = copy.size();
    this.myColumns = copy.myColumns;
    this.translationTable = copy.translationTable;
    this.prototype = copy.prototype;
    this.cache = new Object[size];
    this.evalStack = new boolean[size];
  }

  /**
   * sets a parentrow for this result row. The parent row may not be
   * null. A parent row is queried for the values of data columns defined
   * in this tsResultRow.
   */
  public void setParent(tsRow parent)
  {
    if (parent == null)
      throw new NullPointerException("Parent may not be null");
    this.row = parent;
  }

  /**
   * Translates tsResultColumns into baseRow-columns.
   */
  private final int lookup(int myColumn)
  {
    int in = translationTable[myColumn];
    return in;
  }

  /**
   * retrieve the data from the row; Evaluate expressions when needed.
   * Detects dead locks. If a column is defined in the parent, the
   * column value is evaluated using parent.get(column), even when
   * the column is a expression column.
   */
  public synchronized Object get(int column) throws tinySQLException
  {
    int rowcol = lookup(column);

    if (rowcol != -1)
    {
      return row.get(rowcol);
    }

    if (evalStack[column] == true)
    {
      Log.warn("Self-Referenced column detected, returning null");
      throw new tinySQLException("Self-Referenced column detected");
    }
    evalStack[column] = true;

    Object result = cache[column];
    if (result == null)
    {
      tsColumn myColumn = getColumnDefinition(column);
      LValue expr = myColumn.getExpression();
      if (expr != null)
      {
        result = expr.evaluate(this);
        cache[column] = result;
      }
    }

    evalStack[column] = false;
    return result;
  }

  /**
   * retrieve the column definition for the given column.
   */
  public tsColumn getColumnDefinition(int column)
  {
    return myColumns[column];
  }

  /**
   * returns the number of columns in this row.
   */
  public int size()
  {
    return myColumns.length;
  }

  /**
   * refreshes the row by calling refresh on the parent and discarding
   * the cache.
   */
  public void refresh() throws tinySQLException
  {
    Arrays.fill(cache, null);
    row.refresh();
  }

  /**
   * returns a string representation of this result row.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < size(); i++)
    {
      if (i != 0)
      {
        b.append(", ");
      }

      tsColumn col = getColumnDefinition(i);
      b.append("[");
      b.append(col.getDisplayName());
      b.append("]");
      try
      {
        b.append(get(i));
      }
      catch (Exception e)
      {
        b.append("<!EXCEPTION>");
      }
    }
    return b.toString();
  }
}
