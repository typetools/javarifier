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

import ORG.as220.tinySQL.tinySQLException;
import ORG.as220.tinySQL.tinySQLStatement;

import java.sql.BatchUpdateException;
import java.util.Vector;

/**
 * Encapsulates multiple parsed statements into a batch.
 *
 * Usually one would add Update or Insert statements to the batch.
 * If you add table structure changing statements to the batch, the
 * execution of the next statement accessing the modified table will
 * fail, as ALTER TABLE and CREATE TABLE close every reference to the
 * table to get exclusive write access and tables are assigned after
 * the parse process.
 */
public class SQLStatementBatch
{
  private Vector statements;
  private tinySQLStatement parent;

  /**
   * creates a new batch. Usually you need one per statement instance.
   */
  public SQLStatementBatch(tinySQLStatement parent)
  {
    statements = new Vector();
    this.parent = parent;
  }

  /**
   * returns the statement to which this batch-object is assigned.
   */
  public tinySQLStatement getParent()
  {
    return parent;
  }

  /**
   * adds a Statement to the batch.
   */
  public void add(SQLStatement statement)
  {
    statements.addElement(statement);
  }

  /**
   * clears the batch
   */
  public void clear()
  {
    statements.clear();
  }

  /**
   * Executes the batch
   *
   * Submits a batch of commands to the database for execution and if all
   * commands execute successfully, returns an array of update counts. The
   * int elements of the array that is returned are ordered to correspond
   * to the commands in the batch, which are ordered according to the order
   * in which they were added to the batch.
   */
  public int[] executeAll()
      throws tinySQLException, BatchUpdateException
  {
    int size = statements.size();
    int[] results = new int[size];

    for (int i = 0; i < size; i++)
    {
      SQLStatement stmt = (SQLStatement) statements.elementAt(i);

      if (stmt.execute())
      {
        throw new BatchUpdateException("Statement tries to return ResultSet", results);
      }
      results[i] = stmt.getUpdateCount();
    }
    return results;
  }

  /**
   * Returns a string representation of the object.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();

    int size = statements.size();
    for (int i = 0; i < size; i++)
    {
      SQLStatement stmt = (SQLStatement) statements.elementAt(i);
      b.append("Statement: ");
      b.append(stmt.toString());
      b.append("\n");
    }
    return b.toString();
  }
}
