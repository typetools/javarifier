// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/DivTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:23 $
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

import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.Tag;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class DivTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.DivTagTest", "DivTagTest");
    }

    public DivTagTest (String name)
    {
        super(name);
    }
    
    public void testScan() throws ParserException {
        createParser("<table><div align=\"left\">some text</div></table>");
        parseAndAssertNodeCount(1);
        assertType("node should be table",TableTag.class,node[0]);
        TableTag tableTag = (TableTag)node[0];
        Div div = (Div)tableTag.searchFor(Div.class, true).toNodeArray()[0];
        assertEquals("div contents","some text",div.toPlainTextString());
    }

    /**
     * Test case for bug #735193 Explicit tag type recognition for CompositTags not working.
     */
    public void testInputInDiv() throws ParserException
    {
        createParser("<div><INPUT type=\"text\" name=\"X\">Hello</INPUT></div>");
        parser.setNodeFactory (
            new PrototypicalNodeFactory (
                new Tag[]
                {
                    new Div (),
                    new InputTag (),
                }));
        parseAndAssertNodeCount(1);
        assertType("node should be div",Div.class,node[0]);
        Div div = (Div)node[0];
        assertType("child not input",InputTag.class,div.getChild (0));
    }
}
