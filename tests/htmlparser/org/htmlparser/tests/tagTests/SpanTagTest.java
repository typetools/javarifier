// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/SpanTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:28 $
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

package org.htmlparser.tests.tagTests;

import org.htmlparser.Node;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.tags.Span;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tests.ParserTestCase;

public class SpanTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.SpanTagTest", "SpanTagTest");
    }

    private static final String HTML_WITH_SPAN =
        "<TD BORDER=\"0.0\" VALIGN=\"Top\" COLSPAN=\"4\" WIDTH=\"33.33%\">" +
        "   <DIV>" +
        "       <SPAN>Flavor: small(90 to 120 minutes)<BR /></SPAN>" +
        "       <SPAN>The short version of our Refactoring Challenge gives participants a general feel for the smells in the code base and includes time for participants to find and implement important refactorings.&#013;<BR /></SPAN>" +
        "   </DIV>" +
        "</TD>";

    public SpanTagTest (String name)
    {
        super(name);
    }
    
    public void testScan() throws Exception {
        createParser(
            HTML_WITH_SPAN
        );
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[] {
                    new TableColumn (),
                    new Span (),
                }));
        parseAndAssertNodeCount(1);
        assertType("node",TableColumn.class,node[0]);
        TableColumn col = (TableColumn)node[0];
        Node spans [] = col.searchFor(Span.class, true).toNodeArray();
        assertEquals("number of spans found",2,spans.length);
        assertStringEquals(
            "span 1",
            "Flavor: small(90 to 120 minutes)",
            spans[0].toPlainTextString()
        );
        assertStringEquals(
            "span 2",
            "The short version of our Refactoring Challenge gives participants a general feel for the smells in the code base and includes time for participants to find and implement important refactorings.&#013;",
            spans[1].toPlainTextString()
        );

    }
}
