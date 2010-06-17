/**
 *
 * Statement object for the tinySQL driver
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
 *
 */

package ORG.as220.tinySQL;


import ORG.as220.tinySQL.sqlparser.SQLStatement;
import ORG.as220.tinySQL.sqlparser.SQLStatementBatch;
import ORG.as220.tinySQL.util.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Vector;

/**
 * @author mgs - SQLStatement statment added to hold the last statement
 */
public class tinySQLStatement implements Statement
{

  /**
   * Holds the last used queryString. execute() has to be synchronized,
   * to guarantee thread-safety
   */
  private SQLStatement statement;

  /**
   * A Vector holding all resultsets opend by this statement
   */
  private Vector results;
  /**
   *
   * A connection object to execute queries and... stuff
   *
   */
  private tinySQLConnection connection;

  /**
   *
   * A result set returned from this query
   *
   */
  private tinySQLResultSet result;

  /**
   *
   * The max field size for tinySQL
   * This can be pretty big, before things start to break.
   *
   */
  private int max_field_size = 0;

  /**
   *
   * The max rows supported by tinySQL
   * I can't think of any limits, right now, but I'm sure some
   * will crop up...
   * Anyway, setting 0 signals we dont know of a limit.
   */
  private int max_rows = 0;

  /**
   *
   * The number of seconds the driver will allow for a SQL statement to
   * execute before giving up.  The default is to wait forever (0).
   *
   */
  private int timeout = 0;

  /**
   * How many rows to fetch in a single run. Default is now 4096 rows.
   */
  private int fetchsize = 2048;

  /**
   * the default resultset type
   */
  private int defaultResultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;

  /**
   * the default fetchdirection for ResultSets
   */
  private int defaultDirection = ResultSet.FETCH_UNKNOWN;

  /**
   * The statement batch of this resultset
   */
  private SQLStatementBatch batch;

  /**
   *
   * Constructs a new tinySQLStatement object.
   * @param conn the tinySQLConnection object
   *
   */
  public tinySQLStatement(tinySQLConnection conn)
  {

    connection = conn;
    results = new Vector();
    batch = new SQLStatementBatch(this);
  }

  /**
   *
   * Execute an SQL statement and return a result set.
   * @see java.sql.Statement#executeQuery
   * @exception SQLException raised for any errors
   * @param sql the SQL statement string
   * @return the result set from the query
   *
   */
  public synchronized ResultSet executeQuery(String sql)
      throws SQLException
  {

    boolean result = execute(sql);
    if (result == true)
      return getResultSet();
    else
      throw new tinySQLException("Statement returned an UpdateCount\n" + sql);

  }

  /**
   *
   * Execute an update, insert, delete, create table, etc. This can
   * be anything that doesn't return rows.
   * @see java.sql.Statement#executeUpdate
   * @exception java.sql.SQLException thrown when an error occurs executing
   * the SQL
   * @return either the row count for INSERT, UPDATE or DELETE or 0 for SQL statements that return nothing
   */
  public synchronized int executeUpdate(String sql) throws SQLException
  {

    boolean result = execute(sql);
    if (result == false)
      return getUpdateCount();
    else
      throw new tinySQLException("Statement returned a ResultSet");
  }

  /**
   *
   * Executes some SQL and returns true or false, depending on
   * the success. The result set is stored in result, and can
   * be retrieved with getResultSet();
   * @see java.sql.Statement#execute
   * @exception SQLException raised for any errors
   * @param sql the SQL to be executed
   * @return true if there is a result set available
   */
  public synchronized boolean execute(String sql) throws SQLException
  {

    // a result set object
    //
    boolean result;

    // execute the statement
    //
    setStatement(connection.getDatabaseEngine().parse(this, sql));
    result = statement.execute();
    return result;
  }

  /**
   *
   * Close any result sets. This is not used by tinySQL.
   * @see java.sql.Statement#close
   *
   */
  public void close() throws SQLException
  {
    Vector v = new Vector(results);

    for (int i = 0; i < v.size(); i++)
    {
      tinySQLResultSet res = (tinySQLResultSet) results.elementAt(i);
      res.close();
    }
  }

  /**
   *
   * Returns the last result set
   * @see java.sql.Statement#getResultSet
   * @return null if no result set is available, otherwise a result set
   *
   */
  public ResultSet getResultSet() throws SQLException
  {

    return statement.getResultSet();
  }

  /**
   * CallBack function called from tinySQLResultSet when the resultset
   * is opened. Add it to the list of open results.
   */
  public void onCreateResultSet(tinySQLResultSet result)
  {
    Log.debug("Statement: ResultSet opened");
    results.add(result);
  }

  /**
   * CallBack function called from tinySQLResultSet when the resultset
   * is opened. Add it to the list of open results.
   */
  public void onCloseResultSet(tinySQLResultSet result)
  {
    Log.debug("Statement: ResultSet close");
    results.remove(result);
  }

  /**
   *
   * Return the row count of the last operation. tinySQL does not support
   * this, so it returns -1
   * @see java.sql.Statement#getUpdateCount
   * @return -1
   */
  public int getUpdateCount() throws SQLException
  {
    return statement.getUpdateCount();
  }

  /**
   *
   * This returns true if there are any pending result sets. This
   * should only be true after invoking execute()
   * @see java.sql.Statement#getMoreResults
   * @return true if rows are to be gotten
   *
   */
  public boolean getMoreResults() throws SQLException
  {

    return statement.getMoreResults();

  }

  /**
   *
   * Get the maximum field size to return in a result set.
   * @see java.sql.Statement#getMaxFieldSize
   * @return the value of max field size
   *
   */
  public int getMaxFieldSize() throws SQLException
  {
    return max_field_size;
  }

  /**
   *
   * set the max field size.
   * @see java.sql.Statement#setMaxFieldSize
   * @param max the maximum field size
   *
   */
  public void setMaxFieldSize(int max) throws SQLException
  {
    max_field_size = max;
  }

  /**
   *
   * Get the maximum row count that can be returned by a result set.
   * @see java.sql.Statement#getMaxRows
   * @return the maximum rows
   *
   */
  public int getMaxRows() throws SQLException
  {
    return max_rows;
  }

  /**
   *
   * Get the maximum row count that can be returned by a result set.
   * @see java.sql.Statement.setMaxRows
   * @param max the max rows
   *
   */
  public void setMaxRows(int max) throws SQLException
  {
    max_rows = max;
  }

  /**
   *
   * If escape scanning is on (the default) the driver will do
   * escape substitution before sending the SQL to the database.
   * @see java.sql.Statement#setEscapeProcessing
   * @param enable this does nothing right now
   *
   */
  public void setEscapeProcessing(boolean enable)
      throws SQLException
  {
    // ignore this call as the parser will always process escapes
  }

  /**
   *
   * Discover the query timeout.
   * @see java.sql.Statement#getQueryTimeout
   * @see setQueryTimeout
   * @return the timeout value for this statement
   *
   */
  public int getQueryTimeout() throws SQLException
  {
    return timeout;
  }

  /**
   *
   * Set the query timeout.
   * @see java.sql.Statement#setQueryTimeout
   * @see getQueryTimeout
   * @param x the new query timeout value
   *
   */
  public void setQueryTimeout(int x) throws SQLException
  {
    timeout = x;
  }

  /**
   *
   * This can be used by another thread to cancel a statement. This
   * doesn't matter for tinySQL, as far as I can tell.
   * @see java.sql.Statement#cancel
   *
   */
  public void cancel()
  {
    // not yet ...
  }

  /**
   *
   * Get the warning chain associated with this Statement
   * @see java.sql.Statement#getWarnings
   * @return the chain of warnings
   *
   */
  public final SQLWarning getWarnings() throws SQLException
  {
    return null;
  }

  /**
   *
   * Clear the warning chain associated with this Statement
   * @see java.sql.Statement#clearWarnings
   *
   */
  public void clearWarnings() throws SQLException
  {
  }

  /**
   *
   * Sets the cursor name for this connection. Presently unsupported.
   *
   */
  public void setCursorName(String unused) throws SQLException
  {
    throw new SQLException("tinySQL does not support cursors.");
  }

  //--------------------------JDBC 2.0-----------------------------


  /**
   * JDBC 2.0
   *
   * Gives the driver a hint as to the direction in which
   * the rows in a result set
   * will be processed. The hint applies only to result sets created
   * using this Statement object.  The default value is
   * ResultSet.FETCH_FORWARD.
   * <p>Note that this method sets the default fetch direction for
   * result sets generated by this <code>Statement</code> object.
   * Each result set has its own methods for getting and setting
   * its own fetch direction.
   * @param direction the initial direction for processing rows
   * @exception SQLException if a database access error occurs
   * or the given direction
   * is not one of ResultSet.FETCH_FORWARD, ResultSet.FETCH_REVERSE, or
   * ResultSet.FETCH_UNKNOWN
   */
  public void setFetchDirection(int direction) throws SQLException
  {
    this.defaultDirection = direction;
  }

  /**
   * JDBC 2.0
   *
   * Retrieves the direction for fetching rows from
   * database tables that is the default for result sets
   * generated from this <code>Statement</code> object.
   * If this <code>Statement</code> object has not set
   * a fetch direction by calling the method <code>setFetchDirection</code>,
   * the return value is implementation-specific.
   *
   * @return the default fetch direction for result sets generated
   *          from this <code>Statement</code> object
   * @exception SQLException if a database access error occurs
   */
  public int getFetchDirection() throws SQLException
  {
    return this.defaultDirection;
  }

  /**
   * JDBC 2.0
   *
   * Gives the JDBC driver a hint as to the number of rows that should
   * be fetched from the database when more rows are needed.  The number
   * of rows specified affects only result sets created using this
   * statement. If the value specified is zero, then the hint is ignored.
   * The default value is zero.
   *
   * @param rows the number of rows to fetch
   * @exception SQLException if a database access error occurs, or the
   * condition 0 <= rows <= this.getMaxRows() is not satisfied.
   */
  public void setFetchSize(int rows) throws SQLException
  {
    if ((rows <= 0) || (rows >= this.getMaxRows()))
      throw new SQLException("Condition 0 <= rows <= this.getMaxRows() is not satisfied");

    fetchsize = rows;
  }

  /**
   * JDBC 2.0
   *
   * Retrieves the number of result set rows that is the default
   * fetch size for result sets
   * generated from this <code>Statement</code> object.
   * If this <code>Statement</code> object has not set
   * a fetch size by calling the method <code>setFetchSize</code>,
   * the return value is implementation-specific.
   * @return the default fetch size for result sets generated
   *          from this <code>Statement</code> object
   * @exception SQLException if a database access error occurs
   */
  public int getFetchSize() throws SQLException
  {
    return fetchsize;
  }

  /**
   * JDBC 2.0
   *
   * Retrieves the result set concurrency.
   */
  public int getResultSetConcurrency() throws SQLException
  {
    return ResultSet.CONCUR_READ_ONLY;
  }

  /**
   * JDBC 2.0
   *
   * Determine the result set type.
   */
  public int getResultSetType() throws SQLException
  {
    return defaultResultSetType;
  }

  public void setDefaultResultSetType(int type)
  {
    this.defaultResultSetType = type;
  }

  /**
   * JDBC 2.0
   *
   * Adds a SQL command to the current batch of commmands for the statement.
   * This method is optional.
   *
   * @param sql typically this is a static SQL INSERT or UPDATE statement
   * @exception SQLException if a database access error occurs, or the
   * driver does not support batch statements
   */
  public void addBatch(String sql)
      throws SQLException
  {
    statement = connection.getDatabaseEngine().parse(this, sql);
    Vector params = statement.getParameters();
    if (params.size() != 0)
    {
      throw new tinySQLException("Parameterized statements are not supported, use PreparedStatement for Parameters");
    }

    addBatch(statement);
  }

  /**
   * JDBC 2.0
   *
   * Adds a SQL command to the current batch of commmands for the statement.
   * This method is optional.
   *
   * @param sql typically this is a static SQL INSERT or UPDATE statement
   * @exception SQLException if a database access error occurs, or the
   * driver does not support batch statements
   */
  protected void addBatch(SQLStatement sql) throws SQLException
  {
    batch.add(sql);
  }

  /**
   * JDBC 2.0
   *
   * Makes the set of commands in the current batch empty.
   * This method is optional.
   *
   * @exception SQLException if a database access error occurs or the
   * driver does not support batch statements
   */
  public void clearBatch() throws SQLException
  {
    batch.clear();
  }

  /**
   * JDBC 2.0
   *
   * Submits a batch of commands to the database for execution.
   * This method is optional.
   *
   * @return an array of update counts containing one element for each
   * command in the batch.  The array is ordered according
   * to the order in which commands were inserted into the batch.
   * @exception SQLException if a database access error occurs or the
   * driver does not support batch statements
   */
  public int[] executeBatch() throws SQLException
  {
    return batch.executeAll();
  }

  /**
   * JDBC 2.0
   *
   * Returns the <code>Connection</code> object
   * that produced this <code>Statement</code> object.
   * @return the connection that produced this statement
   * @exception SQLException if a database access error occurs
   */
  public Connection getConnection() throws SQLException
  {
    return connection;
  }

  protected SQLStatement getStatement()
  {
    return statement;
  }

  protected void setStatement(SQLStatement statement)
  {
    this.statement = statement;
  }

}
