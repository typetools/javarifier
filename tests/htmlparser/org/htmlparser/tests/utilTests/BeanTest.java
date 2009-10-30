// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.tests.*;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class BeanTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.BeanTest", "BeanTest");
    }

    public BeanTest (String name)
    {
        super (name);
    }

    protected byte[] pickle (Object object)
        throws
            IOException
    {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;
        byte[] ret;

        bos = new ByteArrayOutputStream ();
        oos = new ObjectOutputStream (bos);
        oos.writeObject (object);
        oos.close ();
        ret = bos.toByteArray ();

        return (ret);
    }

    protected Object unpickle (byte[] data)
        throws
            IOException,
            ClassNotFoundException
    {
        ByteArrayInputStream bis;
        ObjectInputStream ois;
        Object ret;

        bis = new ByteArrayInputStream (data);
        ois = new ObjectInputStream (bis);
        ret = ois.readObject ();
        ois.close ();

        return (ret);
    }

    /**
     * Makes sure that the bean returns text when passed the html.
     */
    protected void check (StringBean bean, String html, String text)
    {
        String path;
        File file;
        PrintWriter out;
        String string;

        path = System.getProperty ("user.dir");
        if (!path.endsWith (File.separator))
            path += File.separator;
        file = new File (path + "delete_me.html");
        try
        {
            out = new PrintWriter (new FileWriter (file));
            out.print (html);
            out.close ();
            bean.setURL (file.getAbsolutePath ());
            string = bean.getStrings ();
        }
        catch (Exception e)
        {
            fail (e.toString ());
            string = null; // never reached
        }
        finally
        {
            file.delete ();
        }
        assertStringEquals ("stringbean text differs", text, string);
    }

    public void testZeroArgPageConstructor ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        Page page;
        byte[] data;

        page = new Page ();
        data = pickle (page);
        page = (Page)unpickle (data);
    }

    public void testZeroArgLexerConstructor ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        Lexer lexer;
        byte[] data;

        lexer = new Lexer ();
        data = pickle (lexer);
        lexer = (Lexer)unpickle (data);
    }

    public void testZeroArgParserConstructor ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        Parser parser;
        byte[] data;

        parser = new Parser ();
        data = pickle (parser);
        parser = (Parser)unpickle (data);
    }

    public void testSerializable ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        Parser parser;
        Vector vector;
        NodeIterator enumeration;
        byte[] data;

        parser = new Parser ("http://htmlparser.sourceforge.net/test/example.html");
        enumeration = parser.elements ();
        vector = new Vector (50);
        while (enumeration.hasMoreNodes ())
            vector.addElement (enumeration.nextNode ());

        data = pickle (parser);
        parser = (Parser)unpickle (data);

        enumeration = parser.elements ();
        while (enumeration.hasMoreNodes ())
            assertEquals (
                "Nodes before and after serialization differ",
                ((Node)vector.remove (0)).toHtml (),
                enumeration.nextNode ().toHtml ());
    }

    public void testSerializableScanners ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        Parser parser;
        Vector vector;
        NodeIterator enumeration;
        byte[] data;

        parser = new Parser ("http://htmlparser.sourceforge.net/test/example.html");
        enumeration = parser.elements ();
        vector = new Vector (50);
        while (enumeration.hasMoreNodes ())
            vector.addElement (enumeration.nextNode ());

        data = pickle (parser);
        parser = (Parser)unpickle (data);

        enumeration = parser.elements ();
        while (enumeration.hasMoreNodes ())
            assertEquals (
                "Nodes before and after serialization differ",
                ((Node)vector.remove (0)).toHtml (),
                enumeration.nextNode ().toHtml ());
    }

    public void testSerializableStringBean ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        StringBean sb;
        String text;
        byte[] data;

        sb = new StringBean ();
        sb.setURL ("http://htmlparser.sourceforge.net/test/example.html");
        text = sb.getStrings ();

        data = pickle (sb);
        sb = (StringBean)unpickle (data);

        assertEquals (
            "Strings before and after serialization differ",
            text,
            sb.getStrings ());
    }

    public void testSerializableLinkBean ()
        throws
            IOException,
            ClassNotFoundException,
            ParserException
    {
        LinkBean lb;
        URL[] links;
        byte[] data;
        URL[] links2;

        lb = new LinkBean ();
        lb.setURL ("http://htmlparser.sourceforge.net/test/example.html");
        links = lb.getLinks ();

        data = pickle (lb);
        lb = (LinkBean)unpickle (data);

        links2 = lb.getLinks ();
        assertEquals ("Number of links after serialization differs", links.length, links2.length);
        for (int i = 0; i < links.length; i++)
        {
            assertEquals (
                "Links before and after serialization differ",
                links[i],
                links2[i]);
        }
    }

    public void testStringBeanListener ()
    {
        final StringBean sb;
        final Boolean hit[] = new Boolean[1];

        sb = new StringBean ();
        hit[0] = Boolean.FALSE;
        sb.addPropertyChangeListener (
            new PropertyChangeListener ()
            {
                public void propertyChange (PropertyChangeEvent event)
                {
                    if (event.getSource ().equals (sb))
                        if (event.getPropertyName ().equals (StringBean.PROP_STRINGS_PROPERTY))
                            hit[0] = Boolean.TRUE;
                }
            });

        hit[0] = Boolean.FALSE;
        sb.setURL ("http://htmlparser.sourceforge.net/test/example.html");
        assertTrue (
            "Strings property change not fired for URL change",
            hit[0].booleanValue ());

        hit[0] = Boolean.FALSE;
        sb.setLinks (true);
        assertTrue (
            "Strings property change not fired for links change",
            hit[0].booleanValue ());
    }

    public void testLinkBeanListener ()
    {
        final LinkBean lb;
        final Boolean hit[] = new Boolean[1];

        lb = new LinkBean ();
        hit[0] = Boolean.FALSE;
        lb.addPropertyChangeListener (
            new PropertyChangeListener ()
            {
                public void propertyChange (PropertyChangeEvent event)
                {
                    if (event.getSource ().equals (lb))
                        if (event.getPropertyName ().equals (LinkBean.PROP_LINKS_PROPERTY))
                            hit[0] = Boolean.TRUE;
                }
            });

        hit[0] = Boolean.FALSE;
        lb.setURL ("http://htmlparser.sourceforge.net/test/example.html");
        assertTrue (
            "Links property change not fired for URL change",
            hit[0].booleanValue ());
    }

    /**
     * Test no text returns empty string.
     */
    public void testCollapsed1 ()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (false);
        check (sb, "<html><head></head><body></body></html>", "");
        check (sb, "<html><head></head><body> </body></html>", " ");
        check (sb, "<html><head></head><body>\t</body></html>", "\t");
        sb.setCollapse (true);
        check (sb, "<html><head></head><body></body></html>", "");
        check (sb, "<html><head></head><body> </body></html>", "");
        check (sb, "<html><head></head><body>\t</body></html>", "");
    }

    /**
     * Test multiple whitespace returns empty string.
     */
    public void testCollapsed2 ()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (false);
        check (sb, "<html><head></head><body>  </body></html>", "  ");
        check (sb, "<html><head></head><body>\t\t</body></html>", "\t\t");
        check (sb, "<html><head></head><body> \t\t</body></html>", " \t\t");
        check (sb, "<html><head></head><body>\t \t</body></html>", "\t \t");
        check (sb, "<html><head></head><body>\t\t </body></html>", "\t\t ");
        sb.setCollapse (true);
        check (sb, "<html><head></head><body>  </body></html>", "");
        check (sb, "<html><head></head><body>\t\t</body></html>", "");
        check (sb, "<html><head></head><body> \t\t</body></html>", "");
        check (sb, "<html><head></head><body>\t \t</body></html>", "");
        check (sb, "<html><head></head><body>\t\t </body></html>", "");
    }

    /**
     * Test text preceded or followed by whitespace returns just text.
     */
    public void testCollapsed3 ()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (false);
        check (sb, "<html><head></head><body>x  </body></html>", "x  ");
        check (sb, "<html><head></head><body>x\t\t</body></html>", "x\t\t");
        check (sb, "<html><head></head><body>x \t\t</body></html>", "x \t\t");
        check (sb, "<html><head></head><body>x\t \t</body></html>", "x\t \t");
        check (sb, "<html><head></head><body>x\t\t </body></html>", "x\t\t ");
        sb.setCollapse (true);
        check (sb, "<html><head></head><body>x  </body></html>", "x");
        check (sb, "<html><head></head><body>x\t\t</body></html>", "x");
        check (sb, "<html><head></head><body>x \t\t</body></html>", "x");
        check (sb, "<html><head></head><body>x\t \t</body></html>", "x");
        check (sb, "<html><head></head><body>x\t\t </body></html>", "x");
        check (sb, "<html><head></head><body>  x</body></html>", "x");
        check (sb, "<html><head></head><body>\t\tx</body></html>", "x");
        check (sb, "<html><head></head><body> \t\tx</body></html>", "x");
        check (sb, "<html><head></head><body>\t \tx</body></html>", "x");
        check (sb, "<html><head></head><body>\t\t x</body></html>", "x");
    }

    /**
     * Test text including a "pre" tag
     */
    public void testOutputWithPreTags() {
        StringBean sb;
        sb = new StringBean ();
        String sampleCode = "public class Product {}";
        check (sb, "<body><pre>"+sampleCode+"</pre></body>", sampleCode);
    }

    /**
     * Test text including a "script" tag
     */
    public void testOutputWithScriptTags() {
        StringBean sb;
        sb = new StringBean ();

        String sampleScript =
          "<script language=\"javascript\">\r\n"
        + "if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n"
        + " document.write ('xxx');\r\n"
        + "else\r\n"
        + " document.write ('yyy');\r\n"
        + "</script>\r\n";

        check (sb, "<body>"+sampleScript+"</body>", "");
    }

    /*
     * Test output with pre and any tag.
     */
    public void testOutputWithPreAndAnyTag()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (false);
        check (sb, "<html><head></head><body><pre><hello></pre></body></html>", "");
    }

    /*
     * Test output with pre and any tag and text.
     */
    public void testOutputWithPreAndAnyTagPlusText()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (false);
        check (sb, "<html><head></head><body><pre><hello>dogfood</hello></pre></body></html>", "dogfood");
    }

    /*
     * Test output with pre and any tag and text.
     */
    public void testOutputWithPreAndAnyTagPlusTextWithWhitespace()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (true);
        check (sb, "<html><head></head><body><pre><hello>dog  food</hello></pre></body></html>", "dog  food");
    }

    /*
     * Test output without pre and any tag and text.
     */
    public void testOutputWithoutPreAndAnyTagPlusTextWithWhitespace()
    {
        StringBean sb;

        sb = new StringBean ();
        sb.setLinks (false);
        sb.setReplaceNonBreakingSpaces (true);
        sb.setCollapse (true);
        check (sb, "<html><head></head><body><hello>dog  food</hello></body></html>", "dog food");
    }

    /**
     * Test output with pre and script tags
     */
    public void xtestOutputWithPreAndScriptTags() {
        StringBean sb;
        sb = new StringBean ();

        String sampleScript =
          "<script language=\"javascript\">\r\n"
        + "if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n"
        + " document.write ('xxx');\r\n"
        + "else\r\n"
        + " document.write ('yyy');\r\n"
        + "</script>\r\n";

        check (sb, "<body><pre>"+sampleScript+"</pre></body>", sampleScript);
    }

}

