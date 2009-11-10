/*
 *
 * tsColumn.java
 * Column Object for tinySQL.
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

import ORG.as220.tinySQL.sqlparser.LValue;

/**
 * object to hold a column definition used in sql-statements<br>
 *
 */
public class tsColumn
{
  /**
   * Constant value for data columns. Data columns retrieve their data
   * from physical tables.
   */
  public static final int COL_DATA = 0;

  /**
   * Constant value for expression columns. Expression columns retrieve
   * their data by evaluating the supplied expression
   */
  public static final int COL_EXPR = 1;

  /**
   * Invalid columns are not valid for use in rows. You may need such columns
   * when creating new tables.
   */
  public static final int COL_INVALID = -1;


  private LValue expression;       // If this column is an expression
  private String physName = null;      // the column's real name in the table
  private String displayName = null;   // the column's alias name
  private String fqname = null;
  private int datatype = -1;           // the column's type
  // dBase types:
  // 'C' Char (max 254 bytes)
  // 'N' '-.0123456789' (max 19 bytes)
  // 'L' 'YyNnTtFf?' (1 byte)
  // 'M' 10 digit .DBT block number
  // 'D' 8 digit YYYYMMDD
  private int decimalPlaces = -1;  // not yet supported
  private Object defaultVal = null;// a default value; physical rows will be initialized with that value
  private boolean notNull = false; // not yet supported, true: should be NOT NULL

  private int bytePos = -1;       // internal use - byte position in table

  private int tablePos = -1;       // Position of column in table
  private int resultPos = -1;
  private int size = -1;         // the column's size
  private int coltype = -1;

  private tinySQLTableView table = null; // the table which "owns" the column

  /**
   * Constructs a new data column. The table is assigned to this
   * column and will be used to retrieve column data. The display name
   * is set to the given alias.
   */
  public tsColumn(tinySQLTableView table, String col, String alias)
  {
    if (col == null)
      throw new NullPointerException("Name must not be null");

    if (col.indexOf('.') != -1) throw new IllegalArgumentException();

    this.table = table;
    this.physName = col;
    if (alias == null)
    {
      this.displayName = col;
    }
    else
    {
      this.displayName = alias;
    }
    this.coltype = COL_DATA;
  }

  /**
   * Constructs a new data column. The table is assigned to this
   * column and will be used to retrieve column data. The display name
   * will be set to the column name
   */
  public tsColumn(tinySQLTableView table, String col)
  {
    this(table, col, null);
  }

  /**
   * Constructs a new expression column. The expression is assigned to this
   * column and will be used to compute column data. The display name
   * will be set to the column name
   */
  public tsColumn(String col, LValue ex)
  {
    if (col == null)
      throw new NullPointerException("Name must not be null");

    this.expression = ex;
    this.physName = col;
    this.displayName = col;
    this.coltype = COL_EXPR;
  }

  /**
   * Constructs a tsColumn object. Needed for CreateTable. The column object
   * will crash any query and will cause errors. Handle with care!
   *
   * @param s the column name
   */
  public tsColumn(String col)
  {
    if (col == null)
      throw new NullPointerException("Name must not be null");

    this.physName = col;
    this.displayName = col;
    this.coltype = COL_INVALID;
  }

  /**
   * Constructs a tsColumn object by copying the fields from col.
   *
   * @param s the column name
   */
  public tsColumn(tsColumn col)
  {
    if (col == null)
      throw new NullPointerException("Name was null?");

    physName = col.physName;
    displayName = col.displayName;
    coltype = col.coltype;
    datatype = col.datatype;
    decimalPlaces = col.decimalPlaces;
    defaultVal = col.defaultVal;
    notNull = col.notNull;
    size = col.size;
    table = col.table;
    tablePos = col.tablePos;
    bytePos = col.bytePos;
    resultPos = col.resultPos;
  }

  /**
   * Constructs a tsColumn object by copying the fields from col
   * and setting the table for this column to <code>table</code>.
   */
  public tsColumn(tinySQLTableView table, tsColumn col)
  {
    this(col);
    this.table = table;
  }

  /**
   * Adjusts all fields by copying the fields from col.
   * Table, names, expression or columntype are not affected by this method.
   */
  public void setAll(tsColumn col)
  {
    coltype = col.coltype;
    datatype = col.datatype;
    decimalPlaces = col.decimalPlaces;
    defaultVal = col.defaultVal;
    notNull = col.notNull;
    size = col.size;
    tablePos = col.tablePos;
    bytePos = col.bytePos;
    resultPos = col.resultPos;
  }

  /**
   * returns the physical name of this column. If this column
   * is a datacolumn the physical name is a fieldname in the assigned
   * table.
   */
  public String getPhysicalName()
  {
    return physName;
  }

  /**
   * returns the display name of this column. The display name is
   * an alias for the column's name.
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * returns the full qualified column name of this column.
   * If this column is a data column, then the full qualified column name
   * is "tablename.columnName".
   */
  public String getFQName()
  {
    return formColname(physName, table);
  }

  /**
   * Set the phsyical name of the column. Be aware that the type
   * of this column changes to COL_INVALID.
   */
  public void setPhysicalName(String name)
  {
    this.physName = name;
    fqname = null;
    coltype = COL_INVALID;
  }

  /**
   * returns the default value defined by the column definition in the
   * database. If no default value is defined, <code>null</code> will
   * be returned.
   */
  public Object getDefaultValue()
  {
    return defaultVal;
  }

  /**
   * sets the default value of this column.
   */
  public void setDefaultValue(Object value)
  {
    this.defaultVal = value;
  }

  /**
   * sets the datatype of the column. The datatype must be a valid
   * java.sql.Types constant.
   */
  public void setType(int type)
  {
    this.datatype = type;
  }

  /**
   * returns the datatype of the column. The datatype is a valid
   * java.sql.Types constant.
   */
  public int getType()
  {
    return datatype;
  }

  /**
   * return true, if this column is nullable, false otherwise
   */
  public boolean isNullable()
  {
    return (!notNull);
  }

  /**
   * define, whether this column is nullable.
   */
  public void setNullable(boolean notNull)
  {
    this.notNull = notNull;
  }

  /**
   * set the size of this column. The size must not be negative.
   */
  public void setSize(int size)
  {
    if (size < 0)
      throw new IllegalArgumentException("Size must be >= 0");

    this.size = size;
    this.decimalPlaces = 0;
  }

  /**
   * set the size and the number decimals of this column. The size must
   * not be negative and the number of decimals must be smaller than the
   * given size.
   */
  public void setSize(int size, int decimals)
  {
    if (size < 0)
      throw new IllegalArgumentException("Size must be >= 0");

    if (decimals >= size)
      throw new IllegalArgumentException("Decimals [" + decimals + "] must be < than size [" + size + "]");

    this.size = size;
    this.decimalPlaces = decimals;
  }

  /**
   * returns the size of this column
   */
  public int getSize()
  {
    return size;
  }

  /**
   * returns the number of decimal places in this column
   */
  public int getDecimalPlaces()
  {
    return decimalPlaces;
  }

  /**
   * sets the position of this column in a arbitary resultset.
   * Do not change this value unless you know what you are doing, as
   * this method is able to destroy your query and your tables.
   */
  public void setResultPosition(int pos)
  {
    resultPos = pos;
  }

  /**
   * returns the resultset position of this column.
   */
  public int getResultPosition()
  {
    return resultPos;
  }

  /**
   * sets the byte position of this column in the source table.
   * Do not change this value unless you know what you are doing, as
   * this method is able to destroy your query and your tables.
   */
  public void setBytePosition(int pos)
  {
    bytePos = pos;
  }

  /**
   * returns the byte position of this column.
   */
  public int getBytePosition()
  {
    return bytePos;
  }

  /**
   * returns the position of this column in the given table.
   * if this column is not a datatable, null is returned.
   */
  public int getTablePosition()
  {
    if (table == null)
    {
      return -1;
    }
    if (tablePos == -1)
    {
      tablePos = table.findColumn(getPhysicalName());
    }
    return tablePos;
  }

  /**
   * sets the position of this column in the given table.
   * if this column is not a datatable, this method has no effect.
   */
  public void setTablePosition(int pos)
  {
    tablePos = pos;
  }

  /**
   * returns a stringrepresentation of the column.
   */
  public String toString()
  {
    StringBuffer b = new StringBuffer();
    b.append("tsColumn:={FQName =");
    b.append(getFQName());
    b.append(", display_name=");
    b.append(displayName);
    b.append(", tablepos=" + getTablePosition());
    b.append(", bytepos=" + getBytePosition());
    b.append(", size= (" + size);
    b.append("," + decimalPlaces + ")");
    b.append(", type=" + datatype);
    b.append(", coltype=" + coltype);
    b.append("}");
    return b.toString();
  }

  /**
   * returns a the columntype of this column.
   * @returns one of COL_DATA, COL_EXPR or COL_INVALID.
   */
  public int getColumnType()
  {
    return coltype;
  }

  /**
   * returns the assigned table or null if this column has
   * no table.
   */
  public tinySQLTableView getTable()
  {
    return table;
  }

  /**
   * returns the assigned expression or null if this column has
   * no expression.
   */
  public LValue getExpression()
  {
    return expression;
  }

  /**
   * checks if the given object is equal to this column.
   * A Column with different physical name is never
   * considered equal.
   * A Column is equal if it is of the same type
   * and has an equal expression as this column
   * A Column is also equal if it is of the same type, shares the same table,
   * and referes to the same tablePosition as this column
   */
  public boolean equals(Object o)
  {
    if (o instanceof tsColumn)
    {
      tsColumn col = (tsColumn) o;

      if (col.getColumnType() != getColumnType())
      {
        return false;
      }

      // A Column with different physical name is never
      // considered equal
      if (physName != null && col.physName == null)
      {
        return false;
      }
      else if (physName == null && col.physName != null)
      {
        return false;
      }

      if (col.getTable() == null)
      {
        if (getTable() != null)
          return false;

        if (col.getExpression() == null)
        {
          if (getExpression() == null)
            return true;

        }
        else
        {
          if (getExpression() == null)
            return false;

          // A Column is equal if it is of the same type
          // and has an equal expression as this column
          return col.getExpression().equals(getExpression());
        }
      }
      else
      {
        if (getTable() == null)
          return false;

        // A Column is equal if it is of the same type, the same table,
        // and on the same tablePosition as this column
        if (getTablePosition() == col.getTablePosition())
          return true;
      }
    }

    return false;
  }

  /**
   * Checks whether the given name is either a physical name, a display name
   * or a full qualified name.
   */
  public boolean isValidName(String name)
  {
    if (name == null)
      return false;

    if (name.equals(getPhysicalName()))
      return true;

    if (name.equals(getDisplayName()))
      return true;

    if (name.equals(getFQName()))
      return true;

    return false;
  }

  /**
   * helperfunction: forms a full qualified name for this column
   * and chaches it.
   */
  private String formColname(String column, tinySQLTableView table)
  {
    if (table == null)
      return column;

    if (fqname == null)
      fqname = table.getName() + "." + column;
    return fqname;
  }

}

