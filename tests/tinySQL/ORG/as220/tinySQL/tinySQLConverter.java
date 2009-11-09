/**
 * tinySQLConverter
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

import ORG.as220.tinySQL.sqlparser.ParserUtils;
import ORG.as220.tinySQL.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * A converter will translate between dbase-native-objects and JDBC-Objects.
 *
 * A converter is used to translate raw-data from and to a database.
 * The abstract levels of tinySQL will not make any assumptions about used
 * dataformats of its derivates and uses converters to handle IO.
 *
 * Make sure when overriding functions to call the parents functionality
 * or conversion from JDBC to native will not work as expected as it
 * calles convertNativeToJBDC as a normalisation function.
 *
 */
public class tinySQLConverter
{
  /**
   * create a new converter
   */
  public tinySQLConverter()
  {
  }

  /**
   * converts a object into a string. if the object is null, <code>null</code>
   * is returned. This functions handles Time, sql.Date und util.Date specially.
   * If o is a byte or char array, a string is formed using the usual
   * string constructors.
   *
   * @returns a string representation of the object or null
   */
  public String convertNativeToString(tsColumn coldef, Object o) throws tinySQLException
  {
    return ParserUtils.convertToString(o);
  }

  /**
   * returns a object into a boolean. Returns true, if the Object is a
   * boolean value containing Boolean.TRUE or a string containing "true"
   *
   * @returns Boolean.TRUE or Boolean.FALSE
   */
  public Boolean convertNativeToBoolean(tsColumn coldef, Object o) throws tinySQLException
  {
    return ParserUtils.convertToBoolean(o);
  }

  /**
   * Converts a Object into a big decimal. If the Object is a date, its
   * time value is used to create the number. If the Object is a Number,
   * the double value is used. As last try the Object is converted into a
   * String and this String is parsed into a BigDecimal. If this failed,
   * BidgDecimal (0) is returned.
   *
   */
  public BigDecimal convertNativeToNumber(tsColumn coldef, Object o) throws tinySQLException
  {
    return ParserUtils.convertToNumber(o);
  }

  /**
   * Converts a Object into a Integer. Calls convertNativeToNumber by default.
   * Override this function if you want to handle integers specially.
   */
  public Integer convertNativeToInteger(tsColumn coldef, Object o) throws tinySQLException
  {
    BigDecimal bd = convertNativeToNumber(coldef, o);
    return new Integer(bd.intValue());
  }

  /**
   * Converts a Object into a BigInteger. Calls convertNativeToNumber by default.
   * Override this function if you want to handle big integers specially.
   */
  public BigInteger convertNativeToBigInteger(tsColumn coldef, Object o) throws tinySQLException
  {
    BigDecimal bd = convertNativeToNumber(coldef, o);
    return bd.toBigInteger();
  }

  /**
   * Converts a Object into a Short. Calls convertNativeToNumber by default.
   * Override this function if you want to handle small integers specially.
   */
  public Short convertNativeToShort(tsColumn coldef, Object o) throws tinySQLException
  {
    BigDecimal bd = convertNativeToNumber(coldef, o);
    return new Short(bd.shortValue());
  }

  /**
   * Converts a Object into a Byte. Calls convertNativeToNumber by default.
   * Override this function if you want to handle tiny integers specially.
   */
  public Byte convertNativeToByte(tsColumn coldef, Object o) throws tinySQLException
  {
    BigDecimal bd = convertNativeToNumber(coldef, o);
    return new Byte(bd.byteValue());
  }

  /**
   * Converts a Object into a Double. Calls convertNativeToNumber by default.
   * Override this function if you want to handle double values specially.
   */
  public Double convertNativeToDouble(tsColumn coldef, Object o) throws tinySQLException
  {
    BigDecimal bd = convertNativeToNumber(coldef, o);
    return new Double(bd.doubleValue());
  }

  /**
   * Converts a Object into a Float. Calls convertNativeToNumber by default.
   * Override this function if you want to handle float values specially.
   */
  public Float convertNativeToFloat(tsColumn coldef, Object o) throws tinySQLException
  {
    BigDecimal bd = convertNativeToNumber(coldef, o);
    return new Float(bd.floatValue());
  }

  /**
   * Converts a Object into a java.sql.Date. If the Object is a Date,
   * Number or a String of the form yyyy-MM-dd, the conversion will
   * return a new Date, else null is returned.
   *
   * @returns the converted date or null
   */
  public Date convertNativeToDate(tsColumn coldef, Object o) throws tinySQLException
  {
    return ParserUtils.convertToDate(o);
  }

  /**
   * Converts a Object into a java.sql.Time. If the Object is a Date,
   * Number or a String of the form hh:mm:ss, the conversion will
   * return a new Time object, else null is returned.
   *
   * @returns the converted time or null
   */
  public Time convertNativeToTime(tsColumn coldef, Object o) throws tinySQLException
  {
    return ParserUtils.convertToTime(o);
  }

  /**
   * Converts a Object into a java.sql.Timestamp. If the Object is a Date,
   * Number or a String of the form yyyy-MM-dd hh:mm:ss, the conversion will
   * return a new Timestamp, else null is returned.
   *
   * @returns the converted timestamp or null
   */
  public Timestamp convertNativeToTimestamp(tsColumn coldef, Object o) throws tinySQLException
  {
    return ParserUtils.convertToTimestamp(o);
  }

  /**
   * Converts a native value into a null value
   *
   * @returns the converted timestamp or null
   */
  public Object convertNativeToNull(tsColumn coldef) throws tinySQLException
  {
    return null;
  }

  /**
   * Converts a native value into a JDBC value. The type of cnversion is detected
   * by looking at the columndefinition. If the type of the is unknown to the
   * conversion system, the value is returned unchanged.
   *
   * @returns the converted value or null
   */
  public Object convertNativeToJDBC(tsColumn coldef, Object o) throws tinySQLException
  {
    int type = coldef.getType();
    if (o == null)
    {
      return convertNativeToNull(coldef);
    }

    try
    {

      switch (type)
      {
        case Types.BIT:
          return convertNativeToBoolean(coldef, o);

        case Types.TINYINT:
          return convertNativeToByte(coldef, o);

        case Types.SMALLINT:
          return convertNativeToShort(coldef, o);

        case Types.INTEGER:
          return convertNativeToInteger(coldef, o);

        case Types.BIGINT:
          return convertNativeToBigInteger(coldef, o);

        case Types.FLOAT:
          return convertNativeToFloat(coldef, o);

        case Types.REAL:
          return convertNativeToDouble(coldef, o);

        case Types.DOUBLE:
          return convertNativeToDouble(coldef, o);

        case Types.NUMERIC:
          return convertNativeToNumber(coldef, o);

        case Types.DECIMAL:
          return convertNativeToNumber(coldef, o);

        case Types.CHAR:
          return convertNativeToString(coldef, o);

        case Types.VARCHAR:
          return convertNativeToString(coldef, o);

        case Types.LONGVARCHAR:
          return convertNativeToString(coldef, o);

        case Types.DATE:
          return convertNativeToDate(coldef, o);

        case Types.TIME:
          return convertNativeToTime(coldef, o);

        case Types.TIMESTAMP:
          return convertNativeToTimestamp(coldef, o);

          // if we don't know how to handle a object, return it unchanged
        default:
          Log.warn("Unknown type, returning object unchanged.");

          return o;
      }
    }
    catch (Exception e)
    {
      Log.error("Convert Failed for column " + coldef, e);
    }
    return o;
  }

  /**
   * Converts a JDBC value into a native value. The type of cnversion is detected
   * by looking at the columndefinition. All values are normalized using the
   * convertNativeToJDBC conversion. If the type of the is unknown to the
   * conversion system, the value is returned unchanged.
   *
   * @returns the converted value or null
   */
  public Object convertJDBCToNative(tsColumn coldef, Object o) throws tinySQLException
  {
    int type = coldef.getType();
    if (o == null)
      return convertNullToNative(coldef);

    switch (type)
    {
      case Types.BIT:
        return convertBooleanToNative(coldef, convertNativeToBoolean(coldef, o));

      case Types.TINYINT:
        return convertByteToNative(coldef, convertNativeToByte(coldef, o));

      case Types.SMALLINT:
        return convertShortToNative(coldef, convertNativeToShort(coldef, o));

      case Types.INTEGER:
        return convertIntegerToNative(coldef, convertNativeToInteger(coldef, o));

      case Types.BIGINT:
        return convertBigIntegerToNative(coldef, convertNativeToBigInteger(coldef, o));

      case Types.FLOAT:
        return convertFloatToNative(coldef, convertNativeToFloat(coldef, o));

      case Types.REAL:
        return convertDoubleToNative(coldef, convertNativeToDouble(coldef, o));

      case Types.DOUBLE:
        return convertDoubleToNative(coldef, convertNativeToDouble(coldef, o));

      case Types.NUMERIC:
        return convertNumberToNative(coldef, convertNativeToNumber(coldef, o));

      case Types.DECIMAL:
        return convertNumberToNative(coldef, convertNativeToNumber(coldef, o));

      case Types.CHAR:
        return convertStringToNative(coldef, convertNativeToString(coldef, o));

      case Types.VARCHAR:
        return convertStringToNative(coldef, convertNativeToString(coldef, o));

      case Types.LONGVARCHAR:
        return convertStringToNative(coldef, convertNativeToString(coldef, o));

      case Types.DATE:
        return convertDateToNative(coldef, convertNativeToDate(coldef, o));

      case Types.TIME:
        return convertTimeToNative(coldef, convertNativeToTime(coldef, o));

      case Types.TIMESTAMP:
        return convertTimestampToNative(coldef, convertNativeToTimestamp(coldef, o));

      default:
        return o;
    }
  }

  /**
   * @returns <code>null</code> as default notion of a native null value.
   */
  public Object convertNullToNative(tsColumn coldef) throws tinySQLException
  {
    return null;
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertNumberToNative(tsColumn coldef, Number o) throws tinySQLException
  {
    return o;
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertBigIntegerToNative(tsColumn coldef, BigInteger o) throws tinySQLException
  {
    return convertNumberToNative(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertIntegerToNative(tsColumn coldef, Integer o) throws tinySQLException
  {
    return convertNumberToNative(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertShortToNative(tsColumn coldef, Short o) throws tinySQLException
  {
    return convertNumberToNative(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertByteToNative(tsColumn coldef, Byte o) throws tinySQLException
  {
    return convertNumberToNative(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertFloatToNative(tsColumn coldef, Float o) throws tinySQLException
  {
    return convertNumberToNative(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertDoubleToNative(tsColumn coldef, Double o) throws tinySQLException
  {
    return convertNumberToNative(coldef, o);
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertStringToNative(tsColumn coldef, String o) throws tinySQLException
  {
    return o;
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertDateToNative(tsColumn coldef, Date o) throws tinySQLException
  {
    return o;
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertTimestampToNative(tsColumn coldef, Timestamp o) throws tinySQLException
  {
    return o;
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertTimeToNative(tsColumn coldef, Time o) throws tinySQLException
  {
    return o;
  }

  /**
   * @returns the value of <code>o</code> as this class does not know any
   * details about the database format used.
   */
  public Object convertBooleanToNative(tsColumn coldef, Boolean o) throws tinySQLException
  {
    return o;
  }
}
