/**
 * tinySQLTableView
 *
 * Copyright 2002, Brian C. Jepson
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
 */
package ORG.as220.tinySQL;

import ORG.as220.tinySQL.util.Log;

import java.util.Hashtable;

/**
 * Acts as a frontend to the physical table. A physical table is opened only
 * one time and stays open as long as a view is using the table. The database
 * backend is responsible for opening and managing the tables.
 *
 * The table view translates all column names to the numeric column numbers.
 * On an update or an insertation of a row, the rowdata is converted into
 * the native format.
 */
public class tinySQLTableView
{
  private Hashtable colcache;
  private tinySQLTable table;
  private int _currentRowNumber;
  private tsRawRow _currentRow;
  private String alias;
  private tsColumn[] tsColumnCache;
  private boolean isClosed;
  private tinySQLIndex index;

  /**
   * create a new tinySQLTableView with the tables name as alias
   */
  public tinySQLTableView(tinySQLTable table)
      throws tinySQLException
  {
    this(table, table.getName());
  }

  /**
   * create a new tinySQLTableView with given string as alias
   * and register this view to the physical table
   */
  public tinySQLTableView(tinySQLTable table, String alias)
      throws tinySQLException
  {
    if (table == null)
      throw new tinySQLException("No table?");

    this.index = new tinySQLDefaultIndex();
    this.table = table;
    table.createdView(this);
    this.alias = alias;
    colcache = new Hashtable();
    _currentRowNumber = -1;

  }

  /**
   * closes the view and unregister
   */
  public void close() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    table.removedView(this);
    isClosed = true;
  }

  /**
   * returns the column name for a column by retrieving the columns
   * definition and returning the physical name
   */
  public String getColumnName(int col)
  {
    tsColumn coldef = getColumnDefinition(col);
    return coldef.getPhysicalName();
  }

  /**
   * searches the available column definitions and returns the position
   * of the column or -1 if no column with this name, alias or fullqualified
   * name was found
   */
  public int findColumn(String name)
  {
    Integer integ = (Integer) colcache.get(name);
    if (integ != null)
      return integ.intValue();

    int size = getColumnCount();
    for (int i = 0; i < size; i++)
    {
      tsColumn column = getColumnDefinition(i);
      if (column.isValidName(name))
      {
        colcache.put(name, new Integer(i));
        return i;
      }
    }
    Log.error("Column : " + name + " not found in this table");
    return -1;
  }

  /**
   * checks whether the current row is deleted.
   */
  public boolean isDeleted() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    return table.isDeleted(_currentRowNumber);
  }

  /**
   * updates the current row using the data of the tsRow <code>data</code>.
   * The data in the row will be copied into the tables native row and converted
   * to native raw data.
   */
  public void updateRow(tsRow data)
      throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    // discard changes to the current row and replace with this
    int physrow = index.reverseTranslation(_currentRowNumber);
    table.updateRow(physrow, convertRowToNative(data, _currentRow));
    loadRow(_currentRowNumber);
  }

  /**
   * insert the data of the tsRow <code>data</code> into the table.
   * The data in the row will be copied into the tables native row and converted
   * to native raw data.
   *
   * the current record number will be changed to the new record's rownumber.
   */
  public void insertRow(tsRow data)
      throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    int row = table.insertRow(convertRowToNative(data, table.getInsertRow()));
    loadRow(row);
  }

  /**
   * Converts a row into a physical row containing only native values.
   */
  public tsRawRow convertRowToNative(tsRow row, tsRawRow raw)
      throws tinySQLException
  {
    int size = row.size();
    for (int i = 0; i < size; i++)
    {
      tsColumn col = row.getColumnDefinition(i);
      int idx = raw.findColumn(col.getPhysicalName());
      if (idx != -1)
      {
        raw.put(idx, row.get(i));
      }
      else
      {
        idx = raw.findColumn(col.getDisplayName());
        if (idx == -1)
        {
          throw new tinySQLException("Column : " + col);
        }
        raw.put(idx, row.get(i));
      }
    }
    return raw;
  }

  /**
   * returns the column definition for the specified column.
   */
  public tsColumn getColumnDefinition(int column)
  {
    if (tsColumnCache == null)
    {
      tsColumnCache = new tsColumn[table.getColumnCount()];
    }

    if (tsColumnCache[column] == null)
    {
      tsColumn retval = new tsColumn(this, table.getColumnDefinition(column));
      tsColumnCache[column] = retval;
    }
    return tsColumnCache[column];
  }

  /**
   * returns the physical name of the table
   */
  public String getName()
  {
    return table.getName();
  }

  /**
   * returns the alias (or display name) of the table.
   * If no alias was set, the physical name is returned.
   */
  public String getAlias()
  {
    return alias;
  }

  /**
   * adjust the alias for this table
   */
  public void setAlias(String alias)
  {
    if (alias == null)
      throw new NullPointerException("Alias may not be null");
    this.alias = alias;
  }

  /**
   * sets the current record number and physical row
   */
  private void setCurrentRow(int rcdn, tsRawRow row)
  {
    _currentRow = row;
    _currentRowNumber = rcdn;
  }

  // Navigation functions

  /**
   * Moves the cursor down one row from its current position. A tinySQLTableView's
   * cursor is initially positioned before the first row; the first call to
   * the method next makes the first row the current row; the second call makes
   * the second row the current row, and so on.
   */
  public boolean next() throws tinySQLException
  {
    return loadRow(_currentRowNumber + 1);
  }

  /**
   * seek the next record, but skip all deleted records.
   */
  public boolean nextNonDeleted() throws tinySQLException
  {
    int currentRowNumber = _currentRowNumber;

    boolean result = false;
    while (result == false)
    {
      currentRowNumber++;
      if (currentRowNumber < getRowCount())
      {
        result = table.isDeleted(currentRowNumber) == false;
      }
      else
        return false;
    }
    return loadRow(currentRowNumber);
  }

  /**
   * Refreshes the current row with its most recent value in the database.
   */
  public boolean refresh() throws tinySQLException
  {
    return loadRow(_currentRowNumber);
  }

  /**
   * loading a row from the table and return true if the row exists.
   */
  protected boolean loadRow(int rowNumber) throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    tsRawRow row = table.getRow(index.translateRow(rowNumber));
    setCurrentRow(rowNumber, row);
    return row != null;
  }

  /**
   * Moves the cursor to the front of this ResultSet object, just before
   * the first row. This method has no effect if the result set contains
   * no rows - the file pointer is always before first in this case.
   */
  public boolean beforeFirst() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    setCurrentRow(-1, null);
    return table.getRowCount() > 0;
  }

  /**
   * Indicates whether the cursor is before the first row in this tableView.
   */
  public boolean isBeforeFirst() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    return _currentRowNumber == -1;
  }

  /**
   * Moves the cursor to the given row number in this tableView object.
   * <p>
   * If the row number is positive, the cursor moves to the given row number
   * with respect to the beginning of the table. The first row is row 1, the
   * second is row 2, and so on.
   * <p>
   * If the given row number is negative, the cursor moves to an absolute
   * row position with respect to the end of the result set. For example,
   * calling the method absolute(-1) positions the cursor on the last row;
   * calling the method absolute(-2) moves the cursor to the next-to-last
   * row, and so on.
   * <p>
   * An attempt to position the cursor beyond the first/last row in the
   * result set leaves the cursor before the first row or after the last row.
   */
  public boolean absolute(int row) throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    if (row < 0)
    {
      row = (table.getRowCount() - row);
      if (row < 0)
      {
        return beforeFirst();
      }
    }
    else
    {
      if (row > table.getRowCount())
      {
        return afterLast();
      }
    }
    return loadRow(row);
  }

  /**
   * Moves the cursor a relative number of rows, either positive or negative.
   * <p>
   * Attempting to move beyond the first/last row in the tables positions
   * the cursor before/after the the first/last row. Calling relative(0) is
   * valid, but does not change the cursor position.
   * <p>
   * Note: Calling the method relative(1) is different from calling the method
   * next() because is makes sense to call next() when there is no current row,
   * for example, when the cursor is positioned before the first row or after
   * the last row of the table.
   */
  public boolean relative(int row) throws tinySQLException
  {
    return absolute(_currentRowNumber + row);
  }

  public boolean last() throws tinySQLException
  {
    return loadRow(getRowCount() - 1);
  }

  /**
   * Moves the cursor to the end of this tableView, just after the
   * last row. This method has no effect if the result set contains no rows.
   *
   */
  public boolean afterLast() throws tinySQLException
  {
    setCurrentRow(table.getRowCount(), null);
    return false;
  }

  /**
   * returns the number of columns in the assigned Table.
   */
  public int getColumnCount()
  {
    return table.getColumnCount();
  }

  /**
   * returns the number of rows in the assigned table.
   */
  public int getRowCount() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    return table.getRowCount();
  }

  /**
   * returns the JDBC-Value (converted from native) of the specified column
   * in the current row of the table.
   */
  public Object getColumn(int col) throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    return _currentRow.get(col);
  }

  /**
   * returns the JDBC-Value (converted from native) of the specified column
   * in the current row of the table. The column is located using findColumn(col).
   */
  public final Object getColumn(String col) throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    if (_currentRow == null)
    {
      throw new tinySQLException("No such row or not initialized: " + _currentRowNumber);
    }
    return getColumn(findColumn(col));
  }

  /**
   * returns the current record number of the table this view is working on.
   * this is a internal cursor position as a physical table has no cursor.
   */
  public int getCurrentRecordNumber() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    return _currentRowNumber;
  }

  /**
   * checks whether the underlying table is read only
   */
  public boolean isReadOnly() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    return table.isReadOnly();
  }

  /**
   * delete the current row.
   */
  public void deleteRow() throws tinySQLException
  {
    if (isClosed) throw new tinySQLException("table is closed");

    table.deleteRow(_currentRowNumber);
  }

  /**
   * Checks whether this view equals the given object.
   * if o is a tinySQLTableView and name or alias are
   * equal, return true.
   */
  public boolean equals(Object o)
  {
    if (o instanceof tinySQLTableView)
    {
      tinySQLTableView vTable = (tinySQLTableView) o;
      String name = getName();

      if ((name.equals(vTable.getName())) ||
          (name.equals(vTable.getAlias())))
      {
        return true;
      }

      name = getAlias();

      if ((name.equals(vTable.getName())) ||
          (name.equals(vTable.getAlias())))
      {
        return true;
      }
    }
    return false;
  }

  public void setIndex(tinySQLIndex index)
  {
    this.index = index;
  }

  public tinySQLIndex getIndex()
  {
    return index;
  }

}
