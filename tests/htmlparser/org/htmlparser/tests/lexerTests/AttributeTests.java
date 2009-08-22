// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/lexerTests/AttributeTests.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:15 $
// $Revision: 1.1 $
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

package org.htmlparser.tests.lexerTests;

import java.util.Vector;
import org.htmlparser.Node;

import org.htmlparser.Attribute;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class AttributeTests extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.lexerTests.AttributeTests", "AttributeTests");
    }

    private static final boolean JSP_TESTS_ENABLED = false;
    private Tag tag;
    private Vector attributes;

    public AttributeTests (String name) {
        super(name);
    }

    public void getParameterTableFor(String tagContents)
    {
        getParameterTableFor (tagContents, false);
    }

    public void getParameterTableFor(String tagContents, boolean dump)
    {
        String html;
        NodeIterator iterator;
        Node node;

        html = "<" + tagContents + ">";
        createParser (html);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        try
        {
            iterator = parser.elements ();
            node = iterator.nextNode ();
            if (node instanceof Tag)
            {
                tag = (Tag)node;
                attributes = tag.getAttributesEx ();
                if (dump)
                {
                    for (int i = 0; i < attributes.size (); i++)
                    {
                        System.out.print ("Attribute #" + i);
                        Attribute attribute = (Attribute)attributes.elementAt (i);
                        if (null != attribute.getName ())
                            System.out.print (" Name: '" + attribute.getName () + "'");
                        if (null != attribute.getAssignment ())
                            System.out.print (" Assignment: '" + attribute.getAssignment () + "'");
                        if (0 != attribute.getQuote ())
                            System.out.print (" Quote: " + attribute.getQuote ());
                        if (null != attribute.getValue ())
                            System.out.print (" Value: '" + attribute.getValue () + "'");
                        System.out.println ();
                    }
                    System.out.println ();
                }
            }
            else
                attributes = null;
            String string = node.toHtml ();
            assertEquals ("toHtml differs", html, string);
            assertTrue ("shouldn't be any more nodes", !iterator.hasMoreNodes ());
        }
        catch (ParserException pe)
        {
            fail (pe.getMessage ());
        }
    }

    /**
     * Test constructors.
     */
    public void testConstructors ()
    {
        Vector attributes;
        Tag tag;
        String html;

        attributes = new Vector ();
         // String, null
        attributes.add (new Attribute ("wombat", null));
        // String
        attributes.add (new Attribute (" "));
        // String, String
        attributes.add (new Attribute ("label", "The civil war."));
        attributes.add (new Attribute (" "));
        // String, String, String
        attributes.add (new Attribute ("frameborder", "= ", "no"));
        attributes.add (new Attribute (" "));
        // String String, String, char
        attributes.add (new Attribute ("name", "=", "topFrame", '"'));
        tag = new TagNode (null, 0, 0, attributes);
        html = "<wombat label=\"The civil war.\" frameborder= no name=\"topFrame\">";
        assertStringEquals ("tag contents", html, tag.toHtml ());
    }

    /**
     * Test bean properties.
     */
    public void testProperties ()
    {
        Attribute attribute;
        Attribute space;
        Vector attributes;
        Tag tag;
        String html;

        attributes = new Vector ();
        attribute = new Attribute ();
        attribute.setName ("wombat");
        assertTrue ("should be standalone", attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should not be valued", !attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
        attributes.add (attribute);
        space = new Attribute ();
        space.setValue (" ");
        assertTrue ("should not be standalone", !space.isStandAlone ());
        assertTrue ("should be whitespace", space.isWhitespace ());
        assertTrue ("should be valued", space.isValued ());
        assertTrue ("should not be empty", !space.isEmpty ());
        attributes.add (space);
        attribute = new Attribute ();
        attribute.setName ("label");
        attribute.setAssignment ("=");
        attribute.setRawValue ("The civil war.");
        assertTrue ("should not be standalone", !attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should be valued", attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
        attributes.add (attribute);
        attributes.add (space);
        attribute = new Attribute ();
        attribute.setName ("frameborder");
        attribute.setAssignment ("= ");
        attribute.setRawValue ("no");
        attributes.add (attribute);
        attributes.add (space);
        attribute = new Attribute ();
        attribute.setName ("name");
        attribute.setAssignment ("=");
        attribute.setValue ("topFrame");
        attribute.setQuote ('"');
        assertTrue ("should not be standalone", !attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should be valued", attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
        attributes.add (attribute);
        tag = new TagNode (null, 0, 0, attributes);
        html = "<wombat label=\"The civil war.\" frameborder= no name=\"topFrame\">";
        assertStringEquals ("tag contents", html, tag.toHtml ());
    }

    /**
     * Test constructors.
     */
    public void testConstructors2 ()
    {
        Vector attributes;
        Tag tag;
        String html;

        attributes = new Vector ();
         // String, null
        attributes.add (new PageAttribute ("wombat", null));
        // String
        attributes.add (new PageAttribute (" "));
        // String, String
        attributes.add (new PageAttribute ("label", "The civil war."));
        attributes.add (new PageAttribute (" "));
        // String, String, String
        attributes.add (new PageAttribute ("frameborder", "= ", "no"));
        attributes.add (new PageAttribute (" "));
        // String String, String, char
        attributes.add (new PageAttribute ("name", "=", "topFrame", '"'));
        tag = new TagNode (null, 0, 0, attributes);
        html = "<wombat label=\"The civil war.\" frameborder= no name=\"topFrame\">";
        assertStringEquals ("tag contents", html, tag.toHtml ());
    }

    /**
     * Test bean properties.
     */
    public void testProperties2 ()
    {
        Attribute attribute;
        Attribute space;
        Vector attributes;
        Tag tag;
        String html;

        attributes = new Vector ();
        attribute = new PageAttribute ();
        attribute.setName ("wombat");
        assertTrue ("should be standalone", attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should not be valued", !attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
        attributes.add (attribute);
        space = new PageAttribute ();
        space.setValue (" ");
        assertTrue ("should not be standalone", !space.isStandAlone ());
        assertTrue ("should be whitespace", space.isWhitespace ());
        assertTrue ("should be valued", space.isValued ());
        assertTrue ("should not be empty", !space.isEmpty ());
        attributes.add (space);
        attribute = new PageAttribute ();
        attribute.setName ("label");
        attribute.setAssignment ("=");
        attribute.setRawValue ("The civil war.");
        assertTrue ("should not be standalone", !attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should be valued", attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
        attributes.add (attribute);
        attributes.add (space);
        attribute = new PageAttribute ();
        attribute.setName ("frameborder");
        attribute.setAssignment ("= ");
        attribute.setRawValue ("no");
        assertTrue ("should not be standalone", !attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should be valued", attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
         attributes.add (attribute);
        attributes.add (space);
        attribute = new PageAttribute ();
        attribute.setName ("name");
        attribute.setAssignment ("=");
        attribute.setValue ("topFrame");
        attribute.setQuote ('"');
        assertTrue ("should not be standalone", !attribute.isStandAlone ());
        assertTrue ("should not be whitespace", !attribute.isWhitespace ());
        assertTrue ("should be valued", attribute.isValued ());
        assertTrue ("should not be empty", !attribute.isEmpty ());
         attributes.add (attribute);
        tag = new TagNode (null, 0, 0, attributes);
        html = "<wombat label=\"The civil war.\" frameborder= no name=\"topFrame\">";
        assertStringEquals ("tag contents", html, tag.toHtml ());
    }

    /**
     * Test simple value.
     */
    public void testParseParameters() {
        getParameterTableFor("a b = \"c\"");
        assertEquals("Value","c",((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test quote value.
     */
    public void testParseTokenValues() {
        getParameterTableFor("a b = \"'\"");
        assertEquals("Value","'",((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test empty value.
     */
    public void testParseEmptyValues() {
        getParameterTableFor("a b = \"\"");
        assertEquals("Value","",((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test no equals or whitespace.
     * This might be reason for another rule, since another interpretation
     * would have an attribute called B with a value of "C".
     */
    public void testParseMissingEqual() {
        getParameterTableFor("a b\"c\"");
        assertEquals("NameC", "b\"c\"", ((Attribute)(attributes.elementAt (2))).getName ());
    }

    /**
     * Test multiple attributes.
     */
    public void testTwoParams(){
        getParameterTableFor("PARAM NAME=\"Param1\" VALUE=\"Somik\"");
        assertEquals("Param1","Param1",((Attribute)(attributes.elementAt (2))).getValue ());
        assertEquals("Somik","Somik",((Attribute)(attributes.elementAt (4))).getValue ());
    }

    /**
     * Test unquoted attributes.
     */
    public void testPlainParams(){
        getParameterTableFor("PARAM NAME=Param1 VALUE=Somik");
        assertEquals("Param1","Param1",((Attribute)(attributes.elementAt (2))).getValue ());
        assertEquals("Somik","Somik",((Attribute)(attributes.elementAt (4))).getValue ());
    }

    /**
     * Test standalone attribute.
     */
    public void testValueMissing() {
        getParameterTableFor("INPUT type=\"checkbox\" name=\"Authorize\" value=\"Y\" checked");
        assertEquals("Name of Tag","INPUT",((Attribute)(attributes.elementAt (0))).getName ());
        assertEquals("Type","checkbox",((Attribute)(attributes.elementAt (2))).getValue ());
        assertEquals("Name","Authorize",((Attribute)(attributes.elementAt (4))).getValue ());
        assertEquals("Value","Y",((Attribute)(attributes.elementAt (6))).getValue ());
        assertEquals("Checked",null,((Attribute)(attributes.elementAt (8))).getValue ());
    }

    /**
     * This is a simulation of a bug reported by Dhaval Udani - wherein
     * a space before the end of the tag causes a problem - there is a key
     * in the table with just a space in it and an empty value
     */
    public void testIncorrectSpaceKeyBug() {
        getParameterTableFor("TEXTAREA name=\"Remarks\" ");
        // There should only be two keys..
        assertEquals("There should only be two attributes",4,attributes.size());
        // The first key is name
        assertEquals("Expected name","TEXTAREA",((Attribute)(attributes.elementAt (0))).getName ());
        assertEquals("Expected value 1", "Remarks",((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test empty attribute.
     */
    public void testNullTag(){
        getParameterTableFor("INPUT type=");
        assertEquals("Name of Tag","INPUT",((Attribute)(attributes.elementAt (0))).getName ());
        assertNull("Type",((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test attribute containing an equals sign.
     */
    public void testAttributeWithSpuriousEqualTo() {
        getParameterTableFor(
            "a class=rlbA href=/news/866201.asp?0sl=-32"
        );
        assertStringEquals(
            "href",
            "/news/866201.asp?0sl=-32",
            ((Attribute)(attributes.elementAt (4))).getValue ()
        );
    }

    /**
     * Test attribute containing a question mark.
     */
    public void testQuestionMarksInAttributes() {
        getParameterTableFor(
            "a href=\"mailto:sam@neurogrid.com?subject=Site Comments\""
        );
        assertStringEquals(
            "href",
            "mailto:sam@neurogrid.com?subject=Site Comments",
            ((Attribute)(attributes.elementAt (2))).getValue ()
        );
        assertStringEquals(
            "tag name",
            "a",
            ((Attribute)(attributes.elementAt (0))).getName ()
        );
    }

    /**
     * Check that an empty tag is considered a string node.
     * Believe it or not Moi (vincent_aumont) wants htmlparser to parse a text file
     * containing something that looks nearly like a tag:
     * <pre>
     * "basic_string&lt;char, string_char_traits&lt;char&gt;, &lt;&gt;&gt;::basic_string()"
     * </pre>
     * This was throwing a null pointer exception when the empty &lt;&gt; was encountered.
     * Bug #725420 NPE in StringBean.visitTag
     **/
    public void testEmptyTag () {
        getParameterTableFor("");
        assertNull ("<> is not a tag",attributes);
    }

    /**
     * Test attributes when they contain scriptlets.
     * Submitted by Cory Seefurth
     * See also feature request #725376 Handle script in attributes.
     */
    public void testJspWithinAttributes() {
        if (JSP_TESTS_ENABLED)
        {
            getParameterTableFor(
                "a href=\"<%=Application(\"sURL\")%>/literature/index.htm"
            );
            assertStringEquals(
                "href",
                "<%=Application(\"sURL\")%>/literature/index.htm",
                ((Attribute)(attributes.elementAt (2))).getValue ()
            );
        }
    }

    /**
     * Test Script in attributes.
     * See feature request #725376 Handle script in attributes.
     */
    public void testScriptedTag () {
        getParameterTableFor("body onLoad=defaultStatus=''");
        String name = ((Attribute)(attributes.elementAt (0))).getName ();
        assertNotNull ("No Tag.TAGNAME", name);
        assertStringEquals("tag name parsed incorrectly", "body", name);
        String value = ((Attribute)(attributes.elementAt (2))).getValue ();
        assertStringEquals ("parameter parsed incorrectly", "defaultStatus=''", value);
    }

    /**
     * Test that stand-alone attributes are kept that way, rather than being
     * given empty values.
     * -Joe Robins, 6/19/03
     */
    public void testStandaloneAttribute ()
    {
        getParameterTableFor ("INPUT DISABLED");
        assertStringEquals("Standalone attribute not parsed","DISABLED",((Attribute)(attributes.elementAt (2))).getName ());
        assertNull ("Standalone attribute has non-null value",((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test missing value.
     */
    public void testMissingAttribute ()
    {
        getParameterTableFor ("INPUT DISABLED=");
        assertStringEquals("Empty attribute has no attribute","DISABLED",((Attribute)(attributes.elementAt (2))).getName ());
        assertEquals ("Attribute has non-blank value",null,((Attribute)(attributes.elementAt (2))).getValue ());
    }

    /**
     * Test Rule 1.
     * See discussion in Bug#891058 Bug in lexer. regarding alternate interpretations.
     */
    public void testRule1 ()
    {
        getParameterTableFor ("tag att = other=fred");
        assertStringEquals("Attribute not parsed","att",((Attribute)(attributes.elementAt (2))).getName ());
        assertEquals ("Attribute has wrong value", "other=fred", ((Attribute)(attributes.elementAt (2))).getValue ());
        for (int i = 0; i < attributes.size (); i++)
            assertTrue ("No attribute should be called =", !((Attribute)(attributes.elementAt (2))).getName ().equals ("="));
    }

    /**
     * Test Rule 2.
     */
    public void testRule2 ()
    {
        getParameterTableFor ("tag att =value other=fred");
        assertStringEquals("Attribute not parsed","att",((Attribute)(attributes.elementAt (2))).getName ());
        assertEquals ("Attribute has wrong value", "value", ((Attribute)(attributes.elementAt (2))).getValue ());
        for (int i = 0; i < attributes.size (); i++)
            assertTrue ("No attribute should be called =value", !((Attribute)(attributes.elementAt (2))).getName ().equals ("=value"));
        assertStringEquals("Empty attribute not parsed","other",((Attribute)(attributes.elementAt (4))).getName ());
        assertEquals ("Attribute has wrong value", "fred", ((Attribute)(attributes.elementAt (4))).getValue ());
    }

    /**
     * Test Rule 3.
     */
    public void testRule3 ()
    {
        getParameterTableFor ("tag att= \"value\" other=fred");
        assertStringEquals("Attribute not parsed","att",((Attribute)(attributes.elementAt (2))).getName ());
        assertEquals ("Attribute has wrong value", "value", ((Attribute)(attributes.elementAt (2))).getValue ());
        for (int i = 0; i < attributes.size (); i++)
            assertTrue ("No attribute should be called \"value\"", !((Attribute)(attributes.elementAt (2))).getName ().equals ("\"value\""));
        assertStringEquals("Empty attribute not parsed","other",((Attribute)(attributes.elementAt (4))).getName ());
        assertEquals ("Attribute has wrong value", "fred", ((Attribute)(attributes.elementAt (4))).getValue ());
    }

    /**
     * Test Rule 4.
     * See discussion in Bug#891058 Bug in lexer. regarding alternate interpretations.
     */
    public void testRule4 ()
    {
        getParameterTableFor ("tag att=\"va\"lue\" other=fred");
        assertStringEquals("Attribute not parsed","att",((Attribute)(attributes.elementAt (2))).getName ());
        assertEquals ("Attribute has wrong value", "va", ((Attribute)(attributes.elementAt (2))).getValue ());
        for (int i = 0; i < attributes.size (); i++)
            assertTrue ("No attribute should be called va\"lue", !((Attribute)(attributes.elementAt (2))).getName ().equals ("va\"lue"));
        assertStringEquals("Attribute missing","att",((Attribute)(attributes.elementAt (2))).getName ());
        assertStringEquals("Attribute not parsed","lue\"",((Attribute)(attributes.elementAt (3))).getName ());
        assertNull ("Attribute has wrong value", ((Attribute)(attributes.elementAt (3))).getValue ());
        assertStringEquals("Empty attribute not parsed","other",((Attribute)(attributes.elementAt (5))).getName ());
        assertEquals ("Attribute has wrong value", "fred", ((Attribute)(attributes.elementAt (5))).getValue ());
    }

    /**
     * Test Rule 5.
     * See discussion in Bug#891058 Bug in lexer. regarding alternate interpretations.
     */
    public void testRule5 ()
    {
        getParameterTableFor ("tag att='va'lue' other=fred");
        assertStringEquals("Attribute not parsed","att",((Attribute)(attributes.elementAt (2))).getName ());
        assertEquals ("Attribute has wrong value", "va", ((Attribute)(attributes.elementAt (2))).getValue ());
        for (int i = 0; i < attributes.size (); i++)
            assertTrue ("No attribute should be called va'lue", !((Attribute)(attributes.elementAt (2))).getName ().equals ("va'lue"));
        assertStringEquals("Attribute not parsed","lue'",((Attribute)(attributes.elementAt (3))).getName ());
        assertNull ("Attribute has wrong value", ((Attribute)(attributes.elementAt (3))).getValue ());
        assertStringEquals("Empty attribute not parsed","other",((Attribute)(attributes.elementAt (5))).getName ());
        assertEquals ("Attribute has wrong value", "fred", ((Attribute)(attributes.elementAt (5))).getValue ());
    }
    
    /**
     * Test for lost attributes.
     * see bug #778781 SRC-attribute suppression in IMG-tags
     * & #753012 IMG SRC not parsed v1.3 & v1.4
     * & #755929 Empty string attr. value causes attr parsing to be stopped
     * & #778781 SRC-attribute suppression in IMG-tags
     * & #832530 empty attribute causes parser to fail
     * & #851882 zero length alt tag causes bug in ImageScanner
     *
     *    HTML before parse:
     *    <img src="images/first" alt="first">"
     *    <img src="images/second" alt="">
     *    <img alt="third" src="images/third">
     *    <img alt="" src="images/fourth">
     *
     *    HTML after parse:
     *    <IMG ALT="first" SRC="images/first">
     *    <IMG ALT="" SRC="images/second">
     *    <IMG ALT="third" SRC="images/third">
     *    <IMG ALT="">
     */
    public void testSrcAndAlt () throws ParserException
    {
        String html = "<img src=\"images/first\" alt=\"first\">";

        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
        ImageTag img = (ImageTag)node[0];
        assertTrue ("bad source", "images/first".equals (img.getImageURL ()));
        assertTrue ("bad alt", "first".equals (img.getAttribute ("alt")));
        assertStringEquals ("toHtml()", html, img.toHtml ());
    }

    /**
     * see bug #778781 SRC-attribute suppression in IMG-tags
     */
    public void testSrcAndEmptyAlt () throws ParserException
    {
        String html = "<img src=\"images/second\" alt=\"\">";

        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
        ImageTag img = (ImageTag)node[0];
        assertTrue ("bad source", "images/second".equals (img.getImageURL ()));
        assertTrue ("bad alt", "".equals (img.getAttribute ("alt")));
        assertStringEquals ("toHtml()", html, img.toHtml ());
    }

    /**
     * see bug #778781 SRC-attribute suppression in IMG-tags
     */
    public void testAltAndSrc () throws ParserException
    {
        String html = "<img alt=\"third\" src=\"images/third\">";

        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
        ImageTag img = (ImageTag)node[0];
        assertTrue ("bad source", "images/third".equals (img.getImageURL ()));
        assertTrue ("bad alt", "third".equals (img.getAttribute ("alt")));
        assertStringEquals ("toHtml()", html, img.toHtml ());
    }

    /**
     * see bug #778781 SRC-attribute suppression in IMG-tags
     */
    public void testEmptyAltAndSrc () throws ParserException
    {
        String html = "<img alt=\"\" src=\"images/third\">";

        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
        ImageTag img = (ImageTag)node[0];
        assertTrue ("bad source", "images/third".equals (img.getImageURL ()));
        assertTrue ("bad alt", "".equals (img.getAttribute ("alt")));
        assertStringEquals ("toHtml()", html, img.toHtml ());
    }

    /**
     * see bug #911565 isValued() and isNull() don't work
     */
    public void testPredicates () throws ParserException
    {
        String html1 = "<img alt=\"\" src=\"images/third\" readonly>";
        String html2 = "<img src=\"images/third\" readonly alt=\"\">";
        String html3 = "<img readonly alt=\"\" src=\"images/third\">";
        String htmls[] = { html1, html2, html3 };

        for (int i = 0; i < htmls.length; i++)
        {
            createParser (htmls[i]);
            parseAndAssertNodeCount (1);
            assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
            ImageTag img = (ImageTag)node[0];
            Attribute src = img.getAttributeEx ("src");
            Attribute alt = img.getAttributeEx ("alt");
            Attribute readonly = img.getAttributeEx ("readonly");
            assertTrue ("src whitespace", !src.isWhitespace ());
            assertTrue ("src not valued", src.isValued ());
            assertTrue ("src empty", !src.isEmpty ());
            assertTrue ("src standalone", !src.isStandAlone ());
            assertTrue ("alt whitespace", !alt.isWhitespace ());
            assertTrue ("alt valued", !alt.isValued ());
            assertTrue ("alt empty", !alt.isEmpty ());
            assertTrue ("alt standalone", !alt.isStandAlone ());
            assertTrue ("readonly whitespace", !readonly.isWhitespace ());
            assertTrue ("readonly valued", !readonly.isValued ());
            assertTrue ("readonly empty", !readonly.isEmpty ());
            assertTrue ("readonly not standalone", readonly.isStandAlone ());
            // try assigning the name and checking again
            src.setName ("SRC");
            assertTrue ("setName() failed", "SRC=\"images/third\"".equals (src.toString ()));
            assertTrue ("src whitespace", !src.isWhitespace ());
            assertTrue ("src not valued", src.isValued ());
            assertTrue ("src empty", !src.isEmpty ());
            assertTrue ("src standalone", !src.isStandAlone ());
            alt.setName ("ALT");
            assertTrue ("setName() failed", "ALT=\"\"".equals (alt.toString ()));
            assertTrue ("alt whitespace", !alt.isWhitespace ());
            assertTrue ("alt valued", !alt.isValued ());
            assertTrue ("alt empty", !alt.isEmpty ());
            assertTrue ("alt standalone", !alt.isStandAlone ());
            readonly.setName ("READONLY");
            assertTrue ("setName() failed", "READONLY".equals (readonly.toString ()));
            assertTrue ("readonly whitespace", !readonly.isWhitespace ());
            assertTrue ("readonly valued", !readonly.isValued ());
            assertTrue ("readonly empty", !readonly.isEmpty ());
            assertTrue ("readonly not standalone", readonly.isStandAlone ());
            // try assigning the assignment and checking again
            src.setAssignment (" = ");
            assertTrue ("setAssignment() failed", "SRC = \"images/third\"".equals (src.toString ()));
            assertTrue ("src whitespace", !src.isWhitespace ());
            assertTrue ("src not valued", src.isValued ());
            assertTrue ("src empty", !src.isEmpty ());
            assertTrue ("src standalone", !src.isStandAlone ());
            alt.setAssignment (" = ");
            assertTrue ("setAssignment() failed", "ALT = \"\"".equals (alt.toString ()));
            assertTrue ("alt whitespace", !alt.isWhitespace ());
            assertTrue ("alt valued", !alt.isValued ());
            assertTrue ("alt empty", !alt.isEmpty ());
            assertTrue ("alt standalone", !alt.isStandAlone ());
            readonly.setAssignment ("=");
            assertTrue ("setAssignment() failed", "READONLY=".equals (readonly.toString ()));
            assertTrue ("readonly whitespace", !readonly.isWhitespace ());
            assertTrue ("readonly valued", !readonly.isValued ());
            assertTrue ("readonly not empty", readonly.isEmpty ());
            assertTrue ("readonly standalone", !readonly.isStandAlone ());
            // try assigning the value and checking again
            createParser (htmls[i]);
            parseAndAssertNodeCount (1);
            assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
            img = (ImageTag)node[0];
            src = img.getAttributeEx ("src");
            alt = img.getAttributeEx ("alt");
            readonly = img.getAttributeEx ("readonly");
            src.setValue ("cgi-bin/redirect");
            assertTrue ("setValue() failed", "src=\"cgi-bin/redirect\"".equals (src.toString ()));
            assertTrue ("src whitespace", !src.isWhitespace ());
            assertTrue ("src not valued", src.isValued ());
            assertTrue ("src empty", !src.isEmpty ());
            assertTrue ("src standalone", !src.isStandAlone ());
            alt.setValue ("no image");
            assertTrue ("setValue() failed", "alt=\"no image\"".equals (alt.toString ()));
            assertTrue ("alt whitespace", !alt.isWhitespace ());
            assertTrue ("alt not valued", alt.isValued ());
            assertTrue ("alt empty", !alt.isEmpty ());
            assertTrue ("alt standalone", !alt.isStandAlone ());
            readonly.setValue ("true"); // this may be bogus, really need to set assignment too, see below
            assertTrue ("setValue() failed", "readonlytrue".equals (readonly.toString ()));
            assertTrue ("readonly whitespace", !readonly.isWhitespace ());
            assertTrue ("readonly not valued", readonly.isValued ());
            assertTrue ("readonly empty", !readonly.isEmpty ());
            assertTrue ("readonly standalone", !readonly.isStandAlone ());
            readonly.setAssignment ("=");
            assertTrue ("setAssignment() failed", "readonly=true".equals (readonly.toString ()));
            assertTrue ("readonly whitespace", !readonly.isWhitespace ());
            assertTrue ("readonly not valued", readonly.isValued ());
            assertTrue ("readonly empty", !readonly.isEmpty ());
            assertTrue ("readonly standalone", !readonly.isStandAlone ());
        }
    }

    /**
     * see bug #911565 isValued() and isNull() don't work
     */
    public void testSetQuote () throws ParserException
    {
        String html = "<img alt=\"\" src=\"images/third\" toast>";

        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
        ImageTag img = (ImageTag)node[0];
        Attribute src = img.getAttributeEx ("src");
        src.setQuote ('\0');
        assertTrue ("setQuote('\\0') failed", "src=images/third".equals (src.toString ()));
        src.setQuote ('\'');
        assertTrue ("setQuote('\\'') failed", "src='images/third'".equals (src.toString ()));
    }
    
    /**
     * see bug #979893 Not Parsing all Attributes
     */
    public void testNoSpace () throws ParserException
    {
        String id = "A19012_00002";
        String rawid = "\"" + id + "\"";
        String cls = "BuyLink";
        String rawcls = "\"" + cls + "\"";
        String href = "http://www.someplace.com/buyme.html";
        String rawhref = "\"" + href + "\"";
        String html = "<a id=" + rawid + /* no space */ "class=" + rawcls + " href=" + rawhref + ">Pick me.</a>";
        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an LinkTag", node[0] instanceof LinkTag);
        LinkTag link = (LinkTag)node[0];
        Vector attributes = link.getAttributesEx ();
        assertEquals ("Incorrect number of attributes", 6, attributes.size ());
        assertStringEquals ("id wrong", rawid, link.getAttributeEx ("id").getRawValue ());
        assertStringEquals ("class wrong", rawcls, link.getAttributeEx ("class").getRawValue ());
        assertStringEquals ("href wrong", rawhref, link.getAttributeEx ("href").getRawValue ());
        assertStringEquals ("id wrong", id, link.getAttributeEx ("id").getValue ());
        assertStringEquals ("class wrong", cls, link.getAttributeEx ("class").getValue ());
        assertStringEquals ("href wrong", href, link.getAttributeEx ("href").getValue ());
    }
}
