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
 * Inserts one or more records into a table. The InsertStatement supports
 * multiple value rows or a select statement delivering the values for the
 * insert.
 *
 * INSERT INTO table SELECT ...
 * INSERT INTO table SET name = expr, name2 = expr2, ...
 * INSERT INTO table (col1, col2) VALUES (expr1, expr2), (expr1a, expr2a), ...
 */
public class InsertStatement implements SQLStatement
{
  private tinySQL db;
  private tinySQLTableView table;
  private int updatecount;
  private Vector columns; // Vector of tsRows
  private Vector tablesCache;
  private Vector values;
  private SelectStatement subselect;

  /**
   * creates a new InsertStatement with the tinySQLStatement statement
   * on which to finally execute the command.
   */
  public InsertStatement(tinySQLStatement statement)
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
    values = new Vector();
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
   * Adds the Values to this insertStatement. If also a SelectStatement is added,
   * the Select has precedence to the values. This function expects a vector
   * of LValues. The size of the vector must match the number of the columns
   * defined.
   */
  public void addValues(Vector v) throws tinySQLException
  {
    if (v.size() != columns.size())
      throw new tinySQLException("Columns-Size does not match values-size");

    Vector sourceCols = new Vector();
    for (int i = 0; i < columns.size(); i++)
    {
      tsColumn target_col = (tsColumn) columns.elementAt(i);
      tsColumn source_col = new tsColumn(target_col.getPhysicalName(), (LValue) v.elementAt(i));
      source_col.setAll(target_col);
      sourceCols.addElement(source_col);
    }

    tsResultRow expressed = new tsResultRow(sourceCols);
    values.add(expressed);
  }

  /**
   * Adds a select statement as source for the insert. If both select and
   * values are added to the statement, then select has precedence before
   * the values. Anyway, it should not happen and only one of both values
   * should be filled.
   */
  public void addSelect(SelectStatement select) throws tinySQLException
  {
    if (select != null)
    {
      this.subselect = select;
    }
    else
      throw new tinySQLException("Empty selection is not allowed");
  }

  /**
   * returns true, if the values are created by a select statement, false
   * otherwise.
   */
  public boolean hasSubSelect()
  {
    return subselect != null;
  }

  /**
   * returns the select statement used in this insert-command.
   */
  public SelectStatement getSelect()
  {
    return subselect;
  }

  /**
   * returns the defined columns as vector. Do not modify the vector,
   * do not add or remove columns, it will break the statement.
   */
  public Vector getColumns()
  {
    return columns;
  }

  /**
   * returns the values Vector. This Vector is a Vector of Vector of LValues.
   * The size of the inner vector matches the number of columns defined.
   */
  public Vector getValues()
  {
    return values;
  }

  /**
   * returns the table, in which to insert the values.
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
    if (tablesCache == null)
    {
      tablesCache = new Vector(1);
      tablesCache.add(table);
    }
    return tablesCache.elements();
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
   * returns the instance of the database wich will be called to
   * execute the statement
   */
  public tinySQL getDatabase() throws tinySQLException
  {
    return db;
  }

  /**
   * Executes the statement. If no table is set, the statement
   * is considered an empty INSERT and no action is done, and
   * no exception is thrown.
   */
  public boolean execute() throws tinySQLException
  {
    if (table != null)
    {
      updatecount = getDatabase().InsertStatement(this);
    }
    return false;
  }

  /**
   * returns the number of rows affected by the execution of the statement.
   */
  public int getUpdateCount() throws tinySQLException
  {
    return updatecount;
  }

  /**
   * returns null, as Insert will never return a resultset.
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as INSERT does not return more than one result.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * Creates a StringRepresenation of this statement.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("INSERT INTO ");
    b.append(table);
    if (hasSubSelect())
    {
      b.append(" ");
      b.append(subselect.toString());
    }
    else
    {
      b.append(" (");
      Enumeration columns = getColumns().elements();
      while (columns.hasMoreElements())
      {
        b.append(columns.nextElement());
        if (columns.hasMoreElements())
        {
          b.append(", ");
        }
      }
      b.append(") VALUES (");
      Enumeration valenum = getValues().elements();
      while (valenum.hasMoreElements())
      {
        tsResultRow row = (tsResultRow) valenum.nextElement();
        b.append(" (");
        b.append(row);
        b.append(") ");

        if (valenum.hasMoreElements())
        {
          b.append(", \n");
        }
      }
      b.append(") \n");
    }
    return b.toString();
  }

  /**
   * returns the parameters used in this statement's expressions and
   * in the whereClause.
   */
  public Vector getParameters()
  {
    Vector v = new Vector();
    Enumeration valenum = getValues().elements();
    while (valenum.hasMoreElements())
    {
      tsResultRow row = (tsResultRow) valenum.nextElement();
      for (int i = 0; i < row.size(); i++)
      {
        tsColumn col = row.getColumnDefinition(i);
        if (col.getColumnType() == tsColumn.COL_EXPR)
        {
          ParserUtils.getParameterElements(v, col.getExpression());
        }
      }
    }
    return v;
  }

}
