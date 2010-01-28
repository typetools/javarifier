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
 * Creates new Columns in table_name, given a vector of
 * column definition (tsColumn) arrays.<br>
 *
 * ALTER TABLE table [ * ] ADD [ COLUMN ] column type
 *
 */
public class AlterTableAddColumnStatement implements SQLStatement
{
  private tinySQL db;
  private tinySQLTableView table;
  private Vector columns;
  private tinySQLStatement statement;
  private int updateCount;

  /**
   * Creates a ALTER TABLE ADD COLUMN statement.
   */
  public AlterTableAddColumnStatement(tinySQLStatement statement)
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
    columns = new Vector();
  }

  /**
   * returns the table, the statement will be working in.
   */
  public tinySQLTableView getTable()
  {
    return table;
  }

  /**
   * Adds a column definition to the statement. The definition must be valid
   * for the underlying database or execute() will throw an Exception.
   */
  public void addColumn(ColumnDefinition coldef) throws tinySQLException
  {
    if (table.findColumn(coldef.getName()) != -1)
      throw new tinySQLException("A column with that name exists in the table");

    columns.add(coldef);
  }

  /**
   * returns all ColumnDefinitons as Vector. Do only modify the column definitions
   * if you are sure about what your are doing.
   */
  public Vector getColumns()
  {
    return new Vector(columns);
  }

  /**
   * sets the table for this statement. After the table is set, you may
   * add columns to the statement.
   */
  public void setTable(String tablename)
      throws tinySQLException
  {
    table = getDatabase().getTable(tablename);
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
    return getDatabase().AlterTableAddCol(this);
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
  public void setUpdateCount(int uc) throws tinySQLException
  {
    updateCount = uc;
  }

  /**
   * returns null as ALTER TABLE does not return a resultset.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as ALTER TABLE does not return multiple results.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * returns the parameters used in this statement.
   *
   * ALTER TABLE ADD COLUMN does not support parameters, so return an empty vector.
   */
  public Vector getParameters()
  {
    return new Vector();
  }

  /**
   * returns the statement to be used to execute the command.
   */
  public tinySQLStatement getStatement()
  {
    return statement;
  }
}
