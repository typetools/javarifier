// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/utilTests/HTMLParserUtilsTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:31 $
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

package org.htmlparser.tests.utilTests;

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.*;
import org.htmlparser.tags.*;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserUtils;

public class HTMLParserUtilsTest extends ParserTestCase {

    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.HTMLParserUtilsTest", "HTMLParserUtilsTest");
    }

    public HTMLParserUtilsTest(String name) {
        super(name);
    }

    public void testRemoveTrailingSpaces() {
        String text = "Hello World  ";
        assertStringEquals(
            "modified text",
            "Hello World",
            ParserUtils.removeTrailingBlanks(text)
        );
    }
    
    public void testButCharsMethods() {
        String[] tmpSplitButChars = ParserUtils.splitButChars("<DIV>  +12.5, +3.4 </DIV>", "+.1234567890");
        assertStringEquals(
            "modified text",
            "+12.5*+3.4",
            new String(tmpSplitButChars[0] + '*' + tmpSplitButChars[1])
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimButChars("<DIV>  +12.5 </DIV>", "+.1234567890")
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimButChars("<DIV>  +1 2 . 5 </DIV>", "+.1234567890")
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimButCharsBeginEnd("<DIV>  +12.5 </DIV>", "+.1234567890")
        );
        assertStringEquals(
            "modified text",
            "+1 2 . 5",
            ParserUtils.trimButCharsBeginEnd("<DIV>  +1 2 . 5 </DIV>", "+.1234567890")
        );
    }
    
    public void testButDigitsMethods() {
        String[] tmpSplitButDigits = ParserUtils.splitButDigits("<DIV>  +12.5, +3.4 </DIV>", "+.");
        assertStringEquals(
            "modified text",
            "+12.5*+3.4",
            new String(tmpSplitButDigits[0] + '*' + tmpSplitButDigits[1])
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimButDigits("<DIV>  +12.5 </DIV>", "+.")
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimButDigits("<DIV>  +1 2 . 5 </DIV>", "+.")
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimButDigitsBeginEnd("<DIV>  +12.5 </DIV>", "+.")
        );
        assertStringEquals(
            "modified text",
            "+1 2 . 5",
            ParserUtils.trimButDigitsBeginEnd("<DIV>  +1 2 . 5 </DIV>", "+.")
        );
    }
    
    public void testCharsMethods() {
        String[] tmpSplitChars = ParserUtils.splitChars("<DIV>  +12.5, +3.4 </DIV>", " <>DIV/,");
        assertStringEquals(
            "modified text",
            "+12.5*+3.4",
            new String(tmpSplitChars[0] + '*' + tmpSplitChars[1])
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimChars("<DIV>  +12.5 </DIV>", "<>DIV/ ")
        );
        assertStringEquals(
            "modified text",
            "Trimallchars",
            ParserUtils.trimChars("<DIV>  Trim all chars   </DIV>", "<>DIV/ ")
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimCharsBeginEnd("<DIV>  +12.5 </DIV>", "<>DIV/ ")
        );
        assertStringEquals(
            "modified text",
            "Trim all spaces but not the ones inside the string",
            ParserUtils.trimCharsBeginEnd("<DIV>  Trim all spaces but not the ones inside the string </DIV>", "<>DIV/ ")
        );
    }
    
    public void testSpacesMethods() {
        String[] tmpSplitSpaces = ParserUtils.splitSpaces("<DIV>  +12.5, +3.4 </DIV>", "<>DIV/,");
        assertStringEquals(
            "modified text",
            "+12.5*+3.4",
            new String(tmpSplitSpaces[0] + '*' + tmpSplitSpaces[1])
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimSpaces("<DIV>  +12.5 </DIV>", "<>DIV/")
        );
        assertStringEquals(
            "modified text",
            "Trimallspaces",
            ParserUtils.trimSpaces("<DIV>  Trim all spaces  </DIV>", "<>DIV/")
        );
        assertStringEquals(
            "modified text",
            "+12.5",
            ParserUtils.trimSpacesBeginEnd("<DIV>  +12.5 </DIV>", "<>DIV/")
        );
        assertStringEquals(
            "modified text",
            "Trim all spaces but not the ones inside the string",
            ParserUtils.trimSpacesBeginEnd("<DIV>  Trim all spaces but not the ones inside the string </DIV>", "<>DIV/")
        );
        assertStringEquals(
            "modified text",
            "0",
            ParserUtils.trimSpacesBeginEnd("0", "")
        );
        assertStringEquals(
            "modified text",
            "verifying the last char x",
            ParserUtils.trimSpacesBeginEnd("verifying the last char x", "")
        );
        assertStringEquals(
            "modified text",
            "verifying the last char x",
            ParserUtils.trimSpacesBeginEnd("verifying the last char x ", "")
        );
        assertStringEquals(
            "modified text",
            "x verifying the first char",
            ParserUtils.trimSpacesBeginEnd("x verifying the first char", "")
        );
        assertStringEquals(
            "modified text",
            "x verifying the first char",
            ParserUtils.trimSpacesBeginEnd(" x verifying the first char", "")
        );
    }
    
    public void testTagsMethods() {
        try
        {
            String[] tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"});
            assertStringEquals(
                "modified text",
                "Begin * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"}, false, false);
            assertStringEquals(
                "modified text",
                "Begin *<DIV>  +12.5 </DIV>* ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"}, true, false);
            assertStringEquals(
                "modified text",
                "Begin *  +12.5 * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"}, false, true);
            assertStringEquals(
                "modified text",
                "Begin * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"})
            );
            assertStringEquals(
                "modified text",
                "<DIV>  +12.5 </DIV> ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"}, false, false)
            );
            assertStringEquals(
                "modified text",
                "  +12.5  ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"}, true, false)
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", new String[] {"DIV"}, false, true)
            );
            // Test trimAllTags method
            assertStringEquals(
                "modified text",
                "  +12.5  ALL OK",
                ParserUtils.trimAllTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", false)
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimAllTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", true)
            );
            assertStringEquals(
                "modified text",
                "  +12.5 ",
                ParserUtils.trimAllTags("<DIV><DIV>  +12.5 </DIV></DIV>", false)
            );
            assertStringEquals(
                "modified text",
                "",
                ParserUtils.trimAllTags("<DIV><DIV>  +12.5 </DIV></DIV>", true)
            );
            assertStringEquals(
                "modified text",
                " YYY ",
                ParserUtils.trimAllTags("<XXX> YYY <ZZZ>", false)
            );
            assertStringEquals(
                "modified text",
                "YYY",
                ParserUtils.trimAllTags("YYY", false)
            );
            assertStringEquals(
                "modified text",
                "> OK <",
                ParserUtils.trimAllTags("> OK <", true)
            );
        }
        catch (Exception e)
        {
            String msg = e.getMessage ();
            if (null == msg)
                msg = e.getClass ().getName ();
            fail (msg);
        }
    }
    
    public void testTagsFilterMethods() {
        try
        {
            NodeFilter filter = new TagNameFilter ("DIV");
            String[] tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter);
            assertStringEquals(
                "modified text",
                "Begin * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, false);
            assertStringEquals(
                "modified text",
                "Begin *<DIV>  +12.5 </DIV>* ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, true, false);
            assertStringEquals(
                "modified text",
                "Begin *  +12.5 * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, true);
            assertStringEquals(
                "modified text",
                "Begin * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter)
            );
            assertStringEquals(
                "modified text",
                "<DIV>  +12.5 </DIV> ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, false)
            );
            assertStringEquals(
                "modified text",
                "  +12.5  ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, true, false)
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, true)
            );
            NodeFilter filterTableRow = new TagNameFilter("TR");
            NodeFilter filterTableColumn = new TagNameFilter("TD");
            OrFilter filterOr = new OrFilter(filterTableRow, filterTableColumn);
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr)
            );
            assertStringEquals(
                "modified text",
                "<TD>  +12.5 </TD> ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr, false, false)
            );
            assertStringEquals(
                "modified text",
                "  +12.5  ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr, true, false)
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr, false, true)
            );
        }
        catch (Exception e)
        {
            String msg = e.getMessage ();
            if (null == msg)
                msg = e.getClass ().getName ();
            fail (msg);
        }
    }
    
    public void testTagsClassMethods() {
        try
        {
            NodeFilter filter = new NodeClassFilter (Div.class);
            String[] tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter);
            assertStringEquals(
                "modified text",
                "Begin * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, false);
            assertStringEquals(
                "modified text",
                "Begin *<DIV>  +12.5 </DIV>* ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, true, false);
            assertStringEquals(
                "modified text",
                "Begin *  +12.5 * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2])
            );
            tmpSplitTags = ParserUtils.splitTags("Begin <DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, true);
            assertStringEquals(
                "modified text",
                "Begin * ALL OK",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter)
            );
            assertStringEquals(
                "modified text",
                "<DIV>  +12.5 </DIV> ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, false)
            );
            assertStringEquals(
                "modified text",
                "  +12.5  ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, true, false)
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<DIV><DIV>  +12.5 </DIV></DIV> ALL OK", filter, false, true)
            );
            NodeFilter filterTableRow = new NodeClassFilter(TableRow.class);
            NodeFilter filterTableColumn = new NodeClassFilter(TableColumn.class);
            OrFilter filterOr = new OrFilter(filterTableRow, filterTableColumn);
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr)
            );
            assertStringEquals(
                "modified text",
                "<TD>  +12.5 </TD> ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr, false, false)
            );
            assertStringEquals(
                "modified text",
                "  +12.5  ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr, true, false)
            );
            assertStringEquals(
                "modified text",
                " ALL OK",
                ParserUtils.trimTags("<TR><TD>  +12.5 </TD></TR> ALL OK", filterOr, false, true)
            );
        }
        catch (Exception e)
        {
            String msg = e.getMessage ();
            if (null == msg)
                msg = e.getClass ().getName ();
            fail (msg);
        }
    }
    
    public void testTagsComplexMethods() {
        try
        {
            NodeFilter filterLink = new NodeClassFilter (LinkTag.class);
            NodeFilter filterDiv = new NodeClassFilter (Div.class);
            OrFilter filterLinkDiv = new OrFilter (filterLink, filterDiv);
            NodeFilter filterTable = new NodeClassFilter (TableColumn.class);
            OrFilter filter = new OrFilter (filterLinkDiv, filterTable);
            String[] tmpSplitTags = ParserUtils.splitTags("OutsideLeft<A>AInside</A><DIV><DIV>DivInside</DIV></DIV><TD>TableColoumnInside</TD>OutsideRight", filter);
            assertStringEquals(
                "modified text",
                "OutsideLeft*OutsideRight",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            tmpSplitTags = ParserUtils.splitTags("OutsideLeft<A>AInside</A><DIV><DIV>DivInside</DIV></DIV><TD>TableColoumnInside</TD>OutsideRight", filter, false, false);
            assertStringEquals(
                "modified text",
                "OutsideLeft*AInside*<DIV>DivInside</DIV>*TableColoumnInside*OutsideRight",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2] + '*' + tmpSplitTags[3] + '*' + tmpSplitTags[4])
            );
            tmpSplitTags = ParserUtils.splitTags("OutsideLeft<A>AInside</A><DIV><DIV>DivInside</DIV></DIV><TD>TableColoumnInside</TD>OutsideRight", filter, true, false);
            assertStringEquals(
                "modified text",
                "OutsideLeft*AInside*DivInside*TableColoumnInside*OutsideRight",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1] + '*' + tmpSplitTags[2] + '*' + tmpSplitTags[3] + '*' + tmpSplitTags[4])
            );
            tmpSplitTags = ParserUtils.splitTags("OutsideLeft<A>AInside</A><DIV><DIV>DivInside</DIV></DIV><TD>TableColoumnInside</TD>OutsideRight", filter, false, true);
            assertStringEquals(
                "modified text",
                "OutsideLeft*OutsideRight",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            tmpSplitTags = ParserUtils.splitTags("OutsideLeft<A>AInside<DIV><DIV>DivInside</DIV></DIV></A><TD>TableColoumnInside</TD>OutsideRight", new String[] {"DIV", "TD", "A"});
            assertStringEquals(
                "modified text",
                "OutsideLeft*OutsideRight",
                new String(tmpSplitTags[0] + '*' + tmpSplitTags[1])
            );
            assertStringEquals(
                "modified text",
                "OutsideLeftOutsideRight",
                ParserUtils.trimTags("OutsideLeft<A>AInside<DIV><DIV>DivInside</DIV></DIV></A><TD>TableColoumnInside</TD>OutsideRight", new String[] {"DIV", "TD", "A"})
            );
        }
        catch (Exception e)
        {
            String msg = e.getMessage ();
            if (null == msg)
                msg = e.getClass ().getName ();
            fail (msg);
        }
    }
}
