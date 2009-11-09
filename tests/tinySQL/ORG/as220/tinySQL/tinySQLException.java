/*
 *
 * tinySQLException.java
 * An Exception that is thrown when a problem has occurred in tinySQL
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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * @author Thomas Morgner <mgs@sherito.org> tinySQLException is now a subclass of
 * SQLException and can be directly passed to the caller.
 */
public class tinySQLException extends SQLException
{

  private Exception inner;

  /**
   *
   * Constructs a new tinySQLException
   * @param message the exception's message
   *
   */
  public tinySQLException(String message)
  {
    super(message);
  }

  /**
   *
   * Constructs a new tinySQLException with no message.
   *
   */
  public tinySQLException()
  {
  }

  /**
   *
   * Constructs a new tinySQLException with the specified
   * inner exception.
   * @param inner the inner exception
   *
   */
  public tinySQLException(Exception inner)
  {
    super(inner.getMessage());
    this.inner = inner;
  }

  /**
   *
   * Constructs a new tinySQLException with the specified
   * inner exception and message.
   * @param message the exception's message
   * @param inner the inner exception
   *
   */
  public tinySQLException(String message, Exception inner)
  {
    super(message);
    this.inner = inner;
  }

  /*
   * Display the stack trace.  If an inner exception exists, use
   * its stack trace.
   */
  public void printStackTrace()
  {
    if (inner != null)
    {
      inner.printStackTrace();
    }
    else
    {
      super.printStackTrace();
    }
  }

  public void printStackTrace(PrintStream s)
  {
    if (inner != null)
    {
      inner.printStackTrace(s);
    }
    else
    {
      super.printStackTrace(s);
    }
  }

  public void printStackTrace(PrintWriter s)
  {
    if (inner != null)
    {
      inner.printStackTrace(s);
    }
    else
    {
      super.printStackTrace(s);
    }
  }


}

