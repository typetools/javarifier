// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2003 Derrick Oswald
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/tagTests/LabelTagTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:27 $
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

import java.util.Hashtable;

import org.htmlparser.tags.LabelTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class LabelTagTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.LabelTagTest", "LabelTagTest");
    }

    public LabelTagTest (String name)
    {
        super(name);
    }
    
    public void testSimpleLabels() throws ParserException
    {
        String html = "<label>This is a label tag</label>";
        createParser(html);
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof LabelTag);
        //  check the title node
        LabelTag labelTag = (LabelTag) node[0];
        assertEquals("Label","This is a label tag",labelTag.getChildrenHTML());
        assertEquals("Label","This is a label tag",labelTag.getLabel());
        assertStringEquals("Label", html, labelTag.toHtml());
    }

    public void testLabelWithJspTag() throws ParserException {
        String label = "<label><%=labelValue%></label>";
        createParser(label);
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof LabelTag);
        //  check the title node
        LabelTag labelTag = (LabelTag) node[0];
        assertStringEquals("Label",label,labelTag.toHtml());
    }

    public void testLabelWithOtherTags() throws ParserException
    {
        String html = "<label><span>Span within label</span></label>";
        createParser(html);
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof LabelTag);
        //  check the title node
        LabelTag labelTag = (LabelTag) node[0];
        assertEquals("Label value","Span within label",labelTag.getLabel());
        assertStringEquals("Label", html, labelTag.toHtml());
    }

    public void testLabelWithManyCompositeTags() throws ParserException {
        String guts = "<span>Jane <b> Doe </b> Smith</span>";
        String html = "<label>" + guts + "</label>";
        createParser(html);
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof LabelTag);
        LabelTag labelTag = (LabelTag) node[0];
        assertEquals("Label value",guts,labelTag.getChildrenHTML());
        assertEquals("Label value","Jane  Doe  Smith",labelTag.getLabel());
        assertStringEquals("Label",html,labelTag.toHtml());
    }


    public void testLabelsID() throws ParserException
    {
        String html = "<label>John Doe</label>";
        createParser(html);
        parseAndAssertNodeCount(1);
        assertTrue(node[0] instanceof LabelTag);
        LabelTag labelTag = (LabelTag) node[0];
        assertStringEquals("Label", html, labelTag.toHtml());
        Hashtable attr = labelTag.getAttributes();
        assertNull("ID",attr.get("id"));
    }

    public void testNestedLabels() throws ParserException
    {
        String label1 = "<label id=\"attr1\">";
        String label2 = "<label>Jane Doe";
        createParser(label1 + label2);
        parseAndAssertNodeCount(2);
        assertTrue(node[0] instanceof LabelTag);
        assertTrue(node[1] instanceof LabelTag);
        LabelTag labelTag = (LabelTag) node[0];
        assertStringEquals("Label", label1 + "</label>", labelTag.toHtml());
        labelTag = (LabelTag) node[1];
        assertStringEquals("Label", label2 + "</label>",labelTag.toHtml());
        Hashtable attr = labelTag.getAttributes();
        assertNull("ID",attr.get("id"));
    }

    public void testNestedLabels2() throws ParserException
    {
        String label1 = "<LABEL value=\"Google Search\">Google</LABEL>";
        String label2 = "<LABEL value=\"AltaVista Search\">AltaVista";
        String label3 = "<LABEL value=\"Lycos Search\"></LABEL>";
        String label4 = "<LABEL>Yahoo!</LABEL>";
        String label5 = "<LABEL>\nHotmail</LABEL>";
        String label6 = "<LABEL value=\"ICQ Messenger\">";
        String label7 = "<LABEL>Mailcity\n</LABEL>";
        String label8 = "<LABEL>\nIndiatimes\n</LABEL>";
        String label9 = "<LABEL>\nRediff\n</LABEL>";
        String label10 = "<LABEL>Cricinfo";
        String label11 = "<LABEL value=\"Microsoft Passport\">";
        String label12 = "<LABEL value=\"AOL\"><SPAN>AOL</SPAN></LABEL>";
        String label13 = "<LABEL value=\"Time Warner\">Time <B>Warner <SPAN>AOL </SPAN>Inc.</B>";
        String testHTML = label1 + label2 + label3 + label4 + label5 + label6 
            + label7 + label8 + label9 + label10 + label11 + label12 + label13;
        createParser(testHTML);
        parseAndAssertNodeCount(13);
        LabelTag LabelTag;
        LabelTag = (LabelTag) node[0];
        assertStringEquals("HTML String", label1, LabelTag.toHtml());
        LabelTag = (LabelTag) node[1];
        assertStringEquals("HTML String", label2 + "</LABEL>", LabelTag.toHtml());
        LabelTag = (LabelTag) node[2];
        assertStringEquals("HTML String", label3, LabelTag.toHtml());
        LabelTag = (LabelTag) node[3];
        assertStringEquals("HTML String", label4, LabelTag.toHtml());
        LabelTag = (LabelTag) node[4];
        assertStringEquals("HTML String", label5, LabelTag.toHtml());
        LabelTag = (LabelTag) node[5];
        assertStringEquals("HTML String", label6 + "</LABEL>",LabelTag.toHtml());
        LabelTag = (LabelTag) node[6];
        assertStringEquals("HTML String", label7, LabelTag.toHtml());
        LabelTag = (LabelTag) node[7];
        assertStringEquals("HTML String", label8, LabelTag.toHtml());
        LabelTag = (LabelTag) node[8];
        assertStringEquals("HTML String", label9, LabelTag.toHtml());
        LabelTag = (LabelTag) node[9];
        assertStringEquals("HTML String", label10 + "</LABEL>",LabelTag.toHtml());
        LabelTag = (LabelTag) node[10];
        assertStringEquals("HTML String", label11 + "</LABEL>",LabelTag.toHtml());
        LabelTag = (LabelTag) node[11];
        assertStringEquals("HTML String", label12, LabelTag.toHtml());
        LabelTag = (LabelTag) node[12];
        assertStringEquals("HTML String", label13 + "</LABEL>",LabelTag.toHtml());
    }
}
