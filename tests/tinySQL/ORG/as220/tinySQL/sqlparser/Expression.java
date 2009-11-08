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
import ORG.as220.tinySQL.util.Log;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Vector;

/**
 * LValue expression: evaluates to an Object
 *
 * Expression := expression OPERATOR LValue
 */
public class Expression implements LValue
{
  private LValue value;
  private Vector addValues;
  private boolean transformed;

  /**
   * The value container is used to concat a previous (base)
   * expression with an operator and an other expression.
   *
   * This is used in the addValues vector.
   */
  private class ValueContainer
  {
    public Operator op;
    public LValue value;

    public ValueContainer(Operator op, LValue value)
    {
      this.op = op;
      this.value = value;
    }
  }

  /**
   * Create a new expression using the LValue value as base for
   * further operations.
   */
  public Expression(LValue value)
  {
    addValues = new Vector();
    this.value = value;
  }

  /**
   * add a new expression to the container. If this expression does
   * not contain ColumnValues or ParameterValues it is safe to evaluate the
   * expression now,
   * the value of the expression cannot change during statement execution.
   */
  public void add(Operator op, LValue value)
  {
    Vector v = new Vector();
    ParserUtils.getColumnElements(v, value);
    ParserUtils.getParameterElements(v, value);

    if ((value instanceof StaticValue) == false)
    {
      try
      {
        if (v.size() == 0)
        {
          addValues.add(new ValueContainer(op, new StaticValue(value.evaluate(null))));
          return;
        }
      }
      catch (Exception e)
      {
        Log.error("Exception in optimize", e);
        // ignore the exceptions
      }
    }
    addValues.add(new ValueContainer(op, value));
  }

  /**
   * Insert an LValue + operator at the beginning of the expression
   */
  private void insert(LValue lv, Operator op)
  {
    ValueContainer vc = new ValueContainer(op, value);
    addValues.add(0, vc);
    value = lv;
  }

  /**
   * Evaluates this expression by evaluating all child-LValues and
   * compute a result using the assigned operators.
   *
   * Be aware: currently there is no support for operator precedence
   * implemented. Currently a multiplication or division has the same
   * priority as an addition. Use parentheses to manualy add precedences.
   *
   */
  public Object evaluate(tsRow rowdata) throws tinySQLException
  {
    if (transformed == false)
    {
      transformExpression();
      transformed = true;
    }
    Object retval = value.evaluate(rowdata);
    for (int i = 0; i < addValues.size(); i++)
    {
      ValueContainer valcon = (ValueContainer) addValues.get(i);
      retval = valcon.op.evaluate(retval, valcon.value.evaluate(rowdata));
    }
    return retval;
  }

  /**
   * Is this expression a wrapper to simulate parantheses?
   */
  public boolean isWrapper()
  {
    return addValues.size() == 0;
  }

  /**
   * returns the name of this expression. Expression have no name, so
   * construct a representation of this expression as a mathematical or
   * boolean term.
   */
  public String getName()
  {
    Log.debug("About to transform: " + transformed);
    if (transformed == false)
    {
      Log.debug("About to transform");
      transformExpression();
      transformed = true;
    }

    StringBuffer b = new StringBuffer(getValueName(value));

    for (int i = 0; i < addValues.size(); i++)
    {
      ValueContainer valcon = (ValueContainer) addValues.get(i);
      b.append(valcon.op.toString());
      b.append(getValueName(valcon.value));
    }
    return b.toString();
  }

  /**
   * Helperfunction for getName() which wraps expressions into parentheses
   * to show that expressions are evaluated before a local operator is
   * applied.
   */
  private String getValueName(LValue val)
  {
    if (val instanceof Expression)
    {
      return ("(" + val.getName() + ")");
    }
    else
      return val.getName();
  }

  /**
   * returns all children of this expression as enumeration. The Enumeration
   * will contain LValues.
   */
  public Enumeration getChildren()
  {
    Vector v = new Vector();
    v.add(value);
    for (int i = 0; i < addValues.size(); i++)
    {
      ValueContainer valcon = (ValueContainer) addValues.get(i);
      v.add(valcon.value);
    }
    return v.elements();
  }

  /**
   * returns the number of children in this expression
   */
  public int getChildCount()
  {
    return addValues.size();
  }

  /**
   * Transform the term to apply some optimisations. This function
   * is currently empty.
   */
  protected void transformExpression()
  {
    // do transformation
    // find highest Operator-level
    int level = 0;
    for (int i = 0; i < addValues.size(); i++)
    {
      ValueContainer vc = (ValueContainer) addValues.get(i);
      if (vc.op.getLevel() > level)
      {
        level = vc.op.getLevel();
      }
    }

    // search for the operator usage. Unused operator levels need not to be processed.
    boolean[] levelsUsed = new boolean[level + 1];
    for (int i = 0; i < addValues.size(); i++)
    {
      ValueContainer vc = (ValueContainer) addValues.get(i);
      levelsUsed[vc.op.getLevel()] = true;
    }

    // check how many different levels are there. If there is only one level,
    // do not transform anymore
    int levelCount = 0;
    int lastLevel = 0;
    for (int i = level; i > -1; i--)
    {
      if (levelsUsed[i] == true)
      {
        levelCount++;
        lastLevel = i;
      }
    }
    if (levelCount < 2) return;

    // finally transform the expression, the last level is the root level, no transform or
    // whole expression would be stacked into a subexpression and cause an endless loop
    for (int i = level; i > lastLevel; i--)
    {
      if (levelsUsed[i] == true)
        transformExpression(i);
    }
  }

  private void transformExpression(int level)
  {
    LValue lv = null;
    for (int i = addValues.size() - 1; i > -1; i--)
    {
      if (getOperator(i).getLevel() >= level)
      {
        mergeExpressions(i);
      }
    }
  }

  /**
   * Merges the expression at (position+1) with the previous expression in the list.
   * So if expression = "a + b * c" and mergeExpressions (1) will
   * create the expression (a + (b * c)). MergeExpression (0) will create (a + b) * c.
   */
  private void mergeExpressions(int pos)
  {
    ValueContainer vc = (ValueContainer) addValues.get(pos);
    LValue rightExpr = getLValue(pos + 1);
    if (rightExpr instanceof Expression == false)
    {
      rightExpr = new Expression(rightExpr);
    }
    Expression ex = (Expression) rightExpr;
    LValue leftExpr = getLValue(pos);
    Operator op = getOperator(pos);
    ex.insert(leftExpr, op);
    addValues.remove(pos);
    setLValue(pos, ex);
  }

  private Operator getOperator(int pos)
  {
    ValueContainer vc = (ValueContainer) addValues.get(pos);
    return vc.op;
  }

  /**
   * get the lvalue on position <code>pos</code>
   */
  private LValue getLValue(int pos)
  {
    if (pos == 0) return value;

    ValueContainer vc = (ValueContainer) addValues.get(pos - 1);
    return vc.value;
  }

  private void setLValue(int pos, LValue lval)
  {
    if (pos == 0)
    {
      value = lval;
      return;
    }

    ValueContainer vc = (ValueContainer) addValues.get(pos - 1);
    vc.value = lval;
  }

  public String toString()
  {
    return getName();
  }

  public static void main(String[] args)
  {
    DriverManager.setLogWriter(new PrintWriter(System.out));
    Expression ex = new Expression(new StaticValue(new Integer(10)));
    ex.add(Operator.ADD, new StaticValue(new Integer(15)));
    ex.add(Operator.MULT, new StaticValue(new Integer(5)));
    ex.add(Operator.SUB, new StaticValue(new Integer(2)));
    ex.add(Operator.DIV, new StaticValue(new Integer(19)));

    System.out.println(ex);
  }
}
