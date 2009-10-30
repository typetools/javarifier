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

package org.htmlparser.tests.visitorsTests;

import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;
import org.htmlparser.visitors.UrlModifyingVisitor;

public class UrlModifyingVisitorTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.visitorsTests.UrlModifyingVisitorTest", "UrlModifyingVisitorTest");
    }

    private static final String HTML_WITH_LINK =
    "<HTML><BODY>" +
        "<A HREF=\"mylink.html\"><IMG SRC=\"mypic.jpg\">" +
        "</A><IMG SRC=\"my second image.gif\">" +
    "</BODY></HTML>";

    // Note: links are only quoted if needed
    private static final String MODIFIED_HTML =
    "<HTML><BODY>" +
        "<A HREF=\"localhost://mylink.html\">" +
        "<IMG SRC=\"localhost://mypic.jpg\"></A>" +
        "<IMG SRC=\"localhost://my second image.gif\">" +
    "</BODY></HTML>";

    public UrlModifyingVisitorTest(String name) {
        super(name);
    }

    public void testUrlModificationWithVisitor() throws Exception {
        Parser parser = Parser.createParser(HTML_WITH_LINK, null);
        UrlModifyingVisitor visitor =
            new UrlModifyingVisitor("localhost://");
        parser.visitAllNodesWith(visitor);
        String result = visitor.getModifiedResult();
        assertStringEquals("Expected HTML",
            MODIFIED_HTML,
            result);
    }

    /**
     * Test a better method of modifying an HTML page.
     */
    public void testPageModification ()
        throws
            Exception
    {
        Parser parser = Parser.createParser (HTML_WITH_LINK, null);
        NodeList list = parser.parse (null); // no filter
        // make an inner class that does the same thing as the UrlModifyingVisitor
        NodeVisitor visitor = new NodeVisitor ()
        {
            String linkPrefix = "localhost://";
            public void visitTag (Tag tag)
            {
                if (tag instanceof LinkTag)
                    ((LinkTag)tag).setLink(linkPrefix + ((LinkTag)tag).getLink());
                else if (tag instanceof ImageTag)
                    ((ImageTag)tag).setImageURL(linkPrefix + ((ImageTag)tag).getImageURL());
            }
        };
        list.visitAllNodesWith (visitor);
        String result = list.toHtml ();
        assertStringEquals("Expected HTML",
            MODIFIED_HTML,
            result);
    }
}
