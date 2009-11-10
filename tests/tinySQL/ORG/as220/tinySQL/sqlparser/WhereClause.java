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
import ORG.as220.tinySQL.tsColumn;
import ORG.as220.tinySQL.tsRow;

import java.util.Vector;

/**
 * This is the WhereClause. It is a rather primitive Class for now,
 * but later it will support a better analysis of tabledata. The
 * Goal for this class is to check table-rows for a match, before
 * all data from all tables is known.
 */
public class WhereClause
{
  private LValue expression;
  private Vector tableColumns;
  private ConditionalStatement parent;
  private boolean lastReturnValue;
  private boolean evaluatedOnce;

  // Key = tsColumn, Value= result_object
  private Object[] cache;
  private int[] cachePos;

  public WhereClause(ConditionalStatement parent)
  {
    expression = new StaticValue(Boolean.TRUE);
    tableColumns = new Vector();
    this.parent = parent;
  }

  public void setExpression(LValue ex)
      throws tinySQLException
  {
    if (ex == null)
      throw new NullPointerException();

    Vector cols = ParserUtils.resolveTableColumns(
        ex,
        ParserUtils.buildVector(parent.getTables())
    );

    tableColumns = new Vector(cols);

    expression = ex;
    cache = null;
  }

  public LValue getExpression()
  {
    return expression;
  }

  public boolean isMatch(tsRow matchRow) throws tinySQLException
  {
    boolean reevaluate = false;

    if (tableColumns.size() > 0)
    {
      /**
       * fill cache on start needed
       */
      if (cache == null)
      {
        cache = new Object[tableColumns.size()];
        cachePos = new int[tableColumns.size()];

        int size = cache.length;
        for (int i = 0; i < size; i++)
        {
          tsColumn col = (tsColumn) tableColumns.elementAt(i);
          int idx = matchRow.findColumn(col.getPhysicalName());

          cachePos[i] = idx;
          cache[i] = matchRow.get(idx);
        }
        reevaluate = true;
      }
      else
      {
        int size = cache.length;
        for (int i = 0; i < size; i++)
        {
          int idx = cachePos[i];
          Object myObject = cache[i];
          Object rowObject = matchRow.get(idx);

          // O1 and O2 should be the same type, so try an equals
          if (rowObject != null)
          {
            if (myObject != null)
            {

              if ((rowObject != myObject) && (rowObject.equals(myObject) == false))
              {
                cache[i] = rowObject;
                reevaluate = true;
              }
            }
            else
            {
              cache[i] = rowObject;
              reevaluate = true;
            }
          }
        }
      }
    }
    else
    {
      if (!evaluatedOnce)
      {
        reevaluate = true;
      }
    }
    if (reevaluate)
    {
      evaluatedOnce = true;
      Boolean b = ParserUtils.convertToBoolean(expression.evaluate(matchRow));
      lastReturnValue = b.booleanValue();
    }
    return lastReturnValue;
  }

  public String toString()
  {
    return "WHERE " + expression.getName();
  }

  public Vector getColumns()
  {
    return new Vector(tableColumns);
  }

  /**
   * returns the parameters used in the expressions of the whereClause.
   */
  public Vector getParameters()
  {
    Vector v = new Vector();
    ParserUtils.getParameterElements(v, getExpression());
    return v;
  }

}
