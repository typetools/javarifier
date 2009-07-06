// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/ImageTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:25 $
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
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;

public class ImageTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.ImageTagTest", "ImageTagTest");
    }

    public ImageTagTest(String name) {
        super(name);
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testImageTag() throws ParserException
    {
        createParser("<IMG alt=Google height=115 src=\"goo/title_homepage4.gif\" width=305>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue("Node should be a HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("The image locn","http://www.google.com/test/goo/title_homepage4.gif",imageTag.getImageURL());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testImageTagBug() throws ParserException
    {
        createParser("<IMG alt=Google height=115 src=\"../goo/title_homepage4.gif\" width=305>","http://www.google.com/test/");
        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue("Node should be a HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("The image locn","http://www.google.com/goo/title_homepage4.gif",imageTag.getImageURL());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;BODY aLink=#ff0000 bgColor=#ffffff link=#0000cc onload=setfocus() text=#000000 <BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testImageTageBug2() throws ParserException
    {
        createParser("<IMG alt=Google height=115 src=\"../../goo/title_homepage4.gif\" width=305>","http://www.google.com/test/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an HTMLImageTag
        assertTrue("Node should be a HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("The image locn","http://www.google.com/goo/title_homepage4.gif",imageTag.getImageURL());
    }

    /**
     * This bug occurs when there is a null pointer exception thrown while scanning a tag using LinkScanner.
     * Creation date: (7/1/2001 2:42:13 PM)
     */
    public void testImageTagSingleQuoteBug() throws ParserException
    {
        createParser("<IMG SRC='abcd.jpg'>","http://www.cj.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Image incorrect","http://www.cj.com/abcd.jpg",imageTag.getImageURL());
    }

    /**
     * The bug being reproduced is this : <BR>
     * &lt;A HREF=&gt;Something&lt;A&gt;<BR>
     * vLink=#551a8b&gt;
     * The above line is incorrectly parsed in that, the BODY tag is not identified.
     * Creation date: (6/17/2001 4:01:06 PM)
     */
    public void testNullImageBug() throws ParserException
    {
        createParser("<IMG SRC=>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an HTMLLinkTag
        assertTrue("Node should be a HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertStringEquals("The image location","",imageTag.getImageURL());
    }

    public void testToHTML() throws ParserException {
        String img = "<IMG alt=Google height=115 src=\"../../goo/title_homepage4.gif\" width=305>";
        createParser(img,"http://www.google.com/test/test/index.html");
        parseAndAssertNodeCount(1);
        // The node should be an ImageTag
        assertTrue("Node should be a ImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertStringEquals("toHtml",img,imageTag.toHtml());
        assertEquals("Alt","Google",imageTag.getAttribute("alt"));
        assertEquals("Height","115",imageTag.getAttribute("height"));
        assertEquals("Width","305",imageTag.getAttribute("width"));
    }

    /**
     * See bug #753003 <IMG> within <A> missed when followed by <MAP>
     * Not reproducible.
     */
    public ImageTag extractLinkImage (LinkTag link)
    {
        Node[] list = ParserUtils.findTypeInNode (link, ImageTag.class);
        return (0 == list.length ? null : (ImageTag)list[0]);
    }

    /**
     * See bug #753003 <IMG> within <A> missed when followed by <MAP>
     * Not reproducible.
     */
    public void testMapFollowImg () throws ParserException
    {
        String html = "<a href=\"Biography/Biography.html\" "
            + "onMouseOut=\"MM_swapImgRestore()\" "
            + "onMouseOver=\"MM_swapImage('Image13','','Graphics/SchneiderPic1.gif',1)\">"
            + "<img name=\"Image13\" border=\"0\" src=\"Graphics/SchneiderPic.gif\" "
            + "width=\"127\" height=\"175\" usemap=\"#Image13Map\" "
            + "alt=\"Graphics/SchneiderPic.gif\"> <map name=\"Image13Map\">"
            + "<area shape=\"circle\" coords=\"67,88,66\" href=\"Biography/Biography.html\" "
            + "onClick=\"newWindow('Biography/Biography.html','HTML','menubar=yes,scrollbars=yes,resizable=yes,left=0,top=0'); return false\" target=\"HTML\">"
            + "</map>"
            + "</a>";
        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be a LinkTag", node[0] instanceof LinkTag);
        LinkTag link = (LinkTag)node[0];
        ImageTag img = extractLinkImage (link);
        assertNotNull ("no image tag", img);
    }

    /**
     * Test empty attribute.
     * See bug #755929 Empty string attr. value causes attr parsing to be stopped
     * and bug #753012 IMG SRC not parsed v1.3 & v1.4
     */
    public void testEmptyStringElement () throws ParserException
    {
        String html = "<img height=\"1\" width=\"1\" alt=\"\" "
            + "src=\"http://i.cnn.net/cnn/images/1.gif\"/>";

        createParser (html);
        parseAndAssertNodeCount (1);
        assertTrue ("Node should be an ImageTag", node[0] instanceof ImageTag);
        ImageTag img = (ImageTag)node[0];
        assertTrue ("bad source", "http://i.cnn.net/cnn/images/1.gif".equals (img.getImageURL ()));
    }

    public void testDynamicRelativeImageScan() throws ParserException {
        createParser("<IMG SRC=\"../abc/def/mypic.jpg\">","http://www.yahoo.com/ghi?abcdefg");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Link","http://www.yahoo.com/abc/def/mypic.jpg",imageTag.getImageURL());
    }

    /**
     * This is the reproduction of a bug which causes a null pointer exception
     */
    public void testExtractImageLocnInvertedCommasBug() throws ParserException
    {
        String locn = "http://us.a1.yimg.com/us.yimg.com/i/ww/m5v5.gif";
        createParser ("<img width=638 height=53 border=0 usemap=\"#m\" src=" + locn + " alt=Yahoo>");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Image Locn",locn,imageTag.getImageURL());
    }

    /**
     * This test has been improved to check for params
     * in the image tag, based on requirement by Annette Doyle.
     * Thereby an important bug was detected.
     */
    public void testPlaceHolderImageScan() throws ParserException {
        createParser("<IMG width=1 height=1 alt=\"a\">","http://www.yahoo.com/ghi?abcdefg");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Image Locn","",imageTag.getImageURL());
        assertEquals("Image width","1",imageTag.getAttribute("WIDTH"));
        assertEquals("Image height","1",imageTag.getAttribute("HEIGHT"));
        assertEquals("alt","a",imageTag.getAttribute("ALT"));
    }

    public void testRelativeImageScan() throws ParserException {
        createParser("<IMG SRC=\"mypic.jpg\">","http://www.yahoo.com");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be ImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Link","http://www.yahoo.com/mypic.jpg",imageTag.getImageURL());
    }

    public void testRelativeImageScan2() throws ParserException {
        createParser("<IMG SRC=\"abc/def/mypic.jpg\">","http://www.yahoo.com");     // Register the image scanner
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Link","http://www.yahoo.com/abc/def/mypic.jpg",imageTag.getImageURL());
    }

    public void testRelativeImageScan3() throws ParserException {
        createParser("<IMG SRC=\"../abc/def/mypic.jpg\">","http://www.yahoo.com/ghi");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Link","http://www.yahoo.com/abc/def/mypic.jpg",imageTag.getImageURL());
    }

    /**
     * Test image url which contains spaces in it.
     * This was actually a bug reported by Sam Joseph (sam@neurogrid.net)
     */
    public void testImageWithSpaces() throws ParserException
    {
        createParser("<IMG SRC=\"../abc/def/Hello World.jpg\">","http://www.yahoo.com/ghi");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Link","http://www.yahoo.com/abc/def/Hello World.jpg",imageTag.getImageURL());
    }

    public void testImageWithNewLineChars() throws ParserException
    {
        createParser("<IMG SRC=\"../abc/def/Hello \r\nWorld.jpg\">","http://www.yahoo.com/ghi");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        String exp = new String("http://www.yahoo.com/abc/def/Hello World.jpg");
        //assertEquals("Length of image",exp.length(),imageTag.getImageLocation().length());
        assertStringEquals("Expected Image",exp,imageTag.getImageURL());
    }

    /**
     * Test case to reproduce bug reported by Annette
     */
    public void testImageTagsFromYahoo() throws ParserException
    {
        createParser("<small><a href=s/5926>Air</a>, <a href=s/5927>Hotel</a>, <a href=s/5928>Vacations</a>, <a href=s/5929>Cruises</a></small></td><td align=center><a href=\"http://rd.yahoo.com/M=218794.2020165.3500581.220161/D=yahoo_top/S=2716149:NP/A=1041273/?http://adfarm.mediaplex.com/ad/ck/990-1736-1039-211\" target=\"_top\"><img width=230 height=33 src=\"http://us.a1.yimg.com/us.yimg.com/a/co/columbiahouse/4for49Freesh_230x33_redx2.gif\" alt=\"\" border=0></a></td><td nowrap align=center width=215>Find your match on<br><a href=s/2734><b>Yahoo! Personals</b></a></td></tr><tr><td colspan=3 align=center><input size=30 name=p>\n"+
        "<input type=submit value=Search> <a href=r/so>advanced search</a></td></tr></table><table border=0 cellspacing=0 cellpadding=3 width=640><tr><td nowrap align=center><table border=0 cellspacing=0 cellpadding=0><tr><td><a href=s/5948><img src=\"http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/eet.gif\" width=20 height=20 border=0></a></td><td> &nbsp; &nbsp; <a href=s/1048><b>Yahooligans!</b></a> - <a href=s/5282>Eet & Ern</a>, <a href=s/5283>Games</a>, <a href=s/5284>Science</a>, <a href=s/5285>Sports</a>, <a href=s/5286>Movies</a>, <a href=s/1048>more</a> &nbsp; &nbsp; </td><td><a href=s/5948><img src=\"http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/ern.gif\" width=20 height=20 border=0></a></td></tr></table></td></tr><tr><td nowrap align=center><small><b>Shop</b>&nbsp;\n","http://www.yahoo.com");
        Node [] node = new Node[10];
        parser.setNodeFactory (new PrototypicalNodeFactory (new ImageTag ()));
        int i = 0;
        Node thisNode;
        for (NodeIterator e = parser.elements();e.hasMoreNodes();) {
            thisNode = e.nextNode();
            if (thisNode instanceof ImageTag)
                node[i++] = thisNode;
        }
        assertEquals("Number of nodes identified should be 3",3,i);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Expected Image","http://us.a1.yimg.com/us.yimg.com/a/co/columbiahouse/4for49Freesh_230x33_redx2.gif",imageTag.getImageURL());
        ImageTag imageTag2 = (ImageTag)node[1];
        assertEquals("Expected Image 2","http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/eet.gif",imageTag2.getImageURL());
        ImageTag imageTag3 = (ImageTag)node[2];
        assertEquals("Expected Image 3","http://us.i1.yimg.com/us.yimg.com/i/ligans/klgs/ern.gif",imageTag3.getImageURL());
    }

    /**
     * Test case to reproduce bug reported by Annette
     */
    public void testImageTagsFromYahooWithAllScannersRegistered() throws ParserException
    {
        createParser(
            "<tr>" +
                "<td>" +
                "   <small><a href=s/5926>Air</a>, <a href=s/5927>Hotel</a>, " +
                    "<a href=s/5928>Vacations</a>, <a href=s/5929>Cruises</a></small>" +
                "</td>" +
                "<td align=center>" +
                    "<a href=\"http://rd.yahoo.com/M=218794.2020165.3500581.220161/D=yahoo_top/S=" +
                    "2716149:NP/A=1041273/?http://adfarm.mediaplex.com/ad/ck/990-1736-1039-211\" " +
                    "target=\"_top\"><img width=230 height=33 src=\"http://us.a1.yimg.com/us.yimg.com/a/co/" +
                    "columbiahouse/4for49Freesh_230x33_redx2.gif\" alt=\"\" border=0></a>" +
                "</td>" +
                "<td nowrap align=center width=215>" +
                    "Find your match on<br><a href=s/2734>" +
                    "<b>Yahoo! Personals</b></a>" +
                "</td>" +
            "</tr>" +
            "<tr>" +
                "<td colspan=3 align=center>" +
                    "<input size=30 " +
                    "name=p>\n" +
                "</td>" +
            "</tr>","http://www.yahoo.com",30
        );
        parseAndAssertNodeCount(2);
        assertType("first node type",TableRow.class,node[0]);
        TableRow row = (TableRow)node[0];
        TableColumn col = row.getColumns()[1];
        Node node = col.children().nextNode();
        assertType("Node identified should be HTMLLinkTag",LinkTag.class,node);
        LinkTag linkTag = (LinkTag)node;
        Node nodeInsideLink = linkTag.children().nextNode();
        assertType("Tag within link should be an image tag",ImageTag.class,nodeInsideLink);
        ImageTag imageTag = (ImageTag)nodeInsideLink;
        assertStringEquals(
            "Expected Image",
            "http://us.a1.yimg.com/us.yimg.com/a/co/columbiahouse/4for49Freesh_230x33_redx2.gif",
            imageTag.getImageURL()
        );
    }

    /**
     * This is the reproduction of a bug reported
     * by Annette Doyle
     */
    public void testImageTagOnMultipleLines() throws ParserException {
        createParser(
            "<td rowspan=3>" +
                "<img height=49 \n\n"+
                "alt=\"Central Intelligence Agency, Director of Central Intelligence\" \n\n"+
                "src=\"graphics/images_home2/cia_banners_template3_01.gif\" \n\n"+
                "width=241>" +
            "</td>",
            "http://www.cia.gov"
        );
        parseAndAssertNodeCount(1);
        assertType("node should be", TableColumn.class, node[0]);
        TableColumn col = (TableColumn)node[0];
        Node node = col.children().nextNode();
        assertType("node inside column",ImageTag.class,node);
        ImageTag imageTag = (ImageTag)node;
        // Get the data from the node
        assertEquals("Image location","http://www.cia.gov/graphics/images_home2/cia_banners_template3_01.gif",imageTag.getImageURL());
        assertEquals("Alt Value","Central Intelligence Agency, Director of Central Intelligence",imageTag.getAttribute("ALT"));
        assertEquals("Width","241",imageTag.getAttribute("WIDTH"));
        assertEquals("Height","49",imageTag.getAttribute("HEIGHT"));
    }

    public void testDirectRelativeLinks() throws ParserException {
        createParser("<IMG SRC  = \"/images/lines/li065.jpg\">","http://www.cybergeo.presse.fr/REVGEO/ttsavoir/joly.htm");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertEquals("Image Location","http://www.cybergeo.presse.fr/images/lines/li065.jpg",imageTag.getImageURL());

    }

    /**
     * Based on a page submitted by Claude Duguay, the image tag has IMG SRC"somefile.jpg" - a missing equal
     * to sign
     */
    public void testMissingEqualTo() throws ParserException {
        createParser("<img src\"/images/spacer.gif\" width=\"1\" height=\"1\" alt=\"\">","http://www.htmlparser.org/subdir1/subdir2");
        parseAndAssertNodeCount(1);
        assertTrue("Node identified should be HTMLImageTag",node[0] instanceof ImageTag);
        ImageTag imageTag = (ImageTag)node[0];
        assertStringEquals("Image Location","http://www.htmlparser.org/images/spacer.gif",imageTag.getImageURL());
        assertEquals("Width","1",imageTag.getAttribute("WIDTH"));
        assertEquals("Height","1",imageTag.getAttribute("HEIGHT"));
        assertEquals("Alt","",imageTag.getAttribute("ALT"));
    }
}
