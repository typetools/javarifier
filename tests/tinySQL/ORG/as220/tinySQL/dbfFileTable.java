/*
 *
 * Extension of tinySQLTable which manipulates dbf files.
 *
 * Copyright 1996 John Wiley & Sons, Inc.
 * See the COPYING file for redistribution details.
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
 */
package ORG.as220.tinySQL;

import ORG.as220.tinySQL.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * dBase read/write access <br>
 * @author Brian Jepson <bjepson@home.com>
 * @author Marcel Ruff <ruff@swand.lake.de> Added write access to dBase and JDK 2 support
 * @author Thomas Morgner <taquera@sherito.org>addapted changes to tinySQLTable for
 * the parser to dbfFileTable, moved cached rows to tinySQLTableView, changed interface
 * to address records by recordnumber.
 */
public class dbfFileTable extends tinySQLTable
{
  // is this table readOnly?
  // this triggers to true, if the randomAccessFile could not been opened in RW-mode
  private boolean readonly;


  private String fullpath;    // the full path to the file

  // the header contains the file-definition as well as the column definitions.
  private DBFHeader dbfHeader;

  // access handle to the dBase file
  private RandomAccessFile ftbl;

  public final static String DBF_EXTENSION = ".DBF";

  // where to find the delete-flag for a row.
  private final static int IS_DELETED_INDEX = 0;

  private final static char RECORD_IS_DELETED = '*';    // '*': is deleted
  private final static char RECORD_IS_NOT_DELETED = ' ';// ' ': is not deleted

  // the converter is used to translate from JDBC to native and back
  private dbfFileConverter converter;

  // the rowprototype is a fully initialized row which is used to
  // create new rows using the copyconstructor when a getRow(int) returns data.
  private dbfFileRow prototype;

  // the current number of rows, cached from header for performance reasons
  private int _rowCount;
  // the current header length, cached from header for performance reasons
  private int _headerLength = -1;
  // the current record length, cached from header for performance reasons
  private int _recordLength = -1;
  private byte[] defaultInsertRow = null;

  private static final int ROW_UNREAD = 0;
  private static final int ROW_DELETED = -1;
  private static final int ROW_NOT_DELETED = 1;
  private byte[] deletedRows;  // 0 = not read, -1 = deleted, 1 = not deleted

  private int insertMode;

  /**
   *
   * Constructs a dbfFileTable. This is only called by getTable()
   * in dbfFile.java.
   *
   * @param dDir data directory
   * @param table_name the name of the table
   *
   */
  public dbfFileTable(String dDir,
                      String table_name,
                      String encoding,
                      boolean readonly,
                      boolean automode)
      throws tinySQLException
  {

    super(new File(table_name).getName());

    this.readonly = readonly;

    // the full path to the file
    //
    if (table_name.toUpperCase().endsWith(DBF_EXTENSION))
    {
      fullpath = dDir + File.separator + table_name;
    }
    else
    {
      fullpath = dDir + File.separator + table_name + DBF_EXTENSION;
    }

    // Open the DBF file
    //
    open_dbf(encoding, automode);
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
    try
    {
      ftbl.getFD().sync();
      ftbl.close();
    }
    catch (IOException e)
    {
      throw new tinySQLException(e);
    }
    return true;
  }

  /**
   * Retrieves the column definition for the column on position <code>col</code>.
   * The delete-flag is not a defined column and cannot be queried directly.
   *
   * @returns the requested columndefinition
   */
  public tsColumn getColumnDefinition(int col)
  {
    tsColumn coldef = (tsColumn) dbfHeader.getColumnDefinition(col);
    if (coldef == null)
    {
      return null;
    }
    return coldef;
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
        converter = new dbfFileConverter(getHeader().getEncoding());
      }
      catch (Exception e)
      {
        throw new tinySQLException(e);
      }
    }
    return converter;
  }

  /**
   * creates the table's notion of a raw column. The column is used to
   * convert JDBC values into native values and back.
   */
  public tsRawRow getInsertRow() throws tinySQLException
  {
    if (prototype == null)
    {
      Vector v = getHeader().getFields();
      prototype = new dbfFileRow(v, getConverter());
    }

    dbfFileRow retval = new dbfFileRow(prototype);
    byte[] data = createInsertRow();
    retval.setData(data);
    return retval;
  }


  private byte[] createInsertRow() throws tinySQLException
  {
    if (defaultInsertRow == null)
    {
      tinySQLConverter converter = getConverter();

      byte[] data = new byte[dbfHeader.getRecordLength()];
      data[0] = (byte)(' ');
      for (int i = 0; i < getColumnCount(); i++)
      {
        tsColumn col = getColumnDefinition(i);
        Object o = col.getDefaultValue();
        byte[] b = (byte[]) converter.convertJDBCToNative(col, null);
        System.arraycopy(b, 0, data, col.getBytePosition(), b.length);
      }
      defaultInsertRow = data;
    }
    return defaultInsertRow;
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
  public void updateRow(int row, tsRawRow values)
      throws tinySQLException
  {

    if (readonly)
    {
      throw new tinySQLException("Database is readonly");
    }

    // finally write it
    try
    {
      dbfFileRow datarow = (dbfFileRow) values;
      writeRow(row, datarow.getData());
    }
    catch (IOException ioe)
    {
      throw new tinySQLException(ioe);
    }
  }

  /**
   * Write the rowdata into the record sepcified by currentRecordNumber
   *
   * This function is private, so no checks are implemented.
   */
  private void writeRow(int currentRecordNumber, byte[] currentUpdateRow)
      throws IOException
  {
//    synchronized (ftbl)
//    {
    // go to the current row
    //
    ftbl.seek(calcRowPos(currentRecordNumber));

    // write out the not deleted indicator
    //
    ftbl.write(currentUpdateRow);

    // update last update time
    dbfHeader.writeFileHeader(ftbl);
//    }
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
    try
    {
      dbfFileRow row = (dbfFileRow) values;

      // go to the next deleted row or to the of the file
      //
      insertRow = getNextInsertRow();

      ftbl.seek(calcRowPos(insertRow));

      // write out the record
      //
      ftbl.write(row.getData());

      Log.debug("Insert row written on position : " + insertRow);
      // if this was a newly inserted row, update the rowcount
      if (insertRow == getRowCount())
        setRowCount(insertRow + 1);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

    return (insertRow);
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
    if (delrow != -1)
      return delrow;

    if (insertMode == textFile.INSERT_DEFAULT)
      return getRowCount();

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
   * load a complete row specified by recordnumber <code>pos</code>
   * Be aware that counting of records changed. Counting starts with
   * 0, not 1 for performance reasons.
   */
  private byte[] _readRow(int pos) throws tinySQLException
  {
    if (pos < 0)
      throw new IllegalArgumentException("Position is negative");

    try
    {
      byte[] b = new byte[dbfHeader.getRecordLength()];
//      synchronized (ftbl)
//      {
      // seek the starting offset of the current record,
      // as indicated by currentRow
      //
      ftbl.seek(calcRowPos(pos));

      // fully read a byte array out to the length of
      // the record.
      //
      ftbl.readFully(b);
//      }
      // make it into a String
      //
      return b;
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }
  }


  /**
   * reads a row as raw-data and returns a dbfFileRow to the caller.
   *
   * @return the tsPhysicalRow containing the data for this row
   * @throws tinySQLException if an IOError or an database error occured.
   */
  public tsRawRow getRow(int row) throws tinySQLException
  {
    if (prototype == null)
    {
      Vector v = getHeader().getFields();
      prototype = new dbfFileRow(v, getConverter());
    }

    byte[] b = _readRow(row);
    if (isDeleted(b))
    {
      deletedRows[row] = ROW_DELETED;
    }
    else
    {
      deletedRows[row] = ROW_NOT_DELETED;
    }
    dbfFileRow myrow = new dbfFileRow(prototype);
    myrow.setData(b);
    return myrow;
  }

  /**
   *
   * Delete the current row.
   *
   * @see tinySQLTable#DeleteRow
   *
   */
  public void deleteRow(int row) throws tinySQLException
  {
    if (row >= getRowCount())
      throw new tinySQLException("Row is >= rowcount");
    try
    {
      deletedRows[row] = ROW_DELETED;
      ftbl.seek(calcRowPos(row));
      ftbl.writeByte(RECORD_IS_DELETED);
    }
    catch (IOException ioe)
    {
      throw new tinySQLException("Unable to delete record " + row);
    }
  }

  /**
   * Helperfunction: calculates the position of the row <code>rcdn</code>
   */
  private int calcRowPos(int rcdn)
  {
    return _headerLength + (rcdn) * _recordLength;
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
    if (deletedRows[row] == ROW_UNREAD) getRow(row);
    if (deletedRows[row] == ROW_DELETED) return true;
    return false;
  }

  /**
   * checks whether the specified row is deleted
   *
   * @returns true, if the row is deleted, false otherwise
   * @throws tinySQLException on database or IO error
   * @see tinySQLTable#isDeleted()
   */
  private boolean isDeleted(byte[] row) throws tinySQLException
  {
    char c = (char) row[IS_DELETED_INDEX];
    if (c == RECORD_IS_DELETED)
      return true;

    return false;
  }

  /**
   * @returns the number of records
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
    return getHeader().getNumberOfFields();
  }


  // end methods implemented from tinySQLTable.java
  // the rest of this stuff is private methods
  // for dbfFileTable
  //


  /**
   * @return Length in bytes of one row
   */
  public int getRecordLength()
  {
    return _recordLength;
  }


  /*
   * opens a DBF file. This is based on Pratap Pereira's
   * Xbase.pm perl module
   * @return column definition list (HashTable)
   *
   * @author Thomas Morgner <mgs@sherito.org> added check for
   * file exists, before the file is opened. Opening a non existing
   * file will create a new file, and we get errors while trying
   * to read the non-existend headers
   */
  private void open_dbf(String encoding, boolean automode) throws tinySQLException
  {

    try
    {
      // open the file ...
      File f = new File(fullpath);
      if (!f.exists() || (!f.canRead()))
      {
        throw new tinySQLException("Unable to open datafile (" + fullpath + "), file does not exist or can't be read." + f.getAbsolutePath());
      }
      if (f.canWrite() && (!readonly))
      {
        ftbl = new RandomAccessFile(f, "rw");
      }
      else
      {
        // Open readonly if the file is not writeable. Needed for
        // databases on CD-Rom
        readonly = true;
        ftbl = new RandomAccessFile(f, "r");
      }

      // read the first 32 bytes ...
      dbfHeader = new DBFHeader(encoding, automode);
      dbfHeader.initializeHeader(ftbl);
      dbfHeader.readColHeader(ftbl);
    }
    catch (Exception e)
    {
      try
      {
        ftbl.close();
      }
      catch (Exception e2)
      {
      }
      throw new tinySQLException(e);
    }
    setRowCount(dbfHeader.getNumberOfRecords());
    _headerLength = dbfHeader.getHeaderLength();
    _recordLength = dbfHeader.getRecordLength();
  }

  private void setRowCount(int rc) throws tinySQLException
  {
    _rowCount = rc;
    byte[] _deletedRows = new byte[rc];

    if (deletedRows != null)
    {
      System.arraycopy(deletedRows, 0, _deletedRows, 0, Math.min(deletedRows.length, _deletedRows.length));
    }
    deletedRows = _deletedRows;

    try
    {
      if (dbfHeader.getNumberOfRecords() != rc)
        dbfHeader.updateNumberOfRecords(rc, ftbl);
    }
    catch (IOException ioe)
    {
      throw new tinySQLException(ioe);
    }
  }

  /**
   * @returns the header of this table.
   */
  public DBFHeader getHeader()
  {
    return dbfHeader;
  }

  /**
   * @returns true, if this table is readonly
   */
  public boolean isReadOnly()
  {
    return readonly;
  }
}

