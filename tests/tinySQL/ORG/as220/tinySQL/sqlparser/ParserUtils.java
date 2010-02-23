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

import ORG.as220.tinySQL.Utils;
import ORG.as220.tinySQL.tinySQLException;
import ORG.as220.tinySQL.tinySQLTableView;
import ORG.as220.tinySQL.tsColumn;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Helper functions for the parser and its statements.
 */
public class ParserUtils
{
  // A gregorian calendar used for date/time conversions
//  private static GregorianCalendar cal = new GregorianCalendar ();

  /**
   * find all ColumnValues used in a specific expression and collect
   * them in the vector v. ColumnValues represent a value read from
   * column of a table.
   */
  public static void getColumnElements(Vector v, LValue root)
  {
    if (root.getChildCount() > 0)
    {
      Enumeration enum = root.getChildren();
      while (enum.hasMoreElements())
      {
        LValue val = (LValue) enum.nextElement();
        getColumnElements(v, val);
      }
    }

    if (root instanceof ColumnValue)
    {
      v.add(root);
    }
  }

  /**
   * find all ParameterValues used in a specific expression and collect
   * them in the vector v. Parameter values are placeholders for PreparedStatement
   * sql-strings.
   */
  public static void getParameterElements(Vector v, LValue root)
  {
    if (root.getChildCount() > 0)
    {
      Enumeration enum = root.getChildren();
      while (enum.hasMoreElements())
      {
        LValue val = (LValue) enum.nextElement();
        getParameterElements(v, val);
      }
    }

    if (root instanceof ParameterValue)
    {
      v.add(root);
    }
  }

  /**
   * Finds all columnValues used in a expression, check whether the column
   * exists and create a tsColumn object for the column. Add the tsColumn
   * to the to be returned hashtable.
   *
   * @param column the expression to check for columns
   * @param db the database to use when searching tables
   * @param tables the known tables for the statement
   */
  public static Vector resolveTableColumns(LValue column, Vector tables)
      throws tinySQLException
  {
    Vector tableColumns = new Vector();

    /**
     * Find all needed columns and check if they exist.
     */
    Vector checkColumns = new Vector();
    getColumnElements(checkColumns, column);
    int size = checkColumns.size();

    // now seek the tables for the columns
    for (int i = 0; i < size; i++)
    {
      ColumnValue colElement = (ColumnValue) checkColumns.get(i);
      String name = colElement.getColumn();
      String tablename = colElement.getTable();

      tinySQLTableView table = null;
      if (tablename == null)
      {
        table = findTableForColumn(tables.elements(), name);
      }
      else
      {
        // the table is not in the list of defined tables, so we
        // can't proceed.
        table = findTable(tablename, tables);
        if (table == null)
          throw new tinySQLException("There is no table called " + table);
      }

      // create the tsColumn for the lvalue
      tsColumn columnDefinition = new tsColumn(table, name);
      tsColumn tablecoldef = table.getColumnDefinition(table.findColumn(name));
      if (tablecoldef == null)
        throw new NullPointerException("Table: " + table.getName() + " Column " + name);

      // copy column definition from table's column into the result column
      columnDefinition.setAll(tablecoldef);

      // only add the result column to the return-vector if there is no such column
      if (tableColumns.indexOf(columnDefinition) == -1)
      {
        tableColumns.add(columnDefinition);
      }
    }
    return tableColumns;
  }

  /**
   *
   * Given a column name, and a Hashtable containing tsTable-Objects
   * try to find which table "owns" a given column.
   *
   * HelperFunction -> into utils?
   */
  public static tinySQLTableView findTableForColumn(Enumeration tables, String col_name)
      throws tinySQLException
  {
    // process each table in the tables Hashtable
    //
    while (tables.hasMoreElements())
    {

      // retrieve the tinySQLTable object
      //
      tinySQLTableView tableDecl = (tinySQLTableView) tables.nextElement();

      // get the table's column info, and check to
      // see if it contains the column name in question.
      // if so, return the tinySQLTable object.
      //
      if (tableDecl.findColumn(col_name) != -1)
      {
        return tableDecl;
      }
    }

    // looks like we couldn't find the column, so throw an exception
    //
    throw new tinySQLException("Column " + col_name + " not found.");
  }

  /**
   * Seeks and returns the table with the name or alias <code>name</code>
   * which has to be contained in the vector <code>tables</code>.
   *
   * @returns the found table or null if no table was found
   */
  public static tinySQLTableView findTable(String name, Vector tables)
  {
    int size = tables.size();
    for (int i = 0; i < size; i++)
    {
      tinySQLTableView vTable = (tinySQLTableView) tables.get(i);
      if ((name.equals(vTable.getName())) ||
          (name.equals(vTable.getAlias())))
      {
        return vTable;
      }
    }
    return null;
  }

  /**
   * Builds a vector from a enumeration by iterating through the enumeration
   * and adding every element to the vector.
   *
   * @return the new vector, which may be empty
   */
  public static Vector buildVector(Enumeration enum)
  {
    Vector retval = new Vector();
    while (enum.hasMoreElements())
    {
      retval.add(enum.nextElement());
    }
    return retval;
  }

  /**
   * converts a object into a string. if the object is null, <code>null</code>
   * is returned. This functions handles Time, sql.Date und util.Date specially.
   * If o is a byte or char array, a string is formed using the usual
   * string constructors.
   *
   * @returns a string representation of the object or null
   */
  public static String convertToString(Object o)
  {
    if (o == null)
      return null;

    if (o instanceof String)
      return (String) o;

    if (o instanceof java.sql.Time)
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.clear();
      cal.setTime((java.sql.Date) o);
      int hour = cal.get(cal.HOUR_OF_DAY);
      int min = cal.get(cal.MINUTE);
      int sec = cal.get(cal.SECOND);
      StringBuffer b = new StringBuffer(10);
      b.append(Utils.forceToSizeLeft(String.valueOf(hour), 2, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(min), 2, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(sec), 2, '0'));
      return b.toString();
    }

    if (o instanceof java.sql.Date)
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.clear();
      cal.setTime((java.sql.Date) o);
      int year = cal.get(cal.YEAR);
      int mon = cal.get(cal.MONTH);
      int day = cal.get(cal.DAY_OF_MONTH);
      StringBuffer b = new StringBuffer(10);
      b.append(Utils.forceToSizeLeft(String.valueOf(year), 4, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(mon + 1), 2, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(day), 2, '0'));
      return b.toString();
    }

    if (o instanceof java.util.Date)
    {
      java.util.Date d = (java.util.Date) o;
      GregorianCalendar cal = new GregorianCalendar();
      cal.clear();
      cal.setTime(d);
      int hour = cal.get(cal.HOUR_OF_DAY);
      int min = cal.get(cal.MINUTE);
      int sec = cal.get(cal.SECOND);
      int year = cal.get(cal.YEAR);
      int mon = cal.get(cal.MONTH);
      int day = cal.get(cal.DAY_OF_MONTH);
      StringBuffer b = new StringBuffer(20);
      b.append(Utils.forceToSizeLeft(String.valueOf(year), 4, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(mon + 1), 2, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(day), 2, '0'));
      b.append(' ');
      b.append(Utils.forceToSizeLeft(String.valueOf(hour), 2, '0'));
      b.append(':');
      b.append(Utils.forceToSizeLeft(String.valueOf(min), 2, '0'));
      b.append(':');
      b.append(Utils.forceToSizeLeft(String.valueOf(sec), 2, '0'));
      return b.toString();
    }

    if (o instanceof byte[])
    {
      return new String((byte[]) o);
    }

    if (o instanceof char[])
    {
      return new String((char[]) o);
    }
    return o.toString();
  }

  /**
   * returns a object into a boolean. Returns true, if the Object is a
   * boolean value containing Boolean.TRUE or a string containing "true"
   *
   * @returns Boolean.TRUE or Boolean.FALSE
   */
  public static Boolean convertToBoolean(Object o)
  {
    if (o == null)
      return null;

    if (o instanceof Boolean)
      return (Boolean) o;

    if (o instanceof String)
    {
      String s = (String) o;
      if (s.trim().toLowerCase().equals("true"))
        return Boolean.TRUE;
    }

    if (o.equals(Boolean.TRUE))
    {
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  /**
   * Converts a Object into a big decimal. If the Object is a date, its
   * time value is used to create the number. If the Object is a Number,
   * the double value is used. As last try the Object is converted into a
   * String and this String is parsed into a BigDecimal. If this failed,
   * BidgDecimal (0) is returned.
   *
   */
  public static BigDecimal convertToNumber(Object o)
  {
    if (o == null)
    {
      return new BigDecimal(0);
    }

    if (o instanceof BigDecimal)
      return (BigDecimal) o;

    if (o instanceof Number)
    {
      return new BigDecimal(((Number) o).doubleValue());
    }

    if (o instanceof java.util.Date)
    {
      return new BigDecimal(((java.util.Date) o).getTime());
    }

    String s = convertToString(o);
    try
    {
      return new BigDecimal(s.trim());
    }
    catch (Exception e)
    {
    }
    return new BigDecimal(0);
  }

  /**
   * Converts a Object into a java.sql.Date. If the Object is a Date,
   * Number or a String of the form yyyy-MM-dd, the conversion will
   * return a new Date, else null is returned.
   *
   * @returns the converted date or null
   */
  public static java.sql.Date convertToDate(Object o)
  {
    if (o == null)
      return new java.sql.Date(0);

    if (o instanceof java.sql.Date)
      return (java.sql.Date) o;

    if (o instanceof Number)
      return new java.sql.Date(((Number) o).longValue());

    if (o instanceof java.util.Date)
    {
      java.util.Date date = (java.util.Date) o;
      return new java.sql.Date(date.getTime());
    }

    if (o instanceof String)
    {
      try
      {
        String s = (String) o;
        if (s.length() == 10)
        {
          int y = toNumber(s.charAt(0)) * 1000 + toNumber(s.charAt(1)) * 100 + toNumber(s.charAt(2)) * 10 + toNumber(s.charAt(3));
          int m = toNumber(s.charAt(5)) * 10 + toNumber(s.charAt(6));
          int d = toNumber(s.charAt(8)) * 10 + toNumber(s.charAt(9));
          GregorianCalendar cal = new GregorianCalendar();
          cal.clear();
          cal.set(y, m, d);
          return new java.sql.Date(cal.getTime().getTime());
        }
        else
        {
          return java.sql.Date.valueOf(s);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    throw new ClassCastException(o.getClass().getName());
  }

  private static int toNumber(char b)
  {
    return b - '0';
  }


  /**
   * Converts a Object into a java.sql.Time. If the Object is a Date,
   * Number or a String of the form hh:mm:ss, the conversion will
   * return a new Time object, else null is returned.
   *
   * @returns the converted time or null
   */
  public static Time convertToTime(Object o)
  {
    if (o == null)
      return new Time(0);

    if (o instanceof Time)
      return (Time) o;

    if (o instanceof Number)
      return new Time(((Number) o).longValue());

    if (o instanceof java.util.Date)
      return new Time(((java.util.Date) o).getTime());

    if (o instanceof String)
    {
      String s = (String) o;
      if (s.length() != 8)
      {
        try
        {
          return Time.valueOf(s);
        }
        catch (Exception e)
        {
        }
      }
      else
      {
        int h = toNumber(s.charAt(0)) * 10 + toNumber(s.charAt(1));
        int m = toNumber(s.charAt(3)) * 10 + toNumber(s.charAt(4));
        int se = toNumber(s.charAt(6)) * 10 + toNumber(s.charAt(7));
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(0, 0, 0, h, m, se);
        return new java.sql.Time(cal.getTime().getTime());
      }
    }
    return null;
  }

  /**
   * Converts a Object into a java.sql.Timestamp. If the Object is a Date,
   * Number or a String of the form yyyy-MM-dd hh:mm:ss, the conversion will
   * return a new Timestamp, else null is returned.
   *
   * @returns the converted timestamp or null
   */
  public static Timestamp convertToTimestamp(Object o)
  {
    if (o == null)
    {
      return new Timestamp(0);
    }
    System.out.println(o.getClass());

    if (o instanceof Timestamp)
    {
      return (Timestamp) o;
    }

    if (o instanceof Number)
      return new Timestamp(((Number) o).longValue());

    if (o instanceof java.util.Date)
      return new Timestamp(((java.util.Date) o).getTime());

    if (o instanceof String)
    {
      try
      {
        return Timestamp.valueOf((String) o);
      }
      catch (Exception e)
      {
      }
    }
    return null;
  }

  /**
   * Converts the java.sql.Types constant into a String literal.
   *
   * Literals here apply to all ColumnDefinition Strings of the parser.
   */
  public static String typeToLiteral(int type)
  {
    if (type == Types.CHAR) return "CHAR";
    if (type == Types.NUMERIC) return "NUMERIC";
    if (type == Types.BIT) return "BIT";
    if (type == Types.INTEGER) return "INTEGER";
    if (type == Types.BINARY) return "BINARY";
    if (type == Types.DATE) return "DATE";
    if (type == Types.TIME) return "TIME";
    if (type == Types.TIMESTAMP) return "TIMESTAMP";
    return "BINARY"; // fallback
  }


}
