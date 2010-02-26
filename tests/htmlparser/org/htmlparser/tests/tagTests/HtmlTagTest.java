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

package org.htmlparser.tests.tagTests;

import org.htmlparser.Node;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;

public class HtmlTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.HtmlTagTest", "HtmlTagTest");
    }

    public HtmlTagTest (String name)
    {
        super(name);
    }
    
    public void testScan() throws Exception {
        createParser(
            "<html>" +
            "   <head>" +
            "       <title>Some Title</title>" +
            "   </head>" +
            "   <body>" +
            "       Some data" +
            "   </body>" +
            "</html>");
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new TitleTag (),
                    new Html (),
                }));
        parseAndAssertNodeCount(1);
        assertType("html tag",Html.class,node[0]);
        Html html = (Html)node[0];
        NodeList nodeList = new NodeList();
        NodeClassFilter filter = new NodeClassFilter (TitleTag.class);
        html.collectInto(nodeList, filter);
        assertEquals("nodelist size",1,nodeList.size());
        Node node = nodeList.elementAt(0);
        assertType("expected title tag",TitleTag.class,node);
        TitleTag titleTag = (TitleTag)node;
        assertStringEquals("title","Some Title",titleTag.getTitle());
    }
}
