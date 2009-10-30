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

import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class HeadTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.HeadTagTest", "HeadTagTest");
    }

    public HeadTagTest (String name)
    {
        super(name);
    }
    
    public void testSimpleHead() throws ParserException {
        createParser("<HTML><HEAD></HEAD></HTML>");
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof Html);
        Html htmlTag = (Html)node[0];
        assertTrue(htmlTag.getChild(0) instanceof HeadTag);
    }

    public void testSimpleHeadWithoutEndTag() throws ParserException {
        createParser("<HTML><HEAD></HTML>");
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof Html);
        Html htmlTag = (Html)node[0];
        assertTrue(htmlTag.getChild(0) instanceof HeadTag);
        HeadTag headTag = (HeadTag)htmlTag.getChild(0);
        assertEquals("toHtml()","<HEAD></HEAD>",headTag.toHtml());
        assertEquals("toHtml()","<HTML><HEAD></HEAD></HTML>",htmlTag.toHtml());
    }

    public void testSimpleHeadWithBody() throws ParserException {
        createParser("<HTML><HEAD><BODY></HTML>");
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof Html);
        Html htmlTag = (Html)node[0];
        assertTrue(htmlTag.getChild(0) instanceof HeadTag);
        //assertTrue(htmlTag.getChild(1) instanceof BodyTag);
        HeadTag headTag = (HeadTag)htmlTag.getChild(0);
        assertEquals("toHtml()","<HEAD></HEAD>",headTag.toHtml());
        assertEquals("toHtml()","<HTML><HEAD></HEAD><BODY></BODY></HTML>",htmlTag.toHtml());
    }
}
