/**
 * dbfFileRow
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

import java.util.Vector;

/**
 * Represents a row read from a single dbase-table.
 * This row is an cache for the binary data from dbase.
 * The data-objects are only instantiated when get() is
 * called the first time. Every subsequent call to get
 * will return a cached value.
 */
public class textFileRow extends tsRawRow
{
  private byte[] data = null;
  private Object[] cache = null;

  /**
   * Creates a new dbfFileRow using the columns contained in
   * the vector <code>columns</code> and the specified converter
   * to convert the native data.
   */
  public textFileRow(Vector columns, tinySQLConverter conv)
  {
    super(columns, conv);
  }

  /**
   * Copyconstructor: create a new dbfFileRow using the same
   * definitions, but discard the cache and the data.
   */
  public textFileRow(textFileRow copy)
  {
    super(copy);
    clearCache();
  }

  /**
   * set the data for this row. The cache is now invalid an will be cleared.
   */
  public void setData(byte[] line)
  {
    data = line;
    clearCache();
  }

  /**
   * @returns the current native data row.
   */
  public byte[] getData()
  {
    return data;
  }

  /**
   * converts the object value into a char array and put the array on the current
   * row.
   */
  protected void nativePut(tsColumn column, Object value)
  {
    try
    {
      byte[] b = (byte[]) value;

      // enforce the correct column length
      if (b == null)
      {
        b = new byte[column.getSize()];
      }
      if (data == null)
      {
        Log.warn("data is null, skipping update of this row.");
      }
      else
      {
        System.arraycopy(b, 0, data, column.getBytePosition(), b.length);
      }

    }
    catch (Exception e)
    {
      Log.error("Error on conversion: ", e);
    }
  }

  /**
   * Retrieve a column's string value from the current row.
   *
   * @param column the column name
   * @see tinySQLTable#GetCol
   */
  protected Object nativeGet(tsColumn coldef) throws tinySQLException
  {
    int size = coldef.getSize();
    byte[] retval = new byte[size];

    System.arraycopy(data, coldef.getBytePosition(), retval, 0, size);
    return retval;
  }

}
