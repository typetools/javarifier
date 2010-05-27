/*
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
package ORG.as220.tinySQL.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

/**
 * The character entity parser replaces all known occurences of an entity
 * in the format &amp;entityname;.
 *
 * depends on: none
 */
public class CharacterEntityParser
{
  private static CharacterEntityParser parser = null;
  private static Properties entities = null;
  private static Hashtable chars = null;

  private CharacterEntityParser()
  {
    entities = CharEntityTable.createTable();
    chars = new Hashtable();
    Log.debug("Entities.size " + entities.size());

    Enumeration enum = entities.keys();
    while (enum.hasMoreElements())
    {
      String key = (String) enum.nextElement();
      String value = (String) entities.get(key);
      Log.debug(key + " = " + value);
      chars.put(new Character(value.charAt(0)), key);
    }
  }

  public static String getCharacter(String key)
  {
    if (parser == null)
    {
      parser = new CharacterEntityParser();
      Log.debug("Parser created: " + chars);
    }

    String val = (String) entities.getProperty(key.toLowerCase());
    if (val == null)
    {
      return key;
    }
    return val;
  }

  public static String parseEntityValue(String value)
  {
    int parserIndex = 0;
    int subStart = 0;
    int subEnd = 0;
    int subValue = 0;
    String replaceString = null;
    StringBuffer bufValue = new StringBuffer(value);

    while (((subStart = value.indexOf("&", parserIndex)) != -1) && (subEnd = value.indexOf(";", parserIndex)) != -1)
    {
      parserIndex = subStart;
      StringBuffer buf = new StringBuffer();
      buf.append(bufValue.substring(subStart + 1, subEnd));
      if (buf.charAt(0) == '#')
      {
        buf.deleteCharAt(0);
        subValue = Integer.parseInt(buf.toString());
        if ((subValue >= 1) && (subValue <= 65536))
        {
          char[] chr = new char[1];
          chr[0] = (char) subValue;
          replaceString = new String(chr);
        }
      }
      else
      {
        replaceString = (String) entities.get(buf.toString().toLowerCase());
      }
      if (replaceString != null)
      {
        replaceString = parseEntityValue(replaceString);
        bufValue.replace(subStart, subEnd + 1, replaceString);
        parserIndex = parserIndex + replaceString.length();
      }
      value = bufValue.toString();
    }
    return bufValue.toString();
  }

  public static String quoteString(String nat)
  {
    if (parser == null)
    {
      parser = new CharacterEntityParser();
      Log.debug("Parser created: " + chars);
    }

    StringBuffer retval = new StringBuffer(nat.length());
    int length = nat.length();
    for (int i = 0; i < length; i++)
    {
      Character natC = new Character(nat.charAt(i));
      String quote = (String) chars.get(natC);
      if (quote == null)
      {
        retval.append(natC.charValue());
      }
      else
      {
        retval.append("&");
        retval.append(quote);
        retval.append(";");
      }
    }
    return retval.toString();
  }
}

