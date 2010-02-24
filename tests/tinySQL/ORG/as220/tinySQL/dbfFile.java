/*
 *
 * dbfFile - an extension of tinySQL for dbf file access
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

import ORG.as220.tinySQL.sqlparser.AlterTableRenameColumnStatement;
import ORG.as220.tinySQL.sqlparser.ColumnDefinition;
import ORG.as220.tinySQL.sqlparser.CreateTableStatement;
import ORG.as220.tinySQL.sqlparser.DropTableStatement;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.Vector;

/**
 * dBase read/write access <br>
 * @author Brian Jepson <bjepson@home.com>
 * @author Marcel Ruff <ruff@swand.lake.de> Added write access to dBase and JDK 2 support
 * @author Thomas Morgner <mgs@sherito.org> Changed ColumnName to 11 bytes and strip name
 *  after first occurence of 0x00.
 *  Types are now handled as java.sql.Types, not as character flag
 */
public class dbfFile extends tinySQL
{
  public static final int INSERT_DEFAULT = 0;
  public static final int INSERT_SIZE = 1;
  public static final int INSERT_SPEED = 2;

  /// The current working directory
  private String dataDir;

  /// the encoding is used for storing unicode characters into an bytestream
  private String encoding;

  // should tinySQL determine the type of the encoding automaticly?
  private boolean autoEnc;

  // is this database readonly? if yes, every write-access will throw an exception
  private boolean readOnly;

  // the current properties used for this instance of tinySQL. every instance
  // may have its own properties
  private Properties p;

  // what table type to use when creating new tables?
  private short tabletype;

  private int deleteMode;
  private int insertMode;

  /**
   *
   * Constructs a new dbfFile object
   *
   * @param d directory with which to override the default data directory
   * @param p the properties of this instance
   * @see dbfFileDriver for details on which properties are available
   */
  public dbfFile(String d, Properties p)
  {
    super();

    dataDir = d; // d is usually extracted from the connection URL
    setProperties(p);
  }


  /**
   * Set some new properties. The changed properties will affect only new actions.
   * Tables created with old properties will remain unchanged and have to be
   * reopened to use the new settings.
   */
  public void setProperties(Properties p)
  {

    if (p == null)
      throw new NullPointerException();

    this.p = p;
    this.encoding = p.getProperty("encoding", "Cp1252");
    this.autoEnc = p.getProperty("autoenc", "true").equalsIgnoreCase("true");
    this.readOnly = p.getProperty("readonly", "false").equalsIgnoreCase("true");
    this.tabletype = getTableType(p.getProperty("tabletype", "db3"));
    String insertMode = p.getProperty("config.insert", "default");

    if (insertMode.equals("speed"))
      this.insertMode = INSERT_SPEED;
    else if (insertMode.equals("size"))
      this.deleteMode = INSERT_SIZE;
    else
      this.deleteMode = INSERT_DEFAULT;

  }

  private short getTableType(String type)
  {
    if (type.equalsIgnoreCase("db3"))
      return DBFHeader.TYPE_DB3;

    if (type.equalsIgnoreCase("db3memo"))
      return DBFHeader.TYPE_DB3_MEMO;

    if (type.equalsIgnoreCase("db4memo"))
      return DBFHeader.TYPE_DB4_MEMO;

    if (type.equalsIgnoreCase("db4sqlsys"))
      return DBFHeader.TYPE_DB4_SQLSYSTEM_NOMEMO;

    if (type.equalsIgnoreCase("db4sqlmemo"))
      return DBFHeader.TYPE_DB4_SQLTABLE_MEMO;

    if (type.equalsIgnoreCase("db4sql"))
      return DBFHeader.TYPE_DB4_SQLTABLE_NOMEMO;

    if (type.equalsIgnoreCase("db2"))
      return DBFHeader.TYPE_DB2;

    if (type.equalsIgnoreCase("foxbase"))
      return DBFHeader.TYPE_FOXBASE;

    if (type.equalsIgnoreCase("foxpro2"))
      return DBFHeader.TYPE_FOXPRO2;

    if (type.equalsIgnoreCase("visualfoxpro"))
      return DBFHeader.TYPE_VISUAL_FOXPRO;

    return DBFHeader.TYPE_DB3;
  }


  /**
   * sets this database readonly.
   *
   * @param b the new readOnly flag
   */
  public void setReadOnly(boolean b)
  {
    readOnly = b;
  }

  /**
   * Checks whether this database is readonly
   *
   * @returns true, if the database is readonly
   */
  public boolean isReadOnly()
  {
    return readOnly;
  }

  /**
   *
   * Creates a table given the name and a vector of
   * column definition (tsColumn) arrays.
   *
   * @param statement the CreateTableStatement created by the parser
   * @see tinySQL#CreateTable
   * @throws tinySQLException if the table could not be created, the
   * tabledefinition contained invalid values or the database is readonly.
   */
  public boolean CreateTable(CreateTableStatement statement)
      throws tinySQLException
  {
    try
    {
      if (readOnly)
        throw new tinySQLException("dbfFile is in readonly mode");

      String tableName = statement.getTable();
      Vector coldefs = statement.getColumnDefinitions();

      //---------------------------------------------------
      // determin meta data ....

      int numCols = coldefs.size();
      int recordLength = 1;        // 1 byte for the flag field

      Vector v = new Vector();
      for (int i = 0; i < numCols; i++)
      {
        ColumnDefinition coldef = (ColumnDefinition) coldefs.elementAt(i);
        tsColumn col = coldef.getColumn();
        v.add(col);
      }

      db_createTable(tableName, v);
      // False signals we have not created a resultset.
      return false;
    }
    catch (IOException ioe)
    {
      throw new tinySQLException(ioe);
    }
  }

// This check is now done in DBFHeader. Evil me writing such a function here ..
//  /**
//   * Checks the validity of the given column. if the column is a special
//   * type (DATE or INTEGER) without an userdefined size, then the default
//   * sizes are set.
//   *
//   * @throws tinySQLException if an unsupported type is supplied.
//   */
//  private void checkTsColumn (tsColumn col) throws tinySQLException
//  {
//    if (col.getPhysicalName ().length () > 11)
//      throw new tinySQLException ("dbfFile-ColumnNames must be smaller than 11 characters");
//
//    if (col.getType () == Types.DATE)
//    {
//      col.setSize (8,0);
//    }
//    else
//    if (col.getType () == Types.TIME)
//    {
//      //col.setSize (8,0);
//      throw new tinySQLException ("this driver does not support TIME");
//    }
//    else
//    if (col.getType () == Types.TIMESTAMP)
//    {
//      throw new tinySQLException ("this driver does not support TIMESTAMP");
//      //col.setSize (8,0);
//    }
//    else
//    if (col.getType () == Types.INTEGER)
//    {
//      col.setSize (4,0);
//    }
//  }
//

  /**
   *
   * Rename columns
   *
   * ALTER TABLE table RENAME war TO peace
   *
   * @param stmt the AlterTableDropColumnStatement created by the parser
   * @see tinySQL#AlterTableDropCol
   *
   * @throws tinySQLException if the table could not be changed, the
   * tabledefinition contained invalid values or the database is readonly.
   */
  public boolean AlterTableRenameCol(AlterTableRenameColumnStatement statement)
      throws tinySQLException
  {
    if (readOnly)
      throw new tinySQLException("dbfFile is in readonly mode");

    String tableName = statement.getTable().getName();
    String fullpath = dataDir + File.separator + tableName + dbfFileTable.DBF_EXTENSION;

    closeTable(tableName);

    // this is not so difficult, read the Header, get the columns,
    // rename the one and write the header back.
    // Anyway, we close every reference to the table, as it will break resultsets
    // and statements currently on the way
    try
    {
      RandomAccessFile ftbl = new RandomAccessFile(fullpath, "rw");

      DBFHeader dbfHeader = new DBFHeader(encoding, true); // read the first 32 bytes ...
      dbfHeader.initializeHeader(ftbl);
      dbfHeader.readColHeader(ftbl);

      Vector coldefs = dbfHeader.getFields();

      Vector v = statement.getColumns();
      for (int i = 0; i < v.size(); i++)
      {
        AlterTableRenameColumnStatement.RenameColumnTuple t =
            (AlterTableRenameColumnStatement.RenameColumnTuple) v.elementAt(i);
        tsColumn col = t.oldcol;
        col.setPhysicalName(t.newname);
        coldefs.setElementAt(col, col.getTablePosition());
      }

      dbfHeader.setColDefinitions(coldefs);
      dbfHeader.writeFileHeader(ftbl);
      dbfHeader.writeColHeader(ftbl);
      ftbl.close();
    }
    catch (Exception e)
    {
      throw new tinySQLException(e);
    }

    // False signals we have not created a resultset.
    return false;

  }

  /**
   *
   * Return a tinySQLTable object, given a table name. Is called only once,
   * every call to getTable() will return a new tinySQLTableView-Object.
   *
   * @param tableName
   * @see tinySQL#getTable
   *
   */
  public tinySQLTable openTable(String tableName) throws tinySQLException
  {
    tinySQLTable table = null;
    table = new dbfFileTable(dataDir, tableName, encoding, readOnly, autoEnc);
    return table;
  }

  /**
   * Low level function of physicaly removing a table. While drop table
   * may alter associated tables and databases, this function simply
   * removes any files needed for the data table.
   */
  protected void db_removeTable(String table_name) throws tinySQLException, IOException
  {
    if (readOnly)
      throw new tinySQLException("dbfFile is in readonly mode");

    Utils.delFile(dataDir + File.separator + table_name + dbfFileTable.DBF_EXTENSION);
  }

  /**
   * Low level function of physicaly removing a table. While drop table
   * may alter associated tables and databases, this function simply
   * removes any files needed for the data table.
   */
  protected void db_renameTable(String table_name, String newname) throws tinySQLException, IOException
  {
    if (readOnly)
      throw new tinySQLException("dbfFile is in readonly mode");

    String source = dataDir + File.separator + table_name + dbfFileTable.DBF_EXTENSION;
    String dest = dataDir + File.separator + newname + dbfFileTable.DBF_EXTENSION;
    if (Utils.renameFile(source, dest) == false)
      throw new IOException("Renaming of " + table_name + " to " + newname + " failed");
  }

  /**
   * Create the files for the table. While CreateTable may update some
   * arbitary database structures, this function just creates the specifiy
   * file structures needed for a data table.
   */
  protected void db_createTable(String table_name, Vector v)
      throws IOException, tinySQLException
  {
    if (readOnly)
      throw new tinySQLException("dbfFile is in readonly mode");


    int size = v.size();

    // create the new table ...
    //---------------------------------------------------
    // create the new dBase file ...
    DBFHeader dbfHeader = new DBFHeader(size, tabletype, encoding);
    dbfHeader.create(dataDir, table_name);

    //---------------------------------------------------
    // write out the rest of the columns' definition.
    dbfHeader.setColDefinitions(v);
    dbfHeader.close();

  }

  protected void db_copyTableMeta(String orgTable, String newname, Vector v)
      throws IOException, tinySQLException
  {
    dbfFileTable table = new dbfFileTable(dataDir, orgTable, encoding, true, autoEnc);
    DBFHeader header = table.getHeader();
    table.close();

    if (readOnly)
      throw new tinySQLException("dbfFile is in readonly mode");

    int size = v.size();

    // create the new table ...
    //---------------------------------------------------
    // create the new dBase file ... by copying the old headers data
    DBFHeader dbfHeader = new DBFHeader(header, size, encoding);
    dbfHeader.create(dataDir, newname);

    //---------------------------------------------------
    // write out the rest of the columns' definition.
    dbfHeader.setColDefinitions(v);
    dbfHeader.close();

  }


  /**
   *
   * The DBF File class provides read-only access to DBF
   * files, so this baby should throw an exception.
   *
   * @param statement the DropTableStatement created by the parser
   * @see tinySQL#DropTable
   *
   */
  public void DropTable(DropTableStatement statement) throws tinySQLException
  {

    if (readOnly)
      throw new tinySQLException("dbfFile is in readonly mode");

    super.DropTable(statement);

  }

}
