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

import java.math.BigDecimal;

/**
 * Container and base class for all Operators.
 *
 * IN and LIKE are not implemented here or in the parser.
 * BETWEEN and NBETWEEN are not implemented in the parser
 */
public class Operator
{
  public static Operator AND;
  public static Operator OR;
  public static Operator ADD;
  public static Operator SUB;
  public static Operator MULT;
  public static Operator DIV;
  public static Operator EQUAL;
  public static Operator NEQUAL;
  public static Operator LEQUAL;
  public static Operator GEQUAL;
  public static Operator GREATER;
  public static Operator LESSER;
  public static Operator BETWEEN;
  public static Operator NBETWEEN;
  public static Operator LIKE;
  public static Operator IN;

  static
  {
    Operator.init();
  }

  private Operator()
  {
  }

  private static void init()
  {
    AND = new AndOperator();
    OR = new OrOperator();

    ADD = new AddOperator();
    SUB = new SubtractOperator();
    MULT = new MultiplyOperator();
    DIV = new DivideOperator();

    EQUAL = new EqualOperator();
    NEQUAL = new NotEqualOperator();
    LEQUAL = new LesserEqualOperator();
    GEQUAL = new GreaterEqualOperator();
    LESSER = new LesserOperator();
    GREATER = new GreaterOperator();

    BETWEEN = new BetweenOperator();
    NBETWEEN = new NotBetweenOperator();
    LIKE = new LikeOperator();
    IN = new InOperator();
  }

  public Object evaluate(Object ob1, Object ob2) throws tinySQLException
  {
    throw new tinySQLException("This operator does not implement the evaluate method");
  }

  private static class AndOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      Boolean b1 = ParserUtils.convertToBoolean(ob1);
      Boolean b2 = ParserUtils.convertToBoolean(ob2);
      return new Boolean(b1.booleanValue() && b2.booleanValue());
    }

    public String toString()
    {
      return " AND ";
    }

    public int getLevel()
    {
      return 0;
    }
  }

  private static class OrOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      Boolean b1 = ParserUtils.convertToBoolean(ob1);
      Boolean b2 = ParserUtils.convertToBoolean(ob2);
      return new Boolean(b1.booleanValue() || b2.booleanValue());
    }

    public String toString()
    {
      return " OR ";
    }

    public int getLevel()
    {
      return 0;
    }
  }

  private static class AddOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      BigDecimal d1 = ParserUtils.convertToNumber(ob1);
      BigDecimal d2 = ParserUtils.convertToNumber(ob2);
      return d1.add(d2);
    }

    public String toString()
    {
      return " + ";
    }

    public int getLevel()
    {
      return 2;
    }
  }

  private static class SubtractOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      BigDecimal d1 = (BigDecimal) ParserUtils.convertToNumber(ob1);
      BigDecimal d2 = (BigDecimal) ParserUtils.convertToNumber(ob2);
      return d1.subtract(d2);
    }

    public String toString()
    {
      return " - ";
    }

    public int getLevel()
    {
      return 2;
    }
  }

  private static class MultiplyOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      BigDecimal d1 = (BigDecimal) ParserUtils.convertToNumber(ob1);
      BigDecimal d2 = (BigDecimal) ParserUtils.convertToNumber(ob2);
      return d1.multiply(d2);
    }

    public String toString()
    {
      return " * ";
    }

    public int getLevel()
    {
      return 3;
    }
  }

  private static class DivideOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      BigDecimal d1 = (BigDecimal) ParserUtils.convertToNumber(ob1);
      BigDecimal d2 = (BigDecimal) ParserUtils.convertToNumber(ob2);
      return d1.divide(d2, BigDecimal.ROUND_HALF_UP);
    }

    public String toString()
    {
      return " / ";
    }

    public int getLevel()
    {
      return 3;
    }
  }

  private static class EqualOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      return new Boolean(compareTo(ob1, ob2) == 0);
    }

    public String toString()
    {
      return " == ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class NotEqualOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      return new Boolean(compareTo(ob1, ob2) != 0);
    }

    public String toString()
    {
      return " <> ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class GreaterOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      return new Boolean(compareTo(ob1, ob2) > 0);
    }

    public String toString()
    {
      return " > ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class GreaterEqualOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      return new Boolean(compareTo(ob1, ob2) >= 0);
    }

    public String toString()
    {
      return " >= ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class LesserOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      return new Boolean(compareTo(ob1, ob2) < 0);
    }

    public String toString()
    {
      return " < ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class LesserEqualOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      return new Boolean(compareTo(ob1, ob2) <= 0);
    }

    public String toString()
    {
      return " <= ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class NotBetweenOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2) throws tinySQLException
    {
      Boolean retval = (Boolean) BETWEEN.evaluate(ob1, ob2);
      if (retval.equals(Boolean.TRUE))
        return Boolean.FALSE;

      return Boolean.TRUE;
    }

    public String toString()
    {
      return " NOT BETWEEN ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  private static class BetweenOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2)
    {
      Object[] arry = (Object[]) ob2;
      Object obtween1 = arry[0];
      Object obtween2 = arry[1];

      int result1 = compareTo(ob1, obtween1);
      int result2 = compareTo(ob1, obtween2);
      return new Boolean(result1 > 0 && result2 < 0);
    }

    public String toString()
    {
      return " BETWEEN ";
    }

    public int getLevel()
    {
      return 1;
    }
  }


  /**
   * How to create an like Operator which does not consume too many resources?
   * Use regexp..
   */
  private static class LikeOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2) throws tinySQLException
    {
      Object[] arry = (Object[]) ob2;
      Object obtween1 = arry[0];
      Object obtween2 = arry[1];
      throw new tinySQLException("Not yet implemented: LIKE");
    }

    public String toString()
    {
      return " LIKE ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  /**
   * How to create an in Operator? We need some array or subselect ...
   */
  private static class InOperator extends Operator
  {
    public Object evaluate(Object ob1, Object ob2) throws tinySQLException
    {
      Object[] arry = (Object[]) ob2;
      Object obtween1 = arry[0];
      Object obtween2 = arry[1];

      throw new tinySQLException("Not yet implemented: IN");
    }

    public String toString()
    {
      return " LIKE ";
    }

    public int getLevel()
    {
      return 1;
    }
  }

  public final int compareTo(Object op1, Object op2)
  {
    if (op1 == null && op2 == null)
    {
      return 0;
    }
    if (op1 == null)
    {
      return -1;
    }
    if (op2 == null)
    {
      return 1;
    }


    if ((op1 instanceof Comparable) && (op2 instanceof Comparable))
    {
      Comparable cmp1 = (Comparable) op1;
      if (op1 instanceof Number && op2 instanceof Number)
      {
        return cmp1.compareTo(op2);
      }
      if (op1 instanceof Number && op2 instanceof String)
      {
        try
        {
          String s = (String) op2;
          return cmp1.compareTo(new BigDecimal(s.trim()));
        }
        catch (Exception e)
        {
        }
      }

      if (op1 instanceof String && op2 instanceof Number)
      {
        try
        {
          Comparable cmp2 = (Comparable) op2;
          String s = (String) op1;
          return cmp2.compareTo(new BigDecimal(s.trim()));
        }
        catch (Exception e)
        {
        }
      }

      try
      {
        return cmp1.compareTo(op2);
      }
      catch (Exception e)
      {
      }
    }

    return op1.toString().compareTo(op2.toString());
  }

  public int getLevel()
  {
    return 0;
  }
}
