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
import ORG.as220.tinySQL.tsColumn;

import java.sql.SQLException;
import java.util.Vector;

/**
 * Renames one or many columns. No all tinySQL-Implementation may support
 * the execution of this statement.
 *
 * ALTER TABLE table RENAME war TO peace
 */
public class AlterTableRenameColumnStatement implements SQLStatement
{
  private tinySQL db;
  private tinySQLTableView table;
  private Vector columns;

  /**
   * A Inner class to hold a reference between a old column and its new
   * name.
   */
  public class RenameColumnTuple
  {
    public tsColumn oldcol;
    public String newname;
  }

  /**
   * creates a new AlterTableRenameColumn statement.
   */
  public AlterTableRenameColumnStatement(tinySQLStatement statement)
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
   * adds a column to this statement. The columnname in oldname must exist
   * in the specified table. Newname is not checked on create time, but if
   * the column specified in newname exists, a database error may be raised.
   */
  public void addColumn(String oldname, String newname)
  {
    tsColumn col = table.getColumnDefinition(table.findColumn(oldname));
    RenameColumnTuple t = new RenameColumnTuple();
    t.oldcol = col;
    t.newname = newname;
    columns.add(t);
  }

  /**
   * returns all columns as vector. The vector contains RenameColumnTuples,
   * which assignes the old column with a new name.
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
    return getDatabase().AlterTableRenameCol(this);
  }

  /**
   * returns the updatecount for the statement. This should be the
   * equal to the number of rows in the modified table.
   *
   * returns -1, does not know the modified columns
   */
  public int getUpdateCount() throws tinySQLException
  {
    return -1;
  }

  /**
   * returns null, as a AlterTable statement will never return resultsets.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false, as a AlterTable statement will never return multiple results.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * returns the parameters used in this statement.
   *
   * ALTER TABLE RENAME COLUMN  does not support parameters, so return an empty vector.
   */
  public Vector getParameters()
  {
    return new Vector();
  }

}
