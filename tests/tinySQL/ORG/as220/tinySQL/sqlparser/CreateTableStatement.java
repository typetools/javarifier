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
 * This is the baseclass for all SQL-Statements
 */
public class CreateTableStatement implements SQLStatement
{
  private tinySQL db;
  private String table;
  private Vector columnDefinitions;

  /**
   * creates a new CreateTableStatement.
   */
  public CreateTableStatement(tinySQLStatement statement)
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

    columnDefinitions = new Vector();
  }

  /**
   * returns the table, the statement will be working in.
   */
  public String getTable()
  {
    return table;
  }

  /**
   * sets the table for this statement. After the table is set, you may
   * add columns to the statement.
   */
  public void setTable(String tablename)
      throws tinySQLException
  {
    table = tablename;
  }

  /**
   * Adds a column definition to the statement. The definition must be valid
   * for the underlying database or execute() will throw an Exception.
   */
  public void addColumnDefinition(ColumnDefinition coldef) throws tinySQLException
  {
    columnDefinitions.add(coldef);
  }

  /**
   * returns all ColumnDefinitons as Vector. Do only modify the column definitions
   * if you are sure about what your are doing.
   */
  public Vector getColumnDefinitions()
  {
    return new Vector(columnDefinitions);
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
    return getDatabase().CreateTable(this);
  }

  /**
   * returns the updatecount for the statement. Will always be null,
   * new tables have no rows.
   */
  public int getUpdateCount() throws tinySQLException
  {
    return 0;
  }

  /**
   * returns null as CREATE TABLE does not return a resultset.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as CREATE TABLE does not return multiple results.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("CREATE TABLE ");
    b.append(getTable());
    b.append("(");
    for (int i = 0; i < columnDefinitions.size(); i++)
    {
      if (i != 0)
        b.append(", ");
      b.append(columnDefinitions.elementAt(i));
    }
    b.append(")");
    return b.toString();
  }

  /**
   * returns the parameters used in this statement.
   *
   * CREATE TABLE does not support parameters, so return an empty vector.
   */
  public Vector getParameters()
  {
    return new Vector();
  }

}
