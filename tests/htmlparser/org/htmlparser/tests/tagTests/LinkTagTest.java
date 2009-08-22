// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/LinkTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:27 $
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
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class LinkTagTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.LinkTagTest", "LinkTagTest");
    }

    public LinkTagTest(String name) {
        super(name);
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug()  throws ParserException
    {
        createParser("<A HREF=\"../test.html\">abcd</A>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[0];
        assertEquals("The image locn","http://www.google.com/test.html",linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug2() throws ParserException
    {
        createParser("<A HREF=\"../../test.html\">abcd</A>","http://www.google.com/test/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[0];
        assertEquals("The image locn","http://www.google.com/test.html",linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * When a url ends with a slash, and the link begins with a slash,the parser puts two slashes
     * This bug was submitted by Roget Kjensrud
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug3() throws ParserException
    {
        createParser("<A HREF=\"/mylink.html\">abcd</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[0];
        assertEquals("Link incorrect","http://www.cj.com/mylink.html",linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * Simple url without index.html, doesent get appended to link
     * This bug was submitted by Roget Kjensrud
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkNodeBug4() throws ParserException
    {
        createParser("<A HREF=\"/mylink.html\">abcd</A>","http://www.cj.com");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[0];
        assertEquals("Link incorrect!!","http://www.cj.com/mylink.html",linkNode.getLink());
    }

    public void testLinkNodeBug5() throws ParserException
    {
        String link1 = "http://note.kimo.com.tw/";
        String link2 = "http://photo.kimo.com.tw/";
        String link3 = "http://address.kimo.com.tw/";
        createParser("<a href=" + link1 + ">���O</a>&nbsp; <a \n"+
        "href=" + link2 + ">��ï</a>&nbsp; <a\n"+
        "href=" + link3 + ">�q�T��</a>&nbsp;&nbsp;","http://www.cj.com");
        parseAndAssertNodeCount(6);
        assertTrue("Node should be a LinkTag",node[2] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[2];
        assertStringEquals("Link incorrect!!",link2,linkNode.getLink());
        assertTrue("Node should be a LinkTag",node[4] instanceof LinkTag);
        LinkTag linkNode2 = (LinkTag)node[4];
        assertStringEquals("Link incorrect!!",link3,linkNode2.getLink());
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testLinkNodeBugNullPointerException() throws ParserException
    {
        createParser("<FORM action=http://search.yahoo.com/bin/search name=f><MAP name=m><AREA\n"+
            "coords=0,0,52,52 href=\"http://www.yahoo.com/r/c1\" shape=RECT><AREA"+
            "coords=53,0,121,52 href=\"http://www.yahoo.com/r/p1\" shape=RECT><AREA"+
            "coords=122,0,191,52 href=\"http://www.yahoo.com/r/m1\" shape=RECT><AREA"+
            "coords=441,0,510,52 href=\"http://www.yahoo.com/r/wn\" shape=RECT>","http://www.cj.com/");
        parser.setNodeFactory (new PrototypicalNodeFactory (new LinkTag ()));
        parseAndAssertNodeCount(6);
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testLinkNodeMailtoBug() throws ParserException
    {
        createParser("<A HREF='mailto:somik@yahoo.com'>hello</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[0];
        assertStringEquals("Link incorrect","somik@yahoo.com",linkNode.getLink());
        assertEquals("Link Type",new Boolean(true),new Boolean(linkNode.isMailLink()));
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testLinkNodeSingleQuoteBug() throws ParserException
    {
        createParser("<A HREF='abcd.html'>hello</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkNode = (LinkTag)node[0];
        assertEquals("Link incorrect","http://www.cj.com/abcd.html",linkNode.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkTag() throws ParserException
    {
        createParser("<A HREF=\"test.html\">abcd</A>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag LinkTag = (LinkTag)node[0];
        assertEquals("The image locn","http://www.google.com/test/test.html",LinkTag.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testLinkTagBug() throws ParserException
    {
        createParser("<A HREF=\"../test.html\">abcd</A>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag LinkTag = (LinkTag)node[0];
        assertEquals("The image locn","http://www.google.com/test.html",LinkTag.getLink());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;A HREF=&gt;Something&lt;A&gt;<BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testNullTagBug() throws ParserException
    {
        createParser("<A HREF=>Something</A>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an LinkTag
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("The link location","",linkTag.getLink());
        assertEquals("The link text","Something",linkTag.getLinkText());
    }

    public void testToPlainTextString() throws ParserException {
        createParser("<A HREF='mailto:somik@yahoo.com'>hello</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("Link Plain Text","hello",linkTag.toPlainTextString());
    }

    public void testToHTML() throws ParserException {
        String link1 = "<A HREF='mailto:somik@yahoo.com'>hello</A>";
        String link2 = "<a \n"+
            "href=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\n"+
            "nical.html\"> Journalism 3.0</a>";
        createParser(link1 + "\n"+
            "<LI><font color=\"FF0000\" size=-1><b>Tech Samachar:</b></font>" +
            link2 + " by Rajesh Jain","http://www.cj.com/");
        parser.setNodeFactory (new PrototypicalNodeFactory (new LinkTag ()));
        parseAndAssertNodeCount(10);
        assertTrue("First Node should be a LinkTag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals("Link Raw Text",link1,linkTag.toHtml());
        assertTrue("Ninth Node should be a LinkTag",node[8] instanceof LinkTag);
        linkTag = (LinkTag)node[8];
        assertStringEquals("Link Raw Text",link2,linkTag.toHtml());
    }

    public void testTypeHttps() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='https://www.someurl.com'>Try https.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is a https link",link.isHTTPSLink());
    }

    public void testTypeFtp() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='ftp://www.someurl.com'>Try ftp.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is an ftp link",link.isFTPLink());
    }

    public void testTypeJavaScript() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='javascript://www.someurl.com'>Try javascript.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is a javascript link",link.isJavascriptLink());
    }

    public void testTypeHttpLink() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='http://www.someurl.com'>Try http.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is a http link : "+link.getLink(),link.isHTTPLink());
    }

    public void testRelativeTypeHttpLink() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='somePage.html'>Try relative http.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This relative link is also a http link : "+link.getLink(),link.isHTTPLink());
    }
    
    public void testTypeNonHttp() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='ftp://www.someurl.com'>Try non-http.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is not a http link : "+link.getLink(),!link.isHTTPLink());
    }

    public void testTypeHttpLikeLink() throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='http://'>Try basic http.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is a http link",link.isHTTPLikeLink());
        
        createParser ("<A HREF='https://www.someurl.com'>Try https.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is a https link",link.isHTTPLikeLink());
    }

    /**
     * Test mail link.
     * Bug #738504 MailLink != HTTPLink
     */
    public void testMailToIsNotAHTTPLink () throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='mailto:derrickoswald@users.sourceforge.net'>Derrick</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue ("bug #738504 MailLink != HTTPLink", !link.isHTTPLink ());
        assertTrue ("bug #738504 MailLink != HTTPSLink", !link.isHTTPSLink ());
    }

    /**
     * Bug #784767 irc://server/channel urls are HTTPLike?
     */
    public void testIrcIsNotAHTTPLink () throws ParserException
    {
        LinkTag link;

        createParser ("<A HREF='irc://server/channel'>Try irc.</A>","http://sourceforge.net");
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        link = (LinkTag)node[0];
        assertTrue("This is not a http link", !link.isHTTPLikeLink ());
    }

    public void testAccessKey() throws ParserException {
        createParser("<a href=\"http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph\" accessKey=1>Click Here</A>");
        parseAndAssertNodeCount(1);
        assertTrue("The node should be a link tag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("Link URL of link tag","http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph",linkTag.getLink());
        assertEquals("Link Text of link tag","Click Here",linkTag.getLinkText());
        assertEquals("Access key","1",linkTag.getAccessKey());
    }

    public void testErroneousLinkBug() throws ParserException {
        createParser(
            "Site Comments?<br>" +
                "<a href=\"mailto:sam@neurogrid.com?subject=Site Comments\">" +
                    "Mail Us" +
                "<a>"
        );
        parseAndAssertNodeCount(4);
        // The first node should be a Text
        assertTrue("First node should be a Text",node[0] instanceof Text);
        Text stringNode = (Text)node[0];
        assertEquals("Text of the Text","Site Comments?",stringNode.getText());
        assertTrue("Second node should be a tag",node[1] instanceof Tag);
        assertTrue("Third node should be a link",node[2] instanceof LinkTag);
        // LinkScanner.evaluate() says no HREF means it isn't a link:
        assertTrue("Fourth node should be a tag",node[3] instanceof Tag); 
    }

    /**
     * Test case based on a report by Raghavender Srimantula, of the parser giving out of memory exceptions. Found to occur
     * on the following piece of html
     * <pre>
     * <a href=s/8741><img src="http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif" height=16 width=16 border=0></img></td><td nowrap> &nbsp;
     * <a href=s/7509>
     * </pre>
     */
    public void testErroneousLinkBugFromYahoo2() throws ParserException {
        String link = "<a href=s/8741>" +
                "<img src=\"http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif\" height=16 width=16 border=0>";
        createParser(
            "<td>" +
                link +
            "</td>" +
            "<td nowrap> &nbsp;\n"+
                "<a href=s/7509><b>Yahoo! Movies</b></a>" +
            "</td>","http://www.yahoo.com");
        Node linkNodes [] = parser.extractAllNodesThatAre(LinkTag.class);

        assertEquals("number of links",2,linkNodes.length);
        LinkTag linkTag = (LinkTag)linkNodes[0];
        assertStringEquals("Link","http://www.yahoo.com/s/8741",linkTag.getLink());
        // Verify the link data
        assertStringEquals("Link Text","",linkTag.getLinkText());
        // Verify the reconstruction html
        assertStringEquals("toHTML",link + "</a>",linkTag.toHtml());
    }

    /**
     * Test case based on a report by Raghavender Srimantula, of the parser giving out of memory exceptions. Found to occur
     * on the following piece of html
     * <pre>
     * <a href=s/8741><img src="http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif" height=16 width=16 border=0></img>This is test
     * <a href=s/7509>
     * </pre>
     */
    public void testErroneousLinkBugFromYahoo() throws ParserException {
        String link =
            "<a href=s/8741>" +
                "<img src=\"http://us.i1.yimg.com/us.yimg.com/i/i16/mov_popc.gif\" " +
                     "height=16 " +
                     "width=16 " +
                     "border=0>" +
                "This is a test\n";
        createParser(
                link +
                "<a href=s/7509>" +
                    "<b>Yahoo! Movies</b>" +
                "</a>",
            "http://www.yahoo.com"
        );
        parseAndAssertNodeCount(2);
        assertTrue("First node should be a LinkTag",node[0] instanceof LinkTag);
        assertTrue("Second node should be a LinkTag",node[1] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("Link","http://www.yahoo.com/s/8741",linkTag.getLink());
        // Verify the link data
        assertEquals("Link Text","This is a test\n",linkTag.getLinkText());
        // Verify the reconstruction html
        assertStringEquals("toHTML()",link + "</a>",linkTag.toHtml());
    }

    /**
     * This is the reproduction of a bug which produces multiple text copies.
     */
    public void testExtractLinkInvertedCommasBug2() throws ParserException
    {
        createParser("<a href=\"http://cbc.ca/artsCanada/stories/greatnorth271202\" class=\"lgblacku\">Vancouver schools plan 'Great Northern Way'</a>");
        parseAndAssertNodeCount(1);
        assertTrue("The node should be a link tag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals("Extracted Text","Vancouver schools plan 'Great Northern Way'", linkTag.getLinkText ());
    }

    /**
     * Bug pointed out by Sam Joseph (sam@neurogrid.net)
     * Links with spaces in them will get their spaces absorbed
     */
    public void testLinkSpacesBug() throws ParserException{
        createParser("<a href=\"http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph\">Click Here</A>");
        parseAndAssertNodeCount(1);
        assertTrue("The node should be a link tag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("Link URL of link tag","http://www.kizna.com/servlets/SomeServlet?name=Sam Joseph",linkTag.getLink());
        assertEquals("Link Text of link tag","Click Here",linkTag.getLinkText());
    }

    /**
     * Bug reported by Raj Sharma,5-Apr-2002, upon parsing
     * http://www.samachar.com, the entire page could not be picked up.
     * The problem was occurring after parsing a particular link
     * after which the parsing would not proceed. This link was spread over three lines.
     * The bug has been reproduced and fixed.
     */
    public void testMultipleLineBug() throws ParserException {
        createParser("<LI><font color=\"FF0000\" size=-1><b>Tech Samachar:</b></font><a \n"+
        "href=\"http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/tech\n"+
        "nical.html\"> Journalism 3.0</a> by Rajesh Jain");
        parser.setNodeFactory (new PrototypicalNodeFactory (new LinkTag ()));
        parseAndAssertNodeCount(8);
        assertTrue("Seventh node should be a link tag",node[6] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[6];
        String exp = new String("http://ads.samachar.com/bin/redirect/tech.txt?http://www.samachar.com/technical.html");
        //assertEquals("Length of link tag",exp.length(), linkTag.getLink().length());
        assertStringEquals("Link URL of link tag",exp,linkTag.getLink());
        assertEquals("Link Text of link tag"," Journalism 3.0",linkTag.getLinkText());
        assertTrue("Eight node should be a string node",node[7] instanceof Text);
        Text stringNode = (Text)node[7];
        assertEquals("String node contents"," by Rajesh Jain",stringNode.getText());
    }

    public void testRelativeLinkScan() throws ParserException {
        createParser("<A HREF=\"mytest.html\"> Hello World</A>","http://www.yahoo.com");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLLinkTag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("Expected Link","http://www.yahoo.com/mytest.html",linkTag.getLink());
    }

    public void testRelativeLinkScan2() throws ParserException {
        createParser("<A HREF=\"abc/def/mytest.html\"> Hello World</A>","http://www.yahoo.com");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLLinkTag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals("Expected Link","http://www.yahoo.com/abc/def/mytest.html",linkTag.getLink());
    }

    public void testRelativeLinkScan3() throws ParserException {
        createParser("<A HREF=\"../abc/def/mytest.html\"> Hello World</A>","http://www.yahoo.com/ghi");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLLinkTag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals("Expected Link","http://www.yahoo.com/abc/def/mytest.html",linkTag.getLink());
    }

    /**
     * Test scan with data which is of diff nodes type
     */
    public void testScan() throws ParserException
    {
        createParser("<A HREF=\"mytest.html\"><IMG SRC=\"abcd.jpg\">Hello World</A>","http://www.yahoo.com");
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[] {
                    new LinkTag (),
                    new ImageTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a link node",node[0] instanceof LinkTag);

        LinkTag linkTag = (LinkTag)node[0];
        // Get the link data and cross-check
        Node [] dataNode= new Node[10];
        int i = 0;
        for (SimpleNodeIterator e = linkTag.children();e.hasMoreNodes();)
        {
            dataNode[i++] = e.nextNode();
        }
        assertEquals("Number of data nodes",new Integer(2),new Integer(i));
        assertTrue("First data node should be an Image Node",dataNode[0] instanceof ImageTag);
        assertTrue("Second data node shouls be a String Node",dataNode[1] instanceof Text);

        // Check the contents of each data node
        ImageTag imageTag = (ImageTag)dataNode[0];
        assertEquals("Image URL","http://www.yahoo.com/abcd.jpg",imageTag.getImageURL());
        Text stringNode = (Text)dataNode[1];
        assertEquals("String Contents","Hello World",stringNode.getText());
    }

    /**
     * A bug in the freshmeat page - really bad html
     * tag - &lt;A&gt;Revision&lt;\a&gt;
     * Reported by Mazlan Mat
     * Note: Actually, this is completely legal HTML - Derrick
     */
    public void testFreshMeatBug() throws ParserException
    {
        String html = "<a>Revision</a>";
        createParser(html,"http://www.yahoo.com");
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be a tag",node[0] instanceof Tag);
        Tag tag = (Tag)node[0];
        assertEquals("Tag Contents",html,tag.toHtml());
        assertEquals("Node 0 should have one child", 1, tag.getChildren ().size ());
        assertTrue("The child should be a string node", tag.getChildren ().elementAt (0) instanceof Text);
        Text stringNode = (Text)tag.getChildren ().elementAt (0);
        assertEquals("Text Contents","Revision",stringNode.getText());
    }

    /**
     * Test suggested by Cedric Rosa
     * A really bad link tag sends parser into infinite loop
     */
    public void testBrokenLink() throws ParserException {
        createParser(
            "<a href=\"faq.html\">" +
                "<br>\n"+
                "<img src=\"images/46revues.gif\" " +
                     "width=\"100\" " +
                     "height=\"46\" " +
                     "border=\"0\" " +
                     "alt=\"Rejoignez revues.org!\" " +
                     "align=\"middle\">",
            "http://www.yahoo.com"
        );
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be a link tag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertNotNull(linkTag.toString());
    }

    public void testLinkDataContents() throws ParserException {
        createParser("<a href=\"http://transfer.go.com/cgi/atransfer.pl?goto=http://www.signs.movies.com&name=114332&srvc=nws&context=283&guid=4AD5723D-C802-4310-A388-0B24E1A79689\" target=\"_new\"><img src=\"http://ad.abcnews.com/ad/sponsors/buena_vista_pictures/bvpi-ban0003.gif\" width=468 height=60 border=\"0\" alt=\"See Signs in Theaters 8-2 - Starring Mel Gibson\" align=><font face=\"verdana,arial,helvetica\" SIZE=\"1\"><b></b></font></a>","http://transfer.go.com");
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[] {
                    new LinkTag (),
                    new ImageTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Node 0 should be a link tag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("Link URL","http://transfer.go.com/cgi/atransfer.pl?goto=http://www.signs.movies.com&name=114332&srvc=nws&context=283&guid=4AD5723D-C802-4310-A388-0B24E1A79689",linkTag.getLink());
        assertEquals("Link Text","",linkTag.getLinkText());
        Node [] containedNodes = new Node[10];
        int i=0;
        for (SimpleNodeIterator e = linkTag.children();e.hasMoreNodes();) {
            containedNodes[i++] = e.nextNode();
        }
        assertEquals("There should be 5 contained nodes in the link tag",5,i);
        assertTrue("First contained node should be an image tag",containedNodes[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)containedNodes[0];
        assertEquals("Image Location","http://ad.abcnews.com/ad/sponsors/buena_vista_pictures/bvpi-ban0003.gif",imageTag.getImageURL());
        assertEquals("Image Height","60",imageTag.getAttribute("HEIGHT"));
        assertEquals("Image Width","468",imageTag.getAttribute("WIDTH"));
        assertEquals("Image Border","0",imageTag.getAttribute("BORDER"));
        assertEquals("Image Alt","See Signs in Theaters 8-2 - Starring Mel Gibson",imageTag.getAttribute("ALT"));
        assertTrue("Second contained node should be Tag",containedNodes[1] instanceof Tag);
        Tag tag1 = (Tag)containedNodes[1];
        assertEquals("Tag Contents","font face=\"verdana,arial,helvetica\" SIZE=\"1\"",tag1.getText());
        assertTrue("Third contained node should be Tag",containedNodes[2] instanceof Tag);
        Tag tag2 = (Tag)containedNodes[2];
        assertEquals("Tag Contents","b",tag2.getText());
        assertTrue("Fourth contained node should be a Tag",containedNodes[3] instanceof Tag);
        Tag tag = (Tag)containedNodes[3];
        assertTrue("Fourth contained node should be an EndTag",tag.isEndTag ());
        assertEquals("Fourth Tag contents","/b",tag.getText());
        assertTrue("Fifth contained node should be a Tag",containedNodes[4] instanceof Tag);
        tag = (Tag)containedNodes[4];
        assertTrue("Fifth contained node should be an EndTag",tag.isEndTag ());
        assertEquals("Fifth Tag contents","/font",tag.getText());

    }

    public void testBaseRefLink() throws ParserException {
        createParser("<html>\n"+
            "<head>\n"+
            "<TITLE>test page</TITLE>\n"+
            "<BASE HREF=\"http://www.abc.com/\">\n"+
            "<a href=\"home.cfm\">Home</a>\n"+
            "...\n"+
            "</html>","http://transfer.go.com");
        parseAndAssertNodeCount(1);
        assertTrue("Node 1 should be a HTML tag", node[0] instanceof Html);
        Html html = (Html)node[0];
        assertTrue("Html tag should have 2 children", 2 == html.getChildCount ());
        assertTrue("Html 2nd child should be HEAD tag", html.getChild (1) instanceof HeadTag);
        HeadTag head = (HeadTag)html.getChild (1);
        assertTrue("Head tag should have 7 children", 7 == head.getChildCount ());
        assertTrue("Head 6th child should be a link tag", head.getChild (5) instanceof LinkTag);
        LinkTag linkTag = (LinkTag)head.getChild (5);
        assertEquals("Resolved Link","http://www.abc.com/home.cfm",linkTag.getLink());
        assertEquals("Resolved Link Text","Home",linkTag.getLinkText());
    }

    /**
     * This is a reproduction of bug 617228, reported by
     * Stephen J. Harrington. When faced with a link like :
     * &lt;A
     * HREF="/cgi-bin/view_search?query_text=postdate&gt;20020701&txt_clr=White&bg_clr=Red&url=http://loc
     * al
     * host/Testing/Report
     * 1.html"&gt;20020702 Report 1&lt;/A&gt;
     *
     * parser is unable to handle the link correctly due to the greater than
     * symbol being confused to be the end of the tag.
     */
    public void testQueryLink() throws ParserException {
        createParser("<A \n"+
        "HREF=\"/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html\">20020702 Report 1</A>","http://transfer.go.com");
        parseAndAssertNodeCount(1);
        assertTrue("Node 1 should be a link tag",node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals("Resolved Link","http://transfer.go.com/cgi-bin/view_search?query_text=postdate>20020701&txt_clr=White&bg_clr=Red&url=http://localhost/Testing/Report1.html",linkTag.getLink());
        assertEquals("Resolved Link Text","20020702 Report 1",linkTag.getLinkText());

    }

    public void testNotMailtoLink() throws ParserException {
        createParser("<A HREF=\"mailto.html\">not@for.real</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];

        assertEquals("Link Plain Text", "not@for.real", linkTag.toPlainTextString());
        assertTrue("Link is not a mail link", !linkTag.isMailLink());
    }

    public void testMailtoLink() throws ParserException {
        createParser("<A HREF=\"mailto:this@is.real\">this@is.real</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];
        assertEquals("Link Plain Text", "this@is.real", linkTag.toPlainTextString());
        assertTrue("Link is a mail link", linkTag.isMailLink());
    }

    public void testJavascriptLink() throws ParserException {
        createParser("<A HREF=\"javascript:alert('hello');\">say hello</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];

        assertEquals("Link Plain Text", "say hello", linkTag.toPlainTextString());
        assertTrue("Link is a Javascript command", linkTag.isJavascriptLink());
    }

    public void testNotJavascriptLink() throws ParserException {
        createParser("<A HREF=\"javascript_not.html\">say hello</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];

        assertEquals("Link Plain Text", "say hello", linkTag.toPlainTextString());
        assertTrue("Link is not a Javascript command", !linkTag.isJavascriptLink());
    }

    public void testFTPLink() throws ParserException {
        createParser("<A HREF=\"ftp://some.where.it\">my ftp</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];

        assertEquals("Link Plain Text", "my ftp", linkTag.toPlainTextString());
        assertTrue("Link is a FTP site", linkTag.isFTPLink());
    }

    public void testNotFTPLink() throws ParserException {
        createParser("<A HREF=\"ftp.html\">my ftp</A>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];

        assertEquals("Link Plain Text", "my ftp", linkTag.toPlainTextString());
        assertTrue("Link is not a FTP site", !linkTag.isFTPLink());
    }

    public void testRelativeLinkNotHTMLBug() throws ParserException {
        createParser("<A HREF=\"newpage.html\">New Page</A>","http://www.mysite.com/books/some.asp");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];
        assertEquals("Link","http://www.mysite.com/books/newpage.html",linkTag.getLink());
    }

    public void testBadImageInLinkBug() throws ParserException {
        createParser("<a href=\"registration.asp?EventID=1272\"><img border=\"0\" src=\"\\images\\register.gif\"</a>","http://www.fedpage.com/Event.asp?EventID=1272");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLLinkTag", node[0] instanceof LinkTag);
        LinkTag linkTag = (LinkTag) node[0];
        // Get the image tag from the link

        Node insideNodes [] = new Node[10];
        int j =0 ;
        for (SimpleNodeIterator e = linkTag.children();e.hasMoreNodes();) {
            insideNodes[j++]= e.nextNode();
        }
        assertEquals("Number of contained internal nodes",1,j);
        assertTrue(insideNodes[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)insideNodes[0];
        assertEquals("Image Tag Location","http://www.fedpage.com/images\\register.gif",imageTag.getImageURL());
    }

    /**
     * This is an attempt to reproduce bug 677874
     * reported by James Moliere. A link tag of the form
     * <code>
     * <a class=rlbA href=/news/866201.asp?0sl=-
     * 32>Shoe bomber handed life sentence</a>
     * </code>
     * is not parsed correctly. The second '=' sign in the link causes
     * the parser to treat it as a seperate attribute
     */
    public void testLinkContainsEqualTo() throws Exception {
        createParser(
            "<a class=rlbA href=/news/866201.asp?0sl=-" +
            "32>Shoe bomber handed life sentence</a>"
        );
        parseAndAssertNodeCount(1);
        assertType("node type",LinkTag.class,node[0]);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals(
            "link text",
            "Shoe bomber handed life sentence",
            linkTag.getLinkText()
        );
        assertStringEquals(
            "link url",
            "/news/866201.asp?0sl=-32",
            linkTag.getLink()
        );
    }

    /**
     * Bug report by Cory Seefurth
     * @throws Exception
     */
    public void _testLinkWithJSP() throws Exception {
        createParser(
            "<a href=\"<%=Application(\"sURL\")% " +
            ">/literature/index.htm\">Literature</a>"
        );
        parseAndAssertNodeCount(1);
        assertType("should be link tag",LinkTag.class,node[0]);
        LinkTag linkTag = (LinkTag)node[0];
        assertStringEquals("expected link","<%=Application(\"sURL\")%>/literature/index.htm",linkTag.getLink());
    }

    public void testTagSymbolsInLinkText() throws Exception {
        createParser(
            "<a href=\"/cataclysm/Langy-AnEmpireReborn-Ch2.shtml#story\"" +
            "><< An Empire Reborn: Chapter 2 <<</a>"
        );
        parseAndAssertNodeCount(1);
        assertType("node",LinkTag.class, node[0]);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals("link text","<< An Empire Reborn: Chapter 2 <<",linkTag.getLinkText());
    }

    /**
     * See bug #813838 links not parsed correctly
     */
    public void testPlainText() throws Exception
    {
        String html = "<a href=Cities/><b>Cities</b></a>";
        createParser (html);
        parseAndAssertNodeCount (1);
        assertType("node", LinkTag.class, node[0]);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals ("plain text", "Cities", linkTag.toPlainTextString ());
    }

    /**
     * See bug #982175 False Positives on &reg; entity
     */
    public void testCharacterReferenceInLink() throws Exception
    {
        String html = "<a href=\"http://www.someplace.com/somepage.html?&region=us\">Search By Region</a>" +
	        "<a href=\"http://www.someplace.com/somepage.html?&region=&destination=184\">Search by Destination</a>";
        createParser (html);
        parseAndAssertNodeCount (2);
        assertType("node", LinkTag.class, node[0]);
        LinkTag linkTag = (LinkTag)node[0];
        assertEquals ("link", "http://www.someplace.com/somepage.html?&region=us", linkTag.getLink());
        assertType("node", LinkTag.class, node[1]);
        linkTag = (LinkTag)node[1];
        assertEquals ("link", "http://www.someplace.com/somepage.html?&region=&destination=184", linkTag.getLink());
    }
    
}
