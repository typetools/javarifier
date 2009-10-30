// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Marc Novakowski
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

package org.htmlparser.tests;

import java.util.Arrays;

import junit.framework.TestSuite;

import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.tests.scannersTests.CompositeTagScannerTest.CustomTag;
import org.htmlparser.util.ParserException;

/**
 * @author Somik Raha
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LineNumberAssignedByNodeReaderTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.LineNumberAssignedByNodeReaderTest", "LineNumberAssignedByNodeReaderTest");
    }

    public LineNumberAssignedByNodeReaderTest(String name) {
        super(name);
    }

    /**
     * Test to ensure that the <code>Tag</code> being created by the
     * <code>CompositeTagScanner</code> has the correct startLine and endLine
     * information in the <code>TagData</code> it is constructed with.
     * @throws ParserException if there is a problem parsing the test data
     */
    public void testLineNumbers1() throws ParserException
    {
        testLineNumber("<Custom/>", 1, 0, 0, 0);
    }

    public void testLineNumbers2() throws ParserException
    {
        testLineNumber("<Custom />", 1, 0, 0, 0);
    }

    public void testLineNumbers3() throws ParserException
    {
        testLineNumber("<Custom></Custom>", 1, 0, 0, 0);
    }

    public void testLineNumbers4() throws ParserException
    {
        testLineNumber("<Custom>Content</Custom>", 1, 0, 0, 0);
    }

    public void testLineNumbers5() throws ParserException
    {
        testLineNumber("<Custom>Content<Custom></Custom>", 1, 0, 0, 0);
    }

    public void testLineNumbers6() throws ParserException
    {
        testLineNumber(
            "<Custom>\n" +
            "   Content\n" +
            "</Custom>",
            1, 0, 0, 2
        );
    }

    public void testLineNumbers7() throws ParserException
    {
        testLineNumber(
            "Foo\n" +
            "<Custom>\n" +
            "   Content\n" +
            "</Custom>",
            2, 1, 1, 3
        );
    }

    public void testLineNumbers8() throws ParserException
    {
        testLineNumber(
            "Foo\n" +
            "<Custom>\n" +
            "   <Custom>SubContent</Custom>\n" +
            "</Custom>",
            2, 1, 1, 3
        );
    }

    public void testLineNumbers9() throws ParserException
    {
        char[] oneHundredNewLines = new char[100];
        Arrays.fill(oneHundredNewLines, '\n');
        testLineNumber(
            "Foo\n" +
            new String(oneHundredNewLines) +
            "<Custom>\n" +
            "   <Custom>SubContent</Custom>\n" +
            "</Custom>",
            2, 1, 101, 103
        );
    }

    /**
     * Helper method to ensure that the <code>Tag</code> being created by the
     * <code>CompositeTagScanner</code> has the correct startLine and endLine
     * information in the <code>TagData</code> it is constructed with.
     * @param xml String containing HTML or XML to parse, containing a Custom tag
     * @param numNodes int number of expected nodes returned by parser
     * @param useNode int index of the node to test (should be of type CustomTag)
     * @param startLine int the expected start line number of the tag
     * @param endLine int the expected end line number of the tag
     * @throws ParserException if there is an exception during parsing
     */
    private void testLineNumber(String xml, int numNodes, int useNode, int expectedStartLine, int expectedEndLine) throws ParserException {
        createParser(xml);
        parser.setNodeFactory (new PrototypicalNodeFactory (new CustomTag ()));
        parseAndAssertNodeCount(numNodes);
        assertType("custom node",CustomTag.class,node[useNode]);
        CustomTag tag = (CustomTag)node[useNode];
        assertEquals("start line", expectedStartLine, tag.getStartingLineNumber ());
        assertEquals("end line", expectedEndLine, tag.getEndTag ().getEndingLineNumber ());
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Line Number Tests");
        suite.addTestSuite(LineNumberAssignedByNodeReaderTest.class);
        return (suite);
    }
}
