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
import ORG.as220.tinySQL.tinySQLConnection;
import ORG.as220.tinySQL.tinySQLException;
import ORG.as220.tinySQL.tinySQLResultSet;
import ORG.as220.tinySQL.tinySQLStatement;

import java.sql.SQLException;
import java.util.Vector;

/**
 * This is the compiled form of an ALTER CREATE TABLE statement.
 *
 * Alters a given table to match the new table definition. Columns
 * with equal names are preserved. Columns not in the oldtable will
 * be created, and existing columns no longer in the definition will
 * be removed.
 *
 * This is not StandardSQL, but it is usefull and faster than multiple
 * addColumn and dropColumn calls.
 */
public class AlterCreateTableStatement implements SQLStatement
{
  private tinySQL db;
  private CreateTableStatement cstmt;
  private tinySQLStatement statement;
  private int updateCount;

  /**
   * creates a new AlterCreateTableStatement with the tinySQLStatement statement
   * on which to finally execute the command.
   */
  public AlterCreateTableStatement(tinySQLStatement statement)
      throws tinySQLException
  {
    try
    {
      this.statement = statement;
      tinySQLConnection c = (tinySQLConnection) statement.getConnection();
      db = c.getTinySqlHandle();
    }
    catch (SQLException sqle)
    {
      throw new tinySQLException("Unable to resolve connection", sqle);
    }
  }

  /**
   * returns the statement to be used to execute the command.
   */
  public tinySQLStatement getStatement()
  {
    return statement;
  }

  /**
   * set the CreateTableStatement for this command.
   * Called by the parser when assembling the statement.
   */
  public void setCreateTableStatement(CreateTableStatement stmt)
  {
    cstmt = stmt;
  }

  /**
   * returns the assigned CreateTableStatement
   */
  public CreateTableStatement getCreateTableStatement()
  {
    return cstmt;
  }

  /**
   * returns the instance of the database that will be called to
   * execute the statement
   */
  public tinySQL getDatabase() throws tinySQLException
  {
    return db;
  }

  /**
   * execute this statement. This is called from statement.execute
   * or SQLStatementBatch.executeAll ()
   *
   * executes the command and returns false, as no resultset will
   * be created.
   */
  public boolean execute() throws tinySQLException
  {
    return getDatabase().AlterCreateTable(this);
  }

  /**
   * returns the updatecount for the statement. This should be the
   * equal to the number of rows in the modified table.
   */
  public int getUpdateCount() throws tinySQLException
  {
    return updateCount;
  }

  /**
   * sets the updatecount for the statement. This should be the
   * equal to the number of rows in the modified table.
   */
  public void setUpdateCount(int uc)
  {
    updateCount = uc;
  }

  /**
   * returns null as CREATE TABLE does not return a statement.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as CREATE TABLE does not return more than one result.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * returns the parameters used in this statement.
   *
   * ALTER CREATE TABLE does not support parameters, so return an empty vector.
   */
  public Vector getParameters()
  {
    return new Vector();
  }
}
