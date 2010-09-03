/*
 *
 * tinySQLDriver - the tinySQLDriver abstract class
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
 */

package ORG.as220.tinySQL;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;

/**
 * This is the abstract base class of the driver.
 *
 */
public abstract class tinySQLDriver implements java.sql.Driver
{

  /**
   *
   * Constructs a new tinySQLDriver
   *
   */
  public tinySQLDriver()
  {
  }

  /**
   *
   * Check to see if the URL is a tinySQL URL. It should start
   * with jdbc:tinySQL in order to qualify.
   *
   * @param url The URL of the database.
   * @return True if this driver can connect to the given URL.
   *
   */
  public abstract boolean acceptsURL(String url) throws SQLException;

  /**
   *
   * The getPropertyInfo method is intended to allow a generic GUI tool to
   * discover what properties it should prompt a human for in order to get
   * enough information to connect to a database.  Note that depending on
   * the values the human has supplied so far, additional values may become
   * necessary, so it may be necessary to iterate though several calls
   * to getPropertyInfo.
   *
   * @param url The URL of the database to connect to.
   * @param info A proposed list of tag/value pairs that will be sent on
   *          connect open.
   * @return An array of DriverPropertyInfo objects describing possible
   *          properties.  This array may be an empty array if no properties
   *          are required.
   *
   */
  public DriverPropertyInfo[] getPropertyInfo(String url,
                                              java.util.Properties info)
      throws SQLException
  {
    return new DriverPropertyInfo[0];
  }

  /**
   *
   * Gets the driver's major version number.
   * @see java.sql.Driver#getMajorVersion
   * @return the major version
   *
   */
  public int getMajorVersion()
  {
    return 0;
  }

  /**
   *
   * Gets the driver's minor version
   * @see java.sql.Driver#getMinorVersion
   * @return the minor version
   *
   */
  public int getMinorVersion()
  {
    return 9;
  }

  /**
   *
   * Report whether the Driver is a genuine JDBC COMPLIANT (tm) driver.
   * Unfortunately, the tinySQL is "sub-compliant" :-(
   *
   */
  public boolean jdbcCompliant()
  {
    return false;
  }
}
