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
 * An empty statement does nothing and
 * produces no results. It is simply a placeholder when encountering
 * a lonely ";" in an SQLString.
 *
 * The specific Statement is initialized by the parser
 * and will start doing nothing on execute.
 *
 */
public class EmptyStatement implements SQLStatement
{
  private tinySQLStatement statement;

  /**
   * Creates an empty statement. An empty statement does nothing and
   * produces no results. It is simply a placeholder when encountering
   * a lonely ";" in an SQLString.
   */
  public EmptyStatement(tinySQLStatement statement)
  {
    this.statement = statement;
    new Exception().printStackTrace();
  }

  /**
   * returns the instance of the database wich will be called to
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
   * An empty statement will not execute and will always return false
   */
  public boolean execute() throws tinySQLException
  {
    return false;
  }

  /**
   * An empty statement will not return a update count
   */
  public int getUpdateCount() throws tinySQLException
  {
    return -1;
  }

  /**
   * An empty statement will not return a resultset
   */
  public tinySQLResultSet getResultSet() throws tinySQLException
  {
    return null;
  }

  /**
   * returns false as an empty statement does not return a result.
   */
  public boolean getMoreResults() throws tinySQLException
  {
    return false;
  }

  /**
   * An empty statement has no parameters, return a empty vector
   */
  public Vector getParameters()
  {
    return new Vector();
  }
}
