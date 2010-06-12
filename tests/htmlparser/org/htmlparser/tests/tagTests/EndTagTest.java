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
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class EndTagTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.EndTagTest", "EndTagTest");
    }

    public EndTagTest(String name) {
        super(name);
    }

    public void testToHTML() throws ParserException {
        createParser("<HTML></HTML>");
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        parseAndAssertNodeCount(2);
        // The node should be a tag
        assertTrue("Node should be a Tag",node[1] instanceof Tag);
        Tag endTag = (Tag)node[1];
        assertTrue("Node should be an end Tag",endTag.isEndTag ());
        assertEquals("Raw String","</HTML>",endTag.toHtml());
    }

    public void testEndTagFind() throws ParserException {
        String testHtml =
            "<SCRIPT>document.write(d+\".com\")</SCRIPT><BR>";
        createParser(testHtml);
        parser.setNodeFactory (new PrototypicalNodeFactory (true));
        int pos = testHtml.indexOf("</SCRIPT>");
        parseAndAssertNodeCount(4);
        assertTrue("Node should be a Tag",node[2] instanceof Tag);
        Tag endTag = (Tag)node[2];
        assertTrue("Node should be an end Tag",endTag.isEndTag ());
        assertEquals("endtag element begin",pos,endTag.getStartPosition ());
        assertEquals("endtag element end",pos+9,endTag.getEndPosition ());
    }
}
