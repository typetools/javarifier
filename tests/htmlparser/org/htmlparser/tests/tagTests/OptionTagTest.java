// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Dhaval Udani
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/OptionTagTest.java,v $
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

import org.htmlparser.Text;
import org.htmlparser.tags.OptionTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class OptionTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.OptionTagTest", "OptionTagTest");
    }

    private String option1 = "<OPTION value=\"Google Search\">Google</OPTION>";
    private String option2 = "<OPTION value=\"AltaVista Search\">AltaVista";
    private String option3 = "<OPTION value=\"Lycos Search\"></OPTION>";
    private String option4 = "<OPTION>Yahoo!</OPTION>";
    private String option5 = "<OPTION>\nHotmail</OPTION>";
    private String option6 = "<OPTION value=\"ICQ Messenger\">";
    private String option7 = "<OPTION>Mailcity\n</OPTION>";
    private String option8 = "<OPTION>\nIndiatimes\n</OPTION>";
    private String option9 = "<OPTION>\nRediff\n</OPTION>";
    private String option10 = "<OPTION>Cricinfo";
    private String option11 = "<OPTION value=\"Microsoft Passport\">";
    private String option12 = "<OPTION value=\"AOL\"><SPAN>AOL</SPAN></OPTION>";
    private String option13 = "<OPTION value=\"Time Warner\">Time <LABEL>Warner <SPAN>AOL </SPAN>Inc.</LABEL></OPTION>";
    private String testHTML = option1 + option2 + option3 + option4 + option5 + option6 
        + option7 + option8 + option9 + option10 + option11 + option12 + option13;

    private String html = new String(
                                    "<OPTION value=\"Google Search\">Google</OPTION>" +
                                    "<OPTION value=\"AltaVista Search\">AltaVista" +
                                    "<OPTION value=\"Lycos Search\"></OPTION>" +
                                    "<OPTION>Yahoo!</OPTION>" +
                                    "<OPTION>\nHotmail</OPTION>" +
                                    "<OPTION>Mailcity\n</OPTION>"+
                                    "<OPTION>\nIndiatimes\n</OPTION>"+
                                    "<OPTION>\nRediff\n</OPTION>\n" +
                                    "<OPTION>Cricinfo"
                                    );

    public OptionTagTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createParser(testHTML);
        parseAndAssertNodeCount(13);
    }

    public void testToHTML() throws ParserException
    {
        for(int j=0;j<nodeCount;j++)
        {
            assertTrue("Node " + j + " should be Option Tag",node[j] instanceof OptionTag);
//            System.out.println(node[j].getClass().getName());
//            System.out.println(node[j].toHtml());
        }
        OptionTag OptionTag;
        OptionTag = (OptionTag) node[0];
        assertStringEquals("HTML String", option1, OptionTag.toHtml());
        OptionTag = (OptionTag) node[1];
        assertStringEquals("HTML String", option2 + "</OPTION>", OptionTag.toHtml());
        OptionTag = (OptionTag) node[2];
        assertStringEquals("HTML String", option3, OptionTag.toHtml());
        OptionTag = (OptionTag) node[3];
        assertStringEquals("HTML String", option4, OptionTag.toHtml());
        OptionTag = (OptionTag) node[4];
        assertStringEquals("HTML String", option5, OptionTag.toHtml());
        OptionTag = (OptionTag) node[5];
        assertStringEquals("HTML String", option6 + "</OPTION>",OptionTag.toHtml());
        OptionTag = (OptionTag) node[6];
        assertStringEquals("HTML String", option7, OptionTag.toHtml());
        OptionTag = (OptionTag) node[7];
        assertStringEquals("HTML String", option8, OptionTag.toHtml());
        OptionTag = (OptionTag) node[8];
        assertStringEquals("HTML String", option9, OptionTag.toHtml());
        OptionTag = (OptionTag) node[9];
        assertStringEquals("HTML String", option10 + "</OPTION>",OptionTag.toHtml());
        OptionTag = (OptionTag) node[10];
        assertStringEquals("HTML String", option11 + "</OPTION>",OptionTag.toHtml());
        OptionTag = (OptionTag) node[11];
        assertStringEquals("HTML String", option12,OptionTag.toHtml());
        OptionTag = (OptionTag) node[12];
        assertStringEquals("HTML String", option13, OptionTag.toHtml());
    }

    public void testToString() throws ParserException
    {
        for(int j=0;j<11;j++)
        {
            assertTrue("Node " + j + " should be Option Tag",node[j] instanceof OptionTag);
        }
        OptionTag OptionTag;
        OptionTag = (OptionTag) node[0];
        assertEquals("HTML Raw String","OPTION VALUE: Google Search TEXT: Google\n",OptionTag.toString());
        OptionTag = (OptionTag) node[1];
        assertEquals("HTML Raw String","OPTION VALUE: AltaVista Search TEXT: AltaVista\n",OptionTag.toString());
        OptionTag = (OptionTag) node[2];
        assertEquals("HTML Raw String","OPTION VALUE: Lycos Search TEXT: \n",OptionTag.toString());
        OptionTag = (OptionTag) node[3];
        assertEquals("HTML Raw String","OPTION VALUE: null TEXT: Yahoo!\n",OptionTag.toString());
        OptionTag = (OptionTag) node[4];
        assertEquals("HTML Raw String","OPTION VALUE: null TEXT: \nHotmail\n",OptionTag.toString());
        OptionTag = (OptionTag) node[5];
        assertEquals("HTML Raw String","OPTION VALUE: ICQ Messenger TEXT: \n",OptionTag.toString());
        OptionTag = (OptionTag) node[6];
        assertEquals("HTML Raw String","OPTION VALUE: null TEXT: Mailcity\n\n",OptionTag.toString());
        OptionTag = (OptionTag) node[7];
        assertEquals("HTML Raw String","OPTION VALUE: null TEXT: \nIndiatimes\n\n",OptionTag.toString());
        OptionTag = (OptionTag) node[8];
        assertEquals("HTML Raw String","OPTION VALUE: null TEXT: \nRediff\n\n",OptionTag.toString());
        OptionTag = (OptionTag) node[9];
        assertEquals("HTML Raw String","OPTION VALUE: null TEXT: Cricinfo\n",OptionTag.toString());
        OptionTag = (OptionTag) node[10];
        assertEquals("HTML Raw String","OPTION VALUE: Microsoft Passport TEXT: \n",OptionTag.toString());
        OptionTag = (OptionTag) node[11];
        assertEquals("HTML Raw String","OPTION VALUE: AOL TEXT: AOL\n",OptionTag.toString());
        OptionTag = (OptionTag) node[12];
        assertEquals("HTML Raw String","OPTION VALUE: Time Warner TEXT: Time Warner AOL Inc.\n",OptionTag.toString());
    }

    public void testScan() throws ParserException
    {
        createParser(html,"http://www.google.com/test/index.html");
        parseAndAssertNodeCount(10);
        for (int j = 0; j < 10; j++)
        {
            if (node[j] instanceof Text)
                continue;
            assertTrue("Node " + j + " should be Option Tag",node[j] instanceof OptionTag);
        }
    }
}
