/*
 * dbfFileConverter
 *
 * Copyright 2002, Brian C. Jepson
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

import ORG.as220.tinySQL.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.GregorianCalendar;

/**
 * This converter will translate between dbase-byte-arrays and JDBC-Objects.
 *
 * A converter is used to translate raw-data from and to a database.
 * The abstract levels of tinySQL will not make any assumptions about used
 * dataformats of its derivates and uses converters to handle IO.
 *
 * 2002-Jun-01: Bug in seekEnd caused completly filled Strings to be empty.
 */
public class textFileConverter extends tinySQLConverter
{
  private String encoding;
  private DecimalFormat numberFormat;
  private textFileQuoting quoting;

  /**
   * Returns a new converter, wich will create byte-arrays in the specified
   * encoding. If the encoding is not supported, a unsupported encoding exception
   * is thrown.
   *
   * @throws UnsupportedEncodingException if the specified encoding is not
   * supported by the Java VM
   */
  public textFileConverter(String encoding, textFileQuoting quoting)
      throws UnsupportedEncodingException
  {
    // test the encoding
    new String("").getBytes(encoding);
    this.encoding = encoding;
    if (quoting == null)
      throw new NullPointerException("Quoting is null");
    this.quoting = quoting;
    DecimalFormatSymbols syms = new DecimalFormatSymbols();
    syms.setDecimalSeparator('.');
    numberFormat = new DecimalFormat();
    numberFormat.setGroupingSize(0);
    numberFormat.setDecimalFormatSymbols(syms);
  }

  /**
   * converts a boolean to an byte-array with the the length of 1.
   *
   * @return a char[] containing either "T" or "F"
   */
  public Object convertBooleanToNative(tsColumn coldef, Boolean b) throws tinySQLException
  {
    try
    {
      if (b.booleanValue() == true)
        return Utils.forceToSize("T", 1, (byte) 0x0, encoding);
      else
        return Utils.forceToSize("F", 1, (byte) 0x0, encoding);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new tinySQLException("Encoding not supported");
    }
  }

  /**
   * converts a date to an byte-array with the the length of 8.
   *
   * @return a byte[] containing the date in the format "yyyyMMdd"
   */
  public Object convertDateToNative(tsColumn coldef, Date d) throws tinySQLException
  {
    try
    {
      GregorianCalendar cal = new GregorianCalendar();
      cal.clear();

      cal.setTime(d);
      int year = cal.get(cal.YEAR);
      int mon = cal.get(cal.MONTH);
      int day = cal.get(cal.DAY_OF_MONTH);
      Log.debug("Converting. DATE: " + d);
      StringBuffer b = new StringBuffer(10);
      b.append(Utils.forceToSizeLeft(String.valueOf(year), 4, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(mon), 2, '0'));
      b.append('-');
      b.append(Utils.forceToSizeLeft(String.valueOf(day), 2, '0'));
      System.out.println("Quoting: " + quoting);
      return quoting.doQuoting(b.toString(), coldef.getSize()).getBytes(encoding);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new tinySQLException("Encoding not supported");
    }
  }

  /**
   * converts a time to an byte-array with the the length of 8.
   *
   * @throws always an tinySQLException, as time is not yet supported
   */
  public Object convertTimeToNative(tsColumn coldef, Time t) throws tinySQLException
  {
    // Not yet used
    throw new tinySQLException("Conversion not supported");
  }


  /**
   * converts a null value to an byte-array with the the length of the column.
   * If the column is nullable, an array consisting of 0x00-Values are returned,
   * else a array of 'Blank'-Characters is returned
   *
   * @returns a byte array with the lenght of the column.
   */
  public Object convertNullToNative(tsColumn coldef) throws tinySQLException
  {
    try
    {
      if (coldef.isNullable())
      {
        return Utils.forceToSize("", coldef.getSize(), (byte) 0x00, encoding);
      }
      else
      {
        return Utils.forceToSize("", coldef.getSize(), (byte) ' ', encoding);
      }
    }
    catch (UnsupportedEncodingException e)
    {
      // This should not happen
      throw new tinySQLException("Encoding not supported");
    }
  }

  /**
   * converts a number value to an byte-array with the the length of the column.
   * If the number-representation is smaller than the column, the byte array is
   * left-padded with blanks.
   *
   * Warning, this function does not yet convert integers 'I' and doubles 'D'
   * properly. They are not yet supported anyway.
   *
   * @returns a byte array with the lenght of the column.
   */
  public Object convertNumberToNative(tsColumn col, Number n) throws tinySQLException
  {
    int size = col.getSize();
    int dec = col.getDecimalPlaces();
    String s = null;

    synchronized (numberFormat)
    {
      if ((col.getType() == Types.NUMERIC) || (col.getType() == Types.DECIMAL))
      {
        numberFormat.setMaximumIntegerDigits(size - dec);
        numberFormat.setMinimumIntegerDigits(dec);
        numberFormat.setMaximumFractionDigits(dec);
        numberFormat.setMinimumFractionDigits(dec);
        s = numberFormat.format(n);
      }
      else if ((col.getType() == Types.BIGINT) ||
          (col.getType() == Types.INTEGER) ||
          (col.getType() == Types.SMALLINT) ||
          (col.getType() == Types.TINYINT))
      {
        numberFormat.setMaximumIntegerDigits(size);
        numberFormat.setMinimumIntegerDigits(0);
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setMinimumFractionDigits(0);
        s = numberFormat.format(n);
      }
      else if ((col.getType() == Types.REAL) ||
          (col.getType() == Types.FLOAT) ||
          (col.getType() == Types.DOUBLE))
      {
        numberFormat.setMaximumIntegerDigits(size);
        numberFormat.setMinimumIntegerDigits(0);
        numberFormat.setMaximumFractionDigits(size);
        numberFormat.setMinimumFractionDigits(0);
        s = numberFormat.format(n);
      }
    }
    if (s == null)
    {
      Object o = super.convertNumberToNative(col, n);
      if (o == null)
      {
        s = "";
      }
      else
      {
        s = o.toString();
      }
    }
    String s2 = leftPadString(s, size);
    try
    {
      return Utils.forceToSize(quoting.doQuoting(s2, size), size, (byte) ' ', encoding);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new tinySQLException("Encoding not supported");
    }
  }

  /**
   * left-pads a string to the size. The string is NOT cut if s.length > size.
   */
  private String leftPadString(String s, int size) throws tinySQLException
  {
    int pad = size - s.length();
    if (pad > 0)
    {
      StringBuffer b = new StringBuffer(pad);
      for (int i = 0; i < pad; i++)
      {
        b.append(' ');
      }
      b.append(s);
      b.toString();
    }
    return s;
  }

  /**
   * converts a string value to an byte-array with the the length of the column.
   * If the string-representation is smaller than the column, the byte array is
   * right-padded with blanks.
   *
   * @returns a byte array with the lenght of the column.
   */
  public Object convertStringToNative(tsColumn col, String s) throws tinySQLException
  {
    try
    {
      int size = col.getSize();
      return Utils.forceToSize(quoting.doQuoting(s, size), size, (byte) ' ', encoding);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new tinySQLException("Encoding not supported");
    }
  }

  /**
   * converts a native value to null, simply by returning null
   *
   * @returns null
   */
  public Object convertNativeToNull(tsColumn col) throws tinySQLException
  {
    return null;
  }

  /**
   * Converts a native value to an boolean value. If the value is a byte array,
   * has the length of 1 and contains one of the character 'T', 't', 'Y' or 'y'
   * return true.
   *
   * Remark: If a boolean column is updated, and the value is TRUE, the 'T' is
   * always inserted.
   *
   * @returns a Boolean.TRUE or a Boolean.FALSE.
   */
  public Boolean convertNativeToBoolean(tsColumn coldef, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;
      if (b.length == 1)
      {
        if (b[0] == 'T' || b[0] == 't' || b[0] == 'Y' || b[0] == 'y')
        {
          return Boolean.TRUE;
        }
      }
      return Boolean.FALSE;
    }

    return super.convertNativeToBoolean(coldef, o);
  }

  /**
   * Converts a native value to an date value. If the value is a byte array and
   * has the length of 8 a conversion is started. It is not checked whether the
   * value contains only numbers.
   *
   * For performance reasons no SimpleDateFormat is used. It is 4 times slower
   * than a manual convert.
   *
   * @returns a date.
   */
  public Date convertNativeToDate(tsColumn coldef, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;
      if (b.length == 10)
      {

        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();

        int y = toNumber(b[0]) * 1000 + toNumber(b[1]) * 100 + toNumber(b[2]) * 10 + toNumber(b[3]);
        int m = toNumber(b[5]) * 10 + toNumber(b[6]);
        int d = toNumber(b[8]) * 10 + toNumber(b[9]);
        cal.set(y, m, d);
        return new java.sql.Date(cal.getTime().getTime());
      }
    }

    return super.convertNativeToDate(coldef, o);
  }

  private int toNumber(byte b)
  {
    return b - (byte) '0';
  }


  /**
   * Converts a native value to an time value. If the value is a byte array and
   * has the length of 8 a conversion is started. It is not checked whether the
   * value contains only numbers.
   *
   * For performance reasons no SimpleDateFormat is used. It is 4 times slower
   * than a manual convert.
   *
   * @returns a byte array with the lenght of the column.
   * @throws tinySQLException automaticly, as time is not yet supported
   */
  public Time convertNativeToTime(tsColumn coldef, Object o) throws tinySQLException
  {
    throw new tinySQLException("Conversion not supported");
  }


  /**
   * Converts a object to a native value. If the object is a byte[], it is
   * assumed, that native values are contained and the byte[] is returned.
   *
   * @returns a byte array with the lenght of the column.
   * @throws tinySQLException automaticly, as time is not yet supported
   */
  public Object convertJDBCToNative(tsColumn col, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;
      if (b.length == col.getSize())
      {
        // We are native, so do not convert
        return b;
      }
    }
    return super.convertJDBCToNative(col, o);
  }

  /**
   * Converts native value to an String. The string is converted using the current
   * encoding. The String is trimed, as dbase does not terminate strings in CHAR
   * columns.
   *
   * ToDo: Do only a right-trim of the string, as the first spaces may be needed.
   *
   * @returns a byte array with the lenght of the column.
   * @throws tinySQLException automaticly, as time is not yet supported
   */
  public String convertNativeToString(tsColumn col, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;
      try
      {
        String s = new String(b, 0, seekEnd(b), encoding);
        return quoting.undoQuoting(s.trim());
      }
      catch (UnsupportedEncodingException e)
      {
        throw new tinySQLException("Encoding not supported");
      }
    }
    else
      return super.convertNativeToString(col, o);
  }

  /**
   * seeks the end of an byte[] containing a native string. Only blanks
   * are checked, other whitespaces at the strings end, are considered valueable
   * information.
   *
   * @returns the index of the last non-blank character
   */
  private int seekEnd(byte[] b)
  {
    if (b.length == 0)
      return 0;
    int counter = b.length - 1;
    while ((b[counter] != ' ') && (counter > 0))
    {
      counter--;
    }
    if (counter == 0)
      return b.length;
    return counter + 1;
  }

  /**
   * Converts native value to an number. If the columns is of type INTEGER or
   * DOUBLE the byte[]-contents are considered binary values, else the byte-array
   * is converted into an string and converted into a BigDecimal using its
   * string-constructor
   *
   * ToDo: Do only a right-trim of the string, as the first spaces may be needed.
   *
   * @returns a byte array with the lenght of the column.
   * @throws tinySQLException automaticly, as time is not yet supported
   */
  public BigDecimal convertNativeToNumber(tsColumn col, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;

      try
      {
        String s = new String(b, 0, b.length, encoding).trim();
        if (s.length() == 0)
          return new BigDecimal(0);

        return new BigDecimal(quoting.undoQuoting(s));
      }
      catch (UnsupportedEncodingException ue)
      {
        throw new tinySQLException("Encoding not supported");
      }
      catch (Exception e)
      {
        throw new tinySQLException("Parsing failed");
      }
    }
    else
      return super.convertNativeToNumber(col, o);
  }

}
