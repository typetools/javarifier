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

/**
 * A column definition holds a single definition for a new column used in
 * CREATE TABLE.
 *
 * "(" name type ["(" size [ "," decimals ] ")" ] ")"
 */
public class ColumnDefinition
{
  private String name;
  private int type;
  private int size;
  private int decimals;
  private boolean nullable;

  /**
   * creates a new column definition for a column with the specified name.
   * The definition has to be valid to the underlying database, or a database
   * error is thrown on execution.
   */
  public ColumnDefinition(String name)
  {
    this.name = name;
  }

  /**
   * returns the defined name for this column.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Alters the name of the column to the give java.sql.Types constant.
   * If the database does not support this type, a error will be thrown
   * on execution.
   */
  public void setType(int type)
  {
    this.type = type;
  }

  /**
   * returns the data type of this column.
   */
  public int getType()
  {
    return type;
  }

  /**
   * sets the size of the column. Decimals are set to 0. If size is
   * negative, a tinySQLException is thrown.
   */
  public void setSize(int size) throws tinySQLException
  {
    if (size <= 0)
      throw new tinySQLException("Size must be > 0");

    this.size = size;
    this.decimals = 0;
  }

  /**
   * sets the size of the column. Decimals are set to the specified value.
   * If size is negative or decimals is not smaller than size, a
   * tinySQLException is thrown.
   */
  public void setSize(int size, int decimals) throws tinySQLException
  {
    if (size <= decimals)
      throw new tinySQLException("Decimals may not be greater or equal to size");

    if (size <= 0)
      throw new tinySQLException("Size must be > 0");

    this.size = size;
    this.decimals = decimals;
  }

  /**
   * returns the size of this column.
   */
  public int getSize()
  {
    return size;
  }

  /**
   * returns the decimal places used in this column.
   */
  public int getDecimals()
  {
    return decimals;
  }

  /**
   * declares whether this column can contain null values. This property
   * defaults to false.
   */
  public void setNullable(boolean b)
  {
    this.nullable = b;
  }

  /**
   * returns whether this column can contain null values.
   */
  public boolean isNullable()
  {
    return nullable;
  }

  /**
   * forms a tsColumn object from this column definition.
   */
  public tsColumn getColumn()
  {
    tsColumn myCol = new tsColumn(getName());
    if (getSize() > 0)
    {
      myCol.setSize(getSize(), getDecimals());
    }
    myCol.setType(getType());
    myCol.setNullable(isNullable());
    return myCol;
  }

  /**
   * returns a string representation of this object.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("( ");
    b.append(name);
    b.append("; type= ");
    b.append(ParserUtils.typeToLiteral(type));
    b.append("; size= ");
    if (size == 0)
    {
      b.append("DEFAULT )");
    }
    else
    {
      b.append(size);
      b.append("; decimals= ");
      b.append(decimals);
      b.append(") ");
    }
    return b.toString();
  }
}
