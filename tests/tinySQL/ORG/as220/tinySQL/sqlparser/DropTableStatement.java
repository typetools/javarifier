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
import ORG.as220.tinySQL.tinySQLTableView;

import java.sql.SQLException;
import java.util.Vector;

/**
 * Removes a table from the database.
 */
public class DropTableStatement implements SQLStatement
{
  private tinySQL db;
  private tinySQLTableView table;

  public DropTableStatement(tinySQLStatement statement)
      throws tinySQLException
  {
    try
    {
      tinySQLConnection c = (tinySQLConnection) statement.getConnection();
      db = c.getTinySqlHandle();
    }
    catch (SQLException sqle)
    {
      throw new tinySQLException("Unable to resolve connection", sqle);
    }

  }

  /**
   * sets the table for this statement. After the table is set, you may
   * add columns to the statement.
   */
  public void setTable(String tablename, boolean ifExists)
      throws tinySQLException
  {
    /**
     * Implicitly check, whether the table exists.
     */
    try
    {
      table = getDatabase().getTable(tablename);
    }
    catch (tinySQLException sqle)
    {
      if (!ifExists)
        throw new tinySQLException("Table " + tablename + "  does not exist or is not valid.");
    }
  }

  /**
   * returns the table, the statement will be working in.
   */
  public tinySQLTableView getTable()
  {
    return table;
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
    if (table != null)
    {
      getDatabase().DropTable(this);
    }
    return false;
  }

  /**
   * execute this statement. This is called from statement.execute
   * or SQLStatementBatch.executeAll ()
   *
   * executes the command and returns false, as no resultset will
   * be created.
   */
  public int getUpdateCount() throws tinySQLException
  {
    return -1;
  }

  /**
   * returns null as DROP TABLE does not return a result set.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as DROP TABLE does not return multiple results.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * returns the parameters used in this statement.
   *
   * DROP TABLE does not support parameters, so return an empty vector.
   */
  public Vector getParameters()
  {
    return new Vector();
  }

}
