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

package org.htmlparser.tests.scannersTests;

import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.tags.JspTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class JspScannerTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.scannersTests.JspScannerTest", "JspScannerTest");
    }

    private static final boolean JSP_TESTS_ENABLED = false;

    public JspScannerTest(String name) {
        super(name);
    }

    /**
     * In response to bug report 621117, wherein jsp tags
     * are not recognized if they occur within string nodes.
     */
    public void testScan() throws ParserException {
        createParser(
        "<h1>\n"+
        "This is a <%=object%>\n"+
        "</h1>");

        parser.setNodeFactory (new PrototypicalNodeFactory (new JspTag ()));
        parseAndAssertNodeCount(5);
        // The first node should be an JspTag
        assertTrue("Third should be an JspTag",node[2] instanceof JspTag);
        JspTag tag = (JspTag)node[2];
        assertEquals("tag contents","%=object%",tag.getText());
    }

    /**
     * Testcase submitted by Johan Naudts, demonstrating bug
     * 717573, <b>NullPointerException when unclosed HTML tag
     * inside JSP tag</b>
     * @throws ParserException
     */
    public void testUnclosedTagInsideJsp() throws ParserException {
        if (JSP_TESTS_ENABLED)
        {
            createParser(
                "<%\n" +
                "public String getHref(String value) \n" +
                "{ \n" +
                "int indexs = value.indexOf(\"<A HREF=\");\n" +
                "int indexe = value.indexOf(\">\");\n" +
                "if (indexs != -1) {\n" +
                "return value.substring(indexs+9,indexe-2);\n" +
                "}\n" +
                "return value;\n" +
                "}\n" +
                "%>");
            parser.setNodeFactory (new PrototypicalNodeFactory (new JspTag ()));
            parseAndAssertNodeCount(1);
        }
    }
}
