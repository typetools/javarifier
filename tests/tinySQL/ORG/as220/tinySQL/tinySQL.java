/*
 *
 * tinySQL.java
 *
 * A trivial implementation of SQL in an abstract class.
 * Plug it in to your favorite non-SQL data source, and
 * QUERY AWAY!
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

package ORG.as220.tinySQL;

import ORG.as220.tinySQL.parser.ParseException;
import ORG.as220.tinySQL.parser.TinySQLParser;
import ORG.as220.tinySQL.sqlparser.AlterCreateTableStatement;
import ORG.as220.tinySQL.sqlparser.AlterTableAddColumnStatement;
import ORG.as220.tinySQL.sqlparser.AlterTableDropColumnStatement;
import ORG.as220.tinySQL.sqlparser.AlterTableRenameColumnStatement;
import ORG.as220.tinySQL.sqlparser.ColumnDefinition;
import ORG.as220.tinySQL.sqlparser.ColumnValue;
import ORG.as220.tinySQL.sqlparser.CompactTableStatement;
import ORG.as220.tinySQL.sqlparser.CreateTableStatement;
import ORG.as220.tinySQL.sqlparser.DeleteStatement;
import ORG.as220.tinySQL.sqlparser.DropTableStatement;
import ORG.as220.tinySQL.sqlparser.InsertStatement;
import ORG.as220.tinySQL.sqlparser.SQLStatement;
import ORG.as220.tinySQL.sqlparser.SelectStatement;
import ORG.as220.tinySQL.sqlparser.UpdateStatement;
import ORG.as220.tinySQL.sqlparser.WhereClause;
import ORG.as220.tinySQL.util.Log;

import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * @author Thomas Morger <mgs@sherito.org> Changed tinySQL to reflect changes in
 * tinySQLResultSet - an instance of the connection is passed to a new resultset
 * on construction time.
 * When fetching a resultset, the number of rows to fetch can be set in the statement.
 * The builder will return, when the given number of rows has been reached and will be
 * restarted by tsResultSet when more rows are needed.
 *
 * tinySQL gives access to the different implementations of sql-commands. It also
 * contains access to the parser. The parser will never execute statements, the
 * parse function returns a single SQLStatement, wich is the compiled form
 * of the sql querystring.
 *
 * To execute the statement call <code>SQLStatement.execute()</code>.
 * The concept of an command-object is usefull when creating custom querys,
 * as done in most of the ALTER-commands. You dont have to work with strings,
 * and you don't have to parse the same statement over and over again.
 */
public abstract class tinySQL
{

  // The hashtable contains all open physical (of type tinySQLTable) tables
  // every table is opened once and closed when no view is registered to the
  // table anymore.
  private Hashtable tables;
  private tinySQLConnection connection;

  /**
   *
   * Constructs a new tinySQL object.
   */
  public tinySQL()
  {
    tables = new Hashtable();
  }

  public tinySQLConnection getConnection()
  {
    return connection;
  }

  public void setConnection(tinySQLConnection c)
  {
    this.connection = c;
  }

  /**
   * Parses the Statement and forms an SQLStatement. Separation of parsing and
   * execution will help making PreparedStatement more efficient.
   *
   * Creating a parser is almost as expensive as using synchronisation ...
   * Use some pooling? Or declare everything unsynchronized an leave it as it is?
   *
   * @param s SQL Statement to parse. The string is retrieved by getQueryString ();
   * @exception tinySQLException
   *
   */
  public SQLStatement parse(tinySQLStatement s, String sql) throws tinySQLException
  {
    StringReader reader = new StringReader(sql);
    TinySQLParser parser = new TinySQLParser(reader);
    SQLStatement stmt = null;

    try
    {
      stmt = parser.getStatement(s);
    }
    catch (ParseException pe)
    {
      throw new tinySQLException("Parse error: " + pe);
    }
    catch (Exception pe)
    {
      throw new tinySQLException("Error while generating statement: " + pe, pe);
    }
    return stmt;
  }

  /**
   * Prepares a select statement. Please note that no query is done.
   * The statements physical columns are ordered by table and the
   * physical row is created. A tsResultSet Object is created and
   * filled with the prepared values from the statement.
   *
   * The query is initiated using tsResult.getMoreResults() and executed
   * by continueQuery ().
   *
   * SELECT expr1 [AS name], expr2, ... FROM table [AS name], table WHERE ...
   *
   * @returns tsResultSet a prepared resultset containing values from the statement
   * @param select the select-statement to be executed.
   * @throws tinySQLException if a database error occured or if select was
   * not correctly prepared.
   */
  public tsResultSet SelectStatement(SelectStatement select)
      throws tinySQLException
  {
    /**
     * All Tables needed for the Query
     */
    Enumeration tableEnum = select.getTables();
    Vector tables = new Vector();
    while (tableEnum.hasMoreElements())
    {
      tinySQLTableView table = (tinySQLTableView) tableEnum.nextElement();
      tables.add(table);
//      Log.debug ("Added Table: " + table);
    }

    /**
     * All Columns to be queried.
     */
    Vector tableColumnVector = select.getTableColumns();

    /**
     * WhereClause limits resulting Rows
     */
    WhereClause whereC = select.getWhereClause();
    tinySQLStatement stmt = select.getStatement();

    /**
     * Fill ResultColumns
     *
     * Order by tables: every table is represented by an Vector wich contains
     * the needed table-columns.
     */
    Hashtable tableColumns = new Hashtable();

    int size = tableColumnVector.size();
    for (int i = 0; i < size; i++)
    {
      tsColumn column = (tsColumn) tableColumnVector.elementAt(i);
//      Log.debug ("Processing Column: " + column);
      tinySQLTableView table = column.getTable();

      Vector v = (Vector) tableColumns.get(table);
      if (v == null)
      {
        v = new Vector();
      }
      v.add(column);
      tableColumns.put(table, v);
    }

    // instantiate a new tsResultSet
    //
    tsWeakPhysicalRow row = new tsWeakPhysicalRow(tableColumnVector);
    tsResultSet jrs = new tsResultSet(row, select.getResultColumns(), tableColumns, whereC, this);
    try
    {
      jrs.setFetchSize(stmt.getFetchSize());
      jrs.setType(stmt.getResultSetType());
    }
    catch (SQLException sqle)
    {
      Log.warn("Caught SQLException while setting Fetchsize and ResultSetType");
      Log.warn("   This event not expected to occur and will be ignored.");
      sqle.printStackTrace();
    }
    return jrs;
  }


  /**
   * Support function for restartable queries. Continue to
   * read the query result. The current state is taken from
   * tsResultSet. Proceed until maxFetchSize or eof has reached.
   */
  public int continueQuery(tsResultSet res)
      throws tinySQLException
  {
    // the table scan here is an iterative tree expansion, similar to
    // the algorithm shown in the outline example in Chapter 5.
    //

    // A Vector containing all tsTable Objects needed.
    //
    Vector tables = res.getTables();
    if (tables.size() == 0)
    {
      Log.info("No Tables in select, signaling end-of-file");
      return -1;
    }
    // A Hashtable containing all column Objects needed to complete
    // a row.
    //
    Hashtable tableColumns = res.getColumns();

    // which table level are we on?
    // Initialy this is set to 1, the deeper we search, the higher
    // the count. The Level is used as index to the current table
    // in Vector tables.
    int level = res.getLevel();

    // create a row object; this is added to the
    // result set. The resultSet is a clone of the Prototype
    // created in SelectStatement()
    //
    tsPhysicalRow record = res.createPhysicalRow();

    // Initialize the state to the first table of the query
    //
    tinySQLTableView currentTable = null;
    Stack state = new Stack();
    for (int i = 0; i <= level; i++)
    {
      currentTable = (tinySQLTableView) tables.elementAt(i);

      Vector columns = (Vector) tableColumns.get(currentTable);
      if (currentTable.isBeforeFirst() == false)
      {
        addColumnsToRow(columns, record, currentTable);
      }

      state.push(currentTable);
    }

    // keep retrieving rows until we run out of rows to
    // process.
    //
    // Level should be direct proportional to the stacksize
    //
    while (level > -1)
    {
      boolean levelFound = false;

      /**
       * Retrieve the next Table
       */
      currentTable = (tinySQLTableView) state.peek();


      // skip to the next undeleted record; at some point,
      // this will run out of records, and found will be
      // false.
      //
      boolean found = false;
      boolean eof = false;        // did we hit eof?
      boolean haveRecord = false; // did we get a record or not?

      if (currentTable.nextNonDeleted())
      {
        // add each column for this table to
        // the record; record is a tsRow object that
        // is used to hold the values of the current
        // row. It represents every row in every table,
        // and is not added to the result set Vector
        // until we have read a row in the last table
        // in the table list.
        //

        Vector columns = (Vector) tableColumns.get(currentTable);
        addColumnsToRow(columns, record, currentTable);
        // since we were just able to get a row, then
        // we are not at the end of file
        //
        eof = false;

        // If the table we are processing is not the last in
        // the list, then we should increment level and loop
        // to the top.
        //
        if ((level + 1) < tables.size())
        {

          // increment level
          //
          level++;

          // add the next table in the list of tables to
          // the tbl_list, the Hashtable of "to be processed"
          // tables.
          //
          state.push(tables.elementAt(level));

        }
        else
        {

          // if the table that was just processed is the last in
          // the list, then we have drilled down to the bottom;
          // all columns have values, and we can add it to the
          // result set. The next time through, the program
          // will try to read another row at this level; if it's
          // found, only columns for the table being read will
          // be overwritten in the tsRow.
          //
          // Columns for the other table will be left alone, and
          // another row will be added to the result set. Here
          // is the essence of the Cartesian Product which is
          // being built here.
          //
          haveRecord = true;
        }
      }
      else
      {
        // we didn't find any more records at this level.
        // Reset the record pointer to the top of the table,
        // and decrement level. We have hit end of file here.
        //
        level--;
        eof = true;
        currentTable.beforeFirst();
        state.pop();
      }

      // if we got a record, then add it to the result set.
      //
      if (haveRecord)
      {
        // Patch submitted by Todd McNeill <toddm@tngi.com>
        // If the record evaluates correctly, add it to the final result set.
        //
        if (res.getWhereClause().isMatch(record))
        {

          boolean addOK = res.addPhysicalRow(new tsPhysicalRow(record));
          if (addOK == false)
          {
            return level;
          }
        }
      }
    }
    return level;
  }


  /**
   * add all columns of vector <code>columns</code> to the physical row <code>record</code>.
   * The values for the record are contained in the current row of tableview <code>jtbl</code>.
   *
   */
  protected void addColumnsToRow(Vector columns, tsPhysicalRow record, tinySQLTableView jtbl)
      throws tinySQLException
  {
    int size = columns.size();
    for (int ii = 0; ii < size; ii++)
    {
      tsColumn col = (tsColumn) columns.elementAt(ii);
      Object o = jtbl.getColumn(col.getTablePosition());

      int incol = col.getResultPosition();
      record.put(incol, o);
    }
  }

  /**
   *
   * Delete rows which match a where clause.
   * DELETE FROM table WHERE ...
   *
   */
  public int DeleteStatement(DeleteStatement stmt)
      throws tinySQLException
  {

    int updateCount = 0;

    /**
     * Get Table and WhereClause from Statement
     */
    tinySQLTableView jtbl = stmt.getTable();
    WhereClause w = stmt.getWhereClause();

    tsPhysicalRow rec = new tsPhysicalRow(stmt.getWhereClause().getColumns());

    // process each row in the table
    //
    jtbl.beforeFirst();
    while (jtbl.nextNonDeleted())
    {
      rec.refresh();

      // invoke isMatch to see if the table matches
      // the WHERE clause(s) - if so, delete it.
      //
      if (w.isMatch(rec))
      {
        jtbl.deleteRow();
        updateCount++;
      }
    }
    jtbl.close();
    return updateCount;
  }

  /**
   *
   * Update rows which match a WHERE clause
   *
   * UPDATE table SET col1 = expression, col2 = expression2, ...
   */
  public int UpdateStatement(UpdateStatement upd)
      throws tinySQLException
  {

    // create the table
    //
    tinySQLTableView jtbl = upd.getTable();
    WhereClause w = upd.getWhereClause();

    int updateCount = 0;
    Vector names = upd.getColumns();
    tsPhysicalRow rec = new tsPhysicalRow(names);

    // this is the resultrow of the updatestatement.
    // It will compute expressions before returning
    // objects. Column data is filled in the parent row
    tsResultRow resrow = upd.getValues();

    // process each row in the table
    //
    jtbl.beforeFirst();
    while (jtbl.nextNonDeleted())
    {
      rec.refresh();

      // invoke isMatch to see if the table matches
      // the WHERE clause(s) - if so, delete it.
      //
      if (w.isMatch(rec))
      {
        resrow.setParent(rec);
//        System.out.println (resrow);
        jtbl.updateRow(resrow);
        updateCount++;
      }
//      else
//      {
//        System.out.println ("No Match");
//
//      }
    }
    jtbl.close();
    return updateCount;
  }

  /**
   *
   * Issue an insert statement
   * Accepts subselects.
   *
   * INSERT INTO table SELECT ...
   * INSERT INTO table SET name = expr, name2 = expr2, ...
   * INSERT INTO table (col1, col2) VALUES (expr1, expr2), (expr1a, expr2a), ...
   *
   */
  public int InsertStatement(InsertStatement ins)
      throws tinySQLException
  {

    // create the tinySQLTable object
    //
    int updateCount = 0;
    tinySQLTableView jtbl = ins.getTable();
    try
    {

      if (ins.hasSubSelect())
      {
        Vector cols = ins.getColumns();
        int size = cols.size();

        // Get the sub select and execute it. throw an error if no
        // resultset is returned
        //
        SelectStatement stmt = ins.getSelect();
        if (stmt.execute() == false)
          throw new tinySQLException("Select did not return a statement");

        // Check the size of the resultset. Throw an error if the resultsets
        // columncountis not equal to the insert-column-count
        ResultSet res = stmt.getResultSet();

        tsPhysicalRow insertRow = new tsPhysicalRow(cols);
        String[] name = new String[insertRow.size()];
        for (int i = 0; i < name.length; i++)
        {
          name[i] = insertRow.getColumnDefinition(i).getPhysicalName();
        }

        while (res.next())
        {
          for (int i = 0; i < size; i++)
          {
            insertRow.put(i, res.getObject(name[i]));
          }
          jtbl.insertRow(insertRow);
          updateCount++;
        }
        res.close();
      }
      else
      {
        // insert a row, and update it
        //
        Vector values = ins.getValues();
        for (int i = 0; i < values.size(); i++)
        {
          tsRow row = (tsRow) values.elementAt(i);
          jtbl.insertRow(row);
          updateCount++;
        }
      }
    }
    catch (SQLException e)
    {
      throw new tinySQLException("Insert failed", e);
    }
    finally
    {
      jtbl.close();
    }
    return updateCount;
  }

  /**
   * Create the files for the table. While CreateTable may update some
   * arbitary database structures, this function just creates the specifiy
   * file structures needed for a data table.
   */
  protected abstract void db_createTable(String name, Vector v) throws IOException, tinySQLException;

  /**
   * Create the files for the table. This method copies the metadata of a
   * a table into the new table. If this method is not modified, it calls
   * db_createTable (newname, v);
   */
  protected void db_copyTableMeta(String orgTable, String newname, Vector v) throws IOException, tinySQLException
  {
    db_createTable(newname, v);
  }


  /**
   * Low level function of physicaly removing a table. While drop table
   * may alter associated tables and databases, this function simply
   * removes any files needed for the data table.
   */
  protected abstract void db_removeTable(String name) throws IOException, tinySQLException;

  /**
   * Low level function of physicaly removing a table. While drop table
   * may alter associated tables and databases, this function simply
   * removes any files needed for the data table.
   */
  protected abstract void db_renameTable(String name, String newname) throws IOException, tinySQLException;

  /*
   * Creates a table given a table_name, and a Vector of column
   * definitions.
   *
   * The column definitions are an array of tsColumn
   */
  public abstract boolean CreateTable(CreateTableStatement statement)
      throws tinySQLException;

  /**
   * Creates new Columns in table_name, given a vector of
   * column definition (tsColumn) arrays.<br>
   *
   * ALTER TABLE table [ * ] ADD [ COLUMN ] column type
   *
   * @param table_name the name of the table
   * @param v a Vector containing arrays of column definitions.
   * @see tinySQL#AlterTableAddCol
   */
  public boolean AlterTableAddCol(AlterTableAddColumnStatement stmt)
      throws tinySQLException
  {

    tinySQLTableView table = stmt.getTable();
    String tableName = table.getName();

    // Close all references to the table
    closeTable(tableName);

    // create some pathnames we need later
    String orgTableName = tableName + "_tmp_ATAC_ORG";
    String newTableName = tableName + "_tmp_ATAC";

    // Open the Original file,
    // Add the new columnsdefinitions and form a new empty file
    // Copy data from Original file into new (temporary file)
    // if successfull:
    //   rename "original" to "temp2"
    //   rename "new(temp)" to "original"
    //   remove "temp2"
    try
    {
      db_removeTable(orgTableName);
      db_removeTable(newTableName);

      // Opens the original file, read the header of that file
      // get the column definitions
      tinySQLTable oldFile = openTable(tableName);

      Vector coldef_list = new Vector();
      int size = oldFile.getColumnCount();
      for (int i = 0; i < size; i++)
      {
        coldef_list.addElement(oldFile.getColumnDefinition(i));
      }

      // add the new column definitions to the existing ones ...
      // alter recordlength
      Vector v = stmt.getColumns();
      Vector n_coldef_list = new Vector(coldef_list);
      for (int jj = 0; jj < v.size(); jj++)
      {
        ColumnDefinition coldef = (ColumnDefinition) v.elementAt(jj);
        tsColumn col = coldef.getColumn();
        n_coldef_list.add(col);
      }

      oldFile.close();
      // create the new table ...
      //---------------------------------------------------
      db_copyTableMeta(tableName, newTableName, n_coldef_list);

      // now copy every column from old to the new table.
      // the newly created rows remain empty
      //
      InsertStatement insert = new InsertStatement(stmt.getStatement());
      insert.setTable(newTableName);

      SelectStatement select = new SelectStatement(stmt.getStatement());
      select.addTable(tableName, tableName);

      size = coldef_list.size();
      for (int i = 0; i < size; i++)
      {
        tsColumn column = (tsColumn) coldef_list.elementAt(i);
        String name = column.getFQName();
        select.addColumn(new ColumnValue(name), name);
        insert.addColumn(name);
      }

      insert.addSelect(select);
      boolean b = insert.execute();
      if (b == true)
        throw new tinySQLException("Insert .. select returned a resultSet?!");

      stmt.setUpdateCount(insert.getUpdateCount());

      closeTable(tableName);
      closeTable(newTableName);

      // finally rename the file ...
      // Original to copy
      db_renameTable(tableName, orgTableName);

      // finally rename the file ...
      // new to Original
      db_renameTable(newTableName, tableName);

      db_removeTable(orgTableName);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

    // False signals we have not created a resultset.
    return false;
  }

  /**
   * Alters a given table to match the new table definition. Columns
   * with equal names are preserved. Columns not in the oldtable will
   * be created, and existing columns no longer in the definition will
   * be removed.
   *
   * This is not StandardSQL, but it is usefull and faster than multiple
   * addColumn and dropColumn calls.
   *
   * Beware: This function does not properly copy the old tables metadata
   * into the new table.
   *
   * ALTER CREATE table ...
   */
  public boolean AlterCreateTable(AlterCreateTableStatement stmt)
      throws tinySQLException
  {
    try
    {
      CreateTableStatement cstmt = stmt.getCreateTableStatement();

      String tablename = cstmt.getTable();
      String newTableName = tablename + "_tmp_ACT";
      String orgTableName = tablename + "_tmp_ACT_ORG";

      cstmt.setTable(newTableName);
      cstmt.execute();

      tinySQLTable oldTable = openTable(tablename);
      tinySQLTable newTable = openTable(newTableName);

      // gleiche Columns werden übernommen, neue dazugefügt, überflüssige entfernt
      Vector newnames = new Vector();
      for (int i = 0; i < newTable.getColumnCount(); i++)
      {
        newnames.add(newTable.getColumnDefinition(i).getPhysicalName());
      }

      Vector names = new Vector();
      for (int i = 0; i < oldTable.getColumnCount(); i++)
      {
        String name = oldTable.getColumnDefinition(i).getPhysicalName();
        if (newnames.indexOf(name) != -1)
        {
          names.add(name);
        }
      }

      oldTable.close();
      newTable.close();

      // now copy every column from old to the new table.
      // the newly created rows remain empty
      //
      InsertStatement insert = new InsertStatement(stmt.getStatement());
      insert.setTable(newTableName);

      SelectStatement select = new SelectStatement(stmt.getStatement());
      select.addTable(tablename, tablename);

      int size = names.size();
      for (int i = 0; i < size; i++)
      {
        String name = (String) names.elementAt(i);
        select.addColumn(new ColumnValue(name), name);
        insert.addColumn(name);
      }

      insert.addSelect(select);
      boolean b = insert.execute();
      if (b == true)
        throw new tinySQLException("Insert .. select returned a resultSet?!");

      stmt.setUpdateCount(insert.getUpdateCount());

      closeTable(tablename);
      closeTable(newTableName);

      // finally rename the file ...
      // Original to copy
      db_renameTable(tablename, orgTableName);

      // finally rename the file ...
      // new to Original
      db_renameTable(newTableName, tablename);

      db_removeTable(orgTableName);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

    return false;
  }

  /*
   * Deletes Columns given a table_name, and a Vector of
   * column definition (tsColumn) arrays.<br>
   *
   * ALTER TABLE table DROP [ COLUMN ] column { RESTRICT | CASCADE }
   */
  public boolean AlterTableDropCol(AlterTableDropColumnStatement stmt)
      throws tinySQLException
  {
    tinySQLTableView table = stmt.getTable();
    String tableName = table.getName();

    // Close all references to the table
    closeTable(tableName);

    // create some pathnames we need later
    String orgTableName = tableName + "_tmp_ATAC_ORG";
    String newTableName = tableName + "_tmp_ATAC";

    // Open the Original file,
    // remove the statements columnsdefinitions and form a new empty file
    // Copy data from Original file into new (temporary file)
    // if successfull:
    //   rename "original" to "temp2"
    //   rename "new(temp)" to "original"
    //   remove "temp2"
    try
    {
      db_removeTable(orgTableName);
      db_removeTable(newTableName);

      // Opens the original file, read the header of that file
      // get the column definitions
      tinySQLTable oldFile = openTable(tableName);

      Vector coldef_list = new Vector();
      int size = oldFile.getColumnCount();
      for (int i = 0; i < size; i++)
      {
        coldef_list.addElement(oldFile.getColumnDefinition(i));
      }

      // add the new column definitions to the existing ones ...
      // alter recordlength
      Vector v = stmt.getColumns();
      for (int jj = 0; jj < v.size(); jj++)
      {
        tsColumn col = (tsColumn) v.elementAt(jj);
        int ix = findColumn(col, coldef_list);
        if (ix == -1)
        {
          Log.warn("ALTER TABLE " + tableName + ": There is no such column: " + col);
        }
        else
        {
          coldef_list.removeElementAt(ix); // equals() compare of tsColumns
        }
      }

      oldFile.close();

      // create the new table ...
      //---------------------------------------------------
      db_copyTableMeta(tableName, newTableName, coldef_list);

      // now copy every column from old to the new table.
      // the newly created rows remain empty
      //
      InsertStatement insert = new InsertStatement(stmt.getStatement());
      insert.setTable(newTableName);

      SelectStatement select = new SelectStatement(stmt.getStatement());
      select.addTable(tableName, tableName);

      size = coldef_list.size();
      for (int i = 0; i < size; i++)
      {
        tsColumn column = (tsColumn) coldef_list.elementAt(i);
        String name = column.getFQName();
        select.addColumn(new ColumnValue(name), name);
        insert.addColumn(name);
      }

      insert.addSelect(select);
      boolean b = insert.execute();
      if (b == true)
        throw new tinySQLException("Insert .. select returned a resultSet?!");

      stmt.setUpdateCount(insert.getUpdateCount());

      closeTable(tableName);
      closeTable(newTableName);

      // finally rename the file ...
      // Original to copy
      db_renameTable(tableName, orgTableName);

      // finally rename the file ...
      // new to Original
      db_renameTable(newTableName, tableName);

      db_removeTable(orgTableName);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

    // False signals we have not created a resultset.
    return false;

  }

  public int findColumn(tsColumn col, Vector v)
  {
    int size = v.size();
    for (int i = 0; i < size; i++)
    {
      tsColumn vcol = (tsColumn) v.elementAt(i);
      if (col.isValidName(vcol.getPhysicalName()))
      {
        return i;
      }
    }
    return -1;
  }


  /**
   * Rename columns, This implementation is optional.
   *
   * ALTER TABLE table RENAME war TO peace
   */
  public boolean AlterTableRenameCol(AlterTableRenameColumnStatement stmt)
      throws tinySQLException
  {
    throw new tinySQLException("ALTER TABLE RENAME is not supported by this implementation");
  }

  /**
   *
   * Removes a table from the database.
   *
   * @param fname table name
   * @see tinySQL#DropTable
   *
   */
  public void DropTable(DropTableStatement statement) throws tinySQLException
  {

    tinySQLTableView fname = statement.getTable();
    try
    {
      String name = fname.getName();
      closeTable(name);
      db_removeTable(name);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

  }

  /**
   *
   * Return a tinySQLTable object, given a table name. Is called only once,
   * every call to getTable() will return a new tinySQLTableView-Object.
   *
   * @param tableName
   * @see tinySQL#getTable
   *
   */
  public abstract tinySQLTable openTable(String table_name) throws tinySQLException;

  /**
   * Finally close the table and all assigned views. This is needed for
   * Commands that will change the table structure.
   */
  public void closeTable(String table_name)
      throws tinySQLException
  {
    tinySQLTable table = (tinySQLTable) tables.get(table_name);
    if (table != null)
    {
      if (table.close() == true)
        tables.remove(table_name);
      Log.debug("TinySQL: Table " + table_name + " closed.");
    }
  }

  public void close()
  {
    Enumeration enum = tables.keys();
    while (enum.hasMoreElements())
    {
      String name = (String) enum.nextElement();
      try
      {
        closeTable(name);
      }
      catch (tinySQLException e)
      {
        Log.warn("Table " + name + " was not closed cleanly");
      }
    }
  }

  /*
   *
   * Create a tinySQLTableView object by table name. If the underlying table
   * is not yet opened, try to open it using openTable(). Subsequent calls
   * to get the same table, will return new tableViews assigned to the
   * tinySQLTable instance.
   *
   */
  public tinySQLTableView getTable(String table_name) throws tinySQLException
  {
    tinySQLTable table = (tinySQLTable) tables.get(table_name);
    if (table == null)
    {
      table = openTable(table_name);
      tables.put(table_name, table);
      Log.debug("TinySQL: Table " + table_name + " opened.");
    }
    tinySQLTableView view = new tinySQLTableView(table);
    return view;
  }


  /*
   *
   * Compacts a tinySQLTable object by table name. It may not be supported
   * by all drivers, so don't make it abstract.
   *
   */
  public void CompactTable(CompactTableStatement stmt)
      throws tinySQLException
  {
    tinySQLTableView table = stmt.getTable();
    String tableName = table.getName();
    // Close all references to the table
    //

    // create some pathnames we need later
    String orgTableName = tableName + "_tmp_CPT_ORG";
    String newTableName = tableName + "_tmp_CPT";

    // Open the Original file,
    // remove the statements columnsdefinitions and form a new empty file
    // Copy data from Original file into new (temporary file)
    // if successfull:
    //   rename "original" to "temp2"
    //   rename "new(temp)" to "original"
    //   remove "temp2"
    try
    {
      db_removeTable(orgTableName);
      db_removeTable(newTableName);

      // Opens the original file, read the header of that file
      // get the column definitions
      tinySQLTableView oldFile = getTable(tableName);

      Vector coldef_list = new Vector();
      int size = oldFile.getColumnCount();
      for (int i = 0; i < size; i++)
      {
        coldef_list.addElement(oldFile.getColumnDefinition(i));
      }

      // create the new table ...
      //---------------------------------------------------
      db_copyTableMeta(tableName, newTableName, coldef_list);

      // now copy every column from old to the new table.
      // Deleted Rows are ignored.
      InsertStatement insert = new InsertStatement(stmt.getStatement());
      insert.setTable(newTableName);

      SelectStatement select = new SelectStatement(stmt.getStatement());
      select.addTable(tableName, tableName);

      size = coldef_list.size();
      for (int i = 0; i < size; i++)
      {
        tsColumn column = (tsColumn) coldef_list.elementAt(i);
        String name = column.getPhysicalName();
        select.addColumn(new ColumnValue(name), name);
        insert.addColumn(name);
      }

      insert.addSelect(select);
      boolean b = insert.execute();
      if (b == true)
        throw new tinySQLException("Insert .. select returned a resultSet?!");

      closeTable(tableName);
      closeTable(newTableName);

      // finally rename the file ...
      // Original to copy
      db_renameTable(tableName, orgTableName);

      // finally rename the file ...
      // new to Original
      db_renameTable(newTableName, tableName);

      db_removeTable(orgTableName);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

  }
}


