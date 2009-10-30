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

import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.visitors.StringFindingVisitor;

public class StringFindingVisitorTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.visitorsTests.StringFindingVisitorTest", "StringFindingVisitorTest");
    }

    private static final String HTML =
        "<HTML><HEAD><TITLE>This is the Title</TITLE>" +
        "</HEAD><BODY>Hello World, this is an excellent parser</BODY></HTML>";

    private static final String HTML_TO_SEARCH =
        "<HTML><HEAD><TITLE>test</TITLE></HEAD>\n"+
        "<BODY><H1>This is a test page</H1>\n"+
        "Writing tests is good for code. Testing is a good\n"+
        "philosophy. Test driven development is even better.\n";

    public StringFindingVisitorTest(String name) {
        super(name);
    }

    public void testSimpleStringFind() throws Exception {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("Hello");
        parser.visitAllNodesWith(visitor);
        assertTrue("Hello found", visitor.stringWasFound());
    }

    public void testStringNotFound() throws Exception {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("industrial logic");
        parser.visitAllNodesWith(visitor);
        assertTrue("industrial logic should not have been found", !visitor.stringWasFound());
    }

    public void testStringInTagNotFound() throws Exception {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("HTML");
        parser.visitAllNodesWith(visitor);
        assertTrue("HTML should not have been found", !visitor.stringWasFound());
    }

    public void testStringFoundInSingleStringNode() throws Exception {
        createParser("this is some text!");
        StringFindingVisitor visitor = new StringFindingVisitor("text");
        parser.visitAllNodesWith(visitor);
        assertTrue("text should be found", visitor.stringWasFound());
    }

    public void testStringFoundCount() throws Exception {
        createParser(HTML);
        StringFindingVisitor visitor = new StringFindingVisitor("is");
        parser.visitAllNodesWith(visitor);
        assertEquals("# times 'is' was found", 2, visitor.stringFoundCount());

        visitor = new StringFindingVisitor("and");
        parser.visitAllNodesWith(visitor);
        assertEquals("# times 'and' was found", 0, visitor.stringFoundCount());
    }

    public void testStringFoundMultipleTimes() throws Exception {
        createParser(HTML_TO_SEARCH);
        StringFindingVisitor visitor = new StringFindingVisitor("TEST");
        visitor.doMultipleSearchesWithinStrings();
        parser.visitAllNodesWith(visitor);
        assertEquals("TEST found", 5, visitor.stringFoundCount());
    }



}
