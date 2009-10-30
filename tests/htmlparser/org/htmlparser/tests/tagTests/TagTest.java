// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
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

package org.htmlparser.tests.tagTests;

import org.htmlparser.Attribute;

import org.htmlparser.Node;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class TagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.TagTest", "TagTest");
    }

    private static final boolean JSP_TESTS_ENABLED = false;

    public TagTest(String name) {
        super(name);
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     */
    public void testBodyTagBug1() throws ParserException {
        String body = "<BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc "
            + "onload=setfocus() text=#000000\nvLink=#551a8b>";
        createParser(body);
        parseAndAssertNodeCount(1);
        // The node should be a body Tag
        assertTrue("Node should be a BodyTag",node[0] instanceof BodyTag);
        BodyTag tag = (BodyTag)node[0];
        String text = tag.toHtml ();
        assertEquals("Contents of the tag",body + "</BODY>",text);
    }

    /**
     * The following should be identified as a tag : <BR>
     *  &lt;MYTAG abcd\n"+
     *      "efgh\n"+
     *      "ijkl\n"+
     *      "mnop&gt;
     * Creation date: (6/17/2001 5:27:42 PM)
     */
    public void testLargeTagBug() throws ParserException {
        String mytag = "MYTAG abcd\n"+
            "efgh\n"+
            "ijkl\n"+
            "mnop";
        createParser(
            "<" + mytag + ">"
        );
        parseAndAssertNodeCount(1);
        // The node should be an Tag
        assertTrue("Node should be a Tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("Contents of the tag",mytag,tag.getText());


    }
    /**
     * Bug reported by Gordon Deudney 2002-03-15
     * Nested JSP Tags were not working
     */
    public void testNestedTags() throws ParserException
    {
        if (JSP_TESTS_ENABLED)
        {
            String s = "input type=\"text\" value=\"<%=\"test\"%>\" name=\"text\"";
            String line = "<"+s+">";
            createParser(line);
            parseAndAssertNodeCount(1);
            assertTrue("The node found should have been an Tag",node[0] instanceof Tag);
            Tag tag = (Tag) node[0];
            assertEquals("Tag Contents",s,tag.getText());
        }
    }

    /**
     * Test parseParameter method
     * Created by Kaarle Kaila (august 2001)
     * the tag name is here G
     */
    public void testParseParameter3() throws ParserException {
        Tag tag;
        Node node=null;
        String lin1 = "<DIV class=\"userData\" id=\"oLayout\" name=\"oLayout\"></DIV>";
        createParser(lin1);
        NodeIterator en = parser.elements();

        try {

            if (en.hasMoreNodes()) {
                node = en.nextNode();

                tag = (Tag)node;
                String classValue= tag.getAttribute ("CLASS");
                assertEquals ("The class value should be ","userData",classValue);
            }

        }
        catch (ClassCastException ce) {
            fail("Bad class element = " + node.getClass().getName());
        }
    }

    /**
     * Test parseParameter method
     * Created by Kaarle Kaila (august 2001)
     * the tag name is here A (and should be eaten up by linkScanner)
     */
    public void testParseParameterA() throws ParserException {
        Tag tag;
        Tag etag;
        Text snode;
        Node node=null;
        String lin1 = "<A href=\"http://www.iki.fi/kaila\" myParameter yourParameter=\"Kaarle Kaaila\">Kaarle's homepage</A><p>Paragraph</p>";
        createParser(lin1);
        NodeIterator en = parser.elements();
        String a,href,myValue,nice;

        try {

            if (en.hasMoreNodes()) {
                node = en.nextNode();

                tag = (Tag)node;
                a = ((Attribute)(tag.getAttributesEx ().elementAt (0))).getName ();
                href = tag.getAttribute ("HREF");
                myValue = tag.getAttribute ("MYPARAMETER");
                nice = tag.getAttribute ("YOURPARAMETER");
                assertEquals ("Link tag (A)","A",a);
                assertEquals ("href value","http://www.iki.fi/kaila",href);
                assertEquals ("myparameter value",null,myValue);
                assertEquals ("yourparameter value","Kaarle Kaaila",nice);
            }
            if (!(node instanceof LinkTag)) {
                // linkscanner has eaten up this piece
                if ( en.hasMoreNodes()) {
                    node = en.nextNode();
                    snode = (Text)node;
                    assertEquals("Value of element","Kaarle's homepage",snode.getText());
                }

                if (en.hasMoreNodes()) {
                    node = en.nextNode();
                    etag = (Tag)node;
                    assertEquals("endtag of link","/A", etag.getText());
                }
            }
            // testing rest
            if (en.hasMoreNodes()) {
                node = en.nextNode();

                tag = (Tag)node;
                assertEquals("following paragraph begins",tag.getText(),"p");
            }
            if (en.hasMoreNodes()) {
                node = en.nextNode();
                snode = (Text)node;
                assertEquals("paragraph contents","Paragraph",snode.getText());
            }
            if (en.hasMoreNodes()) {
                node = en.nextNode();
                etag = (Tag)node;
                assertEquals("paragrapg endtag","/p",etag.getText());
            }

        }
        catch (ClassCastException ce) {
            fail("Bad class element = " + node.getClass().getName());
        }
    }

    /**
     * Test parseParameter method
     * Created by Kaarle Kaila (august 2001)
     * the tag name is here G
     */
    public void testParseParameterG() throws ParserException{
        Tag tag;
        Tag etag;
        Text snode;
        Node node=null;
        String lin1 = "<G href=\"http://www.iki.fi/kaila\" myParameter yourParameter=\"Kaila\">Kaarle's homepage</G><p>Paragraph</p>";
        createParser(lin1);
        NodeIterator en = parser.elements();
        String a,href,myValue,nice;

        try {

            if (en.hasMoreNodes()) {
                node = en.nextNode();

                tag = (Tag)node;
                a = ((Attribute)(tag.getAttributesEx ().elementAt (0))).getName ();
                href = tag.getAttribute ("HREF");
                myValue = tag.getAttribute ("MYPARAMETER");
                nice = tag.getAttribute ("YOURPARAMETER");
                assertEquals ("The tagname should be G",a,"G");
                assertEquals ("Check the http address",href,"http://www.iki.fi/kaila");
                assertEquals ("myValue is not null",myValue,null);
                assertEquals ("The second parameter value",nice,"Kaila");
            }
            if (en.hasMoreNodes()) {
                node = en.nextNode();
                snode = (Text)node;
                assertEquals("The text of the element",snode.getText(),"Kaarle's homepage");
            }

            if (en.hasMoreNodes()) {
                node = en.nextNode();
                etag = (Tag)node;
                assertEquals("Endtag is G","/G", etag.getText());
            }
            // testing rest
            if (en.hasMoreNodes()) {
                node = en.nextNode();

                tag = (Tag)node;
                assertEquals("Follow up by p-tag","p", tag.getText());
            }
            if (en.hasMoreNodes()) {
                node = en.nextNode();
                snode = (Text)node;
                assertEquals("Verify the paragraph text","Paragraph", snode.getText());
            }
            if (en.hasMoreNodes()) {
                node = en.nextNode();
                etag = (Tag)node;
                assertEquals("Still patragraph endtag","/p", etag.getText());
            }

        } catch (ClassCastException ce) {
            fail("Bad class element = " + node.getClass().getName());
        }
    }


   /**
    * Test parseParameter method
    * Created by Kaarle Kaila (august 2002)
    * the tag name is here A (and should be eaten up by linkScanner)
    * Tests elements where = sign is surrounded by spaces
    */
    public void testParseParameterSpace() throws ParserException{
        Tag tag;
        Tag etag;
        Text snode;
        Node node=null;
        String lin1 = "<A yourParameter = \"Kaarle\">Kaarle's homepage</A>";
        createParser(lin1);
        NodeIterator en = parser.elements();
        String a,nice;

        try {

            if (en.hasMoreNodes()) {
                node = en.nextNode();

                tag = (Tag)node;
                a = ((Attribute)(tag.getAttributesEx ().elementAt (0))).getName ();
                nice = tag.getAttribute ("YOURPARAMETER");
                assertEquals ("Link tag (A)",a,"A");
                assertEquals ("yourParameter value","Kaarle",nice);
            }
            if (!(node instanceof LinkTag)) {
                // linkscanner has eaten up this piece
                if ( en.hasMoreNodes()) {
                    node = en.nextNode();
                    snode = (Text)node;
                    assertEquals("Value of element","Kaarle's homepage",snode.getText());
                }

                if (en.hasMoreNodes()) {
                    node = en.nextNode();
                    etag = (Tag)node;
                    assertEquals("Still patragraph endtag","/A",etag.getText());
                }
            }
            // testing rest

        } catch (ClassCastException ce) {
            fail("Bad class element = " + node.getClass().getName());
        }
    }

    /**
     * Reproduction of a bug reported by Annette Doyle
     * This is actually a pretty good example of dirty html - we are in a fix
     * here, bcos the font tag (the first one) has an erroneous inverted comma. In Tag,
     * we ignore anything in inverted commas, and dont if its outside. This kind of messes
     * up our parsing almost completely.
     */
    public void testStrictParsing() throws ParserException {
        String testHTML =
        "<div align=\"center\">" +
            "<font face=\"Arial,\"helvetica,\" sans-serif=\"sans-serif\" size=\"2\" color=\"#FFFFFF\">" +
                "<a href=\"/index.html\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Home</font></a>\n"+
                "<a href=\"/cia/notices.html\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Notices</font></a>\n"+
                "<a href=\"/cia/notices.html#priv\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Privacy</font></a>\n"+
                "<a href=\"/cia/notices.html#sec\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Security</font></a>\n"+
                "<a href=\"/cia/contact.htm\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Contact Us</font></a>\n"+
                "<a href=\"/cia/sitemap.html\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Site Map</font></a>\n"+
                "<a href=\"/cia/siteindex.html\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Index</font></a>\n"+
                "<a href=\"/search\" link=\"#000000\" vlink=\"#000000\"><font color=\"#FFFFFF\">Search</font></a>\n"+
            "</font>" +
        "</div>";

        createParser(testHTML,"http://www.cia.gov");
        parseAndAssertNodeCount(1);
        // Check the tags
        assertType("node",Div.class,node[0]);
        Div div = (Div)node[0];
        Tag fontTag = (Tag)div.children().nextNode();
        // an alternate interpretation: assertEquals("Second tag should be corrected","font face=\"Arial,helvetica,\" sans-serif=\"sans-serif\" size=\"2\" color=\"#FFFFFF\"",fontTag.getText());
        assertEquals("Second tag should be corrected","font face=\"Arial,\"helvetica,\" sans-serif=\"sans-serif\" size=\"2\" color=\"#FFFFFF\"",fontTag.getText());
        assertEquals("font sans-serif parameter","sans-serif",fontTag.getAttribute("SANS-SERIF"));
        // an alternate interpretation: assertEquals("font face parameter","Arial,helvetica,",table.get("FACE"));
        // another: assertEquals("font face parameter","Arial,\"helvetica,",table.get("FACE"));
        assertEquals("font face parameter","Arial,",fontTag.getAttribute("FACE"));
    }

    public void testToHTML() throws ParserException {
        String tag1 = "<MYTAG abcd\n"+
            "efgh\n"+
            "ijkl\n"+
            "mnop>";
        String testHTML = tag1 +
            "\n"+
            "<TITLE>Hello</TITLE>\n"+
            "<A HREF=\"Hello.html\">Hey</A>";
        createParser(testHTML);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(9);
        // The node should be an Tag
        assertTrue("1st Node should be a Tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertStringEquals("toHTML()",tag1,tag.toHtml());
        assertTrue("3rd Node should be a Tag",node[2] instanceof Tag);
        assertTrue("5th Node should be a Tag",node[6] instanceof Tag);
        tag = (Tag)node[2];
        assertEquals("Raw String of the tag","<TITLE>",tag.toHtml());
        tag = (Tag)node[6];
        assertEquals("Raw String of the tag","<A HREF=\"Hello.html\">",tag.toHtml());
    }


    /**
     * Test parseParameter method
     * Created by Kaarle Kaila (22 Oct 2001)
     * This test just wants the text in the element
     */
    public void testWithoutParseParameter() throws ParserException{
        Node node;
        String testHTML = "<A href=\"http://www.iki.fi/kaila\" myParameter yourParameter=\"Kaarle\">Kaarle's homepage</A><p>Paragraph</p>";
        createParser(testHTML);
        NodeIterator en = parser.elements();
        String result="";
        while (en.hasMoreNodes()) {
            node = en.nextNode();
            result += node.toHtml();
        }
        assertStringEquals("Check collected contents to original", testHTML, result);
    }

    /**
    * Test parseParameter method
    * Created by Kaarle Kaila (09 Jan 2003)
    * This test just wants the text in the element
    */
   public void testEmptyTagParseParameter() throws ParserException{
       Node node;
       String testHTML = "<INPUT name=\"foo\" value=\"foobar\" type=\"text\" />";

       createParser(testHTML);
       NodeIterator en = parser.elements();
       String result="";
       while (en.hasMoreNodes()) {
           node = en.nextNode();
           result = node.toHtml();
       }
       assertStringEquals("Check collected contents to original", testHTML, result);
    }


    public void testStyleSheetTag() throws ParserException{
        String testHTML1 = new String("<link rel src=\"af.css\"/>");
        createParser(testHTML1,"http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("StyleSheet Source","af.css",tag.getAttribute("src"));
    }

    /**
     * Bug report by Cedric Rosa, causing null pointer exceptions when encountering a broken tag,
     * and if this has no further lines to parse
     */
    public void testBrokenTag() throws ParserException{
        String testHTML1 = new String("<br");
        createParser(testHTML1);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("Node contents","br",tag.getText());
    }

    public void testTagInsideTag() throws ParserException {
        String testHTML = new String("<META name=\"Hello\" value=\"World </I>\">");
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("Node contents","META name=\"Hello\" value=\"World </I>\"",tag.getText());
        assertEquals("Meta Content","World </I>",tag.getAttribute("value"));

    }

    public void testIncorrectInvertedCommas() throws ParserException {
        String content = "DORIER-APPRILL E., GERVAIS-LAMBONY P., MORICONI-EBRARD F., NAVEZ-BOUCHANINE F.";
        String author = "Author";
        String guts = "META NAME=\"" + author + "\" CONTENT = \"" + content + "\"";
        String testHTML = "<" + guts + ">";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertStringEquals("Node contents",guts,tag.getText());
        assertEquals("Meta Content",author,tag.getAttribute("NAME"));
        
        //
        // Big todo here:
        // This involves a change in the lexer state machine from
        // six states to probably 8, or perhaps a half dozen 'substates'
        // on state zero...
        // we shy away from this at the moment:
//        assertEquals("Meta Content",content,tag.getAttribute("CONTENT"));
    }

    public void testIncorrectInvertedCommas2() throws ParserException {
        String guts = "META NAME=\"Keywords\" CONTENT=Moscou, modernisation, politique urbaine, sp\u00e9cificit\u00e9s culturelles, municipalit\u00e9, Moscou, modernisation, urban politics, cultural specificities, municipality\"";
        String testHTML = "<" + guts + ">";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertStringEquals("Node contents",guts,tag.getText());
    }

    public void testIncorrectInvertedCommas3() throws ParserException {
        String testHTML = new String("<meta name=\"description\" content=\"Une base de donn\u00e9es sur les th\u00e8ses de g\"ographie soutenues en France \">");
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("Node contents","meta name=\"description\" content=\"Une base de donn\u00e9es sur les th\u00e8ses de g\"ographie soutenues en France \"",tag.getText());
    }

    /**
     * Ignore empty tags.
     */
    public void testEmptyTag() throws ParserException {
        String testHTML = "<html><body><>text</body></html>";
        createParser(testHTML);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(5);
        assertTrue("Third node should be a string node",node[2] instanceof Text);
        Text stringNode = (Text)node[2];
        assertEquals("Third node has incorrect text","<>text",stringNode.getText());
    }

    /**
     * Ignore empty tags.
     */
    public void testEmptyTag2() throws ParserException {
        String testHTML = "<html><body>text<></body></html>";
        createParser(testHTML);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(5);
        assertTrue("Third node should be a string node",node[2] instanceof Text);
        Text stringNode = (Text)node[2];
        assertEquals("Third node has incorrect text","text<>",stringNode.getText());
    }

    /**
     * Ignore empty tags.
     */
    public void testEmptyTag3() throws ParserException {
        String testHTML = "<html><body>text<>text</body></html>";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Only node should be an HTML node",node[0] instanceof Html);
        Html html = (Html)node[0];
        assertTrue("HTML node should have one child",1 == html.getChildCount ());
        assertTrue("Only node should be an BODY node",html.getChild(0) instanceof BodyTag);
        BodyTag body = (BodyTag)html.getChild(0);
        assertTrue("BODY node should have one child",1 == body.getChildCount ());
        assertTrue("Only node should be a string node",body.getChild(0) instanceof Text);
        Text stringNode = (Text)body.getChild(0);
        assertEquals("Third node has incorrect text","text<>text",stringNode.getText());
    }

    /**
     * Ignore empty tags.
     */
    public void testEmptyTag4() throws ParserException {
        String testHTML = "<html><body>text\n<>text</body></html>";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Only node should be an HTML node",node[0] instanceof Html);
        Html html = (Html)node[0];
        assertTrue("HTML node should have one child",1 == html.getChildCount ());
        assertTrue("Only node should be an BODY node",html.getChild(0) instanceof BodyTag);
        BodyTag body = (BodyTag)html.getChild(0);
        assertTrue("BODY node should have one child",1 == body.getChildCount ());
        assertTrue("Only node should be a string node",body.getChild(0) instanceof Text);
        Text stringNode = (Text)body.getChild(0);
        String actual = stringNode.getText();
        assertEquals("Third node has incorrect text","text\n<>text",actual);
    }

    /**
     * Ignore empty tags.
     */
    public void testEmptyTag5() throws ParserException {
        String testHTML = "<html><body>text<\n>text</body></html>";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Only node should be an HTML node",node[0] instanceof Html);
        Html html = (Html)node[0];
        assertTrue("HTML node should have one child",1 == html.getChildCount ());
        assertTrue("Only node should be an BODY node",html.getChild(0) instanceof BodyTag);
        BodyTag body = (BodyTag)html.getChild(0);
        assertTrue("BODY node should have one child",1 == body.getChildCount ());
        assertTrue("Only node should be a string node",body.getChild(0) instanceof Text);
        Text stringNode = (Text)body.getChild(0);
        String actual = stringNode.getText();
        assertEquals("Third node has incorrect text","text<\n>text",actual);
    }

    /**
     * Ignore empty tags.
     */
    public void testEmptyTag6() throws ParserException {
        String testHTML = "<html><body>text<>\ntext</body></html>";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Only node should be an HTML node",node[0] instanceof Html);
        Html html = (Html)node[0];
        assertTrue("HTML node should have one child",1 == html.getChildCount ());
        assertTrue("Only node should be an BODY node",html.getChild(0) instanceof BodyTag);
        BodyTag body = (BodyTag)html.getChild(0);
        assertTrue("BODY node should have one child",1 == body.getChildCount ());
        assertTrue("Only node should be a string node",body.getChild(0) instanceof Text);
        Text stringNode = (Text)body.getChild(0);
        String actual = stringNode.getText();
        assertEquals("Third node has incorrect text","text<>\ntext",actual);
    }

    public void testAttributesReconstruction() throws ParserException {
        String expectedHTML = "<TEXTAREA name=\"JohnDoe\" >";
        String testHTML = expectedHTML + "</TEXTAREA>";
        createParser(testHTML);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(2);
        assertTrue("First node should be an HTMLtag",node[0] instanceof Tag);
        Tag htmlTag = (Tag)node[0];
        assertStringEquals("Expected HTML",expectedHTML,htmlTag.toHtml());
    }

    public void testIgnoreState() throws ParserException
    {
        String testHTML = "<A \n"+
        "HREF=\"/a?b=c>d&e=f&g=h&i=http://localhost/Testing/Report1.html\">20020702 Report 1</A>";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        String href = tag.getAttribute("HREF");
        assertStringEquals("Resolved Link","/a?b=c>d&e=f&g=h&i=http://localhost/Testing/Report1.html",href);
    }

    /**
     * See bug #726913 toHtml() method incomplete
     */
    public void testSetText() throws ParserException
    {
        String testHTML = "<LABEL ID=\"JohnDoe\">John Doe</LABEL>";
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        org.htmlparser.tags.LabelTag htmlTag = (org.htmlparser.tags.LabelTag)node[0];
        String expectedHTML = "<LABEL ID=\"JohnDoe\">John Doe</LABEL>";
        assertStringEquals("Expected HTML",expectedHTML,htmlTag.toHtml());
        assertStringEquals("Expected HTML","John Doe",htmlTag.getLabel());

        ((org.htmlparser.Text)((org.htmlparser.tags.CompositeTag)htmlTag).getChild(0)).setText("Jane Doe");
        expectedHTML = "<LABEL ID=\"JohnDoe\">Jane Doe</LABEL>";
        assertStringEquals("Expected HTML",expectedHTML,htmlTag.toHtml());
        assertStringEquals("Expected HTML","Jane Doe",htmlTag.getLabel());
    }

    /**
     * From oyoaha
     */
    public void testTabText () throws ParserException
    {
        String testHTML = "<a\thref=\"http://cbc.ca\">";
        createParser (testHTML);
        parseAndAssertNodeCount (1);
        assertTrue("Node should be a LinkTag", node[0] instanceof LinkTag);
        LinkTag tag = (LinkTag)node[0];
        String href = tag.getAttribute ("HREF");
        assertStringEquals("Resolved Link","http://cbc.ca", href);
    }

    /**
     * See bug #741026 registerScanners() mangles output HTML badly.
     */
    public void testHTMLOutputOfDifficultLinksWithRegisterScanners () throws ParserException
    {
        // straight out of a real world example
        String html = "<a href=http://www.google.com/webhp?hl=en>";
        createParser (html);
        String temp = null;
        for (NodeIterator e = parser.elements (); e.hasMoreNodes ();)
        {
            Node newNode = e.nextNode ();  // Get the next HTML Node
            temp = newNode.toHtml();
        }
        assertNotNull ("No nodes", temp);
        assertStringEquals ("Incorrect HTML output: ", html + "</a>", temp);
    }

    /**
     * See bug #740411 setParsed() has no effect on output.
     */
    public void testParameterChange() throws ParserException
    {
        createParser("<TABLE BORDER=0>");
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(1);
        // the node should be a Tag
        assertTrue("Node should be a Tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("Initial text should be","TABLE BORDER=0",tag.getText ());
        tag.setAttribute ("BORDER","\"1\"");
        assertEquals("HTML should be","<TABLE BORDER=\"1\">", tag.toHtml ());
    }
}
