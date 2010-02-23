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

import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class ScriptTagTest extends ParserTestCase{

    static
    {
        System.setProperty ("org.htmlparser.tests.tagTests.ScriptTagTest", "ScriptTagTest");
    }

    public ScriptTagTest(String name)
    {
        super(name);
    }

    public void testCreation() throws ParserException
    {
        String testHtml = "<SCRIPT>Script Code</SCRIPT>";
        createParser(testHtml,"http://localhost/index.html");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertEquals("Script Tag Begin",0,scriptTag.getStartPosition ());
        assertEquals("Script Tag End",28,scriptTag.getEndTag ().getEndPosition ());
        assertEquals("Script Tag Code","Script Code",scriptTag.getScriptCode());
    }

    public void testToHTML() throws ParserException {
        createParser("<SCRIPT>document.write(d+\".com\")</SCRIPT>");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        // Check the data in the applet tag
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertEquals("Expected Raw String","<SCRIPT>document.write(d+\".com\")</SCRIPT>",scriptTag.toHtml());
    }

    /**
     * Test raw string.
     * Bug check by Wolfgang Germund 2002-06-02
     * Upon parsing :
     * &lt;script language="javascript"&gt;
     * if(navigator.appName.indexOf("Netscape") != -1)
     * document.write ('xxx');
     * else
     * document.write ('yyy');
     * &lt;/script&gt;
     * check toRawString().
     */
    public void testToHTMLWG() throws ParserException
    {
        StringBuffer sb2 = new StringBuffer();
        sb2.append("<script language=\"javascript\">\r\n");
        sb2.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
        sb2.append(" document.write ('xxx');\r\n");
        sb2.append("else\r\n");
        sb2.append(" document.write ('yyy');\r\n");
        sb2.append("</script>");
        String expectedHTML = sb2.toString();

        StringBuffer sb1 = new StringBuffer();
        sb1.append("<body>");
        sb1.append(expectedHTML);
        sb1.append("\r\n");
        String testHTML1 = sb1.toString();

        createParser(testHTML1);
        parser.setNodeFactory (new PrototypicalNodeFactory (new ScriptTag ()));
        parseAndAssertNodeCount(3);
        assertTrue("Node should be a script tag",node[1]
        instanceof ScriptTag);
        // Check the data in the script tag
        ScriptTag scriptTag = (ScriptTag)node[1];
        assertStringEquals("Expected Script Code",expectedHTML,scriptTag.toHtml());
    }

    public void testParamExtraction() throws ParserException {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<script src=\"/adb.js\" language=\"javascript\">\r\n");
        sb1.append("if(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
        sb1.append(" document.write ('xxx');\r\n");
        sb1.append("else\r\n");
        sb1.append(" document.write ('yyy');\r\n");
        sb1.append("</script>\r\n");
        createParser(sb1.toString());
        parseAndAssertNodeCount(2);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertEquals("Script Src","/adb.js",scriptTag.getAttribute("src"));
        assertEquals("Script Language","javascript",scriptTag.getAttribute("language"));
    }

    public void testVariableDeclarations() throws ParserException {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<script language=\"javascript\">\n");
        sb1.append("var lower = '<%=lowerValue%>';\n");
        sb1.append("</script>\n");
        createParser(sb1.toString());
        parseAndAssertNodeCount(2);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals("Script toHTML()","<script language=\"javascript\">\nvar lower = '<%=lowerValue%>';\n</script>",scriptTag.toHtml());
    }

    public void testSingleApostropheParsingBug() throws ParserException {
        String script = "<script src='<%=sourceFileName%>'></script>";
        createParser(script);
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals("Script toHTML()",script,scriptTag.toHtml());
    }

}
