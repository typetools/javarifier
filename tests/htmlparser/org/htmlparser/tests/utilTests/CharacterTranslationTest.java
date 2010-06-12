// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derick Oswald
//
//
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//

package org.htmlparser.tests.utilTests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.CharacterReference;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.Translate;
import org.htmlparser.util.sort.Sort;

public class CharacterTranslationTest
    extends
        ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.CharacterTranslationTest", "CharacterTranslationTest");
    }

    /**
     * The list of references.
     */
    protected static CharacterReference[] mReferences;
    
    public CharacterTranslationTest (String name)
    {
        super (name);
    }

    /**
     * Class loader to access the compiled character references.
     */
    class SimpleClassLoader extends ClassLoader
    {
        /**
         * The class path for this class loader.
         */
        String mRoot;

        public SimpleClassLoader (String root)
        {
            if (!root.endsWith (File.separator))
                root += File.separator;
            mRoot = root;
        }

        public Class loadClass (String className)
            throws
                ClassNotFoundException
        {
            return (loadClass (className, true));
        }
        
        public synchronized Class loadClass (String className, boolean resolveIt)
            throws
                ClassNotFoundException
        {
            byte data[];
            FileInputStream in;
            Class ret;
            
            try
            {
                // try system class loader
                ret = super.findSystemClass (className);
            }
            catch (ClassNotFoundException e)
            {
                try
                {
                    in = new FileInputStream (mRoot + className + ".class");
                    data = new byte[in.available ()];
                    in.read (data);
                    in.close ();
                    ret = defineClass (className, data, 0, data.length);
                    if (null == ret)
                        throw new ClassFormatError ();
                    if (resolveIt)
                        resolveClass (ret);
                }
                catch (IOException ioe)
                {
                    throw new ClassNotFoundException ();
                }
            }
            
            return (ret);
        }
    }

    /**
     * Create a character reference translation class source file.
     * Usage:
     * <pre>
     *     java -classpath .:lib/htmlparser.jar Generate > Translate.java
     * </pre>
     * Derived from HTMLStringFilter.java provided as an example with the
     * htmlparser.jar file available at
     * <a href="http://htmlparser.sourceforge.net">htmlparser.sourceforge.net</a>
     * written by Somik Raha (
     * <a href='mailto:somik@industriallogic.com?
     * subject=htmlparser'>somik@industriallogic. com</a>
     * <a href="http://industriallogic.com">http://industriallogic.com</a>).
     * @author <a href='mailto:DerrickOswald@users.sourceforge.net?subject=Character Reference Translation class'>Derrick Oswald</a>
     */
    public class Generate
    {
        /**
         * The working parser.
         */
        protected Parser mParser;

        protected String nl = System.getProperty ("line.separator", "\n");
        
        /**
         * Create a Generate object.
         * Sets up the generation by creating a new <code>Parser</code> pointed
         * at <a href="http://www.w3.org/TR/REC-html40/sgml/entities.html">http://www.w3.org/TR/REC-html40/sgml/entities.html</a>
         * with the standard scanners registered.
         */
        public Generate ()
            throws ParserException
        {
            mParser = new Parser ("http://www.w3.org/TR/REC-html40/sgml/entities.html");
        }

        /**
         * Translate character references.
         * After generating the Translate class we could use it
         * to do this job, but that would involve a bootstrap
         * problem, so this method does the reference conversion
         * for a very tiny subset (enough  to understand the w3.org
         * page).
         * @param string The raw string.
         * @return The string with character references fixed.
         */
        public String translate (String string)
        {
            int index;
            int amp;
            StringBuffer ret;

            ret = new StringBuffer (4096);

            index = 0;
            while ((index < string.length ()) && (-1 != (amp = string.indexOf ('&', index))))
            {
                // include the part before the special character
                ret.append (string.substring (index, amp));
                if (string.startsWith ("&nbsp;", amp))
                {
                    ret.append (" ");
                    index = amp + 6;
                }
                else if (string.startsWith ("&lt;", amp))
                {
                    ret.append ("<");
                    index = amp + 4;
                }
                else if (string.startsWith ("&gt;", amp))
                {
                    ret.append (">");
                    index = amp + 4;
                }
                else if (string.startsWith ("&amp;", amp))
                {
                    ret.append ("&");
                    index = amp + 5;
                }
                else if (string.startsWith ("&quote;", amp))
                {
                    ret.append ("\"");
                    index = amp + 7;
                }
                else if (string.startsWith ("&divide;", amp))
                {
                    //ret.append ('\u00F7');
                    //index = amp + 8;
                    ret.append ("&");
                    index = amp + 1;
                }
                else if (string.startsWith ("&copy;", amp))
                {
                    //ret.append ('\u00A9');
                    //index = amp + 6;
                    ret.append ("&");
                    index = amp + 1;
                }
                else
                {
                    System.out.println ("unknown special character starting with " + string.substring (amp, amp + 7));
                    ret.append ("&");
                    index = amp + 1;
                }
            }
            ret.append (string.substring (index));

            return (ret.toString ());
        }

        public void gather (Node node, StringBuffer buffer)
        {
            NodeList children;

            if (node instanceof Text)
            {
                // Node is a plain string
                // Cast it to an HTMLText
                Text stringNode = (Text)node;
                // Retrieve the data from the object
                buffer.append (stringNode.getText ());
            }
            else if (node instanceof LinkTag)
            {
                // Node is a link
                // Cast it to an HTMLLinkTag
                LinkTag linkNode = (LinkTag)node;
                // Retrieve the data from the object and print it
                buffer.append (linkNode.getLinkText ());
            }
            else if (node instanceof Tag)
            {
                String name = ((Tag)node).getTagName ();
                if (name.equals ("BR") || name.equals ("P"))
                    buffer.append (nl);
                else
                {
                    children = ((Tag)node).getChildren ();
                    if (null != children)
                        for (int i = 0; i < children.size (); i++)
                            gather (children.elementAt (i), buffer);
                }
            }
            else if (node instanceof Remark)
            {
            }
            else
            {
                System.out.println ();
                System.out.println(node.toString());
            }
        }

        /**
         * Find the lowest index of whitespace (space or newline).
         * @param string The string to look in.
         * @param index Where to start looking.
         * @return -1 if there is no whitespace, the minimum index otherwise.
         */
        public int indexOfWhitespace (String string, int index)
        {
            int space;
            int cr;
            int ret;

            space = string.indexOf (" ", index);
            cr = string.indexOf (nl, index);
            if (-1 == space)
                ret = cr;
            else if (-1 == cr)
                ret = space;
            else
                ret = Math.min (space, cr);

            return (ret);
        }

        /**
         * Rewrite the comment string.
         * In the sgml table, the comments are of the form:
         * <pre>
         * -- latin capital letter I with diaeresis,
         *             U+00CF ISOlat1
         * </pre>
         * so we just want to make a one-liner without the spaces and newlines.
         * @param string The raw comment.
         * @return The single line comment.
         */
        public String pack (String string)
        {
            int index;
            int spaces;
            StringBuffer ret;

            ret = new StringBuffer (string.length ());

            if (string.startsWith ("-- "))
                string = string.substring (3);
            // remove doublespaces
            index = 0;
            while ((index < string.length ()) && (-1 != (spaces = indexOfWhitespace (string, index))))
            {
                ret.append (string.substring (index, spaces));
                ret.append (" ");
                while ((spaces < string.length ()) && Character.isWhitespace (string.charAt (spaces)))
                    spaces++;
                index = spaces;
            }
            if (index < string.length ())
                ret.append (string.substring (index));

            return (ret.toString ());
        }

        /**
         * Pretty up a comment string.
         * @param string The comment to operate on.
         * @return The beautiful comment string.
         */
        public String pretty (String string)
        {
            int index;
            int spaces;
            StringBuffer ret;

            ret = new StringBuffer (string.length ());

            // newline instead of doublespaces
            index = 0;
            while ((index < string.length ()) && (-1 != (spaces = string.indexOf ("  ", index))))
            {
                ret.append ("        // " + string.substring (index, spaces));
                if (!string.substring (index, spaces).endsWith (nl))
                    ret.append (nl);
                while ((spaces < string.length ()) && Character.isWhitespace (string.charAt (spaces)))
                    spaces++;
                index = spaces;
            }
            if (index < string.length ())
                ret.append ("        // " + string.substring (index));

            return (ret.toString ());
        }

        /**
         * Pad a string on the left with the given character to the length specified.
         * @param string The string to pad
         * @param character The character to pad with.
         * @param length The size to pad to.
         * @return The padded string.
         */
        public String pad (String string, char character, int length)
        {
            StringBuffer ret;

            ret = new StringBuffer (length);
            ret.append (string);
            while (length > ret.length ())
                ret.insert (0, character);

            return (ret.toString ());
        }

        /**
         * Convert the textual representation of the numeric character reference to a character.
         * @param string The numeric character reference (in quotes).
         * @return The character represented by the numeric character reference.
         *
         */
        public String unicode (String string)
        {
            int code;

            if (string.startsWith ("\"&#") && string.endsWith (";\""))
            {
                string = string.substring (3, string.length () - 2);
                try
                {
                    code = Integer.parseInt (string);
                    string = "'\\u" + pad (Integer.toHexString (code), '0', 4) + "'";
                }
                catch (Exception e)
                {
                    e.printStackTrace ();
                }
                return (string);
            }
            else
                return (string);
        }

        /**
         * Parse the sgml declaration for character entity reference
         * name, equivalent numeric character reference and a comment.
         * Emit a java hash table 'put' with the name as the key, the
         * numeric character as the value and comment the insertion
         * with the comment.
         * @param string The contents of the sgml declaration.
         * @param out The sink for output.
         */
        public void extract (String string, PrintWriter out)
        {
            int space;
            String token;
            String code;

            if (string.startsWith ("<!--"))
                out.println (pretty (string.substring (4, string.length () - 3).trim ()));
            else if (string.startsWith ("<!ENTITY"))
            {
                string = string.substring (8, string.length () - 3).trim ();
                if (-1 != (space = string.indexOf (" ")))
                {
                    token = string.substring (0, space);
                    string = string.substring (space).trim ();
                    if (string.startsWith ("CDATA"))
                    {
                        string = string.substring (5).trim ();
                        if (-1 != (space = string.indexOf (" ")))
                        {
                            code = string.substring (0, space).trim ();
                            code = unicode (code);
                            string = string.substring (space).trim ();
                            out.println (
                                "        new CharacterReference (\"" + token + "\","
                                // no token is larger than 8 characters - yet
                                + pad (code, ' ', code.length () + 9 - token.length ()) + "),"
                                + " // "
                                + pack (string));
                        }
                        else
                            out.println (string);
                    }
                    else
                        out.println (string);
                }
                else
                    out.println (string);
            }
            else
                out.println (string);
        }

        /**
         * Extract special characters.
         * Scan the string looking for substrings of the form:
         * <pre>
         * &lt;!ENTITY nbsp   CDATA "&amp;#160;" -- no-break space = non-breaking space, U+00A0 ISOnum --&gt;
         * </pre>
         * and emit a java definition for each.
         * @param string The raw string from w3.org.
         * @param out The sink for output.
         */
        public void sgml (String string, PrintWriter out)
        {
            int index;
            int begin;
            int end;

            index = 0;
            while (-1 != (begin = string.indexOf ("<", index)))
            {
                if (-1 != (end = string.indexOf ("-->", begin)))
                {
                    extract (string.substring (begin, end + 3), out);
                    index = end + 3;
                }
                else
                    index = begin + 1;
            }
        }

        /**
         * Pull out text elements from the HTML.
         * @param out The sink for output.
         */
        public void parse (PrintWriter out)
            throws
                ParserException
        {
            Node node;
            StringBuffer buffer = new StringBuffer (4096);

            // Run through an enumeration of html elements, and pick up
            // only those that are plain string.
            for (NodeIterator e = mParser.elements (); e.hasMoreNodes ();)
            {
                node = e.nextNode ();
                gather (node, buffer);
            }

            String text = translate (buffer.toString ());
            sgml (text, out);
        }
    }

    public CharacterReference[] getReferences ()
    {
        final String class_name = "CharacterEntityReferenceList";
        String paths;
        String path;
        String source;
        PrintWriter out;
        Generate generate;
        SimpleClassLoader loader;
        Class hello;
        Field field;
        CharacterReference[] ret;

        ret = mReferences;
        if (null == ret)
        {
            paths = System.getProperty ("java.class.path");
            path = System.getProperty ("user.home");
            if (!path.endsWith (File.separator))
                path += File.separator;
            source = path + class_name + ".java";
            try
            {
                // create it
                generate = new Generate ();
                out = new PrintWriter (new FileWriter (source));
                out.println ("import org.htmlparser.util.CharacterReference;");
                out.println ();
                out.println ("/** Generated by " + this.getClass ().getName () + " **/");
                out.println ("public class " + class_name);
                out.println ("{");
                out.println ("    /**");
                out.println ("     * Table mapping character to entity reference.");
                out.println ("     */");
                out.println ("    public static final CharacterReference[] mCharacterReferences =");
                out.println ("    {");
                generate.parse (out);
                out.println ("    };");
                out.println ("}");
                out.close ();
                // compile it
                if (0 == com.sun.tools.javac.Main.compile (new String[] {"-classpath", paths, source}))
                {
                    try
                    {
                        // load it
                        loader = new SimpleClassLoader (path);
                        hello = loader.loadClass (class_name);
                        try
                        {
                            // get the references
                            field = hello.getField ("mCharacterReferences");
                            ret = (CharacterReference[])field.get (null);
                            Sort.QuickSort (ret);
                        }
                        catch (IllegalAccessException iae)
                        {
                            fail ("references not accessible");
                        }
                        catch (NoSuchFieldException nsfe)
                        {
                            fail ("references not found");
                        }
                    }
                    catch (ClassNotFoundException cnfe)
                    {
                        fail ("couldn't load class");
                    }
                    finally
                    {
                        File classfile;

                        classfile = new File (path + class_name + ".class");
                        classfile.delete ();
                    }
                }
                else
                    fail ("couldn't compile class");
                mReferences = ret;
            }
            catch (IOException ioe)
            {
                fail ("couldn't write class");
            }
            catch (ParserException ioe)
            {
                fail ("couldn't parse w3.org entities list");
            }
        }
        
        return (ret);
    }

    public void testInitialCharacterEntityReference ()
    {
        assertEquals (
            "character entity reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&divide; is the division sign."));
    }

    public void testInitialNumericCharacterReference1 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#247; is the division sign."));
    }

    public void testInitialNumericCharacterReference2 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#0247; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference1 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#xf7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference2 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#xF7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference3 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#x0f7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference4 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#x0F7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference5 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#Xf7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference6 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#XF7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference7 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#X0f7; is the division sign."));
    }

    public void testInitialHexNumericCharacterReference8 ()
    {
        assertEquals (
            "numeric character reference at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#X0F7; is the division sign."));
    }

    public void testInitialCharacterEntityReferenceWithoutSemi ()
    {
        assertEquals (
            "character entity reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&divide is the division sign."));
    }

    public void testInitialNumericCharacterReferenceWithoutSemi ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#247 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi1 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#xf7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi2 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#xF7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi3 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#x0f7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi4 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#x0F7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi5 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#Xf7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi6 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#XF7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi7 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#X0f7 is the division sign."));
    }

    public void testInitialHexNumericCharacterReferenceWithoutSemi8 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at start of string doesn't work",
            "\u00f7 is the division sign.",
            Translate.decode ("&#X0F7 is the division sign."));
    }

    public void testFinalCharacterEntityReference ()
    {
        assertEquals (
            "character entity reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &divide;"));
    }

    public void testFinalNumericCharacterReference ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#247;"));
    }

    public void testFinalHexNumericCharacterReference1 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#xf7;"));
    }

    public void testFinalHexNumericCharacterReference2 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#xF7;"));
    }

    public void testFinalHexNumericCharacterReference3 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#x0f7;"));
    }

    public void testFinalHexNumericCharacterReference4 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#x0F7;"));
    }

    public void testFinalHexNumericCharacterReference5 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#Xf7;"));
    }

    public void testFinalHexNumericCharacterReference6 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#XF7;"));
    }

    public void testFinalHexNumericCharacterReference7 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#X0f7;"));
    }

    public void testFinalHexNumericCharacterReference8 ()
    {
        assertEquals (
            "numeric character reference at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#X0F7;"));
    }

    public void testFinalCharacterEntityReferenceWithoutSemi ()
    {
        assertEquals (
            "character entity reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &divide"));
    }

    public void testFinalNumericCharacterReferenceWithoutSemi1 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#247"));
    }

    public void testFinalNumericCharacterReferenceWithoutSemi2 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#0247"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi1 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#xf7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi2 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#xF7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi3 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#x0f7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi4 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#x0F7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi5 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#Xf7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi6 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#XF7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi7 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#X0f7"));
    }

    public void testFinalHexNumericCharacterReferenceWithoutSemi8 ()
    {
        assertEquals (
            "numeric character reference without a semicolon at end of string doesn't work",
            "The division sign (\u00f7) is \u00f7",
            Translate.decode ("The division sign (\u00f7) is &#X0F7"));
    }

    public void testReferencesInString ()
    {
        assertEquals (
            "character references within a string don't work",
            "Thus, the character entity reference \u00f7 is a more convenient form than \u00f7 for obtaining the division sign (\u00f7)",
            Translate.decode ("Thus, the character entity reference &divide; is a more convenient form than &#247; for obtaining the division sign (\u00f7)"));
    }

    public void testBogusCharacterEntityReference1 ()
    {
        assertEquals (
            "bogus character entity reference doesn't work",
            "The character entity reference &divode; is bogus",
            Translate.decode ("The character entity reference &divode; is bogus"));
    }

    public void testBogusCharacterEntityReference2 ()
    {
        assertEquals (
            "bogus character entity reference doesn't work",
            "The character entity reference &(divide) is bogus",
            Translate.decode ("The character entity reference &(divide) is bogus"));
    }

    public void testBogusNumericCharacterReference ()
    {
        assertEquals (
            "bogus numeric character reference doesn't work",
            "The numeric character reference &#BF7; is bogus",
            Translate.decode ("The numeric character reference &#BF7; is bogus"));
    }

    public void testBogusHexNumericCharacterReference ()
    {
        assertEquals (
            "bogus numeric character reference doesn't work",
            "The numeric character reference &#xKJ7; is bogus",
            Translate.decode ("The numeric character reference &#xKJ7; is bogus"));
    }

    public void testPoorlyTerminatedCharacterEntityReference1 ()
    {
        assertEquals (
            "poorly terminated character entity reference doesn't work",
            "The character entity reference \u00f7d should be decoded",
            Translate.decode ("The character entity reference &divided should be decoded"));
    }

    public void testPoorlyTerminatedCharacterEntityReference2 ()
    {
        assertEquals (
            "poorly terminated character entity reference doesn't work",
            "The character entity reference \u00f7<br> should be decoded",
            Translate.decode ("The character entity reference &divide<br> should be decoded"));
    }

    public void testPoorlyTerminatedNumericCharacterReference1 ()
    {
        assertEquals (
            "poorly terminated numeric character reference doesn't work",
            "The numeric character reference \u00f7pop should be decoded",
            Translate.decode ("The numeric character reference &#xf7pop should be decoded"));
    }

    public void testPoorlyTerminatedNumericCharacterReference2 ()
    {
        assertEquals (
            "poorly terminated numeric character reference doesn't work",
            "The numeric character reference \u00f7<br> should be decoded",
            Translate.decode ("The numeric character reference &#xf7<br> should be decoded"));
    }

    public void testPoorlyTerminatedNumericCharacterReference3 ()
    {
        assertEquals (
            "poorly terminated numeric character reference doesn't work",
            "The numeric character reference \u00f7xpert should be decoded",
            Translate.decode ("The numeric character reference &#xf7xpert should be decoded"));
    }

    public void testEncode ()
    {
        assertEquals (
            "encode doesn't work",
            "Character entity reference: &divide;, another: &nbsp;, numeric character reference: &#9831;.",
            Translate.encode ("Character entity reference: \u00f7, another: \u00a0, numeric character reference: \u2667."));
    }

    public void testEncodeLink ()
    {
        assertEquals (
            "encode link doesn't work",
            "&lt;a href=&quot;http://www.w3.org/TR/REC-html40/sgml/entities.html&quot;&gt;http://www.w3.org/TR/REC-html40/sgml/entities.html&lt;/a&gt;",
            Translate.encode ("<a href=\"http://www.w3.org/TR/REC-html40/sgml/entities.html\">http://www.w3.org/TR/REC-html40/sgml/entities.html</a>"));
    }

    public byte[] encodedecode (byte[] bytes)
        throws
            IOException
    {
        InputStream in;
        ByteArrayOutputStream out;
        byte[] data;

        // encode
        in = new ByteArrayInputStream (bytes);
        out = new ByteArrayOutputStream ();
        Translate.encode (in, new PrintStream (out, false, "ISO-8859-1"));
        in.close ();
        out.close ();
        data = out.toByteArray ();

        // decode
        in = new ByteArrayInputStream (data);
        out = new ByteArrayOutputStream ();
        Translate.decode (in, new PrintStream (out, false, "ISO-8859-1"));
        in.close ();
        out.close ();
        data = out.toByteArray ();

        return (data);
    }

    public void check (byte[] reference, byte[] result)
        throws
            IOException
    {
        InputStream ref;
        InputStream in;
        int i;
        int i1;
        int i2;

        ref = new ByteArrayInputStream (reference);
        in = new ByteArrayInputStream (result);
        i = 0;
        do
        {
            i1 = ref.read ();
            i2 = in.read ();
            if (i1 != i2)
                fail ("byte difference detected at offset " + i + " expected " + i1 + ", actual " + i2);
            i++;
        }
        while (-1 != i1);
        ref.close ();
        in.close ();
    }

    public void testHexNumericEncoding ()
        throws
            IOException
    {
        try
        {
            Translate.ENCODE_HEXADECIMAL = true;
            assertEquals (
                "hex value incorrect",
                "&#x5ab; is a non-existant character.",
                Translate.encode ("\u05AB is a non-existant character."));
        }
        finally
        {
            Translate.ENCODE_HEXADECIMAL = false;
        }
    }

    public void testLastCharacterEntityReference ()
        throws
            IOException
    {
        assertEquals (
            "poorly terminated numeric character reference doesn't work",
            "The character entity reference\u200cshould be decoded",
            Translate.decode ("The character entity reference&zwnjshould be decoded"));
    }

    public void testEncodeDecodePage () throws IOException
    {
        URL url;
        URLConnection connection;
        InputStream in;
        ByteArrayOutputStream out;
        byte[] bytes;
        byte[] result;
        int c;

        // get some bytes
        url = new URL ("http://sourceforge.net/projects/htmlparser");
        connection = url.openConnection ();
        in = connection.getInputStream ();
        out = new ByteArrayOutputStream ();
        while (-1 != (c = in.read ()))
            out.write (c);
        in.close ();
        out.close ();
        bytes = out.toByteArray ();

        // run it through
        result = encodedecode (bytes);
        
        // check
        check (bytes, result);
    }

    /**
     * Check all references read in from the w3.org site.
     * If this test fails but the others pass, suspect that the list of
     * entity references has been augmented. The updated list is in the
     * CharacterEntityReferenceList.java file in your home directory.
     */
    public void testEncodeDecodeAll ()
    {
        CharacterReference[] list;
        StringBuffer stimulus;
        StringBuffer response;
        CharacterReference ref;
        String string;

        list = getReferences ();
        stimulus = new StringBuffer ();
        response = new StringBuffer ();
        for (int i = 0; i < list.length; i++)
        {
            ref = list[i];
            stimulus.append ((char)ref.getCharacter ());
            response.append ("&");
            response.append (ref.getKernel ());
            response.append (";");
        }
        string = Translate.encode (stimulus.toString ());
        if (!string.equals (response.toString ()))
            fail ("encoding incorrect, expected \n\"" + response.toString () + "\", encoded \n\"" + string + "\""); 
        string = Translate.decode (string);
        if (!string.equals (stimulus.toString ()))
            fail ("decoding incorrect, expected \n\"" + stimulus.toString () + "\", decoded \n\"" + string + "\", encoded \n\"" + response.toString () + "\""); 
    }

    public void testEncodeDecodeRandom ()
    {
        Random random;
        CharacterReference[] list;
        StringBuffer stimulus;
        StringBuffer response;
        char character;
        CharacterReference ref;
        String string;

        random = new Random ();
        list = getReferences ();
        stimulus = new StringBuffer ();
        response = new StringBuffer ();
        for (int i = 0; i < 1000; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                // some random characters
                for (int k = 0; k < 10; k++)
                {
                    character = (char)random.nextInt (127);
                    if (character >= ' ')
                    {
                        if ('&' == character)
                        {
                            stimulus.append (character);
                            response.append ("&amp;");
                        }
                        else if ('"' == character)
                        {
                            stimulus.append (character);
                            response.append ("&quot;");
                        }
                        else if ('<' == character)
                        {
                            stimulus.append (character);
                            response.append ("&lt;");
                        }
                        else if ('>' == character)
                        {
                            stimulus.append (character);
                            response.append ("&gt;");
                        }
                        else
                        {
                            stimulus.append (character);
                            response.append (character);
                        }
                    }
                }
                ref = list[random.nextInt (list.length)];
                stimulus.append ((char)ref.getCharacter ());
                response.append ("&");
                response.append (ref.getKernel ());
                response.append (";");
                // some more random characters
                for (int k = 0; k < 10; k++)
                {
                    character = (char)random.nextInt (127);
                    if (character >= ' ')
                    {
                        if ('&' == character)
                        {
                            stimulus.append (character);
                            response.append ("&amp;");
                        }
                        else if ('"' == character)
                        {
                            stimulus.append (character);
                            response.append ("&quot;");
                        }
                        else if ('<' == character)
                        {
                            stimulus.append (character);
                            response.append ("&lt;");
                        }
                        else if ('>' == character)
                        {
                            stimulus.append (character);
                            response.append ("&gt;");
                        }
                        else
                        {
                            stimulus.append (character);
                            response.append (character);
                        }
                    }
                }
            }
            string = Translate.encode (stimulus.toString ());
            if (!string.equals (response.toString ()))
                fail ("encoding incorrect, expected \n\"" + response.toString () + "\", encoded \n\"" + string + "\""); 
            string = Translate.decode (string);
            if (!string.equals (stimulus.toString ()))
                fail ("decoding incorrect, expected \n\"" + stimulus.toString () + "\", decoded \n\"" + string + "\", encoded \n\"" + response.toString () + "\""); 
            stimulus.setLength (0);
            response.setLength (0);
        }   
        
    }

    public void testEncodeDecodeRandomNoSemi ()
    {
        Random random;
        CharacterReference[] list;
        StringBuffer stimulus;
        StringBuffer response;
        char character;
        int index;
        CharacterReference ref;
        String kernel;
        ArrayList forbidden;
        String string;

        random = new Random ();
        list = getReferences ();
        stimulus = new StringBuffer ();
        response = new StringBuffer ();
        for (int i = 0; i < 1000; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                // some random characters
                for (int k = 0; k < 10; k++)
                {
                    character = (char)random.nextInt (127);
                    if (character >= ' ')
                    {
                        if ('&' == character)
                        {
                            stimulus.append (character);
                            response.append ("&amp;");
                        }
                        else if ('"' == character)
                        {
                            stimulus.append (character);
                            response.append ("&quot;");
                        }
                        else if ('<' == character)
                        {
                            stimulus.append (character);
                            response.append ("&lt;");
                        }
                        else if ('>' == character)
                        {
                            stimulus.append (character);
                            response.append ("&gt;");
                        }
                        else
                        {
                            stimulus.append (character);
                            response.append (character);
                        }
                    }
                }
                index = random.nextInt (list.length);
                ref = list[index];
                kernel = ref.getKernel ();
                stimulus.append ((char)ref.getCharacter ());
                response.append ("&");
                response.append (kernel);
                // to be fair, we ensure that the next character isn't valid
                // for a different reference, i.e. &sup shouldn't be followed
                // by a 1, 2, 3 or e
                forbidden = new ArrayList ();
                for (int k = index + 1; k < list.length; k++)
                    if (list[k].getKernel ().regionMatches (
                        0,
                        kernel,
                        0,
                        kernel.length ()))
                        forbidden.add (new Character (list[k].getKernel ().charAt (kernel.length ())));
                    else
                        break;
                do
                {
                    character = (char)random.nextInt (127);
                    if (   (' ' <= character)
                        && ('&' != character)
                        && ('"' != character)
                        && ('<' != character)
                        && ('>' != character)
                        && (';' != character)
                        && !(forbidden.contains (new Character (character))))
                    {
                        stimulus.append (character);
                        response.append (character);
                        character = 0;
                    }
                    else
                        character = ' ';
                        
                }
                while (0 != character);
                // some more random characters
                for (int k = 0; k < 10; k++)
                {
                    character = (char)random.nextInt (127);
                    if (character >= ' ')
                    {
                        if ('&' == character)
                        {
                            stimulus.append (character);
                            response.append ("&amp;");
                        }
                        else if ('"' == character)
                        {
                            stimulus.append (character);
                            response.append ("&quot;");
                        }
                        else if ('<' == character)
                        {
                            stimulus.append (character);
                            response.append ("&lt;");
                        }
                        else if ('>' == character)
                        {
                            stimulus.append (character);
                            response.append ("&gt;");
                        }
                        else
                        {
                            stimulus.append (character);
                            response.append (character);
                        }
                    }
                }
            }
            string = Translate.decode (response.toString ());
            if (!string.equals (stimulus.toString ()))
                fail ("decoding incorrect:\nexpected \"" + stimulus.toString () + "\"\n decoded \"" + string + "\"\n encoded \"" + response.toString () + "\""); 
            stimulus.setLength (0);
            response.setLength (0);
        }   
    }
}



