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
import java.util.Enumeration;
import java.util.Vector;

/**
 * The SELECT statement is used to form queries for extracting information
 * out of the database. The select statements results can be limited using
 * a where clause.
 */
public class SelectStatement implements ConditionalStatement
{
  public static final int MOD_NONE = 0;
  public static final int MOD_ALL = 1;
  public static final int MOD_DISTINCT = 2;

  private WhereClause whereC;
  private OrderByClause orderC;

  /**
   * All Columns as they will show up in tsResultSet
   */
  private Vector resultColumns;

  /**
   * All ColumnValue-Elements. They define, which TableRows
   * to query. Every ColumnElement is only filled once.
   */
  private Vector tableColumns;

  private Vector tables;

  private tinySQLStatement statement;
  private tinySQLResultSet myResult;
  private int mod;

  /**
   * creates a new selectstatement.
   */
  public SelectStatement(tinySQLStatement statement)
  {
    mod = MOD_NONE;
    this.statement = statement;
    resultColumns = new Vector();
    tableColumns = new Vector();
    tables = new Vector();
    whereC = new WhereClause(this);
    orderC = new OrderByClause(this);
  }

  /**
   * add a table to this statement. After all tables are set, you may
   * add columns to the statement.
   */
  public void addTable(String tablename, String alias)
      throws tinySQLException
  {
    /**
     * Implicitly check, whether the table exists.
     */
    tinySQLTableView dbtable = getDatabase().getTable(tablename);
    dbtable.setAlias(alias);
    tables.add(dbtable);
  }

  public void addColumn(LValue column, String alias)
      throws tinySQLException
  {
    if (column instanceof JokerColumnValue)
    {
      // Take Care of Jokers. A Joker resolves to many many columns.
      resolveJokers((JokerColumnValue) column);
      return;
    }

    tinySQL db = getDatabase();
    tableColumns.addAll(ParserUtils.resolveTableColumns(column, tables));

    // Now add this column to the columns.
    //
    // columnvalues are directly read from physical tables, so handle them
    // specially
    if (column instanceof ColumnValue)
    {
      ColumnValue colVal = (ColumnValue) column;

      // Seek the table for this column
      // Throws an exception, if not table was found
      tinySQLTableView table = null;
      String tablename = colVal.getTable();
      if (tablename == null)
      {
        table = ParserUtils.findTableForColumn(tables.elements(), colVal.getColumn());
      }
      else
      {
        table = ParserUtils.findTable(colVal.getTable(), tables);
      }

      /**
       * Create the results Columndefinition
       */
      tsColumn columnDefinition = new tsColumn(table, colVal.getColumn(), alias);

      // Copy tables column definition into the results columndefinition
      columnDefinition.setAll(table.getColumnDefinition(table.findColumn(colVal.getColumn())));

      resultColumns.add(columnDefinition);
    }
    else
    {
      tsColumn columnDefinition = new tsColumn(alias, column);
      resultColumns.add(columnDefinition);
    }
  }

  /**
   * resolves a JokerColumnValue into real table columns.
   */
  private void resolveJokers(JokerColumnValue column)
      throws tinySQLException
  {

    String tablename = column.getTable();
    if (tablename == null)
    {
      Enumeration tables = getTables();
      while (tables.hasMoreElements())
      {
        tinySQLTableView table = (tinySQLTableView) tables.nextElement();
        addAllTableColumns(table);
      }
    }
    else
    {
      tinySQLTableView table = (tinySQLTableView) ParserUtils.findTable(tablename, tables);
      if (table == null)
      {
        throw new tinySQLException("There is no such table: " + tablename);
      }
      addAllTableColumns(table);
    }

  }

  /**
   * Adds all columns of the table to the resultset.
   */
  private void addAllTableColumns(tinySQLTableView table)
  {
    int size = table.getColumnCount();
    for (int i = 0; i < size; i++)
    {
      tsColumn coldef = (tsColumn) table.getColumnDefinition(i);
      tableColumns.add(coldef);
      resultColumns.add(coldef);
    }
  }

  /**
   *  returns the modifier for the resultset. Wil return MOD_NONE, MOD_ALL or
   * MOD_DISTINCT. tinySQL's Select-Method does not yet support modifications
   * and ignores them.
   */
  public int getModifier()
  {
    return mod;
  }

  /**
   * Sets the modifier. mod has to be one of MOD_ALL, MOD_NONE, MOD_DISTINCT,
   * or a illegalArgumentException is thrown.
   */
  public void setModifier(int mod)
  {
    if (mod == MOD_NONE || mod == MOD_ALL || mod == MOD_DISTINCT)
      this.mod = mod;
    else
      throw new IllegalArgumentException("Not a valid modification");
  }

  /**
   * returns all tables, the statement will be working with, as enumeration.
   * This is the implementation for ConditionalStatement.getTables ()
   */
  public Enumeration getTables()
  {
    return tables.elements();
  }

  /**
   * Vector of tsColumn-Objects. These columns may not contain expressions
   * or aliased table-columns. The columns in this Vector must exist.
   */
  public Vector getTableColumns()
  {
    Vector v = new Vector(tableColumns);
    v.addAll(whereC.getColumns());
    v.addAll(orderC.getColumns());
    return v;
  }

  /**
   * Vector of tsColumn-Objects. These columns may contain expressions
   * and aliased table-columns.
   */
  public Vector getResultColumns()
  {
    return new Vector(resultColumns);
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
   * set a whereclause for the statement. If no where clause is set, a
   * default clause of TRUE is applied, which will accept any record as valid.
   */
  public void setOrderByClause(OrderByClause c)
      throws tinySQLException
  {
    if (c == null)
      throw new NullPointerException();

    orderC = c;
  }

  /**
   * returns the where clause of the statement. A where clause acts as
   * filter to a resultset accepting only records that match the expression.
   */
  public OrderByClause getOrderByClause()
  {
    return orderC;
  }

  /**
   * returns the instance of the database that will be called to
   * execute the statement
   */
  public tinySQL getDatabase()
      throws tinySQLException
  {
    try
    {
      tinySQLConnection c = (tinySQLConnection) statement.getConnection();
      return c.getTinySqlHandle();
    }
    catch (SQLException sqle)
    {
      throw new tinySQLException("Unable to resolve connection", sqle);
    }
  }

  /**
   * returns the assigned statement, on which to execute the query
   */
  public tinySQLStatement getStatement()
  {
    return statement;
  }

  /**
   * execute this statement. This is called from statement.execute
   * or SQLStatementBatch.executeAll ()
   *
   * executes the command and returns false, as no resultset will
   * be created.
   */
  public boolean execute()
      throws tinySQLException
  {
    tinySQL database = getDatabase();
    Vector v = getParameters();
    for (int i = 0; i < v.size(); i++)
    {
      ParameterValue pv = (ParameterValue) v.get(i);
      if (pv.isEmpty())
        throw new tinySQLException("Parameter " + i + " is not yet set");
    }

    myResult = new tinySQLResultSet(database.SelectStatement(this), getStatement());
    return true;
  }

  /**
   * returns the updatecount for the statement. This should be the
   * equal to the number of rows in the modified table.
   */
  public int getUpdateCount()
  {
    return -1;
  }

  /**
   * returns the result of the query. If no rows were selected, an
   * empty resultset is returned.
   */
  public tinySQLResultSet getResultSet()
  {
    tinySQLResultSet retval = myResult;
    myResult = null;
    return retval;
  }

  /**
   * returns false as SELECT does not return more than one result.
   */
  public boolean getMoreResults()
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
    Enumeration valenum = getResultColumns().elements();
    while (valenum.hasMoreElements())
    {
      tsColumn col = (tsColumn) valenum.nextElement();
      if (col.getColumnType() == tsColumn.COL_EXPR)
      {
        LValue colVal = (LValue) col.getExpression();
        ParserUtils.getParameterElements(v, colVal);
      }
    }

    v.addAll(whereC.getParameters());
    return v;
  }

  /**
   * t.b.im
   */
  //public Group getGroupBy ()
  //{
  //}

  /**
   * t.b.im
   */
  //public Having getHaving ()
  //{
  //}


  /**
   * returns a string representation of this select statement.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("SELECT ");
    if (getModifier() == MOD_ALL)
      b.append("ALL ");
    else if (getModifier() == MOD_DISTINCT)
      b.append("DISTINCT ");

    b.append("<!-- TABLECOLUMNS: USED TABLES FOR ALL EXPRESSIONS IN THIS QUERY \n");
    Enumeration enum = getTableColumns().elements();
    while (enum.hasMoreElements())
    {
      b.append(enum.nextElement());
      if (enum.hasMoreElements())
      {
        b.append(", \n");
      }
    }
    b.append("\n --> \n");

    b.append("<!-- RESULTCOLUMNS: COLUMNS WHICH WILL BE DISPLAYED \n");
    enum = getResultColumns().elements();
    while (enum.hasMoreElements())
    {
      b.append(enum.nextElement());
      if (enum.hasMoreElements())
      {
        b.append(", \n");
      }
    }
    b.append("\n --> \n");

    enum = getTables();
    if (enum.hasMoreElements())
      b.append(" FROM ");

    while (enum.hasMoreElements())
    {
      b.append(enum.nextElement());
      if (enum.hasMoreElements())
      {
        b.append(", ");
      }
    }

    b.append(" " + whereC.toString());
    return b.toString();
  }
}
