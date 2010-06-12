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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * This converter will translate between dbase-byte-arrays and JDBC-Objects.
 *
 * A converter is used to translate raw-data from and to a database.
 * The abstract levels of tinySQL will not make any assumptions about used
 * dataformats of its derivates and uses converters to handle IO.
 *
 *
 */
public class dbfFileConverter extends tinySQLConverter
{
  private String encoding;
  private DecimalFormat numberFormat;

  /**
   * Returns a new converter, wich will create byte-arrays in the specified
   * encoding. If the encoding is not supported, a unsupported encoding exception
   * is thrown.
   *
   * @throws UnsupportedEncodingException if the specified encoding is not
   * supported by the Java VM
   */
  public dbfFileConverter(String encoding)
      throws UnsupportedEncodingException
  {
    new String("").getBytes(encoding);
    this.encoding = encoding;

    DecimalFormatSymbols syms = new DecimalFormatSymbols();
    syms.setDecimalSeparator('.');
    numberFormat = new DecimalFormat();
    numberFormat.setGroupingSize(0);
    numberFormat.setDecimalFormatSymbols(syms);
  }

  /**
   * converts a boolean to an byte-array with the the length of 1.
   *
   * @return a byte[] containing either "T" or "F"
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
      StringBuffer b = new StringBuffer(8);
      b.append(Utils.forceToSizeLeft(String.valueOf(year), 4, '0'));
      b.append(Utils.forceToSizeLeft(String.valueOf(mon), 2, '0'));
      b.append(Utils.forceToSizeLeft(String.valueOf(day), 2, '0'));
      return b.toString().getBytes(encoding);
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
      return Utils.forceToSize(s2, size, (byte) ' ', encoding);
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
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < pad; i++)
      {
        b.append(" ");
      }
      b.append(s);
      s = b.toString();
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
      return Utils.forceToSize(s, size, (byte) ' ', encoding);
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
      if (b.length == 8)
      {
        // parse for null dates
        int i=0;
        while((i<b.length) && (b[i]==0)) {
          i++;
        }
        if(i>=b.length) {
          return null;
        }

        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();

        int y = toNumber(b[0]) * 1000 + toNumber(b[1]) * 100 + toNumber(b[2]) * 10 + toNumber(b[3]);
        int m = toNumber(b[4]) * 10 + toNumber(b[5]);
        int d = toNumber(b[6]) * 10 + toNumber(b[7]);

        // month value is zero-based!
        cal.set(y, m-1, d);
        return new java.sql.Date(cal.getTime().getTime());
      }
    }

    Date d = super.convertNativeToDate(coldef, o);
    if (d == null)
      throw new tinySQLException("Converter returned null");
    return d;
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
        return rightTrim(s);
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
    while ((b[counter] == ' ') && (counter > 0))
    {
      counter--;
    }
    if (counter == 0)
    {
      return b.length;
    }
    return counter + 1;
  }

  /**
   * Converts native value to an number. If the columns is of type INTEGER or
   * DOUBLE the byte[]-contents are considered binary values, else the byte-array
   * is converted into an string and converted into a BigDecimal using its
   * string-constructor
   *
   * @returns a byte array with the lenght of the column.
   * @throws tinySQLException automaticly, as time is not yet supported
   */
  public BigDecimal convertNativeToNumber(tsColumn col, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;

      if (col.getType() == Types.INTEGER)
      {
        if (b.length != 4)
          throw new tinySQLException("An integer is defined as a 4 bytes value");

        // convert the byte array into a integer
        // 4-7 number of records
        return new BigDecimal(Utils.vax_to_long(b));

      }
      else if (col.getType() == Types.DOUBLE)
      {
        if (b.length != 8)
          throw new tinySQLException("An double is defined as a 8 bytes value");

        throw new tinySQLException("Doubles are not yet supported.");
      }
      else
      {
        try
        {
          String s = new String(b, 0, b.length, encoding).trim();
          if (s.length() == 0)
            return new BigDecimal(0);

          return new BigDecimal(s);
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
    }
    else
      return super.convertNativeToNumber(col, o);
  }

  private String rightTrim(String str) {
    if(str != null) {
      int i = str.length();
      while((i > 0) && (str.charAt(i-1) <= ' ')) {
        i--;
      }
      if(i < str.length()) {
        str = str.substring(0,i);
      }
    }
    return str;
  }

  /**
   * Converts a Object into a Integer. It expects a 4 byte byte-array.
   */
  public Integer convertNativeToInteger(tsColumn coldef, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;
      if (b.length == 4)
      {
        return new Integer((int) Utils.vax_to_long(b));
      }
    }
    return super.convertNativeToInteger(coldef, o);
  }

  /**
   * @returns a 4 byte byte[] of <code>o</code>
   */
  public Object convertIntegerToNative(tsColumn coldef, Integer o) throws tinySQLException
  {
    return Utils.intToLittleEndian(o.intValue());
  }

  /**
   * Converts a native value to an timestamp value. If the value is a byte array and
   * has the length of 8 a conversion is started. It is not checked whether the
   * value contains only numbers.
   *
   * For performance reasons no SimpleDateFormat is used. It is 4 times slower
   * than a manual convert.
   *
   * @returns a byte array with the lenght of the column.
   * @throws tinySQLException automaticly, as timestamp is not yet supported
   */
  public Timestamp convertNativeToTimestamp(tsColumn coldef, Object o) throws tinySQLException
  {
    if (o instanceof byte[])
    {
      byte[] b = (byte[]) o;
      if (b.length == 8)
      {
        byte[] bd = new byte[4];
        byte[] bm = new byte[4];

        System.arraycopy(b, 0, bd, 0, 4);
        System.arraycopy(b, 4, bm, 0, 4);

        long daytime = (long) calcToTime((int) Utils.vax_to_long(bd));
        long mintime = (long) Utils.vax_to_long(bm);

        /**
         * Is it a bug or not, Visual FoxPro does not store values of 1 second,
         * it stores 999 milliseconds. Weird thing ...
         */
        if (mintime != 0)
          mintime++;

        Timestamp ts = new Timestamp(daytime + mintime - getTimeZone().getRawOffset());
        return ts;
      }
      throw new tinySQLException("This native data does not represent a timestamp");
    }
    return super.convertNativeToTimestamp(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertTimestampToNative(tsColumn coldef, Timestamp o) throws tinySQLException
  {
    long time = o.getTime() + getTimeZone().getRawOffset();
    int days = calcFromTime(time);
    int mins = (int) (time % DAY_DIV);

    byte[] b = new byte[8];
    byte[] bd = Utils.intToLittleEndian(days);
    byte[] bm = Utils.intToLittleEndian(mins);

    System.arraycopy(bd, 0, b, 0, 4);
    System.arraycopy(bm, 0, b, 4, 4);

    return b;
  }

  private TimeZone _defaultZone;

  private TimeZone getTimeZone()
  {
    if (_defaultZone == null)
    {
      _defaultZone = GregorianCalendar.getInstance().getTimeZone();
    }
    return _defaultZone;
  }


  // 0.0.0000
  // this is the basedate for all dbase timestamps. Dont ask how it is working,
  // i found no documentation and this is the result of some experimenting ...
  private static final int DBASE_BASE_DATE = (0x253D8C);  // 1.1.1970 = JDBC date of '0'
  private static final long DAY_DIV = (1000 * 60 * 60 * 24);

  private static long calcToTime(int fpdate)
  {
    long time1 = (fpdate - DBASE_BASE_DATE) * DAY_DIV;
    return time1;
  }

  private static int calcFromTime(long time)
  {
    // getting the days is simple ... divide msecs, secs, minutes and hours
    long calctime = (time);
    int days = (int) (calctime / DAY_DIV);
    return days + DBASE_BASE_DATE;
  }

//  public static void main (String [] args)
//  {
//    test (0x1a4452); // 1.1.01
//    test (0x1a45bf); // 1.1.02
//    test (0x256859); // 1.1.2000
//    test (0x253D8D); // 1.1.1970
//    System.out.println (new dbfTimestamp (0));
//  }
//
//  private static void test (int time)
//  {
//    java.util.Date d = new java.util.Date (calcToTime (time));
//    System.out.println (d + " -> " + d.getTime());
//
//    int gctime = calcFromTime (d.getTime ());
//    if (gctime == time)
//    {
//      System.out.println ("Pass");
//    }
//    else
//    {
//      System.out.println ("delta:" + (gctime - time));
//      System.out.println ("In HEX:" + Integer.toHexString (gctime));
//    }
//
//    System.out.println ("----------------------------------------------");
//  }
}
