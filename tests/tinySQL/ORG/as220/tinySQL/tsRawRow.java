package ORG.as220.tinySQL;

import ORG.as220.tinySQL.util.Log;

import java.util.Vector;

/**
 * A tsRawRow is the abstract base class for all rows containing native
 * data. Conversion is done using the functions nativeGet and nativePut.
 * The results of nativeGet() are cached for better performance.
 * <p>
 * Subclasses have to implement nativeGet and nativePut to convert from
 * and into the native dataformat used in the table.
 */
public abstract class tsRawRow extends tsPhysicalRow
{
  private Object[] cache = null;
  private tinySQLConverter converter;

  /**
   * Creates a new dbfFileRow using the columns contained in
   * the vector <code>columns</code> and the specified converter
   * to convert the native data.
   */
  public tsRawRow(Vector columns, tinySQLConverter converter)
  {
    super(columns);
    this.converter = converter;
    cache = new Object[columns.size()];
  }

  /**
   * Copyconstructor: create a new dbfFileRow using the same
   * definitions, but discard the cache and the data.
   */
  public tsRawRow(tsRawRow copy)
  {
    super(copy);
    this.converter = copy.converter;
    cache = new Object[size()];
  }

  /**
   * puts a JDBC value to the row by converting it into a native value.
   *
   * This calls nativePut to do the work.
   */
  public void putNative(int column, Object o) throws tinySQLException
  {
    nativePut(getColumnDefinition(column), o);
    cache[column] = null;
  }

  /**
   * reads a JDBC value from the row. If the value has yet been read,
   * the value is converted from native form into the JDBC format, and
   *
   * This calls nativePut to do the work.
   */
  public Object getNative(int column) throws tinySQLException
  {
    return nativeGet(getColumnDefinition(column));
  }

  /**
   * clears the object cache and forces a reconvert of all object
   * in the next get call.
   */
  public void clearCache()
  {
    cache = new Object[size()];
  }

  /**
   * @returns the JDBC-data-object for the column <code>col</code> or null
   * if the converter was unable to create a valid object.
   */
  public Object get(int col)
  {
    Object co = cache[col];
    if (co == null)
    {
      try
      {
        tsColumn coldef = getColumnDefinition(col);
        co = converter.convertNativeToJDBC(coldef, nativeGet(coldef));

        cache[col] = co;
      }
      catch (Exception e)
      {
        Log.error("Failed to getColumn", e);
        return null;
      }
    }
    return co;
  }

  /**
   * puts a value into the cache and converts it into a native value.
   */
  public void put(int col, Object o) throws tinySQLException
  {
    cache[col] = o;
    tsColumn coldef = getColumnDefinition(col);
    nativePut(coldef, converter.convertJDBCToNative(coldef, o));
  }

  /**
   * returns a JDBC-Value for the column specified in col.
   * When implementing the method make sure, the returned value is a
   * valid JDBC object or <code>null</code>.
   *
   * results of this call a cached.
   */
  protected abstract Object nativeGet(tsColumn col) throws tinySQLException;

  /**
   * sets a JDBC-Value for the column specified in col. When implementing
   * the function ensure, that no invalid datatypes or objects are passed
   * to the native level of the database.
   *
   * results of this call a never cached.
   */
  protected abstract void nativePut(tsColumn col, Object o) throws tinySQLException;
}
