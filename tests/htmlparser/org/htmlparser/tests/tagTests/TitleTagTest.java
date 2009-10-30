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

import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.tags.BaseHrefTag;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TitleTagTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.TitleTagTest", "TitleTagTest");
    }

    private TitleTag titleTag;
    private String prefix = "<html><head>";
    private String tag1 = "<title>Yahoo!</title>";
    private String tag2 = "<base href=http://www.yahoo.com/ target=_top>";
    private String tag3 = "<meta http-equiv=\"PICS-Label\" content='(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l r (cz 1 lz 1 nz 1 oz 1 vz 1) gen true for \"http://www.yahoo.com\" r (cz 1 lz 1 nz 1 oz 1 vz 1) \"http://www.rsac.org/ratingsv01.html\" l r (n 0 s 0 v 0 l 0) gen true for \"http://www.yahoo.com\" r (n 0 s 0 v 0 l 0))'>";
    private String tag4 = "<style>a.h{background-color:#ffee99}</style>";
    private String suffix = "</head>";

    public TitleTagTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        super.setUp();
        createParser(prefix + tag1 + tag2 + tag3 + tag4 + suffix);
        parseAndAssertNodeCount(1);
        assertTrue("Only node should be an HTML node",node[0] instanceof Html);
        Html html = (Html)node[0];
        assertTrue("HTML node should have one child",1 == html.getChildCount ());
        assertTrue("Only node should be an HEAD node",html.getChild(0) instanceof HeadTag);
        HeadTag head = (HeadTag)html.getChild(0);
        assertTrue("HEAD node should have four children",4 == head.getChildCount ());
        assertTrue("First child should be a title tag",head.getChild(0) instanceof TitleTag);
        titleTag = (TitleTag)head.getChild(0);
    }
    public void testToPlainTextString() throws ParserException {
        // check the title node
        assertEquals("Title","Yahoo!",titleTag.toPlainTextString());
    }

    public void testToHTML() throws ParserException {
        assertStringEquals("Raw String",tag1,titleTag.toHtml());
    }

    public void testToString() throws ParserException  {
        assertEquals("Title","TITLE: Yahoo!",titleTag.toString());
    }

    public void testScan() throws ParserException {
        createParser("<html><head><title>Yahoo!</title><base href=http://www.yahoo.com/ target=_top><meta http-equiv=\"PICS-Label\" content='(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l r (cz 1 lz 1 nz 1 oz 1 vz 1) gen true for \"http://www.yahoo.com\" r (cz 1 lz 1 nz 1 oz 1 vz 1) \"http://www.rsac.org/ratingsv01.html\" l r (n 0 s 0 v 0 l 0) gen true for \"http://www.yahoo.com\" r (n 0 s 0 v 0 l 0))'><style>a.h{background-color:#ffee99}</style></head>");
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new TitleTag (),
                    new StyleTag (),
                    new MetaTag (),
                }));
        parseAndAssertNodeCount(7);
        assertTrue(node[2] instanceof TitleTag);
        // check the title node
        TitleTag titleTag = (TitleTag) node[2];
        assertEquals("Title","Yahoo!",titleTag.getTitle());
    }

    /**
     * Testcase to reproduce a bug reported by Cedric Rosa,
     * on not ending the title tag correctly, we would get
     * null pointer exceptions..
     */
    public void testIncompleteTitle() throws ParserException {
        String text =
            "<HTML>\n"+
            "<HEAD>\n"+
            // note the missing angle bracket on the close title:
            "<TITLE>SISTEMA TERRA, VOL. VI , No. 1-3, December 1997</TITLE\n"+
            "</HEAD>\n"+
            "<BODY>\n"+
            "The body.\n"+
            "</BODY>\n"+
            "</HTML>";        
        createParser(text);
        parseAndAssertNodeCount(1);
        assertTrue ("Only node is a html tag",node[0] instanceof Html);
        Html html = (Html)node[0];
        assertEquals ("Html node has five children", 5, html.getChildCount ());
        assertTrue ("Second child is a head tag", html.childAt (1) instanceof HeadTag);
        HeadTag head = (HeadTag)html.childAt (1);
        assertEquals ("Head node has two children", 2, head.getChildCount ());
        assertTrue ("Second child is a title tag", head.childAt (1) instanceof TitleTag);
        TitleTag titleTag = (TitleTag)head.childAt (1);
        assertEquals("Title","SISTEMA TERRA, VOL. VI , No. 1-3, December 1997",titleTag.getTitle());
// Note: this will fail because of the extra > inserted to finish the /TITLE tag:
//        assertStringEquals ("toHtml", text, html.toHtml ());
    }

    /**
     * If there are duplicates of the title tag, the parser crashes.
     * This bug was reported by Claude Duguay
     */
    public void testDoubleTitleTag() throws ParserException{
        createParser(
        "<html><head><TITLE>\n"+
        "<html><head><TITLE>\n"+
        "Double tags can hang the code\n"+
        "</TITLE></head><body>\n"+
        "<body><html>");
        parser.setNodeFactory (new PrototypicalNodeFactory (new TitleTag ()));
        parseAndAssertNodeCount(9);
        assertTrue("Third tag should be a title tag",node[2] instanceof TitleTag);
        TitleTag titleTag = (TitleTag)node[2];
        assertEquals("Title","\n",titleTag.getTitle());
        assertTrue("Fourth tag should be a title tag",node[3] instanceof TitleTag);
        titleTag = (TitleTag)node[3];
        assertEquals("Title","\nDouble tags can hang the code\n",titleTag.getTitle());
    }

    /**
     * Testcase based on Claude Duguay's report. This proves
     * that the parser throws exceptions when faced with malformed html
     */
    public void testNoEndTitleTag() throws ParserException {
        createParser(
        "<TITLE>KRP VALIDATION<PROCESS/TITLE>");
        parseAndAssertNodeCount(1);
        TitleTag titleTag = (TitleTag)node[0];
        assertEquals("Expected title","KRP VALIDATION",titleTag.getTitle());
    }

    public void testTitleTagContainsJspTag() throws ParserException {
        String title = "<title><%=gTitleString%></title>";
        createParser("<html><head>" + title + "<base href=http://www.yahoo.com/ target=_top><meta http-equiv=\"PICS-Label\" content='(PICS-1.1 \"http://www.icra.org/ratingsv02.html\" l r (cz 1 lz 1 nz 1 oz 1 vz 1) gen true for \"http://www.yahoo.com\" r (cz 1 lz 1 nz 1 oz 1 vz 1) \"http://www.rsac.org/ratingsv01.html\" l r (n 0 s 0 v 0 l 0) gen true for \"http://www.yahoo.com\" r (n 0 s 0 v 0 l 0))'><style>a.h{background-color:#ffee99}</style></head>");
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new TitleTag (),
                    new BaseHrefTag (),
                    new MetaTag (),
                    new StyleTag (),
                }));
        parseAndAssertNodeCount(7);
        assertTrue(node[2] instanceof TitleTag);
        TitleTag titleTag = (TitleTag) node[2];
        assertStringEquals("HTML Rendering",title,titleTag.toHtml());
    }
}
