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

import ORG.as220.tinySQL.tinySQL;
import ORG.as220.tinySQL.tinySQLException;
import ORG.as220.tinySQL.tinySQLResultSet;

import java.util.Vector;

/**
 * This is the baseclass for all SQL-Statements.
 *
 * The specific Statement is initialized by the parse-tree-walker
 * and will start execution on when execute() is called.
 *
 */
public interface SQLStatement
{
  /**
   * returns the tinySQL Object that will execute this query.
   */
  public tinySQL getDatabase() throws tinySQLException;

  /**
   * Execute the statement.
   *
   * copied from java.sql.Statement:
   * Executes an SQL statement that may return multiple results.
   * Under some (uncommon) situations a single SQL statement may return
   * multiple result sets and/or update counts. Normally you can ignore
   * this unless you are (1) executing a stored procedure that you know
   * may return multiple results or (2) you are dynamically executing an
   * unknown SQL string. The methods execute, getMoreResults, getResultSet,
   * and getUpdateCount let you navigate through multiple results. The
   * execute method executes an SQL statement and indicates the form of
   * the first result. You can then use the methods getResultSet or
   * getUpdateCount to retrieve the result, and getMoreResults to move
   * to any subsequent result(s).
   *
   */
  public boolean execute() throws tinySQLException;

  /**
   * Returns the current result as an update count; if the result is a
   * ResultSet object or there are no more results, -1 is returned. This
   * method should be called only once per result.
   *
   * @returns the current result as an update count; -1 if the current
   * result is a ResultSet object or there are no more results
   */
  public int getUpdateCount() throws tinySQLException;

  /**
   * Returns the current result as a ResultSet object. This method should
   * be called only once per result.
   *
   * @returns the current result as a ResultSet object; null if the result
   * is an update count or there are no more results
   */
  public tinySQLResultSet getResultSet() throws tinySQLException;

  /**
   * Moves to a Statement object's next result. It returns true if this result
   * is a ResultSet object. This method also implicitly closes any current
   * ResultSet object obtained with the method getResultSet.
   *
   * There are no more results when the following is true:
   * <p><code>(!getMoreResults() && (getUpdateCount() == -1)</code></p>
   *
   * @returns true if the next result is a ResultSet object; false if it is
   * an update count or there are no more results
   */
  public boolean getMoreResults() throws tinySQLException;

  /**
   * returns a collection of parameters in this statement.
   *
   * @returns a vector of <code>ParameterValue</code>s.
   */
  public Vector getParameters();
}
