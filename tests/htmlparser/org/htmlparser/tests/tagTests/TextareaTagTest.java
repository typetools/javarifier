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

package org.htmlparser.tests.tagTests;

import org.htmlparser.tags.TextareaTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class TextareaTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.TextareaTagTest", "TextareaTagTest");
    }

    private String area1 = "<TEXTAREA name=\"Remarks\" >The intervention by the UN proved beneficial</TEXTAREA>";
    private String area2 = "<TEXTAREA>The capture of the Somali warloard was elusive</TEXTAREA>";
    private String area3 = "<TEXTAREA></TEXTAREA>";
    private String area4 = "<TEXTAREA name=\"Remarks\">The death threats of the organization\n" +
                            "refused to intimidate the soldiers</TEXTAREA>";
    private String area5 = "<TEXTAREA name=\"Remarks\">The death threats of the LTTE\n" +
                            "refused to intimidate the Tamilians\n</TEXTAREA>";

    private String testHTML = area1 + area2 + area3 + area4 + area5;

    private String html = "<TEXTAREA name=\"Remarks\">The intervention by the UN proved beneficial</TEXTAREA>" +
                          "<TEXTAREA>The capture of the Somali warloard was elusive</TEXTAREA>" +
                          "<TEXTAREA></TEXTAREA>" +
                          "<TEXTAREA name=\"Remarks\">The death threats of the organization\n" +
                          "refused to intimidate the soldiers</TEXTAREA>" +
                          "<TEXTAREA name=\"Remarks\">The death threats of the LTTE\n" +
                          "refused to intimidate the Tamilians\n</TEXTAREA>";

    public TextareaTagTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        createParser(testHTML);
        parseAndAssertNodeCount(5);
    }

    public void testToHTML() throws ParserException
    {
        assertTrue("Node 1 should be Textarea Tag",node[0] instanceof TextareaTag);
        assertTrue("Node 2 should be Textarea Tag",node[1] instanceof TextareaTag);
        assertTrue("Node 3 should be Textarea Tag",node[2] instanceof TextareaTag);
        assertTrue("Node 4 should be Textarea Tag",node[3] instanceof TextareaTag);
        assertTrue("Node 5 should be Textarea Tag",node[4] instanceof TextareaTag);
        TextareaTag textareaTag;
        textareaTag = (TextareaTag) node[0];
        assertStringEquals("HTML String 1",area1,textareaTag.toHtml());
        textareaTag = (TextareaTag) node[1];
        assertStringEquals("HTML String 2",area2,textareaTag.toHtml());
        textareaTag = (TextareaTag) node[2];
        assertStringEquals("HTML String 3",area3,textareaTag.toHtml());
        textareaTag = (TextareaTag) node[3];
        assertStringEquals("HTML String 4",area4,textareaTag.toHtml());
        textareaTag = (TextareaTag) node[4];
        assertStringEquals("HTML String 5",area5,textareaTag.toHtml());

    }

    public void testScan() throws ParserException
    {
        createParser(html);
        parseAndAssertNodeCount(5);

        // check the Textarea node
        for(int j=0;j<nodeCount;j++)
            assertTrue(node[j] instanceof TextareaTag);
    }
}
