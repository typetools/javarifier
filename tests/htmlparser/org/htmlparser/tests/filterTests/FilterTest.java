// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
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

package org.htmlparser.tests.filterTests;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.CssSelectorNodeFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.RegexFilter;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.Text;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * Test the operation of filters.
 */
public class FilterTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.filterTests.FilterTest", "FilterTest");
    }

    public FilterTest (String name)
    {
        super (name);
    }

    /**
     * Test node class filtering.
     */
    public void testNodeClass () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the time for all good men..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (new NodeClassFilter (BodyTag.class));
        assertEquals ("only one element", 1, list.size ());
        assertType ("should be BodyTag", BodyTag.class, list.elementAt (0));
        BodyTag body = (BodyTag)list.elementAt (0);
        assertEquals ("only one child", 1, body.getChildCount ());
        assertSuperType ("should be Text", Text.class, body.getChildren ().elementAt (0));
        assertStringEquals("html", guts, body.toHtml ());
    }


    /**
     * Test tag name filtering.
     */
    public void testTagName () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<booty>Now is the time for all good men..</booty>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (new TagNameFilter ("booty"));
        assertEquals ("only one element", 1, list.size ());
        assertSuperType ("should be Tag", Tag.class, list.elementAt (0));
        assertStringEquals("name", "BOOTY", ((Tag)(list.elementAt (0))).getTagName ());
    }

    /**
     * Test string filtering.
     */
    public void testString () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the <a id=target><b>time</b></a> for all good <time>men</time>..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (new StringFilter ("Time"));
        assertEquals ("only one element", 1, list.size ());
        assertSuperType ("should be String", Text.class, list.elementAt (0));
        assertStringEquals("name", "time", ((Text)list.elementAt (0)).getText ());
        // test case sensitivity
        list = parser.extractAllNodesThatMatch (new StringFilter ("Time", true));
        assertEquals ("should be no elements", 0, list.size ());
    }

    /**
     * Test child filtering.
     */
    public void testChild () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the <a id=target><b>time</b></a> for all good <a href=http://bongo.com>men</a>..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (new HasChildFilter (new TagNameFilter ("b")));
        assertEquals ("only one element", 1, list.size ());
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (0));
        LinkTag link = (LinkTag)list.elementAt (0);
        assertEquals ("three children", 3, link.getChildCount ());
        assertSuperType ("should be TagNode", Tag.class, link.getChildren ().elementAt (0));
        Tag tag = (Tag)link.getChildren ().elementAt (0);
        assertStringEquals("name", "B", tag.getTagName ());
    }

    /**
     * Test attribute filtering.
     */
    public void testAttribute () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the <a id=target><b>time</b></a> for all good <a href=http://bongo.com>men</a>..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (new HasAttributeFilter ("id"));
        assertEquals ("only one element", 1, list.size ());
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (0));
        LinkTag link = (LinkTag)list.elementAt (0);
        assertEquals ("attribute value", "target", link.getAttribute ("id"));
    }

    /**
     * Test and filtering.
     */
    public void testAnd () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the <a id=one><b>time</b></a> for all good <a id=two><b>men</b></a>..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (
            new AndFilter (
                new HasChildFilter (
                    new TagNameFilter ("b")),
                new HasChildFilter (
                    new StringFilter ("men")))
                );
        assertEquals ("only one element", 1, list.size ());
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (0));
        LinkTag link = (LinkTag)list.elementAt (0);
        assertEquals ("attribute value", "two", link.getAttribute ("id"));
    }

    /**
     * Test or filtering.
     */
    public void testOr () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the <a id=one><b>time</b></a> for <a id=two><b>all</b></a> good <a id=three><b>men</b></a>..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (
            new OrFilter (
                new HasChildFilter (
                    new StringFilter ("time")),
                new HasChildFilter (
                    new StringFilter ("men")))
                );
        assertEquals ("two elements", 2, list.size ());
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (0));
        LinkTag link = (LinkTag)list.elementAt (0);
        assertEquals ("attribute value", "one", link.getAttribute ("id"));
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (1));
        link = (LinkTag)list.elementAt (1);
        assertEquals ("attribute value", "three", link.getAttribute ("id"));
    }

    /**
     * Test not filtering.
     */
    public void testNot () throws ParserException
    {
        String guts;
        String html;
        NodeList list;

        guts = "<body>Now is the <a id=one><b>time</b></a> for <a id=two><b>all</b></a> good <a id=three><b>men</b></a>..</body>";
        html = "<html>" + guts + "</html>";
        createParser (html);
        list = parser.extractAllNodesThatMatch (
            new AndFilter (
                new HasChildFilter (
                    new TagNameFilter ("b")),
                new NotFilter (
                    new HasChildFilter (
                        new StringFilter ("all"))))
                );
        assertEquals ("two elements", 2, list.size ());
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (0));
        LinkTag link = (LinkTag)list.elementAt (0);
        assertEquals ("attribute value", "one", link.getAttribute ("id"));
        assertType ("should be LinkTag", LinkTag.class, list.elementAt (1));
        link = (LinkTag)list.elementAt (1);
        assertEquals ("attribute value", "three", link.getAttribute ("id"));
    }

    public void testEscape() throws Exception
    {
        assertEquals ("douchebag", CssSelectorNodeFilter.unescape ("doucheba\\g").toString ());
    }

    public void testSelectors() throws Exception
    {
        String html = "<html><head><title>sample title</title></head><body inserterr=\"true\" yomama=\"false\"><h3 id=\"heading\">big </invalid>heading</h3><ul id=\"things\"><li><br word=\"broken\"/>&gt;moocow<li><applet/>doohickey<li class=\"last\"><b class=\"item\">final<br>item</b></ul></body></html>";
        Lexer l;
        Parser p;
        CssSelectorNodeFilter it;
        NodeIterator i;
        int count;

        l = new Lexer (html);
        p = new Parser (l);
        it = new CssSelectorNodeFilter ("li + li");
        count = 0;
        for (i = p.extractAllNodesThatMatch (it).elements (); i.hasMoreNodes ();)
        {
            assertEquals ("tag name wrong", "LI", ((Tag)i.nextNode()).getTagName());
            count++;
        }
        assertEquals ("wrong count", 2, count);
    }

    /**
     * Test regular expression matching:
     */
    public void testRegularExpression () throws Exception
    {
        String target =
              "\n"
            + "\n"
            + "Most recently, in the Western Conference final, the Flames knocked off \n"
            + "the San Jose Sharks, the Pacific Division champions, to become the first \n"
            + "Canadian team to reach the Stanley Cup Championship series since 1994.";
            
        String html =
              "<html><head><title>CBC Sports Online: NHL Playoffs</title></head>"
            + "<body><h1>CBC SPORTS ONLINE</h1>\n"
            + "The Calgary Flames have already defeated three NHL division winners \n"
            + "during their improbable playoff run. If they are to hoist the Stanley \n"
            + "Cup they'll have to go through one more. <p><table ALIGN=\"Right\" width=196 CELLPADDING=0 cellspacing=0 hspace=4> <tr><td><img src=\"/gfx/topstory/sports/iginla_j0524.jpg\" width=194 height=194 hspace=3 border=1><br>\n"
            + "\n"
            + "<font SIZE=\"1\" FACE=\"verdana,arial\">\n"
            + "Jarome Iginla skates during the Flames' practice on Monday. Calgary takes on the Tampa Bay Lightning in the Stanley Cup finals beginning Tuesday night in Tampa\n"
            + "</font></td></tr></table>\n"
            + "\n"
            + "\n"
            + "In the post-season's first round, the Flames defeated the Vancouver \n"
            + "Canucks, the Northwest Division winners, in seven tough games. <p>\n"
            + "\n"
            + "In Round 2 it was the Detroit Red Wings, who not only won the Central \n"
            + "Division, but also boasted the NHL's best overall record during the \n"
            + "regular season, who fell to the Flames. <p>"
            + target
            + "<p>\n"
            + "\n"
            + "Up next for the Flames is the Tampa Bay Lighting -- the runaway winners \n"
            + "of the NHL's Southeast Division and the Eastern Conference's best team \n"
            + "during the regular season. <p>\n"
            + "\n"
            + "The Lighting advanced by beating the Philadelphia Flyers in the Eastern \n"
            + "Conference final. <p>\n"
            + "</body></html>\n";
        Lexer lexer;
        Parser parser;
        RegexFilter filter;
        NodeIterator iterator;
        int count;

        lexer = new Lexer (html);
        parser = new Parser (lexer);
        filter = new RegexFilter ("(19|20)\\d\\d([- \\\\/.](0[1-9]|1[012])[- \\\\/.](0[1-9]|[12][0-9]|3[01]))?");
        count = 0;
        for (iterator = parser.extractAllNodesThatMatch (filter).elements (); iterator.hasMoreNodes ();)
        {
            assertEquals ("text wrong", target, iterator.nextNode ().toHtml ());
            count++;
        }
        assertEquals ("wrong count", 1, count);
    }
}

