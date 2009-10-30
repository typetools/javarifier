// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Dhaval Udani
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

package org.htmlparser.tests.tagTests;

import org.htmlparser.tags.OptionTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class SelectTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.SelectTagTest", "SelectTagTest");
    }

    private String testHTML = "<SELECT name=\"Nominees\">\n"+
                                    "<option value=\"Spouse\">Spouse"+
                                    "<option value=\"Father\"></option>\n"+
                                    "<option value=\"Mother\">Mother\n" +
                                    "<option value=\"Son\">\nSon\n</option>"+
                                    "<option value=\"Daughter\">\nDaughter\n"+
                                    "<option value=\"Nephew\">\nNephew</option>\n"+
                                    "<option value=\"Niece\">Niece\n" +
                                    "</select>";

    private String correctedHTML = "<SELECT name=\"Nominees\">\n"+
                                    "<option value=\"Spouse\">Spouse</option>"+
                                    "<option value=\"Father\"></option>\n"+
                                    "<option value=\"Mother\">Mother\n</option>" +
                                    "<option value=\"Son\">\nSon\n</option>"+
                                    "<option value=\"Daughter\">\nDaughter\n</option>"+
                                    "<option value=\"Nephew\">\nNephew</option>\n"+
                                    "<option value=\"Niece\">Niece\n</option>" +
                                    "</select>";

    private SelectTag selectTag;
    private String html = "<Select name=\"Remarks\">" +
                                    "<option value='option1'>option1</option>" +
                                    "</Select>" +
                                    "<Select name=\"something\">" +
                                        "<option value='option2'>option2</option>" +
                                    "</Select>" +
                                    "<Select></Select>" +
                                    "<Select name=\"Remarks\">The death threats of the organization\n" +
                                    "refused to intimidate the soldiers</Select>" +
                                    "<Select name=\"Remarks\">The death threats of the LTTE\n" +
                                    "refused to intimidate the Tamilians\n</Select>";

    public SelectTagTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception{
        super.setUp();
        createParser(testHTML);
        parseAndAssertNodeCount(1);
        assertTrue("Node 1 should be Select Tag",node[0] instanceof SelectTag);
        selectTag = (SelectTag) node[0];
    }

    public void testToHTML() throws ParserException
    {
        assertStringEquals("HTML String", correctedHTML, selectTag.toHtml());
    }

    public void testGetOptionTags() {
        OptionTag [] optionTags = selectTag.getOptionTags();
        assertEquals("option tag array length",7,optionTags.length);
        assertEquals("option tag 1","Spouse",optionTags[0].getOptionText());
        assertEquals("option tag 7","Niece\n",optionTags[6].getOptionText());
    }

    public void testScan() throws ParserException
    {

        createParser(html,"http://www.google.com/test/index.html");
        parseAndAssertNodeCount(5);

        // check the Select node
        for(int j=0;j<nodeCount;j++)
            assertTrue(node[j] instanceof SelectTag);

        SelectTag selectTag = (SelectTag)node[0];
        OptionTag [] optionTags = selectTag.getOptionTags();
        assertEquals("option tag array length",1,optionTags.length);
        assertEquals("option tag value","option1",optionTags[0].getOptionText());
    }

    /**
     * Bug reproduction.
     */
    public void testSelectTagWithComments() throws Exception {
        createParser(
            "<form>" +
            "<select> " +
            "<!-- 1 --><option selected>123 " +
            "<option>345 " +
            "</select> " +
            "</form>"
        );
        parseAndAssertNodeCount(1);
    }
}
