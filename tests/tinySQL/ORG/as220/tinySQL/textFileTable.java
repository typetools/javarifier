/*
 *
 * Extension of tinySQLTable which manipulates text files.
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

import ORG.as220.tinySQL.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Thomas Morgner <mgs@sherito.org> Changed column types to java.sql.types.
 */
public class textFileTable extends tinySQLTable
{

  // The data directory for tables
  //
  private String dataDir;
  private textFileConverter converter;

  private Vector column_info;

  // the object I'll use to manipulate the table
  //
  private RandomAccessFile ftbl;

  // some constants that I don't actually use that much...
  //
  private static final int COLUMN_SIZE = 0;
  private static final int COLUMN_TYPE = 1;
  private static final int COLUMN_POS = 2;

  private int _record_length;     // length of a record
  private int _rowCount = -1;
  private String encoding;

  private byte[] delpref;
  private byte[] delpost;
  private byte[] colpref;
  private byte[] colpost;
  private byte[] rowpref;
  private byte[] rowpost;
  private byte[] tablepref;
  private byte[] tablepost;

  private static final byte[] NOT_DELETED = new byte[]{(byte) 'N'};
  private static final byte[] IS_DELETED = new byte[]{(byte) 'Y'};

  private String defext;
  private String tableext;

  private static final int ROW_UNREAD = 0;
  private static final int ROW_DELETED = -1;
  private static final int ROW_NOT_DELETED = 1;
  private byte[] deletedRows;  // 0 = not read, -1 = deleted, 1 = not deleted
  private textFile databaseEngine;

  private int deleteMode;
  private int insertMode;

  private boolean readonly;
  private boolean ignoreFirst;
  private boolean ignoreLast;

  private textFileRow prototype;

  /**
   * A flag used on close. If the deleteMode is DELETE_PACK, the first
   * call on close executes a compressTable statement. The
   * CompressTableStatement calles close a second time, after it has done
   * its work.
   */
  private boolean compressTableCalled = false;

  /**
   *
   * Constructs a textFileTable. This is only called by getTable()
   * in textFile.java.
   *
   * @param dDir data directory
   * @param table_name the name of the table
   *
   */
  public textFileTable(String dDir, String table_name, textFile engine) throws tinySQLException
  {
    super(table_name);
    this.encoding = engine.getEncoding();
    this.readonly = engine.isReadOnly();
    databaseEngine = engine;

    ignoreFirst = engine.isIgnoringFirstColumnPrefix();
    ignoreLast = engine.isIgnoringLastColumnPostfix();

    defext = engine.getDefinitionExtension();
    tableext = engine.getTableExtension();

    dataDir = dDir;      // set the data directory

    delpref = engine.getDelPrefix();
    delpost = engine.getDelPostfix();
    rowpref = engine.getRowPrefix();
    rowpost = engine.getRowPostfix();
    colpref = engine.getColumnPrefix();
    colpost = engine.getColumnPostfix();
    tablepref = engine.getTablePrefix();
    tablepost = engine.getTablePostfix();

    insertMode = engine.getInsertMode();
    deleteMode = engine.getDeleteMode();

    // attempt to open the file in read/write mode
    //
    try
    {
      if (readonly)
      {
        ftbl = new RandomAccessFile(dataDir + "/" + table_name + tableext, "r");
      }
      else
      {
        ftbl = new RandomAccessFile(dataDir + "/" + table_name + tableext, "rw");
      }
    }
    catch (Exception e)
    {
      throw new tinySQLException("Could not open the table " + getName() + " [" + table_name + tableext + "].");
    }

    try
    {
      // read in the table definition
      //
      readColumnInfo();
    }
    catch (tinySQLException e)
    {
      try
      {
        ftbl.close();
      }
      catch (Exception ex)
      {
      }
      throw e;
    }
    prototype = createInsertRowPrototype();
  }

  public void setRecordLength(int rl)
  {
    _record_length = rl;
  }

  public void setRowCount(int rc)
  {
    _rowCount = rc;
    byte[] _deletedRows = new byte[rc];

    if (deletedRows != null)
    {
      System.arraycopy(deletedRows, 0, _deletedRows, 0, Math.min(deletedRows.length, _deletedRows.length));
    }
    deletedRows = _deletedRows;
  }

  /**
   * get the converter for this table. If the converter is not yet initialized,
   * it is created using the current encoding.
   *
   * @returns the current converter for this table.
   * @see Converter
   */
  public tinySQLConverter getConverter() throws tinySQLException
  {
    if (converter == null)
    {
      try
      {
        converter = new textFileConverter(encoding, databaseEngine.getQuoting());
      }
      catch (Exception e)
      {
        throw new tinySQLException(e);
      }
    }
    return converter;
  }


  /**
   *
   * close method. Try not to call this until you are sure
   * the object is about to go out of scope.
   *
   * This method is called from dbfFile.closeTable ()
   */
  public boolean close() throws tinySQLException
  {
    if (compressTableCalled == false)
    {
      Log.info("About to close table " + getName());
      Enumeration e = getViews();
      while (e.hasMoreElements())
      {
        tinySQLTableView view = (tinySQLTableView) e.nextElement();
        view.close();
      }
    }

    if (deleteMode == textFile.DELETE_PACK && compressTableCalled == false)
    {
      compressTableCalled = true;
      if (findDeletedRow() != -1)
      {
        Connection c = databaseEngine.getConnection();
        Log.info("COMPACT TABLE " + getName() + " before close");
        try
        {
          Statement stmt = c.createStatement();
          stmt.executeUpdate("COMPACT TABLE " + getName());
          stmt.close();
        }
        catch (SQLException ex)
        {

        }
        return false;
      }
    }
    Log.info("Closing table " + getName());

    try
    {
      ftbl.close();
    }
    catch (IOException ioe)
    {
      throw new tinySQLException(ioe);
    }
    return true;
  }

  private int findDeletedRow()
  {
    for (int i = 0; i < deletedRows.length; i++)
    {
      if (deletedRows[i] == ROW_DELETED)
        return i;
    }
    return -1;
  }

  private int getNextInsertRow() throws tinySQLException
  {
    if (insertMode == textFile.INSERT_SPEED)
    {
      return getRowCount();
    }

    int delrow = findDeletedRow();
    Log.debug("FindDeletedRow returned : " + delrow);

    if (delrow != -1)
      return delrow;

    if (insertMode == textFile.INSERT_DEFAULT)
    {
      Log.debug("RowCount: " + getRowCount());
      return getRowCount();
    }
    for (int i = 0; i < deletedRows.length; i++)
    {
      if (deletedRows[i] == ROW_DELETED)
        return i;
      else if (deletedRows[i] == ROW_UNREAD)
      {
        boolean del = isDeleted(i);
        if (del == true)
          return i;
      }
    }
    return getRowCount();

  }

  /**
   * Retrieves the column definition for the column on position <code>col</code>.
   * The delete-flag is not a defined column and cannot be queried directly.
   *
   * @returns the requested columndefinition
   */
  public tsColumn getColumnDefinition(int column)
  {
    tsColumn info = null;
    // Ignore the deleted row
    if (deleteMode == textFile.DELETE_DEFAULT)
    {
      info = (tsColumn) column_info.get(column + 1);
    }
    else
    {
      info = (tsColumn) column_info.get(column);
    }
    if (info == null)
    {
      return null;
    }
    return info;
  }

  public tsRawRow getInsertRow()
  {
    textFileRow row = new textFileRow(prototype);
    row.setData(prototype.getData());
    return row;
  }

  /**
   * creates the table's notion of a raw column. The column is used to
   * convert JDBC values into native values and back.
   */
  public textFileRow createInsertRowPrototype() throws tinySQLException
  {
    textFileRow row = new textFileRow(column_info, getConverter());
    byte[] data = new byte[getRecordLength()];
    Arrays.fill(data, (byte) ' ');

    int currentPos = setRawData(rowpref, data, 0);

    if (deleteMode == textFile.DELETE_DEFAULT)
    {
      currentPos = setRawData(delpref, data, currentPos);
      currentPos = setRawData(NOT_DELETED, data, currentPos);
      currentPos = setRawData(delpost, data, currentPos);
    }

    int colcount = getColumnCount();
    for (int i = 0; i < colcount; i++)
    {
      if (!(ignoreFirst && i == 0))
      {
        currentPos = setRawData(colpref, data, currentPos);
      }
      currentPos += getColumnDefinition(i).getSize();
      if (!(ignoreLast && i == colcount - 1))
      {
        currentPos = setRawData(colpost, data, currentPos);
      }
    }
    System.out.println("___" + currentPos);
    currentPos = setRawData(rowpost, data, currentPos);
    row.setData(data);
    return row;
  }

  private int setRawData(byte[] insert, byte[] data, int pos)
  {
    if (insert.length == 0)
      return pos;

    System.arraycopy(insert, 0, data, pos, insert.length);
    return pos + insert.length;
  }

  /**
   *
   * Updates the row in the table.
   *
   * @param row the number of the record to update
   * @param values the row that contains the new JDBC-Objects for the table-row
   * @see tinySQLTable#UpdateCurrentRow
   * @throws tinySQLException if an database error occured or the database is
   * readonly
   */
  public synchronized void updateRow(int row, tsRawRow v)
      throws tinySQLException
  {

    if (readonly)
    {
      throw new tinySQLException("Database is readonly");
    }
    if (v instanceof textFileRow)
    {

      try
      {
        textFileRow nativeRow = (textFileRow) v;
        byte[] ovalue = nativeRow.getData();
        ftbl.seek(calcRowPosition(row));

        // write out the column
        //
        ftbl.write(ovalue);
      }
      catch (IOException ioe)
      {
        throw new tinySQLException(ioe);
      }
    }
    else
      throw new tinySQLException("updateRow expects a native data row");


  }


  /**
   *
   * Insert a row at the end of the table.
   *
   * @param values Ordered Vector (must match order of c) of values
   * @see tinySQLTable#InsertRow()
   *
   */
  public int insertRow(tsRawRow values) throws tinySQLException
  {

    if (readonly)
    {
      throw new tinySQLException("Database is readonly");
    }
    int insertRow = 0;
    synchronized (ftbl)
    {
      try
      {

        textFileRow nativeRow = (textFileRow) values;

        // go to the next deleted row or to the of the file
        //
        insertRow = getNextInsertRow();

        Log.debug("Insering in row : " + insertRow + " -> " + getRowCount());
        ftbl.seek(calcRowPosition(insertRow));

        // write out the record
        //
        ftbl.write(nativeRow.getData());

        // write out the postfix
        //
        ftbl.write(tablepost);

        // if this was a newly inserted row, update the rowcount
        if (insertRow == getRowCount())
          setRowCount(insertRow + 1);
      }
      catch (Exception e)
      {
        throw new tinySQLException(e);
      }
    }
    return (insertRow);
  }

  /**
   * @returns the number of records in this table
   */
  public int getRowCount()
  {
    return _rowCount;
  }

  /**
   * @returns the number of columns in this table
   */
  public int getColumnCount()
  {
    if (deleteMode == textFile.DELETE_DEFAULT)
      return column_info.size() - 1;
    else
      return column_info.size();
  }

  /**
   * reads a complete row as raw-data and returns a tsPhysicalRow to the caller
   * containing JDBC-Objects.
   *
   * @return the tsPhysicalRow containing the data for this row
   * @throws tinySQLException if an IOError or an database error occured.
   */
  public tsRawRow getRow(int row) throws tinySQLException
  {
    byte[] line = new byte[getRecordLength()];
    synchronized (ftbl)
    {
      try
      {
        ftbl.seek(calcRowPosition(row));
        ftbl.readFully(line);
      }
      catch (IOException ioe)
      {
        Log.error("Failed to read record[" + calcRowPosition(row) + ":" + line.length + ": " + row + "]", ioe);
        throw new tinySQLException(ioe);
      }
    }

    if (line.length > 0)
    {
      if (isDeleted(line, row))
      {
        deletedRows[row] = ROW_DELETED;
      }
      else
      {
        deletedRows[row] = ROW_NOT_DELETED;
      }
    }
    else
    {
      deletedRows[row] = ROW_NOT_DELETED;
    }
    textFileRow tsrow = new textFileRow(column_info, getConverter());
    tsrow.setData(line);
    return tsrow;
  }

  /**
   *
   * Retrieve a column's string value from the current row.
   *
   * @param column the column name
   * @param line a complete record
   *
   */
  private Object getColumn(int column, String line) throws tinySQLException
  {

    // read the column info
    //
    tsColumn info = (tsColumn) column_info.elementAt(column);

    // retrieve datatype, size, and position within row
    //
    String datatype = typeToLiteral(info.getType());
    int size = info.getSize();
    int pos = info.getBytePosition();

    String result = line.substring((int) pos, (int) (pos + size));

    tinySQLConverter con = getConverter();

    return con.convertNativeToJDBC(info, result);
  }

  /**
   *
   * Delete the row.
   *
   * @param row the recordnumber of the row to delete
   * @see tinySQLTable#DeleteRow
   *
   */
  public void deleteRow(int row) throws tinySQLException
  {

    if (readonly)
    {
      throw new tinySQLException("Database is readonly");
    }
    // this is real easy; just flip the value of the _DELETED column
    //

    if (row >= getRowCount())
      throw new tinySQLException("No such record");

    Log.debug("DeleteMode : " + deleteMode + " DELETE ROW " + row);

    if (deleteMode == textFile.DELETE_NONE)
      throw new tinySQLException("Deletion of records has been disabled");

    deletedRows[row] = ROW_DELETED;

    if (deleteMode == textFile.DELETE_DEFAULT)
    {
      synchronized (ftbl)
      {
        try
        {
          ftbl.seek(calcRowPosition(row) + rowpref.length + delpref.length);
          ftbl.write(IS_DELETED);
        }
        catch (IOException ioe)
        {
          Log.error("Failed to read record: " + row, ioe);
        }
      }
    }
  }

  protected int calcRowPosition(int row)
  {
    return row * (getRecordLength()) + tablepref.length;
  }

  /**
   * checks whether the specified row is deleted or not
   *
   * @returns true, if the row is deleted, false otherwise
   * @throws tinySQLException on database or IO error
   * @see tinySQLTable#isDeleted()
   */
  public boolean isDeleted(int row) throws tinySQLException
  {

    // this is real easy; just check the value of the _DELETED column
    //
    if (deleteMode == textFile.DELETE_NONE)
      return false;

    if (deletedRows[row] == ROW_UNREAD) getRow(row);
    if (deletedRows[row] == ROW_DELETED) return true;
    return false;
  }

  private boolean isDeleted(byte[] b, int row)
  {
    if (deleteMode == textFile.DELETE_NONE)
      return false;

    if (deleteMode == textFile.DELETE_PACK)
    {
      if (deletedRows[row] == ROW_DELETED)
        return true;
      else if (deletedRows[row] == ROW_NOT_DELETED)
        return false;
    }
    return b[rowpref.length + delpref.length] == (char) 'Y';
  }

  // end methods implemented from tinySQLTable.java
  // the rest of this stuff is internal methods
  // for textFileTable
  //

  /*
   *
   * Reads in a table definition and populates the column_info
   * Hashtable
   *
   */
  protected void readColumnInfo() throws tinySQLException
  {
    boolean fdef_open = false;
    FileInputStream fdef = null;

    try
    {

      column_info = new Vector();

      // Open an FileInputStream to the .def (table
      // definition) file
      //
      fdef = new FileInputStream(dataDir + "/" + getName() + defext);

      fdef_open = true;

      // use a StreamTokenizer to break up the stream.
      //
      Reader r = new BufferedReader(
          new InputStreamReader(fdef));
      StreamTokenizer def = new StreamTokenizer(r);

      // set the | as a delimiter, and set everything between
      // 0 and z as word characters. Let it know that eol is
      // *not* significant, and that it should parse numbers.
      //
      def.whitespaceChars('|', '|');
      def.wordChars('0', 'z');
      def.eolIsSignificant(false);
      def.parseNumbers();

      int tablePos = 0;
      int record_length = rowpref.length;
      // read each token from the tokenizer
      //
      while (def.nextToken() != def.TT_EOF)
      {

        // first token is the datatype
        //
        // Q&D: Default is char value, numeric is special
        int datatype = Types.CHAR;
        if (def.sval.equals("NUMERIC"))
        {
          datatype = Types.NUMERIC;
        }
        else if (def.sval.equals("DATE"))
        {
          datatype = Types.DATE;
        }

        // get the next token; it's the column name
        //
        def.nextToken();
        String column = def.sval;

        // get the third token; it's the size of the column
        //
        def.nextToken();
        int size = (int) def.nval;

        record_length = addColumnDefinition(column, datatype, size, record_length, tablePos);
        Log.debug("RecordLength [" + tablePos + "] : " + record_length);
        // this is the start position of the next column
        //

        tablePos++;
      }
      fdef.close(); // close the file
      fdef_open = false;

      // Add line break
      record_length += rowpost.length;
      if (ignoreLast)
      {
        record_length -= colpost.length;
      }

      System.out.println("RecordLength: " + record_length);

      setRecordLength(record_length);

      long datalength = ftbl.length() - tablepref.length - tablepost.length;

      if (datalength < 0)
      {
        throw new tinySQLException("TableError negative data size: " + datalength);
      }
      else
      {
        setRowCount((int) (datalength / record_length));
      }

//      Log.debug ("TextFileTable " + getName() + ": RecordLength=" + record_length);
//      Log.debug ("TextFileTable " + getName() + ": RowCount=" + getRowCount());
//      Log.debug ("TextFileTable " + getName() + ": ColCount=" + column_info.size());

    }
    catch (Exception e)
    {
      try
      {
        if (fdef_open)
          fdef.close();
      }
      catch (IOException ioe)
      {
      }
      throw new tinySQLException(e);
    }
  }

  private int addColumnDefinition(String column, int datatype, int size, int record_length, int tablePos)
  {

    int retval = 0;

    if (deleteMode == textFile.DELETE_DEFAULT && column.equals("_DELETED"))
    {
      // create an info array
      //
      tsColumn info = new tsColumn(null, column);

      // store the datatype, the size, and the position
      // within the record (the record length *before*
      // we increment it with the size of this column
      //
      // The deleted row gets a special treatment
      //
      info.setBytePosition(record_length + delpref.length);
      info.setType(datatype);
      info.setSize(size);
      info.setTablePosition(tablePos);
      column_info.add(info);
      retval = size + record_length + delpref.length + delpost.length;
      return retval;
    }
    // create an info array
    //
    tsColumn info = new tsColumn(null, column);

    // store the datatype, the size, and the position
    // within the record (the record length *before*
    // we increment it with the size of this column
    //
    int columnPosition = record_length + colpref.length;
    if (ignoreFirst)
    {
      if (
          ((deleteMode == textFile.DELETE_DEFAULT) && (tablePos == 1))
          ||
          (tablePos == 0)
      )
      {
        columnPosition = record_length;
      }
    }
    info.setBytePosition(columnPosition);
    info.setType(datatype);
    info.setSize(size);
    info.setTablePosition(tablePos);
    column_info.add(info);
    retval = size + columnPosition + colpost.length;

    return retval;
  }

  /**
   'C' Char (max 254 bytes)
   'N' '-.0123456789' (max 19 bytes)
   'L' 'YyNnTtFf?' (1 byte)
   'M' 10 digit .DBT block number
   'D' 8 digit YYYYMMDD
   *
   * Uses java.sql.Types as key
   */
  protected static String typeToLiteral(int type)
  {
    if (type == Types.CHAR) return "CHAR";
    if (type == Types.NUMERIC) return "NUMERIC";
    if (type == Types.BIT) return "BOOLEAN";
    if (type == Types.INTEGER) return "NUMERIC";
    if (type == Types.BINARY) return "BINARY";
    if (type == Types.DATE) return "DATE";
    return "CHAR"; // fallback
  }

  /**
   * returns the size in bytes of a single records
   */
  public int getRecordLength()
  {
    return _record_length;
  }
}
