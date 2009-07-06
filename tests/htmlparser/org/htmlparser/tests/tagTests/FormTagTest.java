// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/FormTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:24 $
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

package org.htmlparser.tests.tagTests;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class FormTagTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.FormTagTest", "FormTagTest");
    }

    public static final String FORM_HTML =
    "<FORM METHOD=\""+FormTag.POST+"\" ACTION=\"do_login.php\" NAME=\"login_form\" onSubmit=\"return CheckData()\">\n"+
        "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>User Name</b></font></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"text\" NAME=\"name\" SIZE=\"20\"></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>Password</b></font></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"password\" NAME=\"passwd\" SIZE=\"20\"></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Login\"></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"+
        "<TEXTAREA name=\"Description\" rows=\"15\" cols=\"55\" wrap=\"virtual\" class=\"composef\" tabindex=\"5\">Contents of TextArea</TEXTAREA>\n"+
//      "<TEXTAREA name=\"AnotherDescription\" rows=\"15\" cols=\"55\" wrap=\"virtual\" class=\"composef\" tabindex=\"5\">\n"+
        "<INPUT TYPE=\"hidden\" NAME=\"password\" SIZE=\"20\">\n"+
        "<INPUT TYPE=\"submit\">\n"+
        "</FORM>";

    public FormTagTest(String name) {
        super(name);
    }

    public void assertTypeNameSize(String description,String type,String name,String size,InputTag inputTag)
    {
        assertEquals(description+" type",type,inputTag.getAttribute("TYPE"));
        assertEquals(description+" name",name,inputTag.getAttribute("NAME"));
        assertEquals(description+" size",size,inputTag.getAttribute("SIZE"));
    }

    public void assertTypeNameValue(String description,String type,String name,String value,InputTag inputTag)
    {
        assertEquals(description+" type",type,inputTag.getAttribute("TYPE"));
        assertEquals(description+" name",name,inputTag.getAttribute("NAME"));
        assertEquals(description+" value",value,inputTag.getAttribute("VALUE"));
    }

    public void testScan() throws ParserException
    {
        createParser(FORM_HTML,"http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be Form Tag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];
        assertStringEquals("Method",FormTag.POST,formTag.getFormMethod());
        assertStringEquals("Location","http://www.google.com/test/do_login.php",formTag.getFormLocation());
        assertStringEquals("Name","login_form",formTag.getFormName());
        InputTag nameTag = formTag.getInputTag("name");
        InputTag passwdTag = formTag.getInputTag("passwd");
        InputTag submitTag = formTag.getInputTag("submit");
        InputTag dummyTag = formTag.getInputTag("dummy");
        assertNotNull("Input Name Tag should not be null",nameTag);
        assertNotNull("Input Password Tag should not be null",passwdTag);
        assertNotNull("Input Submit Tag should not be null",submitTag);
        assertNull("Input dummy tag should be null",dummyTag);

        assertTypeNameSize("Input Name Tag","text","name","20",nameTag);
        assertTypeNameSize("Input Password Tag","password","passwd","20",passwdTag);
        assertTypeNameValue("Input Submit Tag","submit","submit","Login",submitTag);

        TextareaTag textAreaTag = formTag.getTextAreaTag("Description");
        assertNotNull("Text Area Tag should have been found",textAreaTag);
        assertEquals("Text Area Tag Contents","Contents of TextArea",textAreaTag.getValue());
        assertNull("Should have been null",formTag.getTextAreaTag("junk"));

        assertStringEquals("toHTML",FORM_HTML,formTag.toHtml());
    }

    public void testScanFormWithNoEnding() throws Exception
    {
        createParser(
        "<TABLE>\n"+
        "<FORM METHOD=\"post\" ACTION=\"do_login.php\" NAME=\"login_form\" onSubmit=\"return CheckData()\">\n"+
        "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>User Name</b></font></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"text\" NAME=\"name\" SIZE=\"20\"></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><FONT face=\"Arial, verdana\" size=2><b>Password</b></font></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"password\" NAME=\"passwd\" SIZE=\"20\"></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"+
        "<TR><TD ALIGN=\"center\"><INPUT TYPE=\"submit\" NAME=\"submit\" VALUE=\"Login\"></TD></TR>\n"+
        "<TR><TD ALIGN=\"center\">&nbsp;</TD></TR>\n"+
        "<INPUT TYPE=\"hidden\" NAME=\"password\" SIZE=\"20\">\n"+
        "</TABLE>","http://www.google.com/test/index.html");

        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new FormTag (),
                    new InputTag (),
                    new TextareaTag (),
                    new SelectTag (),
                    new OptionTag (),
                }));

        parseAndAssertNodeCount(4);
    }

    /**
     * Bug reported by Pavan Podila - forms with links are not being parsed
     * Sample html is from google
     */
    public void testScanFormWithLinks() throws ParserException
    {
        createParser(
        "<form action=\"/search\" name=f><table cellspacing=0 cellpadding=0><tr><td width=75>&nbsp;"+
        "</td><td align=center><input type=hidden name=hl value=en><input type=hidden name=ie "+
        "value=\"UTF-8\"><input type=hidden name=oe value=\"UTF-8\"><input maxLength=256 size=55"+
        " name=q value=\"\"><br><input type=submit value=\"Google Search\" name=btnG><input type="+
        "submit value=\"I'm Feeling Lucky\" name=btnI></td><td valign=top nowrap><font size=-2>"+
        "&nbsp;&#8226;&nbsp;<a href=/advanced_search?hl=en>Advanced&nbsp;Search</a><br>&nbsp;&#8226;"+
        "&nbsp;<a href=/preferences?hl=en>Preferences</a><br>&nbsp;&#8226;&nbsp;<a href=/"+
        "language_tools?hl=en>Language Tools</a></font></td></tr></table></form>"
        );

        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new FormTag (),
                    new InputTag (),
                    new TextareaTag (),
                    new SelectTag (),
                    new OptionTag (),
                    new LinkTag (),
                    new TableTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Should be a HTMLFormTag",node[0] instanceof FormTag);
        NodeList linkTags = new NodeList ();
        NodeClassFilter filter = new NodeClassFilter (LinkTag.class);
        for (NodeIterator e = ((FormTag)node[0]).children (); e.hasMoreNodes ();)
            e.nextNode ().collectInto (linkTags, filter);
        assertEquals("Link Tag Count",3,linkTags.size ());
        LinkTag[] linkTag = new LinkTag[3];
        linkTags.copyToNodeArray (linkTag);
        assertEquals("First Link Tag Text","Advanced&nbsp;Search",linkTag[0].getLinkText());
        assertEquals("Second Link Tag Text","Preferences",linkTag[1].getLinkText());
        assertEquals("Third Link Tag Text","Language Tools",linkTag[2].getLinkText());
    }
    /**
     * Bug 652674 - forms with comments are not being parsed
     */
    public void testScanFormWithComments() throws ParserException {
        createParser(
        "<form action=\"/search\" name=f><table cellspacing=0 cellpadding=0><tr><td width=75>&nbsp;"+
        "</td><td align=center><input type=hidden name=hl value=en><input type=hidden name=ie "+
        "value=\"UTF-8\"><input type=hidden name=oe value=\"UTF-8\"><!-- Hello World -->"+
        "<input maxLength=256 size=55"+
        " name=q value=\"\"><br><input type=submit value=\"Google Search\" name=btnG><input type="+
        "submit value=\"I'm Feeling Lucky\" name=btnI></td><td valign=top nowrap><font size=-2>"+
        "&nbsp;&#8226;&nbsp;<a href=/advanced_search?hl=en>Advanced&nbsp;Search</a><br>&nbsp;&#8226;"+
        "&nbsp;<a href=/preferences?hl=en>Preferences</a><br>&nbsp;&#8226;&nbsp;<a href=/"+
        "language_tools?hl=en>Language Tools</a></font></td></tr></table></form>"
        );

        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new FormTag (),
                    new InputTag (),
                    new TextareaTag (),
                    new SelectTag (),
                    new OptionTag (),
                    new TableTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Should be a HTMLFormTag",node[0] instanceof FormTag);
        NodeList remarkNodes = new NodeList ();
        NodeClassFilter filter = new NodeClassFilter (Remark.class);
        for (NodeIterator e = ((FormTag)node[0]).children (); e.hasMoreNodes ();)
            e.nextNode ().collectInto (remarkNodes, filter);
        assertEquals("Remark Node Count",1,remarkNodes.size ());
        assertEquals("First Remark Node"," Hello World ",remarkNodes.elementAt (0).toPlainTextString());
    }
    /**
     * Bug 652674 - forms with comments are not being parsed
     */
    public void testScanFormWithComments2() throws ParserException {
        createParser(
        "<FORM id=\"id\" name=\"name\" action=\"http://some.site/aPage.asp?id=97\" method=\"post\">\n"+
        "   <!--\n"+
        "   Just a Comment\n"+
        "   -->\n"+
        "</FORM>");
        parseAndAssertNodeCount(1);
        assertTrue("Should be a HTMLFormTag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];
        Remark [] remarkNode = new Remark[10];
        int i = 0;
        for (NodeIterator e=formTag.children();e.hasMoreNodes();) {
            Node formNode = e.nextNode();
            if (formNode instanceof Remark) {
                remarkNode[i++] = (Remark)formNode;
            }
        }
        assertEquals("Remark Node Count",1,i);
    }

    /**
     * Bug 656870 - a form tag with a previously open link causes infinite loop
     * on encounter
     */
    public void testScanFormWithPreviousOpenLink() throws ParserException {
        createParser(
            "<A HREF=\"http://www.oygevalt.org/\">Home</A>\n"+
            "<P>\n"+
            "And now, the good stuff:\n"+
            "<P>\n"+
            "<A HREF=\"http://www.yahoo.com\">Yahoo!\n"+
            "<FORM ACTION=\".\" METHOD=\"GET\">\n"+
                "<INPUT TYPE=\"TEXT\">\n"+
                "<BR>\n"+
                "<A HREF=\"http://www.helpme.com\">Help</A> " +
                "<INPUT TYPE=\"checkbox\">\n"+
                "<P>\n"+
                "<INPUT TYPE=\"SUBMIT\">\n"+
            "</FORM>"
        );
        parseAndAssertNodeCount(5);
        assertTrue("Fourth Node is a paragraph",node[3] instanceof ParagraphTag);
        ParagraphTag paragraph = (ParagraphTag)node[3];
        assertTrue("Second Node of paragraph is a link", paragraph.getChildren ().elementAt (1) instanceof LinkTag);
        LinkTag linkTag = (LinkTag)paragraph.getChildren ().elementAt (1);
        assertEquals("Link Text","Yahoo!\n",linkTag.getLinkText());
        assertEquals("Link URL","http://www.yahoo.com",linkTag.getLink());
        assertType("Fifth Node",FormTag.class,node[4]);
    }

    /**
     * Bug 713907 reported by Dhaval Udani, erroneous
     * parsing of form tag (even when form scanner is not
     * registered)
     */
    public void testFormScanningShouldNotHappen() throws Exception {
        String testHTML =
            "<HTML><HEAD><TITLE>Test Form Tag</TITLE></HEAD>" +
            "<BODY><FORM name=\"form0\"><INPUT type=\"text\" name=\"text0\"></FORM>" +
            "</BODY></HTML>";
        createParser(
            testHTML
        );
        ((PrototypicalNodeFactory)parser.getNodeFactory ()).unregisterTag (new FormTag ());
        Node [] nodes =
            parser.extractAllNodesThatAre(
                FormTag.class
            );
        assertEquals(
            "shouldnt have found form tag",
            0,
            nodes.length
        );
    }

    /**
     * See bug #745566 StackOverflowError on select with too many unclosed options.
     * Under Windows this throws a stack overflow exception.
     */
    public void testUnclosedOptions () throws ParserException
    {
        String url = "http://htmlparser.sourceforge.net/test/overflowpage.html";
        int i;
        Node[] nodes;

        parser = new Parser(url);
        PrototypicalNodeFactory factory = new PrototypicalNodeFactory ();
        // we want to expose the repetitive tags
        factory.unregisterTag (new Html ());
        factory.unregisterTag (new HeadTag ());
        factory.unregisterTag (new BodyTag ());
        factory.unregisterTag (new ParagraphTag ());
        parser.setNodeFactory (factory);
        i = 0;
        nodes = new Node[50];
        for (NodeIterator e = parser.elements(); e.hasMoreNodes();)
            nodes[i++] = e.nextNode();
        assertEquals ("Expected nodes", 39, i);
    }
    
    public void testSetFormLocation() throws ParserException
    {
        createParser(FORM_HTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be Form Tag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];

        formTag.setFormLocation("http://www.yahoo.com/yahoo/do_not_login.jsp");
        String expected = 
            FORM_HTML.substring (0, FORM_HTML.indexOf ("\"do_login.php\""))
            + "\"http://www.yahoo.com/yahoo/do_not_login.jsp\""
            + FORM_HTML.substring (FORM_HTML.indexOf ("\"do_login.php\"") + 14);
        assertStringEquals("Raw String",expected,formTag.toHtml());
    }

    public void testToPlainTextString() throws ParserException
    {
        createParser(FORM_HTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be Form Tag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];
        assertStringEquals("Form Tag string representation","\n&nbsp;\nUser Name\n\nPassword\n\n&nbsp;\n\n&nbsp;\nContents of TextArea\n\n\n", formTag.toPlainTextString());
    }

    public void testSearchFor() throws ParserException
    {
        createParser(FORM_HTML);

        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new FormTag (),
                    new InputTag (),
                    new TextareaTag (),
                    new SelectTag (),
                    new OptionTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be Form Tag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];
        NodeList nodeList = formTag.searchFor("USER NAME");
        assertEquals("Should have found nodes",1,nodeList.size());

        Node[] nodes = nodeList.toNodeArray();

        assertEquals("Number of nodes found",1,nodes.length);
        assertType("search result node",Text.class,nodes[0]);
        Text stringNode = (Text)nodes[0];
        assertEquals("Expected contents of string node","User Name",stringNode.getText());
    }

    public void testSearchForCaseSensitive() throws ParserException
    {
        createParser(FORM_HTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be Form Tag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];
        NodeList nodeList = formTag.searchFor("USER NAME",true);
        assertEquals("Should have not found nodes",0,nodeList.size());

        nodeList = formTag.searchFor("User Name",true);
        assertNotNull("Should have not found nodes",nodeList);
    }


    public void testSearchByName() throws ParserException
    {
        createParser(FORM_HTML);

        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new FormTag (),
                    new InputTag (),
                    new TextareaTag (),
                    new SelectTag (),
                    new OptionTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be Form Tag",node[0] instanceof FormTag);
        FormTag formTag = (FormTag)node[0];

        Tag tag= formTag.searchByName("passwd");
        assertNotNull("Should have found the password node",tag);
        assertType("tag found",InputTag.class,tag);
    }

    /**
     * Bug 713907 reported by Dhaval Udani, erroneous
     * attributes being reported.
     */
    public void testFormRendering() throws Exception
    {
        String testHTML =
            "<HTML><HEAD><TITLE>Test Form Tag</TITLE></HEAD>" +
            "<BODY><FORM name=\"form0\"><INPUT type=\"text\" name=\"text0\"></FORM>" +
            "</BODY></HTML>";
        createParser(
            testHTML
        );
        FormTag formTag =
            (FormTag)(parser.extractAllNodesThatAre(
                FormTag.class
            )[0]);
        assertNotNull("Should have found a form tag",formTag);
        assertStringEquals("name","form0",formTag.getFormName());
        assertNull("action",formTag.getAttribute("ACTION"));
        assertXmlEquals(
            "html",
            "<FORM NAME=\"form0\">" +
                "<INPUT TYPE=\"text\" NAME=\"text0\">" +
            "</FORM>",
            formTag.toHtml()
        );
    }

    /**
     * From support request #772998 Cannot extract input tags
     * The getFormInputs list was reporting zero size and textarea tags were
     * in the inputs list.
     * Neither of these was reproducible.
     */
    public void testTextArea () throws Exception
    {
        FormTag formTag;
        NodeList nl;
        InputTag inpTag;
        TextareaTag texTag;
        
        String html = "<body onload=\"otextnloadHandler()\" onunload=\"closeAdvanced()\">\n" +
            "	<form name=\"searchForm\" onsubmit=\"doSearch()\">\n" +
            "		<table id=\"searchTable\" align=\"left\" valign=\"middle\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
            "			<tbody><tr nowrap=\"\" valign=\"middle\">\n" +
            "				<td id=\"searchTD\">\n" +
            "					<label id=\"searchLabel\" for=\"searchWord\">\n" +
            "					 Search:\n" +
            "					</label>\n" +
            "				</td>\n" +
            "\n" +
            "				<td>\n" +
            "					<input type=\"text\" id=\"searchWord\" name=\"searchWord\" value=\"\" size=\"24\" maxlength=\"256\" alt=\"Search Expression\">\n" +
            "				</td>\n" +
            // note: this was added as there weren't any textarea tags in the page referenced
            "				<td>\n" +
            "					<textarea name=\"mytextarea\" rows=\"1\" cols=\"12\" alt=\"Free Form Text\">\n" +
            "					   The text.\n" +
            "					</textarea>\n" +
            "				</td>\n" +
            "				<td>\n" +
            "					 <input type=\"button\" onclick=\"this.blur();doSearch()\" value=\"GO\" id=\"go\" alt=\"GO\">\n" +
            "					<input type=\"hidden\" name=\"maxHits\" value=\"500\">\n" +
            "				</td>\n" +
            "				<td nowrap=\"nowrap\">\n" +
            "\n" +
            "					<a id=\"scopeLabel\" href=\"javascript:openAdvanced();\" title=\"Search only the following topics\" alt=\"Search only the following topics\" onmouseover=\"window.status='Search only the following topics'; return true;\" onmouseout=\"window.status='';\">Search scope:</a>\n" +
            "				</td>\n" +
            "				<td nowrap=\"nowrap\">\n" +
            "					<input type=\"hidden\" name=\"workingSet\" value=\"All topics\">\n" +
            "					<div id=\"scope\">All topics</div>\n" +
            "				</td>\n" +
            "			</tr>\n" +
            "\n" +
            "		</tbody></table>\n" +
            "	</form>\n" +
            "\n" +
            "</body>\n";
        createParser (html);
        formTag =
            (FormTag)(parser.extractAllNodesThatAre (
                FormTag.class
            )[0]);
        assertNotNull ("Should have found a form tag",formTag);
        assertStringEquals ("name", "searchForm", formTag.getFormName ());
        nl = formTag.getFormInputs ();
        assertTrue ("4 inputs", 4 == nl.size ());
        inpTag = (InputTag)nl.elementAt (0);
        assertStringEquals ("name", "searchWord", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "", inpTag.getAttribute ("value"));
        inpTag = (InputTag)nl.elementAt (1);
        assertNull ("name", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "GO", inpTag.getAttribute ("value"));
        inpTag = (InputTag)nl.elementAt (2);
        assertStringEquals ("name", "maxHits", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "500", inpTag.getAttribute ("value"));
        inpTag = (InputTag)nl.elementAt (3);
        assertStringEquals ("name", "workingSet", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "All topics", inpTag.getAttribute ("value"));
        nl = formTag.getFormTextareas ();
        assertTrue ("1 textarea", 1 == nl.size ());
        texTag = (TextareaTag)nl.elementAt (0);
        assertStringEquals ("name", "mytextarea", texTag.getAttribute ("name"));
        assertTrue ("only 1 child", 1 == texTag.getChildCount ());
        assertStringEquals ("text contents", "\n					   The text.\n					", texTag.getChild (0).toHtml ());
    }

    /**
     * From bug #825645 <input> not getting parsed inside table
     */
    public void testInputInTable () throws Exception
    {
        FormTag formTag;
        NodeList nl;
        InputTag inpTag;

        String html = "<html>\n" +
            "<body>\n" +
            "<form action=\"/cgi-bin/test.pl\" method=\"post\">\n" +
            "<table><tr><td>\n" +
            "<INPUT type=hidden NAME=\"test1\" VALUE=\"insidetable\">\n" +
            "</td></tr>\n" +
            "</table>\n" +
            "<INPUT type=hidden NAME=\"Test2\"\n" +
            "VALUE=\"outsidetable\">\n" +
            "<INPUT type=hidden name=\"a\" value=\"b\">\n" +
            "</form>\n" +
            "</body>\n" +
            "</html>\n";
        createParser (html);
        formTag =
            (FormTag)(parser.extractAllNodesThatAre (
                FormTag.class
            )[0]);
        assertNotNull ("Should have found a form tag",formTag);
        nl = formTag.getFormInputs ();
        assertTrue ("3 inputs", 3 == nl.size ());
        inpTag = (InputTag)nl.elementAt (0);
        assertStringEquals ("name", "test1", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "insidetable", inpTag.getAttribute ("value"));
        inpTag = (InputTag)nl.elementAt (1);
        assertStringEquals ("name", "Test2", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "outsidetable", inpTag.getAttribute ("value"));
        inpTag = (InputTag)nl.elementAt (2);
        assertStringEquals ("name", "a", inpTag.getAttribute ("name"));
        assertStringEquals ("value", "b", inpTag.getAttribute ("value"));
    }
}
