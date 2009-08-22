/*
 * DBFHeader.java
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
import java.io.UnsupportedEncodingException;
import java.sql.Types;
import java.util.Enumeration;
import java.util.Vector;

/**
 * generic dbaseFile Header. The header reads the file and column definitions.
 * Encoding of the table is detected. If the database is not in automode, no
 * changes to the encodings are made, but warning are issued if a different
 * encoding than the default encoding is defined in the table.
 *
 * @author Brian Jepson <bjepson@home.com>
 * @author Marcel Ruff <ruff@swand.lake.de> Added write access to dBase and JDK 2 support
 */
public class DBFHeader
{
  /// type-identifier for FoxBase tables
  public static final short TYPE_DB2 = 0x02;

  /// type-identifier for dbase3 tables
  public static final short TYPE_DB3 = 0x03;

  /// type-identifier for Visual FoxPro tables
  public static final short TYPE_VISUAL_FOXPRO = 0x30;

  /// type-identifier for DBase4 SQLTables without memos
  public static final short TYPE_DB4_SQLTABLE_NOMEMO = 0x43;

  /// type-identifier for DBase4 SQL System Tables without memos
  public static final short TYPE_DB4_SQLSYSTEM_NOMEMO = 0x63;

  /// type-identifier for DBase3 Tables with memos
  public static final short TYPE_DB3_MEMO = 0x83;

  /// type-identifier for DBase4 Data Tables with memos
  public static final short TYPE_DB4_MEMO = 0x8B;

  /// type-identifier for DBase4 SQL-Tables with memos
  public static final short TYPE_DB4_SQLTABLE_MEMO = 0xCB;

  /// type-identifier for FoxPro2 Tables
  public static final short TYPE_FOXPRO2 = 0xF5;

  /// type-identifier for older FoxBase Tables
  public static final short TYPE_FOXBASE = 0xFB;

  // year of the last update of the table
  private short file_update_year = 0;

  // month of the last update of the table
  private short file_update_month = 0;

  // day of the last update of the table
  private short file_update_day = 0;

  private int numFields = 0;     // number of column definitions
  private int numRecords = 0;    // number of data records
  private int headerLength = -1;  // in bytes
  private int recordLength = 0;  // length in bytes of one data row, including the beginning delete flag-byte
  private String encoding;       // encoding of the data
  private boolean readOnly;      // is readonly

  /*
     The dBase III header consists of 32 byte bulks:
     0-32    primary header info
     32      bytes for each column info (n times)
     The number of columns is calculated from the headerLength
  */
  private final static int BULK_SIZE = 32;             //
  private final static int FLAG_INDEX = 0;             // = TableType - see TYPE_* constants
  private final static int DATE_INDEX = 1;             // 1=YY 2=MM 3=DD (last update)
  private final static int NUMBER_OF_REC_INDEX = 4;    // 4-7
  private final static int LENGTH_OF_HEADER_INDEX = 8; // 8-9
  private final static int LENGTH_OF_REC_INDEX = 10;   // 8-11
  private final static int RESERVED_INDEX = 12;        // 12-31
  private final static int TABLE_FLAGS = 28;
  private final static int ENCODING_INDEX = 29;        // Encoding of data

  private final static int DBF_HEADER_SIZE = 32;

  // table flags used by FoxPro
  // the table flag can contain any combination of these bytes
  public static final int TABLE_HAS_STRUCTURE_CDX = 0x01;
  public static final int TABLE_HAS_MEMO_FIELD = 0x02;
  public static final int TABLE_IS_DATABASE = 0x04;

  private final static int DBF_COLDEF_SIZE = 32;

  // dBase III column info offsets (one for each column):
  private final static int FIELD_NAME_INDEX = 0;       // 0-10 column name, ASCIIZ - null padded
  private final static int FIELD_TYPE_INDEX = 11;      // 11-11
  private final static int IMU_INDEX = 12;             // 12-15 (in memory use)
  private final static int FIELD_LENGTH_INDEX = 16;    // 16-16 (max field length = 254)
  private final static int DECIMAL_COUNT_INDEX = 17;   // 17-17
  private final static int FIELD_FLAGS_INDEX = 18;     // 18-18
  private final static int FIELD_RESERVED_INDEX = 19;
  // the header section ends with carriage return CR.

  public static final int FIELD_FLAG_SYSTEM_COLUMN = 0x01;
  public static final int FIELD_FLAG_IS_NULLABLE = 0x02;
  public static final int FIELD_FLAG_IS_BINARY = 0x04; // for char and memo only

  // The backlink structure contains the relative path to an associated database
  // or if the first byte is null, the table is not associated with a database.
  // Databases are never associated with other databases and contain always null
  private static final int BACKLINK_STRUCTURE_SIZE = 263;

  // the complete fileHeader
  private byte[] fileHeader;
  // the complete columnHeaders
  private byte[] colHeader;

  private Vector coldefsSorted;

  private RandomAccessFile ff;
  private short type; // the type of the table .. one of TYPE_...
  private boolean autoEncoding;

  /**
   * Constructs a DBFHeader, read the data from file <br>
   * You need to supply an open file handle to read from
   * @param ff open file handle for read access
   * @author mgs change to work in byte array instead of raf
   */
  public DBFHeader(String encoding, boolean automode) throws tinySQLException
  {
    this.encoding = encoding;
    this.autoEncoding = automode;

    fileHeader = new byte[DBF_HEADER_SIZE];
  }

  /**
   * Initializes the header by reading the start of the file
   *
   * @param ff the file from which to read
   * @throws tinySQLException if a IOError occures
   */
  public void initializeHeader(RandomAccessFile ff)
      throws tinySQLException
  {
    try
    {
      readFileHeader(ff);
      readColHeader(ff);
    }
    catch (IOException ioe)
    {
      throw new tinySQLException("Error reading the header of the table", ioe);
    }
  }

  public DBFHeader(DBFHeader old, int numFields, String encoding) throws tinySQLException
  {
    if (numFields > 255)
      throw new tinySQLException("There are not more than 255 columns per table supported");

    if (numFields < 0)
      throw new tinySQLException("Negative column count is invalid.");

    this.encoding = encoding;
    if (encoding == null)
    {
      throw new NullPointerException("Auto-Mode encoding is not applicable to created tables");
    }
    fileHeader = new byte[DBF_HEADER_SIZE];

    // Copy the fileheader from the old table
    System.arraycopy(old.fileHeader, 0, fileHeader, 0, DBF_HEADER_SIZE);
    applyFileHeader();

    // it seems that only Visual FoxPro uses the backlink structure
    if (old.type == TYPE_VISUAL_FOXPRO)
    {
      colHeader = new byte[(numFields * DBF_HEADER_SIZE) + 1 + BACKLINK_STRUCTURE_SIZE];
    }
    else
    {
      colHeader = new byte[(numFields * DBF_HEADER_SIZE) + 1];
    }
    setTimestamp();
    setNumberOfFields(numFields);
    setRecordLength(-1);
    setNumRecords(0);
  }

  /**
   * Constructs a DBFHeader, read the data from file <br>
   * You need to supply an open file handle to read from
   * @param numFields number of Columns
   * @param type the type of the created table
   * @param encoding the default encoding to use for this table
   */
  public DBFHeader(int numFields, short type, String encoding) throws tinySQLException
  {
    this.encoding = encoding;
    if (encoding == null)
    {
      throw new NullPointerException("Auto-Mode encoding is not applicable to created tables");
    }
    fileHeader = new byte[DBF_HEADER_SIZE];

    setTimestamp();
    setType(type);
    setNumberOfFields(numFields);
    setRecordLength(-1);
    setNumRecords(0);
    // it seems that only Visual FoxPro uses the backlink structure
    colHeader = new byte[getHeaderLength() - 32];
  }


  /**
   * Create new dBase file and write the first 32 bytes<br>
   * the file remains opened
   * @return file handle with read/write access
   */
  public void create(String dataDir, String tableName)
      throws tinySQLException
  {
    try
    {
      // make the data directory, if it needs to be make
      //
      mkDataDirectory(dataDir);

      // perform an implicit drop table.
      //
      File fullPath = new File(dataDir, tableName + dbfFileTable.DBF_EXTENSION);
      if (fullPath.exists())
      {
        throw new tinySQLException("Table " + tableName + " exists.");
      }
      ff = new RandomAccessFile(fullPath, "rw");
    }
    catch (Exception e)
    {
      try
      {
        ff.close();
      }
      catch (Exception e2)
      {
      }
      throw new tinySQLException(e);
    }
  }

  /**
   * Closes the header, from now on it will never change. To change
   * the header you have to rebuild the file. This function is only
   * valid after creating a table.
   *
   * @throws IllegalStateException if the close is called a header
   * that was not created by create()
   */
  public void close()
      throws IllegalStateException
  {
    if (ff == null)
      throw new IllegalStateException("This file is not created by create()");

    try
    {
      writeFileHeader(ff);
      writeColHeader(ff);
      ff.getFD().sync();
      ff.close();
      colHeader = null;
      fileHeader = null;

    }
    catch (Exception ex)
    {
      try
      {
        ff.close();
      }
      catch (Exception e)
      {
      }
      Log.error("Failed to write the header.", ex);
    }
  }

  /**
   * @returns the number of records in the table
   */
  public int getNumberOfRecords()
  {
    return numRecords;
  }

  /**
   * @returns the number of columns defined in the table
   */
  public int getNumberOfFields()
  {
    return numFields;
  }

  /**
   * @returns the size of a single record in bytes
   */
  public int getRecordLength()
  {
    return recordLength;
  }

  /**
   * @returns the size of the header
   */
  public int getHeaderLength()
  {
    return headerLength;
  }

  /**
   * @returns the type constant of this table
   */
  public int getType()
  {
    return type;
  }

  /**
   * sets the type constant of this table
   * This function is only called internally.
   *
   */
  private void setType(short type)
  {
    this.type = type;
  }

  /**
   * @returns copies of all column definitions as a new vector.
   */
  public Vector getFields()
  {
    Vector v = new Vector(coldefsSorted.size());
    Enumeration enum = coldefsSorted.elements();
    while (enum.hasMoreElements())
    {
      tsColumn col = new tsColumn((tsColumn) enum.nextElement());
      v.addElement(col);
    }

    return v;
  }

  /**
   * @returns the columndefinition of the specified column
   */
  public tsColumn getColumnDefinition(int column)
  {
    tsColumn col = (tsColumn) coldefsSorted.elementAt(column);
    return col;
  }

  /**
   * writes the fileheader to the file
   */
  public void writeFileHeader(RandomAccessFile ff)
      throws IOException
  {
    setTimestamp();
    fileHeader[FLAG_INDEX] = (byte) type;
    fileHeader[ENCODING_INDEX] = (byte) transformEncoding(encoding);
    ff.seek(FLAG_INDEX);
    ff.write(fileHeader);
  }

  /**
   * reads the fileheader from the file
   */
  public void readFileHeader(RandomAccessFile ff)
      throws IOException
  {
    ff.seek(FLAG_INDEX);
    ff.readFully(fileHeader);
    applyFileHeader();
  }

  private void applyFileHeader()
  {
    type = Utils.fixByte(fileHeader[0]);

    // get the last update date
    file_update_year = Utils.fixByte(fileHeader[1]);
    file_update_month = Utils.fixByte(fileHeader[2]);
    file_update_day = Utils.fixByte(fileHeader[3]);

    // a byte array to hold little-endian long data
    //
    byte[] b = new byte[4];

    // read that baby in...
    //
    //ff.readFully(b);
    b[0] = fileHeader[4];
    b[1] = fileHeader[5];
    b[2] = fileHeader[6];
    b[3] = fileHeader[7];

    // convert the byte array into a long (really a double)
    // 4-7 number of records
    numRecords = (int) Utils.vax_to_long(b);

    // a byte array to hold little-endian short data
    //
    b = new byte[2];
    // get the data position (where it starts in the file)
    // 8-9 Length of header
    // ff.readFully(b);
    b[0] = fileHeader[8];
    b[1] = fileHeader[9];

    headerLength = Utils.vax_to_short(b);

    // find out the length of the data portion
    // 10-11 Length of Record
    // ff.readFully(b);
    b[0] = fileHeader[10];
    b[1] = fileHeader[11];

    recordLength = Utils.vax_to_short(b);
    String fileenc = resolveEncoding(Utils.fixByte(fileHeader[29]));

    if (fileenc == null)
    {
      Log.info("Unable to determine table codepage, using default: " + encoding);
    }
    else
    {
      if (autoEncoding == true)
      {
        Log.info("Table encoding set to : " + fileenc);
        encoding = fileenc;
      }
      else if (encoding.equals(fileenc) == false)
      {
        Log.warn("Table encoding of '" + fileenc + "' does not match specified encoding of '" + encoding + "'");
      }
      else
      {
        Log.info("Table uses encoding of " + encoding);
      }
    }

    // calculate the number of fields
    //
    colHeader = new byte[headerLength - 32];

    // due to the bug discovered by "Bruno nieuwenhuys" we can no
    // longer calculate header length here
    //    numFields = (int) (headerLength - 33)/32;

  }

  /**
   * @returns the currently used encoding for the table
   */
  public String getEncoding()
  {
    return encoding;
  }

  /**
   * resolves the encoding byte
   */
  private String resolveEncoding(int b)
  {
    String encoding = this.encoding;

    switch (b)
    {
      case 0x0f:
        encoding = "Cp850";
        break; // experimental
      case 0x01:
        encoding = "Cp437";
        break; // U.S. MS-DOS
      case 0x69:
        encoding = "Cp620";
        break; // Mazovla (Polish) MS-DOS
      case 0x6A:
        encoding = "Cp737";
        break; // Greek MS-DOS
      case 0x02:
        encoding = "Cp850";
        break; // International MS-DOS
      case 0x64:
        encoding = "Cp852";
        break; // Eastern Europe MS-DOS
      case 0x6B:
        encoding = "Cp857";
        break; // Turkish MS-DOS
      case 0x67:
        encoding = "Cp861";
        break; // Icelandic MS-DOS
      case 0x66:
        encoding = "Cp865";
        break; // Nordic MS-DOS
      case 0x65:
        encoding = "Cp866";
        break; // Russian MS-DOS
      case 0x7c:
        encoding = "Cp874";
        break; // Thai Windows
      case 0x68:
        encoding = "Cp895";
        break; // Kamenicky (Czech) MS-DOS
      case 0x7b:
        encoding = "Cp932";
        break; // Japanese Windows
      case 0x7a:
        encoding = "Cp936";
        break; // Chinese (PRC, Singapore) Windows
      case 0x79:
        encoding = "Cp949";
        break; // Korean Windows
      case 0x78:
        encoding = "Cp950";
        break; // Chinese (Hong Kong SAR, Taiwan) Windows
      case 0xc8:
        encoding = "Cp1250";
        break; // Eastern European Windows
      case 0xc9:
        encoding = "Cp1251";
        break; // Russian Windows
      case 0x03:
        encoding = "Cp1252";
        break; // Windows ANSI
      case 0xcb:
        encoding = "Cp1253";
        break; // Greek Windows
      case 0xca:
        encoding = "Cp1254";
        break; // Turkish Windows
      case 0x7d:
        encoding = "Cp1255";
        break; // Hebrew Windows
      case 0x7e:
        encoding = "Cp1256";
        break; // Arabic Windows
      case 0x04:
        encoding = "Cp10000";
        break; // Standard Macintosh
      case 0x98:
        encoding = "Cp10006";
        break; // Greek Macintosh
      case 0x96:
        encoding = "Cp10007";
        break; // Russian Macintosh
      case 0x97:
        encoding = "Cp10029";
        break; // Macintosh EE
    }

    return encoding;
  }

  /**
   * transforms an encoding into the corresponding byte value.
   */
  public int transformEncoding(String encoding)
  {
    if (encoding.equals("Cp437"))
    {
      return 0x01;
    }
    if (encoding.equals("Cp620"))
    {
      return 0x69;
    }
    if (encoding.equals("Cp737"))
    {
      return 0x6a;
    }
    if (encoding.equals("Cp850"))
    {
      return 0x02;
    }
    if (encoding.equals("Cp852"))
    {
      return 0x64;
    }
    if (encoding.equals("Cp857"))
    {
      return 0x6b;
    }
    if (encoding.equals("Cp861"))
    {
      return 0x67;
    }
    if (encoding.equals("Cp865"))
    {
      return 0x66;
    }
    if (encoding.equals("Cp866"))
    {
      return 0x65;
    }
    if (encoding.equals("Cp874"))
    {
      return 0x7c;
    }
    if (encoding.equals("Cp895"))
    {
      return 0x68;
    }
    if (encoding.equals("Cp932"))
    {
      return 0x7b;
    }
    if (encoding.equals("Cp936"))
    {
      return 0x7a;
    }
    if (encoding.equals("Cp949"))
    {
      return 0x79;
    }
    if (encoding.equals("Cp950"))
    {
      return 0x78;
    }
    if (encoding.equals("Cp1250"))
    {
      return 0xc8;
    }
    if (encoding.equals("Cp1251"))
    {
      return 0xc9;
    }
    if (encoding.equals("Cp1252"))
    {
      return 0x03;
    }
    if (encoding.equals("Cp1253"))
    {
      return 0xcb;
    }
    if (encoding.equals("Cp1254"))
    {
      return 0xca;
    }
    if (encoding.equals("Cp1255"))
    {
      return 0x7d;
    }
    if (encoding.equals("Cp1256"))
    {
      return 0x7e;
    }
    if (encoding.equals("Cp10000"))
    {
      return 0x04;
    }
    if (encoding.equals("Cp10006"))
    {
      return 0x98;
    }
    if (encoding.equals("Cp10007"))
    {
      return 0x96;
    }
    if (encoding.equals("Cp10029"))
    {
      return 0x97;
    }
    // Return default: Cp1252 - Windows ANSI
    Log.warn("Specified Encoding [" + encoding + "] is not defined, using default: Cp1252");
    return 0x03;
  }

  /**
   * writes the column header to the disk
   */
  public void writeColHeader(RandomAccessFile ff)
      throws IOException
  {
    colHeader[(numFields * DBF_COLDEF_SIZE)] = (byte) 0x0d;

    ff.seek(DBF_HEADER_SIZE);
    ff.write(colHeader);
  }

  public void readColHeader(RandomAccessFile ff)
      throws IOException
  {
    ff.seek(DBF_HEADER_SIZE);
    ff.readFully(colHeader);

    Vector sorted = new Vector();

    int pos = 1;
    int i = 0;
    int maxI = colHeader.length / 32;

    while ((colHeader[i * 32] != 0x0d) && (i < maxI))
    {
      tsColumn col = extractColdef(i);
      col.setBytePosition(pos);
      col.setTablePosition(i);
      sorted.add(col);
      i++;
      pos += calculateSize(col);
    }
    if (pos != getRecordLength())
    {
      Log.warn("calcuated recordlength does not match defined record length");
    }
    numFields = i;
    coldefsSorted = sorted;
  }

  /*
   * Make the data directory unless it already exists
   */
  private void mkDataDirectory(String dataDir)
      throws NullPointerException
  {
    File dd = new File(dataDir);

    if (!dd.exists())
    {
      dd.mkdir();
    }
  }

  private void setTimestamp()
  {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(new java.util.Date());
    int dd = cal.get(java.util.Calendar.DAY_OF_MONTH);
    int mm = cal.get(java.util.Calendar.MONTH) + 1;
    int yy = cal.get(java.util.Calendar.YEAR);
    yy = yy % 100;          // Y2K problem: only 2 digits
//      ff.seek(DATE_INDEX);
//      ff.write(yy);
//      ff.write(mm);
//      ff.write(dd);
    fileHeader[DATE_INDEX + 0] = (byte) yy;
    fileHeader[DATE_INDEX + 1] = (byte) mm;
    fileHeader[DATE_INDEX + 2] = (byte) dd;
  }


  public void updateNumberOfRecords(int numRecords, RandomAccessFile ff)
      throws IOException
  {
    setNumRecords(numRecords);
    writeFileHeader(ff);
  }

  /**
   * Update the header (index 4-7) with the new number of records
   * @param New number of records
   */
  private void setNumRecords(int numRecords)
  {
    this.numRecords = numRecords;
    byte[] intArray = Utils.intToLittleEndian(numRecords);
    fileHeader[4] = intArray[0];
    fileHeader[5] = intArray[1];
    fileHeader[6] = intArray[2];
    fileHeader[7] = intArray[3];
  }

  /**
   * Update the header (index 8-9) with the new number of records
   * @param numFields number of columns (used to calculate header length)
   */
  private void setNumberOfFields(int numFields) throws tinySQLException
  {
    this.numFields = numFields;
    adjustHeaderLength();
  }

  private void adjustHeaderLength()
  {
    if (type == TYPE_VISUAL_FOXPRO)
    {
      this.headerLength = (DBFHeader.BULK_SIZE + 1) + numFields * DBFHeader.BULK_SIZE + BACKLINK_STRUCTURE_SIZE;
    }
    else
    {
      this.headerLength = (DBFHeader.BULK_SIZE + 1) + numFields * DBFHeader.BULK_SIZE;
    }
    byte[] hlLE = Utils.shortToLittleEndian((short) headerLength);
    fileHeader[DBFHeader.LENGTH_OF_HEADER_INDEX + 0] = hlLE[0];
    fileHeader[DBFHeader.LENGTH_OF_HEADER_INDEX + 1] = hlLE[1];
  }


  /**
   * Update the header (index 10-11) with the length of one record
   * @param recordLength Length of one data record (row)
   */
  private void setRecordLength(int recordLength)
  {

    this.recordLength = recordLength;

    if (recordLength < 1)
    {
      fileHeader[DBFHeader.LENGTH_OF_REC_INDEX + 0] = 0;
      fileHeader[DBFHeader.LENGTH_OF_REC_INDEX + 1] = 0;
    }
    else
    {
      byte[] rlLE = Utils.shortToLittleEndian((short) recordLength);
      fileHeader[DBFHeader.LENGTH_OF_REC_INDEX + 0] = rlLE[0];
      fileHeader[DBFHeader.LENGTH_OF_REC_INDEX + 1] = rlLE[1];
    }
  }


  /**
   * Update the header (index 10-11) with the length of one record
   * @param recordLength Length of one data record (row)
   */
  private void setReserved() throws tinySQLException
  {
    byte[] reserved = new byte[DBFHeader.BULK_SIZE - DBFHeader.RESERVED_INDEX];
    for (int i = 0; i < reserved.length; i++)
    {
      fileHeader[DBFHeader.RESERVED_INDEX + i] = reserved[i];
    }
  }

  /**
   * Sets the columns for the new dbase file. This function is not valid
   * for loaded headers and will throw a tinySQLException if called in this
   * case.
   *
   * @throws IllegalStateException if the close is called a header
   * that was not created by create()
   */
  public void setColDefinitions(Vector v)
      throws tinySQLException
  {
    if (v.size() != numFields)
      throw new tinySQLException("the specified vector has not enough columndefs");

//    if (getNumberOfRecords () != 0)
//      throw new tinySQLException ("This function is only valid if the table is empty");
//
    try
    {
      int position = 1;

      for (int i = 0; i < v.size(); i++)
      {
        tsColumn col = (tsColumn) v.elementAt(i);
        setColdef(i, col);
        col.setBytePosition(position);
        col.setTablePosition(i);
        position += calculateSize(col);
      }
      coldefsSorted = new Vector(v);
      setRecordLength(position);
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }
  }


  private int calculateSize(tsColumn col)
  {
    if (col.getType() == Types.CLOB || col.getType() == Types.BLOB)
    {
      return 10;
    }
    if (col.getType() == Types.DATE)
    {
      return 8;
    }
    if (col.getType() == Types.TIMESTAMP)
    {
      return 8;
    }
    if (col.getType() == Types.INTEGER)
    {
      return 4;
    }
    return col.getSize();
  }

  /**
   * Inserts a column definition into the columnHeader. Handles FieldFlags.
   *
   * @param pos the position in the array where to insert the definition
   * @param coldef struct with column info
   */
  private void setColdef(int pos, tsColumn coldef)
      throws UnsupportedEncodingException, tinySQLException
  {
    int arrayPos = pos * DBF_COLDEF_SIZE;

    if (coldef.getPhysicalName().length() > 10)
    {
      throw new tinySQLException("IllegalName: Name must have a length of 10 or lesser.");
    }
    byte[] colName = Utils.forceToSize(coldef.getPhysicalName(),
        FIELD_TYPE_INDEX - FIELD_NAME_INDEX,
        (byte) 0, encoding);

    for (int i = 0; i < colName.length; i++)
    {
      colHeader[arrayPos + FIELD_NAME_INDEX + i] = colName[i];
    }

    // Convert the Java.SQL.Type back to a DBase Type and write it
    char type = ' ';
    int coldeftype = coldef.getType();
    switch (coldeftype)
    {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.BINARY:      // Binary data is marked with FIELD_FLAG_IS_BINARY
        {
          type = 'C';
          break;
          // if the size of the field exceeds 254, the type is mapped into
          // a memo field
        }
      case Types.BLOB:        // Binary data is marked with FIELD_FLAG_IS_BINARY
      case Types.CLOB:
        {
          if (supportsMemos())
          {
            // Binary bit is adjusted later ...
            type = 'M';
            break;
          }
          throw new tinySQLException("This table does not support MEMO");
        }
      case Types.TINYINT:
        {
          throw new tinySQLException("Inavlid type specified: TINYINT");
        }
      case Types.BIGINT:
        {
          throw new tinySQLException("Inavlid type specified: BIG INTEGER");
        }
      case Types.SMALLINT:
        {
          throw new tinySQLException("Inavlid type specified: SMALL INT");
        }
      case Types.INTEGER:
        {
          if (supportsInteger())
          {
            type = 'I';
            coldef.setSize(4, 0);
            break;
          }
          else
            throw new tinySQLException("This table does not support INTEGER");

        }
      case Types.FLOAT:
        {
          throw new tinySQLException("Inavlid type specified: FLOAT");
        }
      case Types.DOUBLE:
        {
          //type = 'D'; break;
          throw new tinySQLException("Inavlid type specified: DOUBLE");
        }
      case Types.REAL:
        {
          //type = 'F'; break;
          // A real is a binary number with floating point.
          // Reals are not supported in dbase, use double or float
          throw new tinySQLException("Inavlid type specified: REAL");
        }
      case Types.NUMERIC:
      case Types.DECIMAL:
        {
          type = 'N';
          break;
        }
      case Types.BIT:
        {
          if (supportsLogical())
          {
            type = 'L';
            coldef.setSize(1, 0);
            break;
          }
          else
            throw new tinySQLException("This table does not support LOGICAL");
        }
      case Types.DATE:
        {
          type = 'D';
          coldef.setSize(8, 0);
          break;
        }
      case Types.TIMESTAMP:
        {
          if (!supportsTimestamp())
          {
            throw new tinySQLException("This table does not support TIMESTAMP");
          }

          if (getType() == TYPE_VISUAL_FOXPRO)
          {
            type = 'T';
          }
          else
          {
            type = '@';
          }
          coldef.setSize(8, 4);
          break;
        }
      default:
        {
          throw new tinySQLException("Inavlid type specified, unable to determine type");
        }
    }
    colHeader[arrayPos + FIELD_TYPE_INDEX] = (byte) type;

    // InMEM Pointer ... we dont use this, set to null
    colHeader[arrayPos + IMU_INDEX + 0] = 0;
    colHeader[arrayPos + IMU_INDEX + 1] = 0;
    colHeader[arrayPos + IMU_INDEX + 2] = 0;
    colHeader[arrayPos + IMU_INDEX + 3] = 0;

    /**
     * If the type is not numeric, use a 16bit length field.
     */
    if (type == 'C')
    {
      byte[] b = Utils.shortToLittleEndian((short) coldef.getSize());
      colHeader[arrayPos + FIELD_LENGTH_INDEX] = b[0];
      colHeader[arrayPos + DECIMAL_COUNT_INDEX] = b[1];
    }
    else
    {
      colHeader[arrayPos + FIELD_LENGTH_INDEX] = (byte) coldef.getSize();
      colHeader[arrayPos + DECIMAL_COUNT_INDEX] = (byte) coldef.getDecimalPlaces();
    }

    colHeader[arrayPos + FIELD_FLAGS_INDEX] = getFieldFlags(coldef);

    byte[] colReserve = Utils.forceToSize(null,
        BULK_SIZE - FIELD_RESERVED_INDEX,
        (byte) 0, encoding);
    for (int i = 0; i < colReserve.length; i++)
    {
      colHeader[arrayPos + FIELD_RESERVED_INDEX + i] = colReserve[i];
    }
  }

  /**
   * if ths column is nullable, the FIELD_FLAG_IS_NULLABLE flag is set.
   * if the column is BINARY or BLOB, the column is mapped to FIELD_FLAG_IS_BINARY
   *
   * @return the fieldflag for the column
   */
  private byte getFieldFlags(tsColumn col)
  {
    short fieldFlags = 0;
    if (col.isNullable())
    {
      fieldFlags = FIELD_FLAG_IS_NULLABLE;
    }

    /**
     * Mark binary fields ...
     */
    if (col.getType() == Types.BINARY
        || col.getType() == Types.BLOB
        || col.getType() == Types.VARBINARY
        || col.getType() == Types.TIMESTAMP
        || col.getType() == Types.INTEGER
        || col.getType() == Types.DOUBLE)
    {
      fieldFlags |= FIELD_FLAG_IS_BINARY;
    }

    return (byte) fieldFlags;
  }

  /**
   * Extracts a columndefinition from the columnHeader array.
   *
   * Handles fieldflags and creates a new tsColumn.
   */
  private tsColumn extractColdef(int pos)
      throws UnsupportedEncodingException
  {
    int arrayPos = pos * DBF_COLDEF_SIZE;

    int strsize = seekNull(colHeader, arrayPos + FIELD_NAME_INDEX, 11);

    String name = new String(colHeader, arrayPos + FIELD_NAME_INDEX, strsize, encoding);
    char type = (char) colHeader[arrayPos + FIELD_TYPE_INDEX];
    int length = Utils.fixByte(colHeader[arrayPos + FIELD_LENGTH_INDEX]);
    int decimals = Utils.fixByte(colHeader[arrayPos + DECIMAL_COUNT_INDEX]);
    /**
     * If the type is character, use a 16bit length field.
     */
    if (type == 'C')
    {
      length = length + decimals * 256;
      decimals = 0;
    }

    short colFlags = colHeader[arrayPos + FIELD_FLAGS_INDEX];

    tsColumn retval = new tsColumn(null, name);
    retval.setType(charToSqlType(type));
    retval.setSize(length, decimals);

    // this may adjust the type as well
    checkFieldAfterRead(colFlags, retval);
    return retval;
  }

  /**
   * Sets the nullable flags and if the binary flag is set the type to
   * Types.BINARY or Types.BLOB
   */
  private void checkFieldAfterRead(short flags, tsColumn col)
  {
    if ((flags & FIELD_FLAG_IS_NULLABLE) == FIELD_FLAG_IS_NULLABLE)
      col.setNullable(true);
    if ((flags & FIELD_FLAG_IS_BINARY) == FIELD_FLAG_IS_BINARY)
    {
      if (col.getType() == Types.CHAR)
        col.setType(Types.BINARY);
      else if (col.getType() == Types.CLOB)
        col.setType(Types.BLOB);
      else if (col.getType() == Types.VARCHAR)
        col.setType(Types.BLOB);
    }
  }

  /**
   * seeks the first occurence of 0x00 in a byte array to terminate
   * Null-terminated strings.
   */
  private int seekNull(byte[] b, int offset, int len)
  {
    for (int i = offset; i < len + offset; i++)
    {
      byte by = b[i];
      if (by == 0)
      {
        return (i - offset);
      }
    }
    return len;
  }

  /**
   * 'C' Char (max 254 bytes)
   * 'N' '-.0123456789' (max 19 bytes)
   * 'L' 'YyNnTtFf?' (1 byte)
   * 'M' 10 digit .DBT block number
   * 'D' 8 digit YYYYMMDD
   *
   * See dbase.txt for more information on dbase types.
   * Uses java.sql.Types as key, needed for dbfFileDatabaseMetaData;
   *
   */
  protected static String typeToLiteral(int type)
  {
    if (type == Types.CHAR) return "CHAR";
    if (type == Types.NUMERIC) return "NUMERIC";
    if (type == Types.BIT) return "BOOLEAN";
    if (type == Types.INTEGER) return "INTEGER";
    if (type == Types.BINARY) return "BINARY";
    if (type == Types.DATE) return "DATE";
    if (type == Types.BLOB) return "BLOB";
    if (type == Types.CLOB) return "CLOB";
    if (type == Types.TIMESTAMP) return "TIMESTAMP";
    return "BINARY"; // fallback
  }


  /**
   'C' Char (max 254 bytes)
   'N' '-.0123456789' (max 19 bytes)
   'L' 'YyNnTtFf?' (1 byte)
   'M' 10 digit .DBT block number
   'D' 8 digit YYYYMMDD
   */
  protected int charToSqlType(char type)
  {
    if (type == 'C') return java.sql.Types.CHAR;
    if (type == 'N') return java.sql.Types.NUMERIC;
    if (type == 'L') return java.sql.Types.BIT;
    if (type == 'M') return java.sql.Types.CLOB;
    if (type == 'D') return java.sql.Types.DATE;
    if (type == 'I') return java.sql.Types.INTEGER;
    if (type == 'T') return java.sql.Types.TIMESTAMP;// MS FoxPro
    if (type == '@') return java.sql.Types.TIMESTAMP;// DBase
//    if (type.equals('F')) return java.sql.Types.FLOAT;
//    if (type.equals('2')) return java.sql.Types.SMALLINT; // A product called flagship
//    if (type.equals('4')) return java.sql.Types.LONGINT ; // A product called flagship
//    if (type.equals('8')) return java.sql.Types.DOUBLE;   // A product called flagship
//    if (type.equals('2')) return java.sql.Types.SMALLINT; // A product called flagship
//    if (type.equals('I')) return java.sql.Types.INTEGER;  // MS FoxPro
//    if (type.equals('B')) return java.sql.Types.DOUBLE;   // MS FoxPro
    return java.sql.Types.BINARY; // fallback
  }

  /**
   * Applys the file-meta-data of a table to an other, preserving recordlength,
   * and numberOfFields. NumberOfRecords has to be 0.
   */
  public void copyDBFHeader(DBFHeader header) throws tinySQLException
  {
    if (getRecordLength() != 0)
    {
      throw new tinySQLException("This function is only valid on empty/new tables");
    }

    // Copy the fileheader from the old table
    byte[] newFileHeader = new byte[DBF_HEADER_SIZE];
    System.arraycopy(header.fileHeader, 0, newFileHeader, 0, DBF_HEADER_SIZE);


    // preserve some values....
    // get the last update date
    newFileHeader[1] = fileHeader[1];
    newFileHeader[2] = fileHeader[2];
    newFileHeader[3] = fileHeader[3];

    // get the data position (where it starts in the file)
    // 8-9 Length of header
    newFileHeader[8] = fileHeader[8];
    newFileHeader[9] = fileHeader[9];

    // find out the length of the data portion
    // 10-11 Length of Record
    newFileHeader[10] = fileHeader[10];
    newFileHeader[11] = fileHeader[11];


    byte[] newColHeader = null;
    int normColHeaderLength = (numFields * DBF_HEADER_SIZE) + 1;

    // it seems that only Visual FoxPro uses the backlink structure
    // copy the column-definitions, append a backlink if needed.

    if (type == TYPE_VISUAL_FOXPRO)
    {
      newColHeader = new byte[normColHeaderLength + BACKLINK_STRUCTURE_SIZE];
      System.arraycopy(colHeader, 0, newColHeader, 0, colHeader.length);
      System.arraycopy(header.colHeader, normColHeaderLength, newColHeader, 0, BACKLINK_STRUCTURE_SIZE);
    }
    else
    {
      newColHeader = new byte[normColHeaderLength];
      System.arraycopy(colHeader, 0, newColHeader, 0, colHeader.length);
    }


  }

  /**
   * returns true if the type supports memos
   */
  public boolean supportsMemos()
  {
    // bit 3 and 7 are indicators for memo support
    if (((type & 0x04) == 0x04) || ((type & 0x80) == 0x80))
    {
      return true;
    }
    return false;
  }

  /**
   * All tables except db2 and foxbase support logical.
   * FoxBase is handled as DB2 until more data about supported
   * types is available.
   */
  public boolean supportsLogical()
  {
    if (type == TYPE_DB2 || type == TYPE_FOXBASE)
      return false;
    return true;
  }

  /**
   * All db4-tables and VisualFoxPro support integer values.
   */
  public boolean supportsInteger()
  {
    if (type == TYPE_VISUAL_FOXPRO ||
        type == TYPE_DB4_MEMO ||
        type == TYPE_DB4_SQLSYSTEM_NOMEMO ||
        type == TYPE_DB4_SQLTABLE_MEMO ||
        type == TYPE_DB4_SQLTABLE_NOMEMO)
    {
      return true;
    }
    return false;
  }

  /**
   * All db4-tables and VisualFoxPro support timestamp values.
   */
  public boolean supportsTimestamp()
  {
    if (type == TYPE_VISUAL_FOXPRO ||
        type == TYPE_DB4_MEMO ||
        type == TYPE_DB4_SQLSYSTEM_NOMEMO ||
        type == TYPE_DB4_SQLTABLE_MEMO ||
        type == TYPE_DB4_SQLTABLE_NOMEMO)
    {
      return true;
    }
    return false;
  }
}

