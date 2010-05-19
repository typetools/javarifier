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

package org.htmlparser.tests.lexerTests;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TagTests extends ParserTestCase {
    static
    {
        System.setProperty ("org.htmlparser.tests.lexerTests.TagTests", "TagTests");
    }

    private static final String TEST_HTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
        "<!-- Server: sf-web2 -->\n" +
        "<html lang=\"en\">\n" +
        "  <head><link rel=\"stylesheet\" type=\"text/css\" href=\"http://sourceforge.net/cssdef.php\">\n" +
        "   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
        "    <TITLE>SourceForge.net: Modify: 711073 - HTMLTagParser not threadsafe as a static variable in Tag</TITLE>\n" +
        "   <SCRIPT language=\"JavaScript\" type=\"text/javascript\">\n" +
        "   <!--\n" +
        "   function help_window(helpurl) {\n" +
        "       HelpWin = window.open( 'http://sourceforge.net' + helpurl,'HelpWindow','scrollbars=yes,resizable=yes,toolbar=no,height=400,width=400');\n" +
        "   }\n" +
        "   // -->\n" +
        "   </SCRIPT>\n" +
        "       <link rel=\"SHORTCUT ICON\" href=\"/images/favicon.ico\">\n" +
        "<!-- This is temp javascript for the jump button. If we could actually have a jump script on the server side that would be ideal -->\n" +
        "<script language=\"JavaScript\" type=\"text/javascript\">\n" +
        "<!--\n" +
        "   function jump(targ,selObj,restore){ //v3.0\n" +
        "   if (selObj.options[selObj.selectedIndex].value)\n" +
        "       eval(targ+\".location='\"+selObj.options[selObj.selectedIndex].value+\"'\");\n" +
        "   if (restore) selObj.selectedIndex=0;\n" +
        "   }\n" +
        "   //-->\n" +
        "</script>\n" +
        "<a href=\"http://normallink.com/sometext.html\">\n" +
        "<style type=\"text/css\">\n" +
        "<!--\n" +
        "A:link { text-decoration:none }\n" +
        "A:visited { text-decoration:none }\n" +
        "A:active { text-decoration:none }\n" +
        "A:hover { text-decoration:underline; color:#0066FF; }\n" +
        "-->\n" +
        "</style>\n" +
        "</head>\n" +
        "<body bgcolor=\"#FFFFFF\" text=\"#000000\" leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" link=\"#003399\" vlink=\"#003399\" alink=\"#003399\">\n";
    private int testProgress;

    public TagTests (String name) {
        super(name);
    }

    public void testTagWithQuotes() throws Exception {
        String testHtml =
        "<img src=\"http://g-images.amazon.com/images/G/01/merchants/logos/marshall-fields-logo-20.gif\" width=87 height=20 border=0 alt=\"Marshall Field's\">";

        createParser(testHtml);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(1);
        assertType("should be Tag",Tag.class,node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals("alt","Marshall Field's",tag.getAttribute("ALT"));
        assertStringEquals(
            "html",
            testHtml,
            tag.toHtml()
        );
    }

    public void testEmptyTag() throws Exception 
    {
        String html = "<custom/>";
        createParser(html);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(1);
        assertType("should be Tag",Tag.class,node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals("tag name","CUSTOM",tag.getTagName());
        assertTrue("empty tag",tag.isEmptyXmlTag());
        assertStringEquals(
            "html",
            html,
            tag.toHtml()
        );
    }

    public void testTagWithCloseTagSymbolInAttribute() throws ParserException {
        createParser("<tag att=\"a>b\">");
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(1);
        assertType("should be Tag",Tag.class,node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals("attribute","a>b",tag.getAttribute("att"));
    }

    public void testTagWithOpenTagSymbolInAttribute() throws ParserException {
        createParser("<tag att=\"a<b\">");
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(1);
        assertType("should be Tag",Tag.class,node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals("attribute","a<b",tag.getAttribute("att"));
    }

    public void testTagWithSingleQuote() throws ParserException {
        String html = "<tag att=\'a<b\'>";
        createParser(html);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(1);
        assertType("should be Tag",Tag.class,node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals("html",html,tag.toHtml());
        assertStringEquals("attribute","a<b",tag.getAttribute("att"));
    }

    /**
     * The following multi line test cases are from
     * bug #725749 Parser does not handle < and > in multi-line attributes
     * submitted by Joe Robins (zorblak)
     */
    public void testMultiLine1 () throws ParserException
    {
        String html = "<meta name=\"foo\" content=\"foo<bar>\">";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html, tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","foo<bar>", attribute2);
    }

    public void testMultiLine2 () throws ParserException
    {
        String html = "<meta name=\"foo\" content=\"foo<bar\">";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html, tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","foo<bar", attribute2);
    }

    public void testMultiLine3 () throws ParserException
    {
        String html = "<meta name=\"foo\" content=\"foobar>\">";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html, tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","foobar>", attribute2);
    }

    public void testMultiLine4 () throws ParserException
    {
        String html = "<meta name=\"foo\" content=\"foo\nbar>\">";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html, tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","foo\nbar>", attribute2);
    }

    /**
     * Test multiline tag like attribute.
     * See feature request #725749 Handle < and > in multi-line attributes.
     */
    public void testMultiLine5 () throws ParserException
    {
        // <meta name="foo" content="<foo>
        // bar">
        String html = "<meta name=\"foo\" content=\"<foo>\nbar\">";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html, tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","<foo>\nbar", attribute2);
    }

    /**
     * Test multiline broken tag like attribute.
     * See feature request #725749 Handle < and > in multi-line attributes.
     */
    public void testMultiLine6 () throws ParserException
    {
        // <meta name="foo" content="foo>
        // bar">
        String html = "<meta name=\"foo\" content=\"foo>\nbar\">";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html, tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","foo>\nbar", attribute2);
    }

    /**
     * Test multiline split tag like attribute.
     * See feature request #725749 Handle < and > in multi-line attributes.
     */
    public void testMultiLine7 () throws ParserException
    {
        // <meta name="foo" content="<foo
        // bar">
        String html = "<meta name=\"foo\" content=\"<foo\nbar\"";
        createParser(html);
        parseAndAssertNodeCount (1);
        assertType ("should be MetaTag", MetaTag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html",html + ">", tag.toHtml ());
        String attribute1 = tag.getAttribute ("NAME");
        assertStringEquals ("attribute 1","foo", attribute1);
        String attribute2 = tag.getAttribute ("CONTENT");
        assertStringEquals ("attribute 2","<foo\nbar", attribute2);
    }

    /**
     * End of multi line test cases.
     */

    /**
     * Test multiple threads running against the parser.
     * See feature request #736144 Handle multi-threaded operation.
     */
    public void testThreadSafety() throws Exception
    {
        createParser("<html></html>");
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        String testHtml1 = "<a HREF=\"/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html\">20020702 Report 1</A>" +
                            TEST_HTML;

        String testHtml2 = "<a href=\"http://normallink.com/sometext.html\">" +
                            TEST_HTML;
        ParsingThread parsingThread [] =
            new ParsingThread[100];
        testProgress = 0;
        for (int i=0;i<parsingThread.length;i++) {
            if (i<parsingThread.length/2)
                parsingThread[i] =
                    new ParsingThread(i,testHtml1,parsingThread.length);
                else
                    parsingThread[i] =
                        new ParsingThread(i,testHtml2,parsingThread.length);

            Thread thread = new Thread(parsingThread[i]);
            thread.start();
        }

        int completionValue = computeCompletionValue(parsingThread.length);

        do {
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
            }
        }
        while (testProgress!=completionValue);
        for (int i=0;i<parsingThread.length;i++)
        {
            if (!parsingThread[i].passed())
            {
                assertNotNull("Thread "+i+" link 1",parsingThread[i].getLink1());
                assertNotNull("Thread "+i+" link 2",parsingThread[i].getLink2());
                if (i<parsingThread.length/2) {
                    assertStringEquals(
                        "Thread "+i+", link 1:",
                        "/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html",
                        parsingThread[i].getLink1().getLink()
                    );
                    assertStringEquals(
                        "Thread "+i+", link 2:",
                        "http://normallink.com/sometext.html",
                        parsingThread[i].getLink2().getLink()
                    );
                } else {
                    assertStringEquals(
                        "Thread "+i+", link 1:",
                        "http://normallink.com/sometext.html",
                        parsingThread[i].getLink1().getLink()
                    );
                    assertNotNull("Thread "+i+" link 2",parsingThread[i].getLink2());
                    assertStringEquals(
                        "Thread "+i+", link 2:",
                        "/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html",
                        parsingThread[i].getLink2().getLink()
                    );
                }
            }
        }
    }

    private int computeCompletionValue(int numThreads) {
        return numThreads * (numThreads - 1) / 2;
    }

    class ParsingThread implements Runnable {
        Parser mParser;
        int mId;
        LinkTag mLink1;
        LinkTag mLink2;
        boolean mResult;
        int mMax;

        ParsingThread(int id, String testHtml, int max) {
            mId = id;
            mMax = max;
            mParser = Parser.createParser(testHtml, null);
        }

        public void run() {
            try {
                mResult = false;
                Node linkTag [] = mParser.extractAllNodesThatAre(LinkTag.class);
                mLink1 = (LinkTag)linkTag[0];
                mLink2 = (LinkTag)linkTag[1];
                if (mId < mMax / 2) {
                    if (mLink1.getLink().equals("/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html") &&
                        mLink2.getLink().equals("http://normallink.com/sometext.html"))
                        mResult = true;
                } else {
                    if (mLink1.getLink().equals("http://normallink.com/sometext.html") &&
                        mLink2.getLink().equals("http://normallink.com/sometext.html"))
                        mResult = true;
                }
            }
            catch (ParserException e) {
                System.err.println("Parser Exception");
                e.printStackTrace();
            }
            finally {
                testProgress += mId;
            }
        }

        public LinkTag getLink1() {
            return (mLink1);
        }

        public LinkTag getLink2() {
            return (mLink2);
        }

        public boolean passed() {
            return (mResult);
        }
    }

    /**
     * Test the toHTML method for a standalone attribute.
     */
    public void testStandAloneToHTML () throws ParserException
    {
        String html = "<input disabled>";
        createParser(html);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount (1);
        assertType ("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html", html, tag.toHtml ());
    }

    /**
     * Test the toHTML method for a missing value attribute.
     */
    public void testMissingValueToHTML () throws ParserException
    {
        String html = "<input disabled=>";
        createParser(html);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount (1);
        assertType ("should be Tag", Tag.class, node[0]);
        Tag tag = (Tag)node[0];
        assertStringEquals ("html", html, tag.toHtml ());
    }
}
