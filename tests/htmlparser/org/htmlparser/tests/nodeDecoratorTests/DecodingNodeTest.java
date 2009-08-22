// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Joshua Kerievsky
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/nodeDecoratorTests/DecodingNodeTest.java,v $
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

public class DecodingNodeTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.nodeDecoratorTests.DecodingNodeTest", "DecodingNodeTest");
    }

    public DecodingNodeTest(String name) {
        super(name);
    }

    private String parseToObtainDecodedResult(String STRING_TO_DECODE)
        throws ParserException {
        StringBuffer decodedContent = new StringBuffer();
        StringNodeFactory stringNodeFactory = new StringNodeFactory();
        stringNodeFactory.setDecode (true);
        createParser(STRING_TO_DECODE);
        parser.setNodeFactory(stringNodeFactory);
        NodeIterator nodes = parser.elements();

        while (nodes.hasMoreNodes())
            decodedContent.append(nodes.nextNode().toPlainTextString());

        return decodedContent.toString();
    }

    public void testAmpersand() throws Exception {
        String ENCODED_WORKSHOP_TITLE =
            "The Testing &amp; Refactoring Workshop";

        String DECODED_WORKSHOP_TITLE =
            "The Testing & Refactoring Workshop";

        assertEquals(
            "ampersand in string",
            DECODED_WORKSHOP_TITLE,
            parseToObtainDecodedResult(ENCODED_WORKSHOP_TITLE));
    }

    public void testNumericReference() throws Exception {
        String ENCODED_DIVISION_SIGN =
            "&#247; is the division sign.";

        String DECODED_DIVISION_SIGN =
            "\u00f7 is the division sign.";

        assertEquals(
            "numeric reference for division sign",
            DECODED_DIVISION_SIGN,
            parseToObtainDecodedResult(ENCODED_DIVISION_SIGN));
    }


    public void testReferencesInString () throws Exception {
        String ENCODED_REFERENCE_IN_STRING =
            "Thus, the character entity reference &divide; is a more convenient" +
            " form than &#247; for obtaining the division sign (\u00f7)";

        String DECODED_REFERENCE_IN_STRING =
            "Thus, the character entity reference \u00f7 is a more convenient" +
            " form than \u00f7 for obtaining the division sign (\u00f7)";

        assertEquals (
            "character references within a string",
            DECODED_REFERENCE_IN_STRING,
            parseToObtainDecodedResult(ENCODED_REFERENCE_IN_STRING));
    }

    public void testBogusCharacterEntityReference() throws Exception {

        String ENCODED_BOGUS_CHARACTER_ENTITY =
            "The character entity reference &divode; is bogus";

        String DECODED_BOGUS_CHARACTER_ENTITY =
            "The character entity reference &divode; is bogus";

        assertEquals (
            "bogus character entity reference",
            DECODED_BOGUS_CHARACTER_ENTITY,
            parseToObtainDecodedResult(ENCODED_BOGUS_CHARACTER_ENTITY));
    }

    public void testDecodingNonBreakingSpaceDoesNotOccur() throws Exception {

        String ENCODED_WITH_NON_BREAKING_SPACE =
            "Here is string with \u00a0.";

        String DECODED_WITH_NON_BREAKING_SPACE =
            "Here is string with \u00a0.";

        assertEquals (
            "bogus character entity reference",
            DECODED_WITH_NON_BREAKING_SPACE,
            parseToObtainDecodedResult(ENCODED_WITH_NON_BREAKING_SPACE));
    }



}
