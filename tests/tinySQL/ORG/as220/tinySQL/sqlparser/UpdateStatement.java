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
import ORG.as220.tinySQL.tsResultRow;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Update rows which match a WHERE clause
 *
 * UPDATE table SET col1 = expression, col2 = expression2, ...
 */
public class UpdateStatement implements ConditionalStatement
{
  private tinySQL db;
  private WhereClause whereC;
  private tinySQLTableView table;
  private Vector columns;
  private tsResultRow values;
  private int updateCount;

  /**
   * Creates the updateStatement and initializes its to execute on statement
   * <code>statement</code>.
   */
  public UpdateStatement(tinySQLStatement statement)
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
    columns = new Vector();
  }

  /**
   * Adds a column to the insert statement. The column given must exist in
   * the table, and has to be a valid identifier for the underlying database.
   * Calling this method is only valid after addTable has been called.
   */
  public void addColumn(String colname)
      throws tinySQLException
  {
    ColumnValue col = new ColumnValue(colname);

    String tablename = col.getTable();
    if (tablename != null)
    {
      if (table.getName().equals(tablename) == false)
      {
        throw new tinySQLException("The specified table [" + tablename + "] is not defined in this statement");
      }
    }
    String name = col.getColumn();
    tsColumn tablecol = table.getColumnDefinition(table.findColumn(name));
    columns.add(tablecol);
  }

  /**
   * returns the defined columns as vector. This includes also all columns
   * from the whereClause
   */
  public Vector getColumns()
  {
    Vector v = new Vector(columns);
    v.addAll(whereC.getColumns());
    return v;
  }

  /**
   * sets the Values for this UpdateStatement. This function expects a vector
   * of LValues. The size of the vector must match the number of the columns
   * defined.
   */
  public void setValues(Vector v) throws tinySQLException
  {
    if (v.size() != columns.size())
      throw new tinySQLException("Columns-Size does not match values-size");

    Vector sourceCols = new Vector();
    for (int i = 0; i < columns.size(); i++)
    {
      tsColumn target_col = (tsColumn) columns.get(i);
      tsColumn source_col = new tsColumn(target_col.getPhysicalName(), (LValue) v.get(i));
      source_col.setAll(target_col);
      sourceCols.add(source_col);
    }

    values = new tsResultRow(sourceCols);
  }

  /**
   * returns the values row. This is a resultrow used to evaluate the
   * expression in context to the current table position.
   */
  public tsResultRow getValues()
  {
    return values;
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

  public void setWhereClause(WhereClause c)
      throws tinySQLException
  {
    if (c == null)
      throw new NullPointerException();

    whereC = c;
  }

  public WhereClause getWhereClause()
  {
    return whereC;
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
   * Executes the statement. If no table is set, the statement
   * is considered an empty UPDATE and no action is done, and
   * no exception is thrown.
   */
  public boolean execute() throws tinySQLException
  {
    Vector v = getParameters();
    for (int i = 0; i < v.size(); i++)
    {
      ParameterValue pv = (ParameterValue) v.get(i);
      if (pv.isEmpty())
        throw new tinySQLException("Parameter " + i + " is not yet set");
    }
    if (table != null)
    {
      updateCount = getDatabase().UpdateStatement(this);
    }
    return false;
  }

  /**
   * returns the number of rows affected by the execution of the statement.
   */
  public int getUpdateCount() throws tinySQLException
  {
    return updateCount;
  }

  /**
   * returns null, as Update will never return a resultset.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as UPDATE does not return more than one result.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * returns the parameters used in this statement's expressions and
   * in the whereClause.
   */
  public Vector getParameters()
  {
    Vector v = new Vector();
    tsResultRow row = getValues();
    for (int i = 0; i < row.size(); i++)
    {
      tsColumn col = row.getColumnDefinition(i);
      if (col.getColumnType() == tsColumn.COL_EXPR)
      {
        ParserUtils.getParameterElements(v, col.getExpression());
      }
    }
    v.addAll(whereC.getParameters());
    return v;
  }

  /**
   * returns a String representation of this statement.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("UPDATE ");
    b.append(table);
    b.append(" SET ");
    tsResultRow row = (tsResultRow) getValues();

    for (int i = 0; i < row.size(); i++)
    {
      if (i != 0)
      {
        b.append(", ");
      }

      tsColumn col = (tsColumn) row.getColumnDefinition(i);
      b.append(col);
      b.append(" = ");
      try
      {
        b.append(row.get(i));
      }
      catch (Exception e)
      {
        b.append("<!EXCEPTION>");
      }
    }
    b.append("");
    return b.toString();
  }

}
