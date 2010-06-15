/*
 *
 * The tinySQLResultSet class for the tinySQL JDBC Driver
 *
 * A lot of this code is based on or directly taken from
 * George Reese's (borg@imaginary.com) mSQL driver.
 *
 * So, it's probably safe to say:
 *
 * Portions of this code Copyright (c) 1996 George Reese
 *
 * The rest of it:
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
 */

package ORG.as220.tinySQL;


import ORG.as220.tinySQL.sqlparser.ParserUtils;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.Calendar;

/**
 * @author Thomas Morgner <mgs@sherito.org>
 * <ul>
 * <li>tinySQLResultSet now holds a reference
 * to statement which created it. If the resultset was not created by an statement,
 * <code>null</code> is returned (f.i. used in DatabaseMetaData-Queries).
 * <li>Changed the code to support setting a fetchsize for queries.
 * <li>Parsing of dates corrected to format yyyyMMdd
 * <li>Now supporting the FetchDirection methods, getType().
 * <li>Concurrency is not returned as CONCUR_READ_ONLY as lowest common standard.
 * <li>getColumnTypes returns now values given in tsResultSet and does not try to
 * guess from strings.
 * </ul>
 */
public class tinySQLResultSet implements java.sql.ResultSet
{

  // The Statement that created this resultset
  private tinySQLStatement statement;

  /**
   *
   * The tsResultSet
   *
   */
  private tsResultSet result;

  /**
   *
   * A tsRow object to hold the current row
   *
   */
  private tsResultRow current_row;

  /**
   *
   * The index of the current row
   *
   */
  private int current_row_index = -1;

  /**
   *
   * The meta data for this result set.
   *
   */
  private tinySQLResultSetMetaData meta;

  /**
   *
   * Given a tsResultSet, this will construct a new tinySQLResultSet
   * @param res the tsResultSet from a query
   *
   */
  public tinySQLResultSet(tsResultSet res, tinySQLStatement statement)
  {
    result = res;
    this.statement = statement;
    if (statement != null)
    {
      statement.onCreateResultSet(this);
    }
  }

  /**
   *
   * Advance to the next row in the result set.
   * @see java.sql.ResultSet#next
   * @exception SQLException thrown in case of error
   * @return true if there are any more rows to process, otherwise false.
   *
   */
  public synchronized boolean next() throws SQLException
  {

    try
    {
      current_row_index++;

      if (current_row_index == result.getResultSize())
      {
        // Moved Pos to 1 row after the last row
//        current_row_index = result.getResultSize ();
        return false;
      }

      current_row = result.getResultRowAt(current_row_index);

      if (current_row_index == result.getResultSize())
      {
        // More Pos to 1 row after the last row
//        current_row_index = result.getResultSize ();
        return false;
      }

      return true;
    }
    catch (Exception e)
    {
      throw new tinySQLException(e.getMessage(), e);
    }

  }

  /**
   *
   * Provides a method to close a ResultSet
   *
   * @see java.sql.ResultSet#close
   *
   */
  public void close() throws SQLException
  {
    result.close();
    if (statement != null)
    {
      statement.onCloseResultSet(this);
    }
  }

  /**
   *
   * Returns whether or not the last column read was null.
   * tinySQL doesn't have nulls, so this is inconsequential...
   * @see java.sql.ResultSet#wasNull
   * @return true if the column was null, false otherwise
   *
   */
  public boolean wasNull() throws SQLException
  {
    return false;
  }

  /**
   *
   * Gets the value of a column (by index) as a String.
   * @see java.sql.ResultSet#getString
   * @exception SQLException thrown for bogus column index
   * @param column the column index
   * @return the column's String value
   *
   */
  public String getString(int column) throws SQLException
  {

    // retrieve the column at the specified index. tinySQL
    // has a column offset of zero, while JDBC uses one.
    // Because of this, I need to subtract one from the
    // index to get the tinySQL index.
    //
//    tsColumn col = result.columnAtIndex(column-1);

    // return the column's value
    //
    try
    {
      Object o = getObject(column);
      return ParserUtils.convertToString(o);
    }
    catch (Exception e)
    {
      throw new tinySQLException("Error in getString ():", e);
    }
  }

  /**
   *
   * Get the value of a column in the current row as a Java byte.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @return the column value
   *
   */
  public byte getByte(int column) throws SQLException
  {

    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return 0;

      Number n = ParserUtils.convertToNumber(o);
      if (n == null)
        return 0;

      return n.byteValue();
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to byte", e);
    }
  }

  /**
   *
   * Get the value of a column in the current row as boolean
   * @see java.sql.ResultSet#getBoolean
   * @exception SQLException Harum Scarum's coming down fast, Clay...
   * @param column the column index
   * @return false for "", null, or "0"; true otherwise
   */
  public boolean getBoolean(int column) throws SQLException
  {

    try
    {

      // get the column as a string
      //
      Object o = getObject(column);

      Boolean b = ParserUtils.convertToBoolean(o);
      return b.booleanValue();
    }
    catch (Exception e)
    {
      throw new SQLException(e.getMessage());
    }
  }

  /**
   *
   * Get the value of a column in the current row as a short.
   * @see java.sql.ResultSet#getShort
   * @exception SQLException D'ohh!
   * @param column the column being retrieved
   * @return the column as a short
   *
   */
  public short getShort(int column)
      throws SQLException
  {

    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return 0;

      Number n = ParserUtils.convertToNumber(o);
      if (n == null)
        return 0;

      return n.shortValue();
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to short", e);
    }
  }

  /**
   *
   * Retrieve a column from the current row as an int
   * @see java.sql.ResultSet#getInt
   * @exception SQLException bad things... the wind began to howl...
   * @param column the column being retrieved
   * @return the column as an integer
   *
   */
  public int getInt(int column)
      throws SQLException
  {

    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return 0;

      Number n = ParserUtils.convertToNumber(o);
      if (n == null)
        return 0;

      return n.intValue();
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to integer", e);
    }
  }

  /**
   *
   * Get the value of a column in the current row as a long
   * @see java.sql.ResultSet#getLong
   * @exception SQLException in case of an error
   * @param column the column being retrieved
   * @return the column as a long
   *
   */
  public long getLong(int column)
      throws SQLException
  {

    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return 0;

      Number n = ParserUtils.convertToNumber(o);
      if (n == null)
        return 0;

      return n.longValue();
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to long", e);
    }
  }

  /**
   *
   * Return a column as a float.
   * @see java.sql.ResultSet#getFloat
   * @exception SQLException in case of error
   * @param column the column being retrieved
   * @return the column as a float
   *
   */
  public float getFloat(int column)
      throws SQLException
  {
    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return 0;

      Number n = ParserUtils.convertToNumber(o);
      if (n == null)
        return 0;

      return n.floatValue();
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to float", e);
    }
  }

  /**
   *
   * Return a column as a double
   * @see java.sql.ResultSet#getDouble
   * @exception SQLException in case of error
   * @param column the column being retrieved
   * @return the column as a double
   *
   */
  public double getDouble(int column)
      throws SQLException
  {

    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return 0;

      Number n = ParserUtils.convertToNumber(o);
      if (n == null)
        return 0;

      return n.doubleValue();
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to double", e);
    }
  }

  /**
   *
   * Return a column as a BigDecimal object
   * @see java.sql.ResultSet#getBigDecimal
   * @exception SQLException in case of a problem
   * @param column the column being retrieved
   * @param scale the number of digits to the right of the decimal
   * @return the column as a BigDecimal
   * @deprecated
   */
  public BigDecimal getBigDecimal(int column, int scale)
      throws SQLException
  {

    try
    {
      // get the column as a string
      //
      Object o = getObject(column);
      if (o == null)
        return null;

      BigDecimal n = ParserUtils.convertToNumber(o);
      if (n == null)
        return null;

      return n.setScale(scale);
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to BigDecimal", e);
    }
  }

  /**
   *
   * Get the value of a column in the current row as a Java byte array.
   * @see java.sql.ResultSet#getBytes
   * @exception SQLException thrown in case of trouble
   * @param column the column being retrieved
   * @return a byte array that is the value of the column
   *
   */
  public byte[] getBytes(int column) throws SQLException
  {

    // get the column as a string
    //
    String str = getString(column);

    if (str == null) return null;
    try
    {
      return str.getBytes(str);
    }
    catch (java.io.UnsupportedEncodingException e)
    {
      throw new tinySQLException("Bad bytes!: ", e);
    }

  }

  /**
   *
   * Get the value of a column in the current row as a java.sql.Date object.
   * @see java.sqlResultSet#getDate
   * @exception SQLException thrown in case of error
   * @param column the column being retrieved
   * @return the java.sql.Date object for the column
   *
   */
  public java.sql.Date getDate(int column)
      throws SQLException
  {

    // get the column as a string
    //
    Object o = getObject(column);

    // return null if the string is null
    //
    if (o == null) return null;

    java.sql.Date date = ParserUtils.convertToDate(o);
    if (date == null)
      return null;

    return date;
  }

  /**
   *
   * Get the value of a column in the current row as a java.sql.Time object.
   *
   * @see java.sql.ResultSet#getTime
   * @exception SQLException thrown in the event of troubles
   * @param column the column being retrieved
   * @return the column as a java.sql.Time object
   *
   */
  public java.sql.Time getTime(int column)
      throws SQLException
  {

    // get the column as a string
    //
    Object o = getObject(column);

    // return null if the string is null
    //
    if (o == null) return null;

    java.sql.Time date = ParserUtils.convertToTime(o);
    if (date == null)
      return null;

    return date;
  }

  /**
   * Get the value of a column in the current row as a java.sql.Timestamp
   * @see java.sql.ResultSet#getTimestamp
   * @exception SQLException thrown in the event of troubles
   * @param column the column being retrieved
   * @return the column as a java.sql.Timestamp object
   */
  public java.sql.Timestamp getTimestamp(int column)
      throws SQLException
  {

    // get the column as a string
    //
    Object o = getObject(column);

    // return null if the string is null
    //
    if (o == null) return null;

    java.sql.Timestamp date = ParserUtils.convertToTimestamp(o);
    if (date == null)
      return null;

    return date;

  }

  /**
   *
   * This is not currently supported.
   *
   */
  public java.io.InputStream getAsciiStream(int column)
      throws SQLException
  {
    return null;
  }

  /**
   *
   * This is not currently supported.
   * @deprecated
   *
   */
  public java.io.InputStream getUnicodeStream(int column)
      throws SQLException
  {
    return null;
  }

  /**
   *
   * This is not currently supported.
   *
   */
  public java.io.InputStream getBinaryStream(int column)
      throws SQLException
  {
    return null;
  }


  /**
   *
   * Get the name of the cursor corresponding to this result set.
   * This has to meaning to tinySQL
   * @see java.sql.ResultSet#getCursorName
   * @return ""
   *
   */
  public String getCursorName() throws SQLException
  {
    return "";
  }

  /**
   *
   * Returns a ResultSetMetaData object for this result set
   * @see java.sql.ResultSet#getMetaData
   * @exception SQLException thrown on error getting meta-data
   * @return ResultSetMetaData object containing result set info
   *
   */
  public ResultSetMetaData getMetaData()
      throws SQLException
  {

    // if we didn't instantiate a meta data object, then
    // do so. Since it's a field of this object, and
    // not private to this method, it will stay around
    // between calls to this method.
    //
    if (meta == null)
    {
      meta = new tinySQLResultSetMetaData(result);
    }

    // return the ResultSetMetaData object
    //
    return meta;
  }

  /**
   *
   * Retrieves data as objects
   * @see java.sql.ResultSet#getObject
   * @exception SQLException in the event of an error
   * @param column the column desired
   * @param type the SQL data type of the field
   * @scale preceision for BigDecimals
   * @return the column specified as an Object
   *
   */
  public Object getObject(int column, int type, int scale)
      throws SQLException
  {

    switch (type)
    {
      case Types.BIT:
        return new Boolean(getBoolean(column));

      case Types.TINYINT:
        return new Character((char) getInt(column));

      case Types.SMALLINT:
        return new Integer(getShort(column));

      case Types.INTEGER:
        return new Integer(getInt(column));

      case Types.BIGINT:
        return new Long(getLong(column));

      case Types.FLOAT:
        return new Float(getFloat(column));

      case Types.REAL:
        return new Float(getFloat(column));

      case Types.DOUBLE:
        return new Double(getDouble(column));

      case Types.NUMERIC:
        return getBigDecimal(column, scale);

      case Types.DECIMAL:
        return getBigDecimal(column, scale);

      case Types.CHAR:
        return getString(column);

      case Types.VARCHAR:
        return getString(column);

      case Types.LONGVARCHAR:
        return getString(column);

      case Types.DATE:
        return getDate(column);

      case Types.TIME:
        return getTime(column);

      case Types.TIMESTAMP:
        return getTimestamp(column);

      case Types.BINARY:
        return getBytes(column);

      case Types.VARBINARY:
        return getBytes(column);

      case Types.LONGVARBINARY:
        return getBytes(column);

      default:
        return null;
    }
  }

  /**
   *
   * Same as above, except with a default scale to 0.
   *
   */
  public Object getObject(int column, int type)
      throws SQLException
  {
    return getObject(column, type, 0);
  }

  /**
   *
   * Same as above, except using the column's default SQL type.
   *
   */
  public Object getObject(int column) throws SQLException
  {

    return current_row.get(column - 1);
  }

  /**
   *
   * Return the String value of a column given its name, rather than
   * its index.
   * @see java.sql.ResultSet#getString
   * @param name the name of the column desired
   * @return the value of the column as a String
   *
   */
  public String getString(String name) throws SQLException
  {

    String retval = getString(findColumn(name));
    return retval;

  }

  /**
   *
   * Returns the column as a byte based on column name
   *
   */
  public byte getByte(String columnName) throws SQLException
  {

    return getByte(findColumn(columnName));

  }

  /**
   *
   * Get the value of a boolean column in the current row
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is false
   *
   */
  public boolean getBoolean(String columnName) throws SQLException
  {

    return getBoolean(findColumn(columnName));

  }

  /**
   *
   * Get the value of a short by column name
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is 0
   *
   */
  public short getShort(String columnName) throws SQLException
  {

    return getShort(findColumn(columnName));

  }

  /**
   *
   * Get the integer value of a column by name
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is 0
   *
   */
  public int getInt(String columnName) throws SQLException
  {

    return getInt(findColumn(columnName));

  }

  /**
   *
   * Get the long value of a column by name
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is 0
   *
   */
  public long getLong(String columnName) throws SQLException
  {

    return getLong(findColumn(columnName));

  }

  /**
   *
   * Get the float value of a column by name
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is 0
   *
   */
  public float getFloat(String columnName) throws SQLException
  {

    return getFloat(findColumn(columnName));

  }

  /**
   *
   * Get the double value of a named column
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is 0
   *
   */
  public double getDouble(String columnName) throws SQLException
  {

    return getDouble(findColumn(columnName));

  }

  /**
   *
   * Get the value of a named column as a BigDecimal object
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is null
   * @deprecated
   */
  public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException
  {

    return getBigDecimal(findColumn(columnName), scale);

  }

  /**
   *
   * Get the value of a named column as a byte array
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is null
   *
   */
  public byte[] getBytes(String columnName) throws SQLException
  {

    return getBytes(findColumn(columnName));

  }

  /**
   *
   * Get a named column as a java.sql.Date
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is null
   *
   */
  public java.sql.Date getDate(String columnName) throws SQLException
  {

    return getDate(findColumn(columnName));

  }

  /**
   *
   * Get a named column as a java.sql.Time
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is null
   *
   */
  public java.sql.Time getTime(String columnName) throws SQLException
  {

    return getTime(findColumn(columnName));

  }

  /**
   *
   * Get a named column as a java.sql.Time
   * @param columnName is the SQL name of the column
   * @return the column value; if isNull the value is null
   *
   */
  public java.sql.Timestamp getTimestamp(String columnName)
      throws SQLException
  {

    return getTimestamp(findColumn(columnName));

  }

  /**
   *
   * This is unsupported, but we'll try to call the corresponding
   * call by column index.
   *
   */
  public java.io.InputStream getAsciiStream(String columnName)
      throws SQLException
  {

    return getAsciiStream(findColumn(columnName));

  }

  /**
   *
   * This is unsupported, but we'll try to call the corresponding
   * call by column index.
   * @deprecated
   *
   */
  public java.io.InputStream getUnicodeStream(String columnName)
      throws SQLException
  {

    return getUnicodeStream(findColumn(columnName));

  }

  /**
   *
   * This is unsupported, but we'll try to call the corresponding
   * call by column index.
   *
   */
  public java.io.InputStream getBinaryStream(String columnName)
      throws SQLException
  {

    return getBinaryStream(findColumn(columnName));

  }

  /**
   *
   * Get the value of a named column as an object
   * @param columnName the SQL column name
   * @param sqlType SQL type code defined by java.sql.Types
   * @return the parameter as an Object
   *
   */
  public Object getObject(String columnName, int sqlType, int scale)
      throws SQLException
  {

    return getObject(findColumn(columnName), sqlType, scale);

  }

  /**
   *
   * Same as above, except defaulting scale to 0.
   *
   */
  public Object getObject(String columnName, int type)
      throws SQLException
  {

    return getObject(findColumn(columnName), type, 0);

  }

  /**
   *
   * Same as above, except returning the default SQL type
   *
   */
  public Object getObject(String columnName) throws SQLException
  {
    return getObject(findColumn(columnName));
  }

  /**
   *
   * Given a column name, this method returns the column number for that
   * name.  Column name to number mappings are kept inside a Hashtable.
   * Applications that do not need the overhead of this calculation are
   * not penalized since the mapping only occurs on the first attempt to
   * access a column number by name.
   * @exception java.sql.SQLException thrown if a bad name is passed
   * @param name the name of the column desired
   * @return the column number, 1 being the first column
   *
   */
  public int findColumn(String name) throws SQLException
  {
    // tinySQL starts counting at 0, not 1
    return result.findColumn(name) + 1;
  }

  /**
   *
   * Return the warning chain. This is presently unsupported.
   * @see java.sql.Statement#getWarnings
   * @return the chain of warnings
   *
   */
  public SQLWarning getWarnings() throws SQLException
  {
    return null;
  }

  /**
   *
   * Clear the chain of warnings. This does nothing, since the
   * warning chain is not used by tinySQL
   * @see java.sql.Statement#clearWarnings
   *
   */
  public void clearWarnings() throws SQLException
  {
  }


  //--------------------------JDBC 2.0-----------------------------------

  //---------------------------------------------------------------------
  // Getter's and Setter's
  //---------------------------------------------------------------------

  /**
   * JDBC 2.0
   *
   * <p>Gets the value of a column in the current row as a java.io.Reader.
   * @param columnIndex the first column is 1, the second is 2, ...
   */
  public java.io.Reader getCharacterStream(int columnIndex) throws SQLException
  {
    return null;
  }

  /**
   * JDBC 2.0
   *
   * <p>Gets the value of a column in the current row as a java.io.Reader.
   * @param columnName the name of the column
   * @return the value in the specified column as a <code>java.io.Reader</code>
   */
  public java.io.Reader getCharacterStream(String columnName) throws SQLException
  {
    return null;
  }

  /**
   * JDBC 2.0
   *
   * Gets the value of a column in the current row as a java.math.BigDecimal
   * object with full precision.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @return the column value (full precision); if the value is SQL NULL,
   * the result is null
   * @exception SQLException if a database access error occurs
   */
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException
  {
    try
    {
      // get the column as a string
      //
      Object o = getObject(columnIndex);
      if (o == null)
        return null;

      BigDecimal n = ParserUtils.convertToNumber(o);
      if (n == null)
        return null;

      return n;
    }
    catch (Exception e)
    {
      throw new tinySQLException("Unable to convert to BigDecimal", e);
    }
  }

  /**
   * JDBC 2.0
   *
   * Gets the value of a column in the current row as a java.math.BigDecimal
   * object with full precision.
   * @param columnName the column name
   * @return the column value (full precision); if the value is SQL NULL,
   * the result is null
   * @exception SQLException if a database access error occurs
   *
   */
  public BigDecimal getBigDecimal(String columnName) throws SQLException
  {
    return getBigDecimal(findColumn(columnName));
  }

  //---------------------------------------------------------------------
  // Traversal/Positioning
  //---------------------------------------------------------------------

  /**
   * JDBC 2.0
   *
   * <p>Indicates whether the cursor is before the first row in the result
   * set.
   *
   * @return true if the cursor is before the first row, false otherwise. Returns
   * false when the result set contains no rows.
   * @exception SQLException if a database access error occurs
   */
  public boolean isBeforeFirst() throws SQLException
  {
    if (current_row_index == -1)
      return true;

    return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Indicates whether the cursor is after the last row in the result
   * set.
   *
   * @return true if the cursor is  after the last row, false otherwise.  Returns
   * false when the result set contains no rows.
   * @exception SQLException if a database access error occurs
   */
  public boolean isAfterLast() throws SQLException
  {
    if (current_row_index < 0)
      return false;

    if (current_row_index == result.getResultSize())
      return true;
    else
      return false;

  }

  /**
   * JDBC 2.0
   *
   * <p>Indicates whether the cursor is on the first row of the result set.
   *
   * @return true if the cursor is on the first row, false otherwise.
   * @exception SQLException if a database access error occurs
   */
  public boolean isFirst() throws SQLException
  {
    if (current_row_index < 0)
      return false;
    if ((current_row_index == 0) && (result.getResultSize() != 0))
      return true;
    else
      return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Indicates whether the cursor is on the last row of the result set.
   * Note: Calling the method <code>isLast</code> may be expensive
   * because the JDBC driver
   * might need to fetch ahead one row in order to determine
   * whether the current row is the last row in the result set.
   *
   * @return true if the cursor is on the last row, false otherwise.
   * @exception SQLException if a database access error occurs
   */
  public boolean isLast() throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor to the front of the result set, just before the
   * first row. Has no effect if the result set contains no rows.
   *
   * @exception SQLException if a database access error occurs or the
   * result set type is TYPE_FORWARD_ONLY
   */
  public void beforeFirst() throws SQLException
  {
    return;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor to the end of the result set, just after the last
   * row.  Has no effect if the result set contains no rows.
   *
   * @exception SQLException if a database access error occurs or the
   * result set type is TYPE_FORWARD_ONLY
   */
  public void afterLast() throws SQLException
  {
    return;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor to the first row in the result set.
   *
   * @return true if the cursor is on a valid row; false if
   *         there are no rows in the result set
   * @exception SQLException if a database access error occurs or the
   * result set type is TYPE_FORWARD_ONLY
   */
  public boolean first() throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor to the last row in the result set.
   *
   * @return true if the cursor is on a valid row;
   * false if there are no rows in the result set
   * @exception SQLException if a database access error occurs or the
   * result set type is TYPE_FORWARD_ONLY.
   */
  public boolean last() throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Retrieves the current row number.  The first row is number 1, the
   * second number 2, and so on.
   *
   * @return the current row number; 0 if there is no current row
   * @exception SQLException if a database access error occurs
   */
  public int getRow() throws SQLException
  {
    return 0;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor to the given row number in the result set.
   *
   * <p>If the row number is positive, the cursor moves to
   * the given row number with respect to the
   * beginning of the result set.  The first row is row 1, the second
   * is row 2, and so on.
   *
   * <p>If the given row number is negative, the cursor moves to
   * an absolute row position with respect to
   * the end of the result set.  For example, calling
   * <code>absolute(-1)</code> positions the
   * cursor on the last row, <code>absolute(-2)</code> indicates the next-to-last
   * row, and so on.
   *
   * <p>An attempt to position the cursor beyond the first/last row in
   * the result set leaves the cursor before/after the first/last
   * row, respectively.
   *
   * <p>Note: Calling <code>absolute(1)</code> is the same
   * as calling <code>first()</code>.
   * Calling <code>absolute(-1)</code> is the same as calling <code>last()</code>.
   *
   * @return true if the cursor is on the result set; false otherwise
   * @exception SQLException if a database access error occurs or
   * row is 0, or result set type is TYPE_FORWARD_ONLY.
   */
  public boolean absolute(int row) throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor a relative number of rows, either positive or negative.
   * Attempting to move beyond the first/last row in the
   * result set positions the cursor before/after the
   * the first/last row. Calling <code>relative(0)</code> is valid, but does
   * not change the cursor position.
   *
   * <p>Note: Calling <code>relative(1)</code>
   * is different from calling <code>next()</code>
   * because is makes sense to call <code>next()</code> when there is no current row,
   * for example, when the cursor is positioned before the first row
   * or after the last row of the result set.
   *
   * @return true if the cursor is on a row; false otherwise
   * @exception SQLException if a database access error occurs, there
   * is no current row, or the result set type is TYPE_FORWARD_ONLY
   */
  public boolean relative(int rows) throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * <p>Moves the cursor to the previous row in the result set.
   *
   * <p>Note: <code>previous()</code> is not the same as
   * <code>relative(-1)</code> because it
   * makes sense to call</code>previous()</code> when there is no current row.
   *
   * @return true if the cursor is on a valid row; false if it is off the result set
   * @exception SQLException if a database access error occurs or the
   * result set type is TYPE_FORWARD_ONLY
   */
  public boolean previous() throws SQLException
  {
    return false;
  }

  //---------------------------------------------------------------------
  // Properties
  //---------------------------------------------------------------------


  /**
   * JDBC 2.0
   *
   * Gives a hint as to the direction in which the rows in this result set
   * will be processed.  The initial value is determined by the statement
   * that produced the result set.  The fetch direction may be changed
   * at any time.
   *
   * @exception SQLException if a database access error occurs or
   * the result set type is TYPE_FORWARD_ONLY and the fetch direction is not
   * FETCH_FORWARD.
   */
  public void setFetchDirection(int direction) throws SQLException
  {
    return;
  }

  /**
   * JDBC 2.0
   *
   * Returns the fetch direction for this result set.
   *
   * @return the current fetch direction for this result set
   * @exception SQLException if a database access error occurs
   */
  public int getFetchDirection() throws SQLException
  {
    return FETCH_FORWARD;
  }

  /**
   * JDBC 2.0
   *
   * Gives the JDBC driver a hint as to the number of rows that should
   * be fetched from the database when more rows are needed for this result
   * set.  If the fetch size specified is zero, the JDBC driver
   * ignores the value and is free to make its own best guess as to what
   * the fetch size should be.  The default value is set by the statement
   * that created the result set.  The fetch size may be changed at any
   * time.
   *
   * @param rows the number of rows to fetch
   * @exception SQLException if a database access error occurs or the
   * condition 0 <= rows <= this.getMaxRows() is not satisfied.
   */
  public void setFetchSize(int rows) throws SQLException
  {
    if (rows <= 0)
      throw new SQLException("Condition 0 <= rows <= this.getMaxRows() is not satisfied");

    result.setFetchSize(rows);
  }

  /**
   * JDBC 2.0
   *
   * Returns the fetch size for this result set.
   *
   * @return the current fetch size for this result set
   * @exception SQLException if a database access error occurs
   */
  public int getFetchSize() throws SQLException
  {
    return result.getFetchSize();
  }

  /**
   * JDBC 2.0
   *
   * Returns the type of this result set.  The type is determined by
   * the statement that created the result set.
   *
   * @return TYPE_FORWARD_ONLY, TYPE_SCROLL_INSENSITIVE, or
   * TYPE_SCROLL_SENSITIVE
   * @exception SQLException if a database access error occurs
   */
  public int getType() throws SQLException
  {
    return result.getType();
  }

  /**
   * JDBC 2.0
   *
   * Returns the concurrency mode of this result set.  The concurrency
   * used is determined by the statement that created the result set.
   *
   * @return the concurrency type, CONCUR_READ_ONLY or CONCUR_UPDATABLE
   * @exception SQLException if a database access error occurs
   */
  public int getConcurrency() throws SQLException
  {
    return CONCUR_READ_ONLY;
  }

  //---------------------------------------------------------------------
  // Updates
  //---------------------------------------------------------------------

  /**
   * JDBC 2.0
   *
   * Indicates whether the current row has been updated.  The value returned
   * depends on whether or not the result set can detect updates.
   *
   * @return true if the row has been visibly updated by the owner or
   * another, and updates are detected
   * @exception SQLException if a database access error occurs
   *
   * @see DatabaseMetaData#updatesAreDetected
   */
  public boolean rowUpdated() throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * Indicates whether the current row has had an insertion.  The value returned
   * depends on whether or not the result set can detect visible inserts.
   *
   * @return true if a row has had an insertion and insertions are detected
   * @exception SQLException if a database access error occurs
   *
   * @see DatabaseMetaData#insertsAreDetected
   */
  public boolean rowInserted() throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * Indicates whether a row has been deleted.  A deleted row may leave
   * a visible "hole" in a result set.  This method can be used to
   * detect holes in a result set.  The value returned depends on whether
   * or not the result set can detect deletions.
   *
   * @return true if a row was deleted and deletions are detected
   * @exception SQLException if a database access error occurs
   *
   * @see DatabaseMetaData#deletesAreDetected
   */
  public boolean rowDeleted() throws SQLException
  {
    return false;
  }

  /**
   * JDBC 2.0
   *
   * Give a nullable column a null value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @exception SQLException if a database access error occurs
   */
  public void updateNull(int columnIndex) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateNull.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a boolean value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateBoolean(int columnIndex, boolean x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBoolean.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a byte value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateByte(int columnIndex, byte x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateByte.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a short value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateShort(int columnIndex, short x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateShort.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an integer value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateInt(int columnIndex, int x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateInt.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a long value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateLong(int columnIndex, long x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateLong.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a float value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateFloat(int columnIndex, float x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateFloat.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Double value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateDouble(int columnIndex, double x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateDouble.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a BigDecimal value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBigDecimal.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a String value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateString(int columnIndex, String x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateString.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a byte array value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateBytes(int columnIndex, byte x[]) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBytes.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Date value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateDate(int columnIndex, java.sql.Date x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateDate.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Time value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateTime(int columnIndex, java.sql.Time x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateTime.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Timestamp value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support updateTimestamp.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an ascii stream value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @param length the length of the stream
   * @exception SQLException if a database access error occurs
   */
  public void updateAsciiStream(int columnIndex,
                                java.io.InputStream x,
                                int length) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateAsciiStream.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a binary stream value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @param length the length of the stream
   * @exception SQLException if a database access error occurs
   */
  public void updateBinaryStream(int columnIndex,
                                 java.io.InputStream x,
                                 int length) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBinaryStream.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a character stream value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @param length the length of the stream
   * @exception SQLException if a database access error occurs
   */
  public void updateCharacterStream(int columnIndex,
                                    java.io.Reader x,
                                    int length) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateCharacterStream.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an Object value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
   *  this is the number of digits after the decimal.  For all other
   *  types this value will be ignored.
   * @exception SQLException if a database access error occurs
   */
  public void updateObject(int columnIndex, Object x, int scale)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support updateObject.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an Object value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateObject(int columnIndex, Object x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateObject.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a null value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @exception SQLException if a database access error occurs
   */
  public void updateNull(String columnName) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateNull.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a boolean value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateBoolean(String columnName, boolean x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBoolean.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a byte value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateByte(String columnName, byte x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateByte.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a short value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateShort(String columnName, short x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateShort.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an integer value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateInt(String columnName, int x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateInt.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a long value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateLong(String columnName, long x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateLong.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a float value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateFloat(String columnName, float x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateFloat.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a double value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateDouble(String columnName, double x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateDouble.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a BigDecimal value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateDecimal.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a String value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateString(String columnName, String x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateString.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a byte array value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateBytes(String columnName, byte x[]) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBytes.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Date value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateDate(String columnName, java.sql.Date x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateDate.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Time value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateTime(String columnName, java.sql.Time x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateTime.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a Timestamp value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateTimestamp(String columnName, java.sql.Timestamp x)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support updateTimestamp.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an ascii stream value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @param length of the stream
   * @exception SQLException if a database access error occurs
   */
  public void updateAsciiStream(String columnName,
                                java.io.InputStream x,
                                int length) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateAsciiStream.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a binary stream value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @param length of the stream
   * @exception SQLException if a database access error occurs
   */
  public void updateBinaryStream(String columnName,
                                 java.io.InputStream x,
                                 int length) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateBinaryStream.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with a character stream value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @param length of the stream
   * @exception SQLException if a database access error occurs
   */
  public void updateCharacterStream(String columnName,
                                    java.io.Reader reader,
                                    int length) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateCharacter.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an Object value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
   *  this is the number of digits after the decimal.  For all other
   *  types this value will be ignored.
   * @exception SQLException if a database access error occurs
   */
  public void updateObject(String columnName, Object x, int scale)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support updateObject.");
  }

  /**
   * JDBC 2.0
   *
   * Updates a column with an Object value.
   *
   * The <code>updateXXX</code> methods are used to update column values in the
   * current row, or the insert row.  The <code>updateXXX</code> methods do not
   * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
   * methods are called to update the database.
   *
   * @param columnName the name of the column
   * @param x the new column value
   * @exception SQLException if a database access error occurs
   */
  public void updateObject(String columnName, Object x) throws SQLException
  {
    throw new SQLException("tinySQL does not support updateObject.");
  }

  /**
   * JDBC 2.0
   *
   * Inserts the contents of the insert row into the result set and
   * the database.  Must be on the insert row when this method is called.
   *
   * @exception SQLException if a database access error occurs,
   * if called when not on the insert row, or if not all of non-nullable columns in
   * the insert row have been given a value
   */
  public void insertRow() throws SQLException
  {
    throw new SQLException("tinySQL does not support insertRow.");
  }

  /**
   * JDBC 2.0
   *
   * Updates the underlying database with the new contents of the
   * current row.  Cannot be called when on the insert row.
   *
   * @exception SQLException if a database access error occurs or
   * if called when on the insert row
   */
  public void updateRow() throws SQLException
  {
    throw new SQLException("tinySQL does not support updateRow.");
  }

  /**
   * JDBC 2.0
   *
   * Deletes the current row from the result set and the underlying
   * database.  Cannot be called when on the insert row.
   *
   * @exception SQLException if a database access error occurs or if
   * called when on the insert row.
   */
  public void deleteRow() throws SQLException
  {
    throw new SQLException("tinySQL does not support deleteRow.");
  }

  /**
   * JDBC 2.0
   *
   * Refreshes the current row with its most recent value in
   * the database.  Cannot be called when on the insert row.
   *
   * The <code>refreshRow</code> method provides a way for an application to
   * explicitly tell the JDBC driver to refetch a row(s) from the
   * database.  An application may want to call <code>refreshRow</code> when
   * caching or prefetching is being done by the JDBC driver to
   * fetch the latest value of a row from the database.  The JDBC driver
   * may actually refresh multiple rows at once if the fetch size is
   * greater than one.
   *
   * All values are refetched subject to the transaction isolation
   * level and cursor sensitivity.  If <code>refreshRow</code> is called after
   * calling <code>updateXXX</code>, but before calling <code>updateRow</code>, then the
   * updates made to the row are lost.  Calling the method <code>refreshRow</code> frequently
   * will likely slow performance.
   *
   * @exception SQLException if a database access error occurs or if
   * called when on the insert row
   */
  public void refreshRow() throws SQLException
  {
    throw new SQLException("tinySQL does not support RefreshRow.");
  }

  /**
   * JDBC 2.0
   *
   * Cancels the updates made to a row.
   * This method may be called after calling an
   * <code>updateXXX</code> method(s) and before calling <code>updateRow</code> to rollback
   * the updates made to a row.  If no updates have been made or
   * <code>updateRow</code> has already been called, then this method has no
   * effect.
   *
   * @exception SQLException if a database access error occurs or if
   * called when on the insert row
   *
   */
  public void cancelRowUpdates() throws SQLException
  {
    throw new SQLException("tinySQL does not support cancelRowUpdate.");
  }

  /**
   * JDBC 2.0
   *
   * Moves the cursor to the insert row.  The current cursor position is
   * remembered while the cursor is positioned on the insert row.
   *
   * The insert row is a special row associated with an updatable
   * result set.  It is essentially a buffer where a new row may
   * be constructed by calling the <code>updateXXX</code> methods prior to
   * inserting the row into the result set.
   *
   * Only the <code>updateXXX</code>, <code>getXXX</code>,
   * and <code>insertRow</code> methods may be
   * called when the cursor is on the insert row.  All of the columns in
   * a result set must be given a value each time this method is
   * called before calling <code>insertRow</code>.
   * The method <code>updateXXX</code> must be called before a
   * <code>getXXX</code> method can be called on a column value.
   *
   * @exception SQLException if a database access error occurs
   * or the result set is not updatable
   */
  public void moveToInsertRow() throws SQLException
  {
    throw new SQLException("tinySQL does not support moveToInsertRow.");
  }

  /**
   * JDBC 2.0
   *
   * Moves the cursor to the remembered cursor position, usually the
   * current row.  This method has no effect if the cursor is not on the insert
   * row.
   *
   * @exception SQLException if a database access error occurs
   * or the result set is not updatable
   */
  public void moveToCurrentRow() throws SQLException
  {
    throw new SQLException("tinySQL does not support moveToCurrentRow.");
  }

  /**
   * JDBC 2.0
   *
   * Returns the Statement that produced this <code>ResultSet</code> object.
   * If the result set was generated some other way, such as by a
   * <code>DatabaseMetaData</code> method, this method returns <code>null</code>.
   *
   * @return the Statment that produced the result set or
   * null if the result set was produced some other way
   * @exception SQLException if a database access error occurs
   */
  public Statement getStatement() throws SQLException
  {
    return statement;
  }

  /**
   * JDBC 2.0
   *
   * Returns the value of a column in the current row as a Java object.
   * This method uses the given <code>Map</code> object
   * for the custom mapping of the
   * SQL structured or distinct type that is being retrieved.
   *
   * @param i the first column is 1, the second is 2, ...
   * @param map the mapping from SQL type names to Java classes
   * @return an object representing the SQL value
   */
  public Object getObject(int i, java.util.Map map) throws SQLException
  {
    throw new SQLException("tinySQL does not support getObject.");
  }

  /**
   * JDBC 2.0
   *
   * Gets a REF(&lt;structured-type&gt;) column value from the current row.
   *
   * @param i the first column is 1, the second is 2, ...
   * @return a <code>Ref</code> object representing an SQL REF value
   */
  public Ref getRef(int i) throws SQLException
  {
    throw new SQLException("tinySQL does not support getRef.");
  }

  /**
   * JDBC 2.0
   *
   * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
   *
   * @param i the first column is 1, the second is 2, ...
   * @return a <code>Blob</code> object representing the SQL BLOB value in
   *         the specified column
   */
  public Blob getBlob(int i) throws SQLException
  {
    throw new SQLException("tinySQL does not support getBlob.");
  }

  /**
   * JDBC 2.0
   *
   * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
   *
   * @param i the first column is 1, the second is 2, ...
   * @return a <code>Clob</code> object representing the SQL CLOB value in
   *         the specified column
   */
  public Clob getClob(int i) throws SQLException
  {
    throw new SQLException("tinySQL does not support getClob.");
  }

  /**
   * JDBC 2.0
   *
   * Gets an SQL ARRAY value from the current row of this <code>ResultSet</code> object.
   *
   * @param i the first column is 1, the second is 2, ...
   * @return an <code>Array</code> object representing the SQL ARRAY value in
   *         the specified column
   */
  public Array getArray(int i) throws SQLException
  {
    throw new SQLException("tinySQL does not support getArray.");
  }

  /**
   * JDBC 2.0
   *
   * Returns the value in the specified column as a Java object.
   * This method uses the specified <code>Map</code> object for
   * custom mapping if appropriate.
   *
   * @param colName the name of the column from which to retrieve the value
   * @param map the mapping from SQL type names to Java classes
   * @return an object representing the SQL value in the specified column
   */
  public Object getObject(String colName, java.util.Map map) throws SQLException
  {
    throw new SQLException("tinySQL does not support getObject.");
  }

  /**
   * JDBC 2.0
   *
   * Gets a REF(&lt;structured-type&gt;) column value from the current row.
   *
   * @param colName the column name
   * @return a <code>Ref</code> object representing the SQL REF value in
   *         the specified column
   */
  public Ref getRef(String colName) throws SQLException
  {
    throw new SQLException("tinySQL does not support getRef.");
  }

  /**
   * JDBC 2.0
   *
   * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
   *
   * @param colName the name of the column from which to retrieve the value
   * @return a <code>Blob</code> object representing the SQL BLOB value in
   *         the specified column
   */
  public Blob getBlob(String colName) throws SQLException
  {
    throw new SQLException("tinySQL does not support getBlob.");
  }

  /**
   * JDBC 2.0
   *
   * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
   *
   * @param colName the name of the column from which to retrieve the value
   * @return a <code>Clob</code> object representing the SQL CLOB value in
   *         the specified column
   */
  public Clob getClob(String colName) throws SQLException
  {
    throw new SQLException("tinySQL does not support getClob.");
  }

  /**
   * JDBC 2.0
   *
   * Gets an SQL ARRAY value in the current row of this <code>ResultSet</code> object.
   *
   * @param colName the name of the column from which to retrieve the value
   * @return an <code>Array</code> object representing the SQL ARRAY value in
   *         the specified column
   */
  public Array getArray(String colName) throws SQLException
  {
    throw new SQLException("tinySQL does not support getArray.");
  }

  /**
   * JDBC 2.0
   *
   * Gets the value of a column in the current row as a java.sql.Date
   * object. This method uses the given calendar to construct an appropriate millisecond
   * value for the Date if the underlying database does not store
   * timezone information.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param cal the calendar to use in constructing the date
   * @return the column value; if the value is SQL NULL, the result is null
   * @exception SQLException if a database access error occurs
   */
  public java.sql.Date getDate(int columnIndex, Calendar cal) throws SQLException
  {
    throw new SQLException("tinySQL does not support getDate.");
  }

  /**
   * Gets the value of a column in the current row as a java.sql.Date
   * object. This method uses the given calendar to construct an appropriate millisecond
   * value for the Date, if the underlying database does not store
   * timezone information.
   *
   * @param columnName the SQL name of the column from which to retrieve the value
   * @param cal the calendar to use in constructing the date
   * @return the column value; if the value is SQL NULL, the result is null
   * @exception SQLException if a database access error occurs
   */
  public java.sql.Date getDate(String columnName, Calendar cal) throws SQLException
  {
    throw new SQLException("tinySQL does not support getDate.");
  }

  /**
   * Gets the value of a column in the current row as a java.sql.Time
   * object. This method uses the given calendar to construct an appropriate millisecond
   * value for the Time if the underlying database does not store
   * timezone information.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param cal the calendar to use in constructing the time
   * @return the column value; if the value is SQL NULL, the result is null
   * @exception SQLException if a database access error occurs
   */
  public java.sql.Time getTime(int columnIndex, Calendar cal) throws SQLException
  {
    throw new SQLException("tinySQL does not support getTime.");
  }

  /**
   * Gets the value of a column in the current row as a java.sql.Time
   * object. This method uses the given calendar to construct an appropriate millisecond
   * value for the Time if the underlying database does not store
   * timezone information.
   *
   * @param columnName the SQL name of the column
   * @param cal the calendar to use in constructing the time
   * @return the column value; if the value is SQL NULL, the result is null
   * @exception SQLException if a database access error occurs
   */
  public java.sql.Time getTime(String columnName, Calendar cal) throws SQLException
  {
    throw new SQLException("tinySQL does not support getTime.");
  }

  /**
   * Gets the value of a column in the current row as a java.sql.Timestamp
   * object. This method uses the given calendar to construct an appropriate millisecond
   * value for the Timestamp if the underlying database does not store
   * timezone information.
   *
   * @param columnIndex the first column is 1, the second is 2, ...
   * @param cal the calendar to use in constructing the timestamp
   * @return the column value; if the value is SQL NULL, the result is null
   * @exception SQLException if a database access error occurs
   */
  public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support getTimestamp.");
  }

  /**
   * Gets the value of a column in the current row as a java.sql.Timestamp
   * object. This method uses the given calendar to construct an appropriate millisecond
   * value for the Timestamp if the underlying database does not store
   * timezone information.
   *
   * @param columnName the SQL name of the column
   * @param cal the calendar to use in constructing the timestamp
   * @return the column value; if the value is SQL NULL, the result is null
   * @exception SQLException if a database access error occurs
   */
  public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support getTimestamp.");
  }

}
