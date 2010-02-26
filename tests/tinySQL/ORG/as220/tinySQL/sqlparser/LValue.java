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
import ORG.as220.tinySQL.tsRow;

import java.util.Enumeration;

/**
 * an LValue is the supertype of all kinds of expressions and
 * evaluates to an Object.
 *
 * Currently there are four implementations used:
 * <ul>
 * <li>ColumnValue represents a reference to a data column
 * <li>Expression represents a composite LValue computing values of many LValues
 * into a single result.
 * <li>ParameterValue is a value representing a PreparedStatement Parameter.
 * <li>JokerColumnValue is a value representing all columns of a single table or
 * of all tables.
 * </ul>
 */
public interface LValue
{
  /**
   * Evaluates the lvaue. rowdata may be null, throw an exception if
   * a LValue cannot be evaluated..
   */
  public Object evaluate(tsRow rowdata) throws tinySQLException;

  /**
   * return the name of this LValue. It is not enforced that the name
   * has be be unique.
   */
  public String getName();

  /**
   * returns the children of the lvalue. Children are other lvalues used
   * to compute parts or all portions of the result.
   */
  public Enumeration getChildren();

  /**
   * returns the number of children used in this LValue
   */
  public int getChildCount();
}
