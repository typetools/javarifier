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
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class MetaTagTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.MetaTagTest", "MetaTagTest");
    }

    public MetaTagTest(String name) {
        super(name);
    }

    public void testToHTML() throws ParserException {
        String description = "description";
        String content = "Protecting the internet community through technology, not legislation.  SpamCop eliminates spam.  Automatically file spam reports with the network administrators who can stop spam at the source.  Subscribe, and filter your email through powerful statistical analysis before it reaches your inbox.";
        String tag = "<META name=\"" + description + "\" content=\"" + content + "\">";
        createParser(
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">\n"+
        "<html>\n"+
        "<head><title>SpamCop - Welcome to SpamCop\n"+
        "</title>\n"+
        tag + "\n"+
        "<META name=\"keywords\" content=\"SpamCop spam cop email filter abuse header headers parse parser utility script net net-abuse filter mail program system trace traceroute dns\">\n"+
        "<META name=\"language\" content=\"en\">\n"+
        "<META name=\"owner\" content=\"service@admin.spamcop.net\">\n"+
        "<META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset=ISO-8859-1\">");
        parseAndAssertNodeCount(3);
        assertTrue("Third node should be an HTML node",node[2] instanceof Html);
        Html html = (Html)node[2];
        assertTrue("HTML node should have two children",2 == html.getChildCount ());
        assertTrue("Second node should be an HEAD node",html.getChild(1) instanceof HeadTag);
        HeadTag head = (HeadTag)html.getChild(1);
        assertTrue("HEAD node should have eleven children",11 == head.getChildCount ());
        assertTrue("Third child should be a title tag",head.getChild(2) instanceof MetaTag);
        MetaTag metaTag = (MetaTag)head.getChild(2);
        assertStringEquals("Meta Tag Name",description,metaTag.getMetaTagName());
        assertStringEquals("Meta Tag Contents",content,metaTag.getMetaContent());
        assertStringEquals("toHTML()",tag,metaTag.toHtml());
    }

    public void testScan() throws ParserException {
        String description = "description";
        String content = "Protecting the internet community through technology, not legislation.  SpamCop eliminates spam.  Automatically file spam reports with the network administrators who can stop spam at the source.  Subscribe, and filter your email through powerful statistical analysis before it reaches your inbox.";
        String tag = "<META name=\"" + description + "\" content=\"" + content + "\">";
        createParser(
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\">\n"+
        "<html>\n"+
        "<head><title>SpamCop - Welcome to SpamCop\n"+
        "</title>\n"+
        tag + "\n"+
        "<META name=\"keywords\" content=\"SpamCop spam cop email filter abuse header headers parse parser utility script net net-abuse filter mail program system trace traceroute dns\">\n"+
        "<META name=\"language\" content=\"en\">\n"+
        "<META name=\"owner\" content=\"service@admin.spamcop.net\">\n"+
        "<META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset=ISO-8859-1\">");
        parser.setNodeFactory (new PrototypicalNodeFactory (new MetaTag ()));
        parseAndAssertNodeCount(18);
        assertTrue("Node 8 should be End Tag",node[7] instanceof Tag && ((Tag)node[7]).isEndTag ());
        assertTrue("Node 10 should be META Tag",node[9] instanceof MetaTag);
        MetaTag metaTag;
        metaTag = (MetaTag) node[9];
        assertEquals("Meta Tag 10 Name",description,metaTag.getMetaTagName());
        assertEquals("Meta Tag 10 Contents",content,metaTag.getMetaContent());

        assertTrue("Node 12 should be META Tag",node[11] instanceof MetaTag);
        assertTrue("Node 14 should be META Tag",node[13] instanceof MetaTag);
        assertTrue("Node 16 should be META Tag",node[15] instanceof MetaTag);
        assertTrue("Node 18 should be META Tag",node[17] instanceof MetaTag);

        metaTag = (MetaTag) node[11];
        assertEquals("Meta Tag 12 Name","keywords",metaTag.getMetaTagName());
        assertEquals("Meta Tag 12 Contents","SpamCop spam cop email filter abuse header headers parse parser utility script net net-abuse filter mail program system trace traceroute dns",metaTag.getMetaContent());
        assertNull("Meta Tag 12 Http-Equiv",metaTag.getHttpEquiv());

        metaTag = (MetaTag) node[13];
        assertEquals("Meta Tag 14 Name","language",metaTag.getMetaTagName());
        assertEquals("Meta Tag 14 Contents","en",metaTag.getMetaContent());
        assertNull("Meta Tag 14 Http-Equiv",metaTag.getHttpEquiv());

        metaTag = (MetaTag) node[15];
        assertEquals("Meta Tag 16 Name","owner",metaTag.getMetaTagName());
        assertEquals("Meta Tag 16 Contents","service@admin.spamcop.net",metaTag.getMetaContent());
        assertNull("Meta Tag 16 Http-Equiv",metaTag.getHttpEquiv());

        metaTag = (MetaTag) node[17];
        assertNull("Meta Tag 18 Name",metaTag.getMetaTagName());
        assertEquals("Meta Tag 18 Contents","text/html; charset=ISO-8859-1",metaTag.getMetaContent());
        assertEquals("Meta Tag 18 Http-Equiv","content-type",metaTag.getHttpEquiv());
    }

    public void testScanTagsInMeta() throws ParserException {
        String description = "Description";
        String content = "Ethnoburb </I>versus Chinatown: Two Types of Urban Ethnic Communities in Los Angeles";
        createParser(
        "<META NAME=\"" + description + "\" CONTENT=\"" + content + "\">",
        "http://www.google.com/test/index.html"
        );
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[] {
                    new MetaTag (),
                }));
        parseAndAssertNodeCount(1);
        assertTrue("Node should be meta tag",node[0] instanceof MetaTag);
        MetaTag metaTag = (MetaTag)node[0];
        assertEquals("Meta Tag Name",description,metaTag.getMetaTagName());
        assertEquals("Content",content,metaTag.getMetaContent());
    }

    /**
     * Tried to reproduce bug 707447 but test passes
     * @throws ParserException
     */
    public void testMetaTagBug() throws ParserException {
        String equiv = "content-type";
        String content = "text/html; charset=windows-1252";
        createParser(
            "<html>" +
            "<head>" +
            "<meta http-equiv=\"" + equiv + "\" " +
            "content=\"" + content + "\">" +
            "</head>" +
            "</html>"
        );
        parser.setNodeFactory (new PrototypicalNodeFactory (new MetaTag ()));
        parseAndAssertNodeCount(5);
        assertType("Meta Tag expected", MetaTag.class, node[2]);
        MetaTag metaTag = (MetaTag)node[2];

        assertStringEquals("http-equiv",equiv,metaTag.getHttpEquiv());
        assertStringEquals("content",content,metaTag.getMetaContent());
    }

    /**
     * Bug report 702547 by Joe Robbins being reproduced.
     * @throws ParserException
     */
    public void testMetaTagWithOpenTagSymbol() throws ParserException {
        String content = "a<b";
        createParser(
            "<html>" +
            "<head>" +
            "<title>Parser Test 2</title>" +
            "<meta name=\"foo\" content=\"" + content + "\">" +
            "</head>" +
            "<body>" +
            "<a href=\"http://www.yahoo.com/\">Yahoo!</a><br>" +
            "<a href=\"http://www.excite.com\">Excite</a>" +
            "</body>" +
            "</html>"
        );
        parser.setNodeFactory (new PrototypicalNodeFactory (
            new Tag[] {
                new MetaTag (),
                new TitleTag (),
                new LinkTag (),
            }));
        parseAndAssertNodeCount(11);
        assertType("meta tag",MetaTag.class,node[3]);
        MetaTag metaTag = (MetaTag)node[3];
        assertStringEquals(
            "meta content",
            content,
            metaTag.getMetaContent()
        );
    }
}
