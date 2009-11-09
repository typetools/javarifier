/*
 * Utils.java
 *
 * tinySQL, some helper methods
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
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

/**
Some helper methods for tinySQL
 @author Brian Jepson <bjepson@home.com>
 @author Marcel Ruff <ruff@swand.lake.de> Added write access to dBase and JDK 2 support
 */
public class Utils
{
  /**
   * Converts a long to a little-endian four-byte array
   *
   */
  public final static byte[] intToLittleEndian(int val)
  {
    byte[] b = new byte[4];
    for (int i = 0; i < 4; i++)
    {
      b[i] = (byte) (val % 256);
      val = val / 256;
    }
    return b;
  }


  /**
   * Converts a long to a little-endian two-byte array
   *
   */
  public final static byte[] shortToLittleEndian(short val)
  {
    byte[] b = new byte[2];
    for (int i = 0; i < 2; i++)
    {
      b[i] = (byte) (val % 256);
      val = (short) (val / 256);
    }
    return b;
  }


  /**
   *
   * Converts a little-endian four-byte array to a long,
   * represented as a double, since long is signed.
   *
   * I don't know why Java doesn't supply this. It could
   * be that it's there somewhere, but I looked and couldn't
   * find it.
   *
   */
  public final static double vax_to_long(byte[] b)
  {

    //existing code that has been commented out
    //return fixByte(b[0]) + ( fixByte(b[1]) * 256) +
    //( fixByte(b[2]) * (256^2)) + ( fixByte(b[3]) * (256^3));

    // Fix courtesy Preetha Suri <Preetha.Suri@sisl.co.in>
    //
    long lngTmp = (long) (0x0ffL & b[0])
        | ((0x0ffL & (long) b[1]) << 8)
        | ((0x0ffL & (long) b[2]) << 16)
        | ((0x0ffL & (long) b[3]) << 24);


    return ((double) lngTmp);

  }


  /**
   *
   * Converts a little-endian four-byte array to a short,
   * represented as an int, since short is signed.
   *
   * I don't know why Java doesn't supply this. It could
   * be that it's there somewhere, but I looked and couldn't
   * find it.
   *
   */
  public final static int vax_to_short(byte[] b)
  {
    return (int) (fixByte(b[0]) + (fixByte(b[1]) * 256));
  }


  /*
   *
   * bytes are signed; let's fix them...
   *
   */
  public final static short fixByte(byte b)
  {

    if (b < 0)
    {
      return (short) (b + 256);
    }
    return b;
  }


  /**
   Cut or padd the string to the given size
   @param a string
   @param size the wanted length
   @param padByte char to use for padding
   @return the string with correct lenght, padded with pad if necessary
   */
  public final static byte[] forceToSize(String str, int size, byte padByte, String encoding)
      throws java.io.UnsupportedEncodingException
  {
    if (str != null && str.length() == size)
    {
      return str.getBytes(encoding);
    }

    byte[] result = new byte[size];

    if (str == null)
    {
//      for (int ii = 0; ii<size; ii++) result[ii] = padByte;
      Arrays.fill(result, padByte);
      return result;
    }

    if (str.length() > size)
      return str.substring(0, size).getBytes(encoding);  // do cutting

    // do padding (Faster Padding with native methods)
    byte[] tmp = str.getBytes(encoding);
    System.arraycopy(tmp, 0, result, 0, tmp.length);
    Arrays.fill(result, tmp.length, result.length, padByte);

    return result;
  }

  /**
   Cut or padd the string to the given size
   @param a string
   @param size the wanted length
   @param padByte char to use for padding
   @return the string with correct lenght, padded with pad if necessary
   */
  public final static char[] forceToSize(String str, int size, char padByte)
      throws java.io.UnsupportedEncodingException
  {
    if (str != null && str.length() == size)
    {
      return str.toCharArray();
    }

    char[] result = new char[size];

    if (str == null)
    {
      Arrays.fill(result, padByte);
      return result;
    }

    if (str.length() > size)
      return str.substring(0, size).toCharArray();  // do cutting

    // do padding (Faster Padding with native methods)
    str.getChars(0, str.length(), result, 0);
    Arrays.fill(result, str.length(), result.length, padByte);

    return result;
  }


  /**
   Cut or padd the string to the given size
   @param a string
   @param size the wanted length
   @param padByte char to use for padding
   @return the string with correct lenght, padded with pad if necessary
   */
  public final static byte[] forceToSizeLeft(String str, int size, byte padByte, String encoding)
      throws java.io.UnsupportedEncodingException
  {
    if (str != null && str.length() == size)
    {
      return str.getBytes(encoding);
    }

    byte[] result = new byte[size];

    if (str == null)
    {
      Arrays.fill(result, padByte);
      return result;
    }

    if (str.length() > size)
    {
      return str.substring(0, size).getBytes(encoding);  // do cutting
    }

    // do padding (Faster Padding with native methods)
    byte[] tmp = str.getBytes(encoding);
    int padSize = result.length - tmp.length;
    System.arraycopy(tmp, 0, result, padSize, tmp.length);
    Arrays.fill(result, 0, padSize, padByte);

    return result;
  }


  /**
   Cut or padd the string to the given size
   @param a string
   @param size the wanted length
   @param padChar char to use for padding (must be of length()==1!)
   @return the string with correct lenght, padded with pad if necessary
   */
  public static String forceToSizeLeft(String str, int size, char padChar)
  {
    if (str != null && str.length() == size)
      return str;

    StringBuffer tmp;
    if (str == null)
    {
      tmp = new StringBuffer(size);
    }
    else
    {
      tmp = new StringBuffer(str);
    }

    if (tmp.length() > size)
    {
      tmp.setLength(size);
      return tmp.toString();  // do cutting
    }
    else
    {
      StringBuffer t2 = new StringBuffer(size);

      int arsize = size - tmp.length();
      char[] ar = new char[arsize];
      for (int i = 0; i < arsize; i++)
      {
        ar[i] = padChar;
      }
      t2.append(ar);
      t2.append(tmp);
      return t2.toString();
    }
  }


  /*
   * Delete a file in the data directory
   */
  public final static void delFile(String fname) throws NullPointerException, IOException
  {
    File f = new File(fname);

    // only delete a file that exists
    //
    if (f.exists())
    {
      // try the delete. If it fails, complain
      //
      if (!f.delete())
      {
        throw new IOException("Could not delete file: " + fname + ".");
      }
    }
    else
      Log.debug("File: " + fname + " does not exist. No action taken on delete.");
  }

  public final static void delFile(String dataDir, String fname) throws NullPointerException, IOException
  {

    File f = new File(dataDir + File.separator + fname);

    // only delete a file that exists
    //
    if (f.exists())
    {
      // try the delete. If it fails, complain
      //
      if (!f.delete())
      {
        throw new IOException("Could not delete file: " +
            dataDir + "/" + fname + ".");
      }
    }
  }


  /**
   rename a file
   @return true if succeeded
   */
  public final static boolean renameFile(String oldName, String newName)
  {
    Log.warn("[Rename] " + oldName + " to " + newName);
    File f_old = new File(oldName);
    File f_new = new File(newName);
    if (f_old.exists() == false)
    {
      Log.warn("File " + oldName + " does not exist");
      return false;
    }

    if (f_new.exists() == true)
    {
      Log.warn("File " + newName + " exist");
      return false;
    }

    boolean ret = f_old.renameTo(f_new);
    return ret;
  }


  /**
   Strip the path and suffix of a file name
   @param file   "/usr/local/dbase/test.DBF"
   @return "test"
   */
  public final static String stripPathAndExtension(final String file)
  {
    String sep = File.separator;
    int begin = file.lastIndexOf(sep);
    if (begin < 0)
      begin = 0;
    else
      begin++;
    int end = file.lastIndexOf(".");
    if (end < 0) end = file.length();
    String str = file.substring(begin, end);
    return str;
  }


  /**
   Scan the given directory for files containing the substrMatch<br>
   Small case extensions '.dbf' are recognized and returned as '.DBF'
   @param path   eg "/usr/local/dbase"
   @suffix       Case insensitive: eg ".DBF"
   */
  public final static Vector getAllFiles(final String path, final String suffix)
  {
    class MyDir extends javax.swing.filechooser.FileSystemView
    {
      public File createNewFolder(File containingDir)
      {
        return null;
      }

      public File[] getRoots()
      {
        return null;
      }

      public boolean isHiddenFile(File f)
      {
        return false;
      }

      public boolean isRoot(File f)
      {
        return false;
      }
    }
    MyDir view = new MyDir();
    Vector vec = new Vector(20);
    File dir = view.createFileObject(path);
    File[] ff = view.getFiles(dir, false);
    String upperSuffix = null;
    if (suffix != null)
      upperSuffix = suffix.toUpperCase();
    for (int ii = 0; ii < ff.length; ii++)
    {
      String file = ff[ii].toString().toUpperCase();
      if (upperSuffix == null || file.endsWith(upperSuffix))
      {
        vec.addElement(ff[ii]);
      }
    }
    return vec;
  }

  /**
   * Prints a resultset. This function is similiar to the exampleFunction in
   * queryDbf, and its usefull enough to put it into the utils collection.
   */
  public static int printResultSet(ResultSet rs, PrintStream out)
      throws SQLException
  {
    // Get information about the result set.  Set the column
    // width to whichever is longer: the length of the label
    // or the length of the data.
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] columnLabels = new String[columnCount];
    int[] columnWidths = new int[columnCount];
    for (int i = 1; i <= columnCount; ++i)
    {
      columnLabels[i - 1] = rsmd.getColumnLabel(i);
      columnWidths[i - 1] = Math.max(columnLabels[i - 1].length(),
          rsmd.getColumnDisplaySize(i));
    }

    // Output the column headings.
    StringBuffer logString = new StringBuffer();
    for (int i = 1; i <= columnCount; ++i)
    {
      logString.append(format(rsmd.getColumnLabel(i), columnWidths[i - 1]) + " ");
    }
    out.println(logString.toString());

    // Output a dashed line.
    StringBuffer dashedLine = new StringBuffer();
    for (int i = 1; i <= columnCount; ++i)
    {
      for (int j = 1; j <= columnWidths[i - 1]; ++j)
        dashedLine.append("-");
      dashedLine.append(" ");
    }
    out.println(dashedLine.toString());

    // Iterate throught the rows in the result set and output
    // the columns for each row.
    int retval = 0;
    while (rs.next())
    {
      logString.delete(0, logString.length());
      for (int i = 1; i <= columnCount; ++i)
      {
        String value = rs.getString(i);
        if (rs.wasNull())
          value = "<null>";

        if (value == null)
          value = "<!null>";

        logString.append(format(value, columnWidths[i - 1]) + " ");
      }
      out.println(logString.toString());
      retval++;
    }
    return retval;
  }

  // Format a string so that it has the specified width.
  public static String format(String s, int width)
  {
    String formattedString;

    // The string is shorter than specified width,
    // so we need to pad with blanks.
    if (s.length() < width)
    {
      StringBuffer buffer = new StringBuffer(s);
      for (int i = s.length(); i < width; ++i)
        buffer.append(" ");
      formattedString = buffer.toString();
    }

    // Otherwise, we need to truncate the string.
    else
      formattedString = s.substring(0, width);

    return formattedString;
  }

}

