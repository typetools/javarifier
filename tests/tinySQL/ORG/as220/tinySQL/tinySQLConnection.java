/*
 *
 * tinySQLConnection - a Connection object for the tinySQL JDBC Driver.
 *
 * Note that since the tinySQL class is abstract, this class needs to
 * be abstract, as well. It's only in such manifestations of tinySQL
 * as textFile that the tinySQLConnection can reach its true potential.
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

import ORG.as220.tinySQL.sqlparser.SQLStatement;

import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author Thomas Morgner <mgs@sherito.org> executetinySQL is now called with a statement
 * containing the SQL-Query String.
 */
public abstract class tinySQLConnection implements java.sql.Connection
{

  /**
   *
   * The tinySQL object
   *
   */
  private tinySQL tsql = null;

  /**
   *
   * The JDBC driver
   *
   */
  private Driver driver;

  /**
   *
   * The URL to the datasource
   *
   */
  private String url;

  /**
   *
   * The user name - currently unused
   *
   */
  private String user;

  /**
   *
   * the catalog - it's not used by tinySQL
   *
   */
  private String catalog;

  /**
   *
   * Transaction isolation level - it's not used by tinySQL
   *
   */
  private int isolation;

  /**
   * Connection-properties.
   */
  private Properties properties;

  /**
   *
   * Constructs a new JDBC Connection for a tinySQL database
   *
   * @exception SQLException in case of an error
   * @param user the user name - currently unused
   * @param u the URL used to connect to the datasource
   * @param d the Driver that instantiated this connection
   *
   */
  public tinySQLConnection(String user, String u, Driver d, Properties p)
      throws SQLException
  {

    this.url = u;
    this.user = user;
    this.driver = d;
    this.properties = p;

    // call get_tinySQL() to return a new tinySQL object.
    // get_tinySQL() is an abstract method which allows
    // subclasses of tinySQL, such as textFile, to be used
    // as JDBC datasources
    //
    tsql = createDatabaseEngine();
    tsql.setConnection(this);
  }

  /**
   * returns the url used to create this connection
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * returns the username used for this connection
   */
  public String getUser()
  {
    return user;
  }

  /**
   *
   * Create and return a tinySQLStatement.
   * @see java.sql.Connection#createStatement
   * @exception SQLException thrown in case of error
   *
   */
  public Statement createStatement() throws SQLException
  {
    return (Statement) new tinySQLStatement(this);
  }

  /**
   *
   * Create and return a PreparedStatement. tinySQL doesn't support
   * these, so it always throws an exception.
   *
   * @see java.sql.Connection#prepareStatement
   * @param sql the SQL Statement
   * @exception SQLException gets thrown if you even look at this method
   *
   */
  public PreparedStatement prepareStatement(String sql)
      throws SQLException
  {
    return new tinySQLPreparedStatement(this, sql, getCatalog());
  }

  /**
   *
   * Create and return a CallableStatement. tinySQL does not support
   * stored procs, so this automatically throws an exception.
   *
   * @see java.sql.Connection#prepareCall
   * @param sql the SQL Statement
   * @exception SQLException gets thrown always
   *
   */
  public CallableStatement prepareCall(String sql)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support stored procedures.");
  }

  /**
   *
   * Converts escaped SQL to tinySQL syntax. This is not supported yet,
   * but some level of it will be meaningful, when tinySQL begins to
   * support scalar functions. For now, it just returns the original SQL.
   *
   * TinySQL does not not have any native SQLSyntax It uses a subset of
   * Ansi-SQL92 commands which are directly processed.
   *
   * @see java.sql.Connection#nativeSQL
   * @param sql the SQL statement
   * @return just what you gave it
   *
   */
  public String nativeSQL(String sql) throws SQLException
  {
    return sql;
  }

  /**
   *
   * Sets autocommit mode - tinySQL has no support for transactions,
   * so this does nothing.
   * @see java.sql.Connection#setAutoCommit
   * @param b this does nothing
   *
   */
  public void setAutoCommit(boolean b) throws SQLException
  {
  }

  /**
   *
   * Commits a transaction. Since all SQL statements are implicitly
   * committed, it's save to preserve the illusion, and when this
   * method is invoked, it does not throw an exception.
   * @see java.sql.Connection#commit
   *
   */
  public void commit() throws SQLException
  {
  }

  /**
   *
   * Rolls back a transaction. tinySQL does not support transactions,
   * so this throws an exception.
   * @see java.sql.Connection#rollback
   * @exception SQLException gets thrown automatically
   *
   */
  public void rollback() throws SQLException
  {
    throw new SQLException("tinySQL does not support rollbacks.");
  }

  /**
   *
   * Close a Connection object. Does nothing, really.
   * @see java.sql.Connection#close
   * @exception SQLException is never thrown
   *
   */
  public void close() throws SQLException
  {
    tsql.close();
    tsql = null;
  }

  /**
   *
   * Returns the status of the Connection.
   * @see java.sql.Connection#isClosed
   * @exception SQLException is never thrown
   * @return true if the connection is closed, false otherwise
   *
   */
  public boolean isClosed() throws SQLException
  {
    return (tsql == null);
  }

  public tinySQL getTinySqlHandle()
  {
    return tsql;
  }

  /**
   *
   * This method would like to retrieve some DatabaseMetaData, but it
   * is presently only supported for dBase access
   * @see java.sql.Connection#getMetData
   * @exception SQLException is never thrown
   * @return a DatabaseMetaData object - someday
   *
   */
  public abstract DatabaseMetaData getMetaData() throws SQLException;

  /**
   * Puts the database in read-only mode... not! This throws an
   * exception whenever it is called. tinySQL does not support
   * a read-only mode, and it might be dangerous to let a program
   * think it's in that mode.
   * @see java.sql.Connection#setReadOnly
   * @param b meaningless
   */
  public void setReadOnly(boolean b) throws SQLException
  {
    throw new SQLException("tinySQL does not have a read-only mode by default.");
  }

  /**
   *
   * Returns true if the database is in read-only mode. It always
   * returns false.
   * @see java.sql.Connection#isReadOnly
   * @return the false will be with you... always
   *
   */
  public boolean isReadOnly() throws SQLException
  {
    return false;
  }

  /**
   *
   * Sets the current catalog within the database. This is not
   * supported by tinySQL, but we'll set the catalog String anyway.
   * @see java.sql.Connection#setCatalog
   * @param str the catalog
   *
   */
  public void setCatalog(String str) throws SQLException
  {
    catalog = str;
  }

  /**
   *
   * Returns the current catalog. This has no significance in tinySQL
   * @see java.sql.Connection#getCatalog
   * @return the catalog name
   *
   */
  public String getCatalog() throws SQLException
  {
    return catalog;
  }

  /**
   *
   * Sets the transaction isolation level, which has no meaning in tinySQL.
   * We'll set the isolation level value anyhow, just to keep it happy.
   * @see java.sql.Connection#setTransactionIsolation
   * @param x the isolation level
   *
   */
  public void setTransactionIsolation(int x)
      throws SQLException
  {
    isolation = x;
  }

  /**
   *
   * Returns the isolation level. This is not significant for tinySQL
   * @see java.sql.Connection#getTransactionIsolation
   * @return the transaction isolation level
   *
   */
  public int getTransactionIsolation() throws SQLException
  {
    return isolation;
  }

  /**
   *
   * Disables autoclosing of connections and result sets. This is
   * not supported by tinySQL.
   * @see java.sql.Connection#disableAutoClose
   *
   */
  public void disableAutoClose() throws SQLException
  {
    setAutoClose(false);
  }

  /**
   *
   * Returns a chain of warnings for the current connection; this
   * is not supported by tinySQL.
   * @see java.sql.Connection#getWarnings
   * @return the chain of warnings for this connection
   *
   */
  public SQLWarning getWarnings() throws SQLException
  {
    return null;
  }

  /**
   *
   * Clears the non-existant warning chain.
   * @see java.sql.Connection#clearWarnings
   *
   */
  public void clearWarnings() throws SQLException
  {
  }

  /**
   *
   * Execute a tinySQL Statement
   * @param sql the statement to be executed
   * @return tsResultSet containing the results of the SQL statement
   *
   */
  public boolean executeStatement(SQLStatement statement)
      throws SQLException
  {

    // the parsed SQLStatement object
    //
    boolean result;

    /**
     * The right place to start commit/rollback code :)
     */

    // try to execute the SQL
    //
    result = statement.execute();

    return result;

  }

  public boolean getAutoCommit()
  {
    return true;
  }

  public void setAutoClose(boolean l)
  {
  }

  public boolean getAutoClose()
  {
    return false;
  }


  /**
   *
   * creates a new tinySQL object and returns it. Well, not really,
   * since tinySQL is an abstract class. When you subclass tinySQLConnection,
   * you will need to include this method, and return some subclass
   * of tinySQL.
   *
   */
  public abstract tinySQL createDatabaseEngine() throws tinySQLException;

  public tinySQL getDatabaseEngine()
  {
    return tsql;
  }

  //--------------------------JDBC 2.0-----------------------------

  /**
   * JDBC 2.0
   *
   * Creates a <code>Statement</code> object that will generate
   * <code>ResultSet</code> objects with the given type and concurrency.
   * This method is the same as the <code>createStatement</code> method
   * above, but it allows the default result set
   * type and result set concurrency type to be overridden.
   *
   * @param resultSetType a result set type; see ResultSet.TYPE_XXX
   * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
   * @return a new Statement object
   * @exception SQLException if a database access error occurs
   */
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support createStatement with concurrency.");
  }

  /**
   * JDBC 2.0
   *
   * Creates a <code>PreparedStatement</code> object that will generate
   * <code>ResultSet</code> objects with the given type and concurrency.
   * This method is the same as the <code>prepareStatement</code> method
   * above, but it allows the default result set
   * type and result set concurrency type to be overridden.
   *
   * @param resultSetType a result set type; see ResultSet.TYPE_XXX
   * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
   * @return a new PreparedStatement object containing the
   * pre-compiled SQL statement
   * @exception SQLException if a database access error occurs
   */
  public PreparedStatement prepareStatement(String sql, int resultSetType,
                                            int resultSetConcurrency)
      throws SQLException
  {
    throw new SQLException("tinySQL does not support preparedStatement with concurrency.");
  }

  /**
   * JDBC 2.0
   *
   * Creates a <code>CallableStatement</code> object that will generate
   * <code>ResultSet</code> objects with the given type and concurrency.
   * This method is the same as the <code>prepareCall</code> method
   * above, but it allows the default result set
   * type and result set concurrency type to be overridden.
   *
   * @param resultSetType a result set type; see ResultSet.TYPE_XXX
   * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
   * @return a new CallableStatement object containing the
   * pre-compiled SQL statement
   * @exception SQLException if a database access error occurs
   */
  public CallableStatement prepareCall(String sql, int resultSetType,
                                       int resultSetConcurrency) throws SQLException
  {
    throw new SQLException("tinySQL does not support prepareCall with concurrency.");
  }

  /**
   * JDBC 2.0
   *
   * Gets the type map object associated with this connection.
   * Unless the application has added an entry to the type map,
   * the map returned will be empty.
   *
   * @return the <code>java.util.Map</code> object associated
   *         with this <code>Connection</code> object
   */
  public java.util.Map getTypeMap() throws SQLException
  {
    throw new SQLException("tinySQL does not support getTypeMap.");
  }

  /**
   * JDBC 2.0
   *
   * Installs the given type map as the type map for
   * this connection.  The type map will be used for the
   * custom mapping of SQL structured types and distinct types.
   *
   * @param the <code>java.util.Map</code> object to install
   *        as the replacement for this <code>Connection</code>
   *        object's default type map
   */
  public void setTypeMap(java.util.Map map) throws SQLException
  {
    throw new SQLException("tinySQL does not support setTypeMap.");
  }

  /**
   * returns the properties for this connection.
   */
  public Properties getProperties()
  {
    return (properties);
  }
}
