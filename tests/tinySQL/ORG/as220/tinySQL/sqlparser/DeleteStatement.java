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
import java.util.Enumeration;
import java.util.Vector;

/**
 * Delete is used to remove rows from a table.
 * Deletes rows which match a where clause.
 * DELETE FROM table WHERE ...
 *
 */
public class DeleteStatement implements ConditionalStatement
{
  private tinySQL db;
  private int limit;
  private tinySQLTableView table;
  private WhereClause whereC;
  private int updatecount;

  public DeleteStatement(tinySQLStatement statement)
      throws tinySQLException
  {
    try
    {
      tinySQLConnection c = (tinySQLConnection) statement.getConnection();
      db = c.getTinySqlHandle();
      whereC = new WhereClause(this);
    }
    catch (SQLException sqle)
    {
      throw new tinySQLException("Unable to resolve connection", sqle);
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
   * returns the table, the statement will be working in, as 1-element enumeration.
   * This is the implementation for ConditionalStatement.getTables ()
   */
  public Enumeration getTables()
  {
    Vector v = new Vector();
    v.add(table);
    return v.elements();
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
   * set a whereclause for the statement. If no where clause is set, a
   * default clause of TRUE is applied, which will accept any record as valid.
   */
  public void setWhereClause(WhereClause c)
      throws tinySQLException
  {
    if (c == null)
      throw new NullPointerException();

    whereC = c;
  }

  /**
   * returns the where clause of the statement. A where clause acts as
   * filter to a resultset accepting only records that match the expression.
   */
  public WhereClause getWhereClause()
  {
    return whereC;
  }

  /**
   * Returns the Limit for this query, or -1 if there is no limit.
   */
  public int getLimit()
  {
    return -1;
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
    Vector v = getParameters();
    for (int i = 0; i < v.size(); i++)
    {
      ParameterValue pv = (ParameterValue) v.elementAt(i);
      if (pv.isEmpty())
        throw new tinySQLException("Parameter " + i + " is not yet set");
    }

    if (table != null)
    {
      updatecount = getDatabase().DeleteStatement(this);
    }
    System.out.println("On DELETE: UpdateCount was " + updatecount);
    return false;
  }

  /**
   * returns the updatecount for the statement. This should be the
   * equal to the number of rows in the modified table.
   */
  public int getUpdateCount() throws tinySQLException
  {
    return updatecount;
  }

  /**
   * returns null, as Delete will never return a resultset.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false, as Delete will never return multiple results.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * returns a string representation of this statement.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("DELETE FROM ");
    b.append(table);
    b.append(" ");
    b.append(whereC);
    return b.toString();
  }

  /**
   * returns the parameters used in this statement.
   *
   * returns the parameters of the whereClause if any.
   */
  public Vector getParameters()
  {
    return whereC.getParameters();
  }

}
