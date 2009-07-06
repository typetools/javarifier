// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/scannersTests/ScriptScannerTest.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:21 $
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

package org.htmlparser.tests.scannersTests;

import java.util.Hashtable;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.scanners.ScriptDecoder;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class ScriptScannerTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.scannersTests.ScriptScannerTest", "ScriptScannerTest");
    }

    public ScriptScannerTest(String name) {
        super(name);
    }

    public void testScan() throws ParserException
    {
        String testHtml = "<SCRIPT>document.write(d+\".com\")</SCRIPT>";
        createParser(testHtml,"http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        // Check the data in the applet tag
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals("Expected Script Code","document.write(d+\".com\")",scriptTag.getScriptCode());
        assertStringEquals("script tag html",testHtml,scriptTag.toHtml());
    }

    /**
     * Test javascript tag attributes.
     * Bug reported by Gordon Deudney 2002-03-27
     * Upon parsing :
     * &lt;SCRIPT LANGUAGE="JavaScript"
     * SRC="../js/DetermineBrowser.js"&gt;&lt;/SCRIPT&gt;
     * the SRC data cannot be retrieved.
     */
    public void testScanBug() throws ParserException
    {
        String src = "../js/DetermineBrowser.js";
        createParser("<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"" + src + "\"></SCRIPT>","http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        // Check the data in the applet tag
        ScriptTag scriptTag = (ScriptTag)node[0];
        Hashtable table = scriptTag.getAttributes();
        String srcExpected = (String)table.get("SRC");
        assertEquals("Expected SRC value",src,srcExpected);
    }

    /**
     * Test script code.
     * Bug check by Wolfgang Germund 2002-06-02
     * Upon parsing :
     * &lt;script language="javascript"&gt;
     * if(navigator.appName.indexOf("Netscape") != -1)
     * document.write ('xxx');
     * else
     * document.write ('yyy');
     * &lt;/script&gt;
     * check getScriptCode().
     */
    public void testScanBugWG() throws ParserException
    {
        StringBuffer sb2 = new StringBuffer();
        sb2.append("\r\nif(navigator.appName.indexOf(\"Netscape\") != -1)\r\n");
        sb2.append(" document.write ('xxx');\r\n");
        sb2.append("else\r\n");
        sb2.append(" document.write ('yyy');\r\n");
        String testHTML2 = sb2.toString();

        StringBuffer sb1 = new StringBuffer();
        sb1.append("<body><script language=\"javascript\">");
        sb1.append(testHTML2);
        sb1.append("</script>");
        String testHTML1 = sb1.toString();

        createParser(testHTML1,"http://www.google.com/test/index.html");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a body tag", node[0] instanceof BodyTag);
        BodyTag body = (BodyTag)node[0];
        assertTrue("Node should have one child", 1 == body.getChildCount ());
        assertTrue("Child should be a script tag", body.getChild (0) instanceof ScriptTag);
        // Check the data in the script tag
        ScriptTag scriptTag = (ScriptTag)body.getChild (0);
        String s = scriptTag.getScriptCode();
        assertStringEquals("Expected Script Code",testHTML2,s);
    }

    public void testScanScriptWithLinks() throws ParserException
    {
        StringBuffer sb1 = new StringBuffer();
        sb1.append("<script type=\"text/javascript\">\r\n"+
            "<A HREF=\"http://thisisabadlink.com\">\r\n"+
            "</script>\r\n");
        String testHTML1 = new String(sb1.toString());

        createParser(testHTML1,"http://www.hardwareextreme.com/");
        parseAndAssertNodeCount(2);
        assertTrue("Node should be a script tag",node[0]
        instanceof ScriptTag);
    }

    public void testScanScriptWithComments() throws ParserException {
        String expectedCode = "\n<!--\n"+
                          "  function validateForm()\n"+
                          "  {\n"+
                          "     var i = 10;\n"+
                          "     if(i < 5)\n"+
                          "     i = i - 1 ; \n"+
                          "     return true;\n"+
                          "  }\n"+
                          "// -->\n";
        createParser("<SCRIPT Language=\"JavaScript\">"+expectedCode+
                          "</SCRIPT>","http://www.hardwareextreme.com/");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0]
        instanceof ScriptTag);
        // Check the data in the applet tag
        ScriptTag scriptTag = (ScriptTag)node[0];
        String scriptCode = scriptTag.getScriptCode();
        assertStringEquals("Expected Code",expectedCode,scriptCode);
    }

    /**
     * Submitted by Dhaval Udani - reproducing bug 664404
     * @throws ParserException
     */
    public void testScriptTagComments() throws
    ParserException
    {
        String testHtml =
        "<SCRIPT LANGUAGE=\"JavaScript\">\r\n"+
            "<!--\r\n"+
            "// -->\r\n"+
        "</SCRIPT>";
        createParser(testHtml);
        parseAndAssertNodeCount(1);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals("scriptag html",testHtml,scriptTag.toHtml());
    }

    /**
     * Duplicates bug reported by James Moliere - whereby,
     * if script tags are generated by script code, the parser
     * interprets them as real tags. The problem was that the
     * string parser was not moving to the ignore state on encountering double
     * quotes (only single quotes were previously accepted).
     * @throws Exception
     */
    public void testScriptTagsGeneratedByScriptCode() throws Exception {
        createParser(
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 " +
            "Transitional//EN\">" +
            "<html>" +
            "<head>" +
            "<title>Untitled Document</title>" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; " +
            "charset=iso-8859-1\">" +
            "</head>" +
            "<script language=\"JavaScript\">" +
            "document.write(\"<script " +
            "language=\\\"JavaScript\\\">\");" +
            "document.write(\"function onmousedown" +
            "(event)\");" +
            "document.write(\"{ // do something\"); " +
            "document.write(\"}\"); " +
            "// parser thinks this is the end tag.\n" +
            "document.write(\"</script>\");" +
            "</script>" +
            "<body>" +
            "</body>" +
            "</html>"
        );
        Node scriptNodes [] =
            parser.extractAllNodesThatAre(ScriptTag.class);
        assertType(
            "scriptnode",
            ScriptTag.class,
            scriptNodes[0]
        );
        ScriptTag scriptTag = (ScriptTag)scriptNodes[0];
        assertStringEquals(
            "script code",
            "document.write(\"<script " +
            "language=\\\"JavaScript\\\">\");" +
            "document.write(\"function onmousedown" +
            "(event)\");" +
            "document.write(\"{ // do something\"); " +
            "document.write(\"}\"); " +
            "// parser thinks this is the end tag.\n" +
            "document.write(\"</script>\");",
            scriptTag.getScriptCode()
        );

    }

    public void testScriptCodeExtraction() throws ParserException {
        createParser(
            "<SCRIPT language=JavaScript>" +
            "document.write(\"<a href=\"1.htm\"><img src=\"1.jpg\" " +
            "width=\"80\" height=\"20\" border=\"0\"></a>\");" +
            "</SCRIPT>"
        );
        parseAndAssertNodeCount(1);
        assertType("script",ScriptTag.class,node[0]);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals(
            "script code",
            "document.write(\"<a href=\"1.htm\"><img src=\"1.jpg\" " +
            "width=\"80\" height=\"20\" border=\"0\"></a>\");",
            scriptTag.getScriptCode()
        );
    }

    public void testScriptCodeExtractionWithMultipleQuotes() throws ParserException {
        createParser(
            "<SCRIPT language=JavaScript>" +
            "document.write(\"<a href=\\\"1.htm\\\"><img src=\\\"1.jpg\\\" " +
            "width=\\\"80\\\" height=\\\"20\\\" border=\\\"0\\\"></a>\");" +
            "</SCRIPT>"
        );
        parseAndAssertNodeCount(1);
        assertType("script",ScriptTag.class,node[0]);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals(
            "script code",
            "document.write(\"<a href=\\\"1.htm\\\"><img src=\\\"1.jpg\\\" " +
            "width=\\\"80\\\" height=\\\"20\\\" border=\\\"0\\\"></a>\");",
            scriptTag.getScriptCode()
        );
    }

    public void testScriptWithinComments() throws Exception {
        createParser(
            "<script language=\"JavaScript1.2\">" +
            "\n" +
            "var linkset=new Array()" +
            "\n" +
            "var ie4=document.all&&navigator.userAgent.indexOf(\"Opera\")==-1" +
            "\n" +
            "var ns6=document.getElementById&&!document.all" +
            "\n" +
            "var ns4=document.layers" +
            "\n" +
            "\n" +
            "\n" +
            "function showmenu(e,which){" +
            "\n" +
            "\n" +
            "\n" +
            "if (!document.all&&!document.getElementById&&!document.layers)" +
            "\n" +
            "return" +
            "\n" +
            "\n" +
            "\n" +
            "clearhidemenu()" +
            "\n" +
            "\n" +
            "\n" +
            "menuobj=ie4? document.all.popmenu : ns6? document.getElementById(\"popmenu\") : ns4? document.popmenu : \"\"\n" +
            "\n" +
            "menuobj.thestyle=(ie4||ns6)? menuobj.style : menuobj" +
            "\n" +
            "\n" +
            "\n" +
            "if (ie4||ns6)" +
            "\n" +
            "menuobj.innerHTML=which" +
            "\n" +
            "else{" +
            "\n" +
            "menuobj.document.write('<layer name=gui bgColor=#E6E6E6 width=165 onmouseover=\"clearhidemenu()\" onmouseout=\"hidemenu()\">'+which+'</layer>')" +
            "\n" +
            "menuobj.document.close()" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "menuobj.contentwidth=(ie4||ns6)? menuobj.offsetWidth : menuobj.document.gui.document.width" +
            "\n" +
            "menuobj.contentheight=(ie4||ns6)? menuobj.offsetHeight : menuobj.document.gui.document.height" +
            "\n" +
            "eventX=ie4? event.clientX : ns6? e.clientX : e.x" +
            "\n" +
            "eventY=ie4? event.clientY : ns6? e.clientY : e.y" +
            "\n" +
            "\n" +
            "\n" +
            "//Find out how close the mouse is to the corner of the window" +
            "\n" +
            "var rightedge=ie4? document.body.clientWidth-eventX : window.innerWidth-eventX" +
            "\n" +
            "var bottomedge=ie4? document.body.clientHeight-eventY : window.innerHeight-eventY" +
            "\n" +
            "\n" +
            "\n" +
            "//if the horizontal distance isn't enough to accomodate the width of the context menu" +
            "\n" +
            "if (rightedge < menuobj.contentwidth)" +
            "\n" +
            "//move the horizontal position of the menu to the left by it's width" +
            "\n" +
            "menuobj.thestyle.left=ie4? document.body.scrollLeft+eventX-menuobj.contentwidth : ns6? window.pageXOffset+eventX-menuobj.contentwidth : eventX-menuobj.contentwidth" +
            "\n" +
            "else" +
            "\n" +
            "//position the horizontal position of the menu where the mouse was clicked" +
            "\n" +
            "menuobj.thestyle.left=ie4? document.body.scrollLeft+eventX : ns6? window.pageXOffset+eventX : eventX" +
            "\n" +
            "\n" +
            "\n" +
            "//same concept with the vertical position" +
            "\n" +
            "if (bottomedge<menuobj.contentheight)" +
            "\n" +
            "menuobj.thestyle.top=ie4? document.body.scrollTop+eventY-menuobj.contentheight : ns6? window.pageYOffset+eventY-menuobj.contentheight : eventY-menuobj.contentheight" +
            "\n" +
            "else" +
            "\n" +
            "menuobj.thestyle.top=ie4? document.body.scrollTop+event.clientY : ns6? window.pageYOffset+eventY : eventY" +
            "\n" +
            "menuobj.thestyle.visibility=\"visible\"\n" +
            "\n" +
            "return false" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "function contains_ns6(a, b) {" +
            "\n" +
            "//Determines if 1 element in contained in another- by Brainjar.com" +
            "\n" +
            "while (b.parentNode)" +
            "\n" +
            "if ((b = b.parentNode) == a)" +
            "\n" +
            "return true;" +
            "\n" +
            "return false;" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "function hidemenu(){" +
            "\n" +
            "if (window.menuobj)" +
            "\n" +
            "menuobj.thestyle.visibility=(ie4||ns6)? \"hidden\" : \"hide\"\n" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "function dynamichide(e){" +
            "\n" +
            "if (ie4&&!menuobj.contains(e.toElement))" +
            "\n" +
            "hidemenu()" +
            "\n" +
            "else if (ns6&&e.currentTarget!= e.relatedTarget&& !contains_ns6(e.currentTarget, e.relatedTarget))" +
            "\n" +
            "hidemenu()" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "function delayhidemenu(){" +
            "\n" +
            "if (ie4||ns6||ns4)" +
            "\n" +
            "delayhide=setTimeout(\"hidemenu()\",500)" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "function clearhidemenu(){" +
            "\n" +
            "if (window.delayhide)" +
            "\n" +
            "clearTimeout(delayhide)" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "function highlightmenu(e,state){" +
            "\n" +
            "if (document.all)" +
            "\n" +
            "source_el=event.srcElement" +
            "\n" +
            "else if (document.getElementById)" +
            "\n" +
            "source_el=e.target" +
            "\n" +
            "if (source_el.className==\"menuitems\"){" +
            "\n" +
            "source_el.id=(state==\"on\")? \"mouseoverstyle\" : \"\"\n" +
            "\n" +
            "}" +
            "\n" +
            "else{" +
            "\n" +
            "while(source_el.id!=\"popmenu\"){" +
            "\n" +
            "source_el=document.getElementById? source_el.parentNode : source_el.parentElement" +
            "\n" +
            "if (source_el.className==\"menuitems\"){" +
            "\n" +
            "source_el.id=(state==\"on\")? \"mouseoverstyle\" : \"\"\n" +
            "\n" +
            "}" +
            "\n" +
            "}" +
            "\n" +
            "}" +
            "\n" +
            "}" +
            "\n" +
            "\n" +
            "\n" +
            "if (ie4||ns6)" +
            "\n" +
            "document.onclick=hidemenu" +
            "\n" +
            "\n" +
            "\n" +
            "</script>"
        );
        parseAndAssertNodeCount(1);

    }

    /**
     * There was a bug in the ScriptScanner when there was multiline script and
     * the last line did not have a newline before the end script tag. For example:
     *
     * &lt;script&gt;alert()
     * alert()&lt;/script&gt;
     *
     * Would generate the following "scriptCode()" result:
     * alert()alert()
     *
     * But should actually return:
     * alert()
     * alert()
     *
     * This was fixed in ScriptScanner, which this test verifies
     */
    public void testScriptCodeExtractionWithNewlines() throws ParserException {
        String scriptContents = "alert()\r\nalert()";
        createParser("<script>" + scriptContents + "</script>");
        parseAndAssertNodeCount(1);
        assertType("script",ScriptTag.class,node[0]);
        ScriptTag scriptTag = (ScriptTag)node[0];
        assertStringEquals(
            "script code",
            scriptContents,
            scriptTag.getScriptCode()
        );
    }

    /**
     * Tests a bug in ScriptScanner where a NPE would be thrown if the
     * script tag was not closed before the document ended.
     */
    public void testScanNoEndTag() throws ParserException   {
        createParser("<script>");
        parseAndAssertNodeCount(1);
    }

    /**
     * See bug #741769 ScriptScanner doesn't handle quoted </script> tags
     */
    public void testScanQuotedEndTag() throws ParserException
    {
        String html = "<SCRIPT language=\"JavaScript\">document.write('</SCRIPT>');</SCRIPT>";
        createParser(html);
        parseAndAssertNodeCount(1);
        assertStringEquals ("Parse error", html, node[0].toHtml ());
    }



    public void testScanScriptWithTagsInComment() throws ParserException {
        String javascript = "\n// This is javascript with <li> tag in the comment\n";
        createParser("<script>"+ javascript + "</script>");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        String scriptCode = scriptTag.getScriptCode();
        assertStringEquals("Expected Code",javascript,scriptCode);
    }

    public void testScanScriptWithJavascriptLineEndings() throws ParserException {
        String javascript =
            "\n" +
            "var s = \"This is a string \\\n" +
            "that spans multiple lines;\"\n";
        createParser("<script>"+ javascript + "</script>");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        String scriptCode = scriptTag.getScriptCode();
        assertStringEquals("Expected Code",javascript,scriptCode);
    }


    public void testScanScriptWithTags() throws ParserException {
        String javascript = "\nAnything inside the script tag should be unchanged, even <li> and other html tags\n";
        createParser("<script>"+ javascript + "</script>");
        parseAndAssertNodeCount(1);
        assertTrue("Node should be a script tag",node[0] instanceof ScriptTag);
        ScriptTag scriptTag = (ScriptTag)node[0];
        String scriptCode = scriptTag.getScriptCode();
        assertStringEquals("Expected Code",javascript,scriptCode);
    }

    /**
     * See bug #839264 toHtml() parse error in Javascripts with "form" keyword
     * Contributed by Ivan Wang (xj92wang)
     */
    public void testScriptsWithForm ()
        throws
            ParserException
    {
        String teststring = "<SCRIPT LANGUAGE=\"JAVASCRIPT\">" +
            "function valForm(frm) { " + 
            " for (n=0; n<this.form.test; n++) this.form.nb++; "+
            "}"+
            "</SCRIPT>";
        StringBuffer htmlBuffer = new StringBuffer ();

        createParser (teststring);
        for (NodeIterator i = parser.elements (); i.hasMoreNodes ();)
        {
            Node tnode = i.nextNode ();
            htmlBuffer.append (tnode.toHtml ());
        }
        assertStringEquals ("bad html", teststring, htmlBuffer.toString ());
    }

    /**
     * See bug #902121 StringBean throws NullPointerException
     * Contributed by Reza Motori (rezamotori)
     */
    public void testDecodeScript ()
        throws ParserException
    {
        String plaintext =
            "<HTML>\n" +
            "<HEAD>\n" +
            "<TITLE>Script Encoder Sample Page</TITLE>\n" +
            "<SCRIPT LANGUAGE=\"JScript.Encode\">\n" +
            "<!--//\n" +
            "//Copyright© 1998 Microsoft Corporation. All Rights Reserved.\n" +
            "//**Start Encode**\r\n" +
            "function verifyCorrectBrowser(){\r\n" +
            "  if(navigator.appName == \"Microsoft Internet Explorer\")\r\n" +
            "    if (navigator.appVersion.indexOf (\"5.\") >= 0)\r\n" +
            "      return(true);\r\n" +
            "    else\r\n" +
            "      return(false);\r\n" +
            "}\r\n" +
            "function getAppropriatePage(){\r\n" +
            "  var str1 = \"Had this been an actual Web site, a page compatible with \";\r\n" +
            "  var str2 = \"browsers other than \";\r\n" +
            "  var str3 = \"Microsoft Internet Explorer 5.0 \";\r\n" +
            "  var str4 = \"would have been loaded.\";\r\n" +
            "  if (verifyCorrectBrowser())\r\n" +
            "    document.write(str1 + str3 + str4);\r\n" +
            "  else\r\n" +
            "    document.write(str1 + str2 + str3 + str4);\r\n" +
            "}\r\n" +
            "//-->\r\n" +
            "</SCRIPT>\n" +
            "</HEAD>\n" +
            "<BODY onload=\"getAppropriatePage()\">\n" +
            "</BODY>\n" +
            "</HTML>";
        String cryptext =
            "<HTML>\n" +
            "<HEAD>\n" +
            "<TITLE>Script Encoder Sample Page</TITLE>\n" +
            "<SCRIPT LANGUAGE=\"JScript.Encode\">\n" +
            "<!--//\n" +
            "//Copyright© 1998 Microsoft Corporation. All Rights Reserved.\n" +
            "//**Start Encode**#@~^ZwIAAA==@#@&0;	mDkW	P7nDb0zZKD.n1YAMGhk+Dvb`@#@&P,kW`UC7kLlDGDcl22gl:n~{'~Jtr1DGkW6YP&xDnD	+OPA62sKD+ME#@#@&P,~~k6PvxC\\rLmYGDcCwa.n.kkWU bx[+X66Pcr*cJ#,@*{~!*@#@&P,P~~,D+D;D	`YM;n#p@#@&P~P~n^/n@#@&~P,P~~M+Y;.	`Wl^d#I@#@&)@#@&6E	^YbWUPT+O)awDK2DblYKCo`*	@#@&~~7l.PkOD8Px~rCl[~Dtr/,8+U,l	Pl1Y!CV,n4,/rO~Pm~wmo+,^G:alDk8Vn~SkOt,Ei@#@&~~7lD~dDD+P{~r4.Khk+DkPKOtD~Y4lU~ri@#@&~P7lD,dOD2P{PEHr^MWdW6OP&xOnMx+O~A62VK.D~lRZPJp@#@&~P7l.PkY.*,'PrAW!VN,4C\\P(+nx~sKl[+9 Jp@#@&~,k0~c7+.k6z;W.M+1YAMWSd+M`b#@#@&~~,PNK^Es+xD ADbY`dY.q,_~/D.&,_~dDDcbI@#@&~Psk+@#@&P,PP9W1;:xORSDrO`/D.F,_PkO. ,_,/ODf~3PdYM*#p@#@&N@#@&z&R @*@#@&qrIAAA==^#~@</SCRIPT>\n" +
            "</HEAD>\n" +
            "<BODY onload=\"getAppropriatePage()\">\n" +
            "</BODY>\n" +
            "</HTML>";
        Lexer lexer;
        
        lexer = new Lexer (cryptext);
        ScriptDecoder.LAST_STATE = ScriptDecoder.STATE_INITIAL; // read everything
        try
        {
            String result = ScriptDecoder.Decode (lexer.getPage (), lexer.getCursor ());
            assertStringEquals ("decoding failed", plaintext, result);
        }
        finally
        {
            ScriptDecoder.LAST_STATE = ScriptDecoder.STATE_DONE;
        }
    }
    
    /**
     * See bug #902121 StringBean throws NullPointerException
     * Contributed by Reza Motori (rezamotori)
     */
    public void testDecodePage ()
        throws ParserException
    {
        String url = "http://htmlparser.sourceforge.net/test/EncryptedScriptExample.html";
        String plaintext =
            "\r\n" +
            "var nows = new Date();\r\n" +
            "var nIndexs = nows.getTime();\r\n" +
            "document.write(\"<img src=\\\"http://www.parsads.com/adserve/scriptinject.asp?F=4&Z=3,4,5,10,12&N=1&U=644&O=&nocache=\"  + nIndexs + \"\\\" width=\\\"1\\\" hight=\\\"1\\\"><img src=\\\"http://www.parsads.com/adserve/scriptinject.asp?F=4&Z=3,4,5,10,12&N=1&U=643&O=&nocache=\"  + nIndexs + \"\\\" width=\\\"1\\\" hight=\\\"1\\\"><img src=\\\"http://www.parsads.com/adserve/scriptinject.asp?F=4&Z=3,4,5,10,12&N=1&U=324&O=&nocache=\"  + nIndexs + \"\\\" width=\\\"1\\\" hight=\\\"1\\\">\");\r\n";
        
        parser = new Parser (url);
        NodeList scripts = parser.extractAllNodesThatMatch (new TagNameFilter ("SCRIPT"));
        assertEquals ("wrong number of scripts found", 2, scripts.size ());
        ScriptTag script = (ScriptTag)scripts.elementAt (1);
        assertStringEquals ("script not decoded correctly", plaintext, script.getScriptCode ());
    }
}
