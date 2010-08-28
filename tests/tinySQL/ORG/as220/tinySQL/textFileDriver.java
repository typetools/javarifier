/*
 *
 * textFile/tinySQL JDBC driver
 *
 * A lot of this code is based on or directly taken from
 * George Reese's (borg@imaginary.com) mSQL driver.
 *
 * So, it's probably safe to say:
 *
 * Portions of this code Copyright (c) 1996 George Reese
 *
 * The rest of it:
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class textFileDriver extends tinySQLDriver
{

  /*
   *
   * Instantiate a new textFileDriver(), registering it with
   * the JDBC DriverManager.
   *
   */
  static
  {
    try
    {
      java.sql.DriverManager.registerDriver(new textFileDriver());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *
   * Constructs a new textFileDriver
   *
   */
  public textFileDriver()
  {
  }

  /**
   *
   * returns a new textFileConnection object, which is cast
   * to a tinySQLConnection object.
   *
   * @exception SQLException when an error occurs
   * @param user the username - currently unused
   * @param url the url to the data source
   * @param d the Driver object.
   *
   */
  public Connection connect
      (String url, Properties p)
      throws SQLException
  {
    if (acceptsURL(url))
    {
      if (p == null)
      {
        p = new Properties();
        p.setProperty("user", "");
      }
      return new textFileConnection(p.getProperty("user"), url, this, p);
    }
    throw new tinySQLException("the given url was not valid");
  }

  /**
   *
   * Check to see if the URL is a dbfFile URL. It should start
   * with jdbc:dbfFile in order to qualify.
   *
   * @param url The URL of the database.
   * @return True if this driver can connect to the given URL.
   *
   */
  public boolean acceptsURL(String url) throws SQLException
  {

    // make sure the length is at least twelve
    // before bothering with the substring
    // comparison.
    //
    if (url.length() < 12)
    {
      return false;
    }

    // if everything after the jdbc: part is
    // dbfFile, then return true.
    //
    return url.substring(5, 12).equals("tinySQL");

  }

}
