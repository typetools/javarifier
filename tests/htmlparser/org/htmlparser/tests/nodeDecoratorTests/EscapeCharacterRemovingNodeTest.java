// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Joshua Kerievsky
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

package org.htmlparser.tests.nodeDecoratorTests;

import org.htmlparser.StringNodeFactory;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

public class EscapeCharacterRemovingNodeTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.nodeDecoratorTests.EscapeCharacterRemovingNodeTest", "EscapeCharacterRemovingNodeTest");
    }

    public EscapeCharacterRemovingNodeTest(String name) {
        super(name);
    }

    private String parseToObtainDecodedResult(String STRING_TO_DECODE)
        throws ParserException {
        StringBuffer decodedContent = new StringBuffer();

        StringNodeFactory stringNodeFactory = new StringNodeFactory();
        stringNodeFactory.setRemoveEscapes (true);
        createParser(STRING_TO_DECODE);
        parser.setNodeFactory(stringNodeFactory);

        NodeIterator nodes = parser.elements();

        while (nodes.hasMoreNodes())
            decodedContent.append(nodes.nextNode().toPlainTextString());

        return decodedContent.toString();
    }

    public void testTab() throws Exception {
        String ENCODED_WORKSHOP_TITLE =
            "The Testing & Refactoring Workshop\tCreated by Industrial Logic, Inc.";

        String DECODED_WORKSHOP_TITLE =
            "The Testing & Refactoring WorkshopCreated by Industrial Logic, Inc.";

        assertEquals(
            "tab in string",
            DECODED_WORKSHOP_TITLE,
            parseToObtainDecodedResult(ENCODED_WORKSHOP_TITLE));
    }

    public void testCarriageReturn() throws Exception {
        String ENCODED_WORKSHOP_TITLE =
            "The Testing & Refactoring Workshop\nCreated by Industrial Logic, Inc.\n";

        String DECODED_WORKSHOP_TITLE =
            "The Testing & Refactoring WorkshopCreated by Industrial Logic, Inc.";

        assertEquals(
            "tab in string",
            DECODED_WORKSHOP_TITLE,
            parseToObtainDecodedResult(ENCODED_WORKSHOP_TITLE));
    }

    public void testWithDecodingNodeDecorator() throws Exception {
        String ENCODED_WORKSHOP_TITLE =
            "The Testing &amp; Refactoring Workshop\nCreated by Industrial Logic, Inc.\n";

        String DECODED_WORKSHOP_TITLE =
            "The Testing & Refactoring WorkshopCreated by Industrial Logic, Inc.";

        StringBuffer decodedContent = new StringBuffer();

        StringNodeFactory stringNodeFactory = new StringNodeFactory();
        stringNodeFactory.setDecode (true);
        stringNodeFactory.setRemoveEscapes (true);

        createParser(ENCODED_WORKSHOP_TITLE);
        parser.setNodeFactory(stringNodeFactory);
        NodeIterator nodes = parser.elements();

        while (nodes.hasMoreNodes())
            decodedContent.append(nodes.nextNode().toPlainTextString());

        assertEquals(
            "tab in string",
            DECODED_WORKSHOP_TITLE,
            decodedContent.toString());

    }
}
