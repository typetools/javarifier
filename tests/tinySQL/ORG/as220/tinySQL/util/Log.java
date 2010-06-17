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
package ORG.as220.tinySQL.util;

import java.io.PrintWriter;
import java.sql.DriverManager;

/**
 *
 */
public class Log
{
  private boolean logSystemOut;

  /**
   * Loglevel ERROR
   */
  public static final int ERROR = 0;

  /**
   * Loglevel WARN
   */
  public static final int WARN = 1;

  /**
   * Loglevel INFO
   */
  public static final int INFO = 2;

  /**
   * Loglevel DEBUG
   */
  public static final int DEBUG = 3;

  public static final String[] levels =
      {
        "ERROR: ",
        "WARN:  ",
        "INFO:  ",
        "DEBUG: "
      };

  private static int debuglevel = 100;

  private Log()
  {
  }

  /**
   * logs an message to the main-log stream. All attached logStreams will also
   * receive this message. If the given log-level is higher than the given debug-level
   * in the main config file, no logging will be done.
   *
   * @param level log level of the message.
   * @param message text to be logged.
   */
  public static void log(int level, String message)
  {
    if (level > 3) level = 3;
    if (level <= debuglevel)
    {
      DriverManager.println(levels[level] + message);
    }
  }

  /**
   * logs an message to the main-log stream. All attached logStreams will also
   * receive this message. If the given log-level is higher than the given debug-level
   * in the main config file, no logging will be done.
   *
   * The exception's stacktrace will be appended to the log-stream
   *
   * @param level log level of the message.
   * @param message text to be logged.
   * @param e the exception, which should be logged.
   */
  public static void log(int level, String message, Exception e)
  {
    if (level > 3) level = 3;
    if (level <= debuglevel)
    {
      DriverManager.println(levels[level] + message);
      PrintWriter w = DriverManager.getLogWriter();
      if (w != null)
        e.printStackTrace(w);
    }
  }

  public static void debug(String message)
  {
    log(DEBUG, message);
  }

  public static void debug(String message, Exception e)
  {
    log(DEBUG, message, e);
  }

  public static void info(String message)
  {
    log(INFO, message);
  }

  public static void info(String message, Exception e)
  {
    log(INFO, message, e);
  }

  public static void warn(String message)
  {
    log(WARN, message);
  }

  public static void warn(String message, Exception e)
  {
    log(WARN, message, e);
  }

  public static void error(String message)
  {
    log(ERROR, message);
  }

  public static void error(String message, Exception e)
  {
    log(ERROR, message, e);
  }


}
