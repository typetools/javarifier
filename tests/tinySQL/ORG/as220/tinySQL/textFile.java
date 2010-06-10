/*
 *
 * textFile - an extension of tinySQL for text file access
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

import ORG.as220.tinySQL.sqlparser.ColumnDefinition;
import ORG.as220.tinySQL.sqlparser.CreateTableStatement;
import ORG.as220.tinySQL.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Types;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

public class textFile extends tinySQL
{

  public static final int DELETE_DEFAULT = -1;
  public static final int DELETE_NONE = 1;
  public static final int DELETE_PACK = 2;

  public static final int INSERT_DEFAULT = 0;
  public static final int INSERT_SIZE = 1;
  public static final int INSERT_SPEED = 2;

  // the data directory where textFile stores its files
  //
  private String dataDir;// = System.getProperty("user.home") + "/.tinySQL";
  private Hashtable tables;
  private String encoding;
  private boolean readOnly;
  private Properties p;

  private byte[] delpref;
  private byte[] delpost;
  private byte[] colpref;
  private byte[] colpost;
  private byte[] rowpref;
  private byte[] rowpost;
  private byte[] tablepref;
  private byte[] tablepost;

  private String defext;
  private String tableext;
  private String quoting;
  private textFileQuoting quotingEngine;
  private int deleteMode;
  private int insertMode;
  private boolean ignoreLastColumnPostfix;
  private boolean ignoreFirstColumnPrefix;

  public textFile(String datadir, Properties p) throws tinySQLException
  {
    this.dataDir = datadir;
    setProperties(p);
  }

  public void setProperties(Properties p) throws tinySQLException
  {
    this.p = p;
    this.encoding = p.getProperty("encoding", "Cp1252");
    this.readOnly = p.getProperty("readonly", "false").equalsIgnoreCase("true");
    this.defext = p.getProperty("definition-extension", ".def");
    this.tableext = p.getProperty("table-extension", "");
    this.quoting = p.getProperty("qouting.engine", textFileQuoting.class.getName());
    quotingEngine = loadTextFileQuoting(quoting);
    String delpref = p.getProperty("delete.prefix", "");
    String delpost = p.getProperty("delete.postfix", "");
    String colpref = p.getProperty("column.prefix", "");
    String colpost = p.getProperty("column.postfix", "");
    String rowpref = p.getProperty("row.prefix", "");
    String rowpost = p.getProperty("row.postfix", "\n");
    String tablepref = p.getProperty("table.prefix", "");
    String tablepost = p.getProperty("table.postfix", "");
    String deleteMode = p.getProperty("config.delete", "default").trim(); // may be none or pack
    String insertMode = p.getProperty("config.insert", "default").trim();

    String ignoreFirstColumnPrefix = p.getProperty("ignore.first.column.prefix", "false");
    String ignoreLastColumnPostfix = p.getProperty("ignore.first.column.postfix", "false");

    this.ignoreFirstColumnPrefix = ignoreFirstColumnPrefix.trim().equalsIgnoreCase("true");
    this.ignoreLastColumnPostfix = ignoreLastColumnPostfix.trim().equalsIgnoreCase("true");

    if (deleteMode.equals("none"))
    {
      this.deleteMode = DELETE_NONE;
    }
    else if (deleteMode.equals("pack"))
    {
      this.deleteMode = DELETE_PACK;
    }
    else
    {
      this.deleteMode = DELETE_DEFAULT;
    }

    if (insertMode.equals("speed"))
      this.insertMode = INSERT_SPEED;
    else if (insertMode.equals("size"))
      this.insertMode = INSERT_SIZE;
    else
      this.insertMode = INSERT_DEFAULT;

    try
    {
      this.delpref = delpref.getBytes(encoding);
      this.delpost = delpost.getBytes(encoding);
      this.colpref = colpref.getBytes(encoding);
      this.colpost = colpost.getBytes(encoding);
      this.rowpref = rowpref.getBytes(encoding);
      this.rowpost = rowpost.getBytes(encoding);
      this.tablepref = tablepref.getBytes(encoding);
      this.tablepost = tablepost.getBytes(encoding);
    }
    catch (UnsupportedEncodingException use)
    {
      throw new tinySQLException(use);
    }
  }

  private textFileQuoting loadTextFileQuoting(String classname) throws tinySQLException
  {
    try
    {
      Class c = Class.forName(classname);
      textFileQuoting o = (textFileQuoting) c.newInstance();
      o.setDatabase(this);
      o.init();
      return o;
    }
    catch (Exception e)
    {
      throw new tinySQLException("QuotingEngine not found: " + classname);
    }
  }

  public Properties getProperties()
  {
    return p;
  }

  public textFileQuoting getQuoting()
  {
    return quotingEngine;
  }

  public boolean isIgnoringFirstColumnPrefix()
  {
    return ignoreFirstColumnPrefix;
  }

  public boolean isIgnoringLastColumnPostfix()
  {
    return ignoreLastColumnPostfix;
  }

  /**
   * @return either DELETE_NONE, DELETE_PACK or DELETE_DEFAULT
   */
  public int getDeleteMode()
  {
    Log.debug("Default DeleteMode : " + deleteMode);

    return deleteMode;
  }

  public int getInsertMode()
  {
    return insertMode;
  }

  public boolean isReadOnly()
  {
    return readOnly;
  }

  /**
   * Creates a table given the name and a vector of
   * column definition (tsColumn) arrays.<br>
   *
   * @param table_name the name of the table
   * @param v a Vector containing arrays of column definitions.
   * @see tinySQL#CreateTable
   */
  public boolean CreateTable(CreateTableStatement stmt)
      throws tinySQLException
  {

    if (isReadOnly())
    {
      throw new tinySQLException("Database is readonly");
    }
    try
    {
      String table_name = stmt.getTable();
      Vector coldefs = stmt.getColumnDefinitions();

      int numCols = coldefs.size();
      int recordLength = 1;        // 1 byte for the flag field

      Vector v = new Vector();
      for (int i = 0; i < numCols; i++)
      {
        ColumnDefinition coldef = (ColumnDefinition) coldefs.elementAt(i);
        tsColumn col = coldef.getColumn();
        v.add(col);
      }

      // make the data directory, if it needs to be make
      //
      mkDataDirectory();

      db_createTable(table_name, v);
    }
    catch (IOException ioe)
    {
      throw new tinySQLException("Create Table failed", ioe);
    }
    return false;
  }

  public String getTableExtension()
  {
    return tableext;
  }

  public String getDefinitionExtension()
  {
    return defext;
  }

  public String getEncoding()
  {
    return encoding;
  }

  public byte[] getDelPostfix()
  {
    return delpost;
  }

  public byte[] getDelPrefix()
  {
    return delpref;
  }

  public byte[] getColumnPostfix()
  {
    return colpost;
  }

  public byte[] getColumnPrefix()
  {
    return colpref;
  }

  public byte[] getRowPostfix()
  {
    return rowpost;
  }

  public byte[] getRowPrefix()
  {
    return rowpref;
  }

  public byte[] getTablePostfix()
  {
    return tablepost;
  }

  public byte[] getTablePrefix()
  {
    return tablepref;
  }


  /**
   * Low level function of physicaly removing a table. While drop table
   * may alter associated tables and databases, this function simply
   * removes any files needed for the data table.
   */
  protected void db_removeTable(String table_name) throws IOException
  {
    Utils.delFile(dataDir + File.separator + table_name + getTableExtension());
    Utils.delFile(dataDir + File.separator + table_name + getDefinitionExtension());
  }

  /**
   * Low level function of physicaly removing a table. While drop table
   * may alter associated tables and databases, this function simply
   * removes any files needed for the data table.
   */
  protected void db_renameTable(String table_name, String newname) throws IOException
  {
    String source = dataDir + File.separator + table_name;
    String dest = dataDir + File.separator + newname;
    if (Utils.renameFile(source + getTableExtension(), dest + getTableExtension()) == false ||
        Utils.renameFile(source + getDefinitionExtension(), dest + getDefinitionExtension()) == false)
      throw new IOException("Renaming of table " + table_name + " to " + newname + " failed");
  }

  /**
   * Create the files for the table. While CreateTable may update some
   * arbitary database structures, this function just creates the specifiy
   * file structures needed for a data table.
   */
  protected void db_createTable(String table_name, Vector v)
      throws IOException, tinySQLException
  {
    // create the table definition file
    //
    BufferedOutputStream fdef =
        new BufferedOutputStream(
            new FileOutputStream(dataDir + File.separator + table_name + getDefinitionExtension()
            ));

    // open it as a DataOutputStream
    //
    DataOutputStream def = new DataOutputStream(fdef);

    if (deleteMode == DELETE_DEFAULT)
    {
      // write out the column definition for the _DELETED column
      //
      def.writeBytes("CHAR|_DELETED|1\n");
    }

    // write out the rest of the columns' definition. The
    // definition consists of datatype, column name, and
    // size delimited by a pipe symbol
    //

    for (int i = 0; i < v.size(); i++)
    {
      tsColumn col = (tsColumn) v.elementAt(i);

      int type = col.getType();
      String stype;
      switch (type)
      {
        case Types.INTEGER:
          stype = "NUMERIC";
          col.setSize(10, 0);
          break;
        case Types.NUMERIC:
          stype = "NUMERIC";
          break;
        case Types.CHAR:
          stype = "CHAR";
          break;
        case Types.DATE:
          stype = "DATE";
          col.setSize(10, 0);
          break;
        default:
          throw new tinySQLException("Unsupported text file type: " + type);
      }
      def.writeBytes(stype + "|");
      def.writeBytes(col.getPhysicalName() + "|");
      def.writeBytes(col.getSize() + "\n");
    }

    // flush the DataOutputStream and jiggle the handle
    //
    def.flush();
    // close the file
    //
    fdef.close();

    // create the table definition file
    //
    fdef = new BufferedOutputStream(new FileOutputStream(dataDir + File.separator + table_name + getTableExtension()));
    fdef.write(getTablePrefix());
    fdef.write(getTablePostfix());
    fdef.close();

  }

  /**
   *
   * Return a tinySQLTable object, given a table name.
   *
   * @param table_name
   * @see tinySQL#getTable
   *
   */
  public tinySQLTable openTable(String table_name) throws tinySQLException
  {
    Log.debug("TextFile-Engine: Opening table " + table_name);
    return (tinySQLTable) new textFileTable(dataDir, table_name, this);
  }


  /*
   *
   * Make the data directory unless it already exists
   *
   */
  private void mkDataDirectory() throws NullPointerException
  {

    File dd = new File(dataDir);

    if (!dd.exists())
    {
      dd.mkdir();
    }

  }
}

