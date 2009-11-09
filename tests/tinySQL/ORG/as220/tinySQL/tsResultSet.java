/*
 *
 * tsResultSet.java
 * Result Set object for tinySQL.
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

import ORG.as220.tinySQL.sqlparser.WhereClause;
import ORG.as220.tinySQL.util.Log;

import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * tsResultSet - object to hold query results
 *
 * A tsResultRow is used to connect the frontend (tinySQLResultSet) with the
 * backend of the database (tinySQLObject). the tsResultSet is responsible for
 * initiating queries, for caching and creating the needed tsResultRow.
 *
 * The backend does not care about expressions defined in the select columns,
 * it is only responsible for reading tables and returning all needed physical
 * column values for a row. Caching and positionating is in the responsibilty
 * of tsResultSet.
 *
 * Remark: tsResultSet does not yet support SCROLLABLE_RESULTSETs as the state
 * of the used tables is not stored with a single record. Only FORWARD_ONLY
 * resultsets will work without producing weired results.
 */
public class tsResultSet
{
  // Constant used when select has not yet started.
  public static final int ROWS_NOT_KNOWN = -1;

  /**
   * the rowcache...
   */
  private Vector rows; // all the rows - Contains ResultRows, which Contain DataRows

  /** Parameter for cache management */
  private int fetchsize;    // How many rows to fetch at once
  private int windowStart;  // where is the current rowset
  private int level;        // the current level
  private int rowsMax;      // the greatest possible rownumber.

  /** State information */
  private tinySQL dbengine;

  // The type of the resultSet. On of ResultSet.TYPE_FORWARD_ONLY,
  // ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.TYPE_SCROLL_INSENSITIVE
  private int type;

  // has End of file been reached for all tables?
  private boolean eof;

  // has started a select and read at least a eof or some records?
  private boolean hasReadResults;

  // the prototype for the physical row
  private tsPhysicalRow rowPrototype;

  // the prototype for the result row
  private tsResultRow resultRowPrototype;

  // the where clause of the query that produced this result set.
  private WhereClause whereC;

  // the table columns. A hashtable keyed by table, values are vectors
  // of columns for the keyed table.
  private Hashtable tableColumns;

  // the Vector of tinySQLTableViews
  private Vector tables;

  // the current resultrow
  private tsResultRow resultRow;

  /**
   * New Way of doing select: to be implemented
   *
   * Check, which Rows are needed for WhereClause, Read the rows,
   * For every Table collect the valid rows for this resultset.
   *
   * On ResultSet.Next() fill the data rows.
   */
  /**
   * Caching and managing of read rows will be delegated to a stratey-object
   */
  /**
   * creates a new tsResultSet to be used for queries.
   *
   * @param row the physical row containing all table columns needed for the query
   * @param resultColumns a vector of tsColumns that will form a new tsResultRow
   * @param tableColumns tableColumns is a Hashtable ((Keys = table), (Value = Vector of Columns))
   *   created by tinySQL.SelectStatement () to prepare the selection.
   * @param w the where clause used to match valid rows
   * @param dbeng the database backend used to continue the query
   *
   */
  public tsResultSet(tsPhysicalRow row,
                     Vector resultColumns,
                     Hashtable tableColumns,
                     WhereClause w,
                     tinySQL dbeng)
  {
    this.rowPrototype = row;
    this.tableColumns = tableColumns;
    this.rows = new Vector();
    this.dbengine = dbeng;
    this.whereC = w;

    tables = new Vector();
    Enumeration tableEnum = tableColumns.keys();
    while (tableEnum.hasMoreElements())
    {
      tables.add(tableEnum.nextElement());
    }

    // Base for tsRow
    type = ResultSet.TYPE_FORWARD_ONLY;

    hasReadResults = false;
    level = 0;
    rowsMax = ROWS_NOT_KNOWN;

    resultRowPrototype = new tsResultRow(resultColumns, row);
  }

  /**
   * create a tsResultSet that will never be used for Select statements.
   * This functionality is used for the artificial resultsets of DatabaseMetaData.
   */
  public tsResultSet(tsPhysicalRow row)
  {
    level = -1;
    this.rowPrototype = row;
    type = ResultSet.TYPE_SCROLL_INSENSITIVE;
    this.rows = new Vector();
    resultRowPrototype = new tsResultRow(row);

    rowsMax = ROWS_NOT_KNOWN;
  }

  /**
   * Set the type of this resultset. If type is set to ResultSet.TYPE_FORWARD_ONLY
   * the caching of rows is disabled.
   *
   * @param type the resultset type.
   */
  public void setType(int type)
  {
    if ((type == ResultSet.TYPE_FORWARD_ONLY) ||
        (type == ResultSet.TYPE_SCROLL_SENSITIVE) ||
        (type == ResultSet.TYPE_SCROLL_INSENSITIVE))

      this.type = type;
  }

  /**
   * @returns the resultset type
   */
  public int getType()
  {
    return type;
  }

  /**
   * Sets the fetchsize of this resultset. The fetchsize defines how many rows
   * are read at a time when the cache has to be refreshed.
   *
   * @param i the new fetchsize
   */
  public void setFetchSize(int i)
  {
    fetchsize = i;
  }

  /**
   * returns the current fetchsize for this result set.
   *
   * @returns the current fetchsize
   */
  public int getFetchSize()
  {
    return fetchsize;
  }

  /**
   * Reads more results from the database-engine.
   * NewPos must be <b>after</b> the old position as the state of the
   * query is not correctly stored to support SCOLLING resultsets.
   *
   * @param newPos the new row, from which to read
   * @param fetchsize the new fetchsize.
   */
  private boolean getMoreResults(int newPos, int fetchsize) throws tinySQLException
  {
    this.fetchsize = fetchsize;
    if (dbengine != null)
    {
      try
      {
        if (type != ResultSet.TYPE_SCROLL_INSENSITIVE)
        {
          rows.clear();
          windowStart = newPos;
        }

        level = dbengine.continueQuery(this);
        if (level > 0)
        {
          eof = false;
          return eof;
        }
        else
        {
          Log.debug("SELECT returned on level: " + level);
        }
      }
      catch (tinySQLException e)
      {
        throw new tinySQLException("getMoreResults failed", e);
      }
    }
    eof = true;
    rowsMax = windowStart + rows.size();
    Log.debug("ResultSet: Max Rows reached: " + rowsMax);
    return eof;
  }

  /**
   * checks whether the end of file for all tables has been reached and
   * no more results can be retrieved from the database.
   */
  public boolean isEOF()
  {
    return eof;
  }

  /**
   * creates an empty tsPhysicalRow using the copyconstructor for the
   * prototype.
   */
  public tsPhysicalRow createPhysicalRow()
  {
    return new tsPhysicalRow(rowPrototype);
  }

  /**
   * Add a row to the resultset.
   *
   * @param tsRow the new row to add
   * @returns false if the row won't fit into the rowcache, true otherwise.
   */
  public boolean addPhysicalRow(tsPhysicalRow row)
  {
    hasReadResults = true;
    tsResultRow addrsRow = new tsResultRow(resultRowPrototype);
    addrsRow.setParent(row);
    rows.add(addrsRow);

    if (type == ResultSet.TYPE_SCROLL_INSENSITIVE)
    {
      if ((fetchsize > 0) && (rows.size() >= (fetchsize + windowStart)))
      {
        return false;
      }
    }
    else
    {
      if ((fetchsize > 0) && (rows.size() >= fetchsize))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * returns the current resolving-level of the SelectRows-algorithm
   * The resolve level defines on which table in the tables-vector to
   * read next. See tinySQL.continueQUery for details of the Selection
   * algorithm.
   *
   * @return the current level for the database-engine
   */
  public int getLevel()
  {
    return level;
  }

  /**
   * @returns the whereclause of the query
   */
  public WhereClause getWhereClause()
  {
    return whereC;
  }

  /**
   * @returns the number of columns in the result set.
   */
  public int getColumnCount()
  {
    return resultRowPrototype.size();
  }

  /**
   * finds the position of the row with the name <code>name</code>
   * by calling tsResultRow.findColumn.
   *
   * @returns the position of the row or -1 if there no such row.
   */
  public int findColumn(String name) throws tinySQLException
  {
    return resultRowPrototype.findColumn(name);
  }

  /**
   * returns the tables used in the query.
   */
  public Vector getTables()
  {
    return tables;
  }

  /**
   * returns the table-columns hashtable used in the query.
   */
  public Hashtable getColumns()
  {
    return tableColumns;
  }

  /**
   * @returns the number of rows in the result set cache.
   */
  public int getCacheSize()
  {
    return rows.size();
  }

  /**
   * Returns the tsRow at a given row offset (starts with zero).
   *
   * @param i the row offset/index
   */
  public tsResultRow getResultRowAt(int row) throws tinySQLException
  {
    // The row is not in cache ... get it from SQLStatement
    if (row >= (windowStart + rows.size()))
    {
      getMoreResults(row, fetchsize);
    }

    int i = row - windowStart;
    if (i < rows.size())
    {
      return (tsResultRow) rows.elementAt(i);
    }
    return null;
  }

  /**
   * Returns the tsColumn at a given column offset (starts with zero).
   *
   * @param i the column offset/index
   */
  public tsColumn getColumnDefinition(int i) throws tinySQLException
  {
    return rowPrototype.getColumnDefinition(i);
  }

  /**
   * closes all tables of this result-set.
   */
  public void close()
  {
    if (tables == null)
      return;

    Log.debug("ResultSet.close(): Closing " + tables.size() + " views");
    Enumeration enum = tables.elements();
    while (enum.hasMoreElements())
    {
      tinySQLTableView table = (tinySQLTableView) enum.nextElement();
      try
      {
        table.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }


  /***
   * returns the resultSize of this query. As long as the end of the file
   * has not been reached, ROWS_NOT_KNOWN is returned.
   *
   */
  public int getResultSize()
  {
    return rowsMax;
  }
}


