// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Joshua Kerievsky
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/nodeDecoratorTests/NonBreakingSpaceConvertingNodeTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:18 $
// $Revision: 1.1 $
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

public class NonBreakingSpaceConvertingNodeTest extends ParserTestCase {
    static
    {
        System.setProperty ("org.htmlparser.tests.nodeDecoratorTests.NonBreakingSpaceConvertingNodeTest", "NonBreakingSpaceConvertingNodeTest");
    }

    public NonBreakingSpaceConvertingNodeTest(String name) {
        super(name);
    }

    private String parseToObtainDecodedResult(String STRING_TO_DECODE)
        throws ParserException {
        StringBuffer decodedContent = new StringBuffer();

        StringNodeFactory stringNodeFactory = new StringNodeFactory();
        stringNodeFactory.setConvertNonBreakingSpaces (true);
        createParser(STRING_TO_DECODE);
        parser.setNodeFactory(stringNodeFactory);

        NodeIterator nodes = parser.elements();

        while (nodes.hasMoreNodes())
            decodedContent.append(nodes.nextNode().toPlainTextString());

        return decodedContent.toString();
    }

    public void testOneNonBreakingSpace() throws Exception {
        String ENCODED_WITH_NON_BREAKING_SPACE =
            "Here is string with \u00a0 inside of it.";

        String DECODED_WITH_NON_BREAKING_SPACE =
            "Here is string with   inside of it.";

        assertEquals (
            "\u00a0 was converted to a space correctly",
            DECODED_WITH_NON_BREAKING_SPACE,
            parseToObtainDecodedResult(ENCODED_WITH_NON_BREAKING_SPACE));
    }

    public void testMultipleNonBreakingSpace() throws Exception {
        String ENCODED_WITH_NON_BREAKING_SPACE =
            "\u00a0Here is string with \u00a0 inside of it\u00a0.";

        String DECODED_WITH_NON_BREAKING_SPACE =
            " Here is string with   inside of it .";

        assertEquals (
            "\u00a0 was converted to a space correctly",
            DECODED_WITH_NON_BREAKING_SPACE,
            parseToObtainDecodedResult(ENCODED_WITH_NON_BREAKING_SPACE));
    }

}
