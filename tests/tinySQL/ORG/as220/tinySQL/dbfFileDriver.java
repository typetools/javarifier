/*
 *
 * dbfFile/tinySQL JDBC driver
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
 * Copyright 1996 John Wiley & Sons, Inc.
 * See the COPYING file for redistribution details.
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
 */
package ORG.as220.tinySQL;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


/**
 * dBase read/write access <br>
 * This driver understands the following properties:
 * <p>
 * <ul>
 * <li>encoding<br>
 * The default encoding used for table data. Enter a valid encoding string,
 * such as "Cp1252" or "Cp850". If the choosen encoding is not supported by your
 * system, an exception is thrown whenever a table is read. This property defaults
 * to "Cp1252" which is "Windows ANSI"-Encoding. See DBFHeader for a list of
 * supported encodings.
 * <li>autoenc<br>
 * If this boolean property is set to true, tinySQL tries to determine the encoding
 * of an existing table automaticly. If the encoding cannot be detected, the default
 * encoding is used. This value defaults to true.
 * <li>readonly<br>
 * If this property is set, the database is readonly and cannot be modified.
 * This feature is usefull when opening files on read-only media. The value defaults
 * to false.
 * </ul>
 *
 * @author Brian Jepson <bjepson@home.com>
 * @author Marcel Ruff <ruff@swand.lake.de> Added write access to dBase and JDK 2 support
 */
public class dbfFileDriver extends tinySQLDriver
{

  /*
   *
   * Instantiate a new dbfFileDriver(), registering it with
   * the JDBC DriverManager.
   *
   */
  static
  {
    try
    {
      java.sql.DriverManager.registerDriver(new dbfFileDriver());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *
   * Constructs a new dbfFileDriver
   *
   */
  public dbfFileDriver()
  {
  }

  /**
   *
   * returns a new dbfFileConnection object, which is cast
   * to a tinySQLConnection object.
   *
   * @exception SQLException when an error occurs
   * @param url the url to the data source
   *
   */
  public Connection connect(String url, Properties p)
      throws SQLException
  {
    if (acceptsURL(url))
    {
      if (p == null)
      {
        p = new Properties();
        p.setProperty("user", "");
      }
      return new dbfFileConnection(p.getProperty("user"), url, this, p);
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
      // System.out.println ("Discarding: Url < 12");
      return false;
    }

    // if everything after the jdbc: part is
    // dbfFile, then return true.
    //
    return url.substring(5, 12).equals("dbfFile");

  }

}
