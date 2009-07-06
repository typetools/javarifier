// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/scannersTests/XmlEndTagScanningTest.java,v $
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

import org.htmlparser.tags.Div;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;

public class XmlEndTagScanningTest extends ParserTestCase{

    static
    {
        System.setProperty ("org.htmlparser.tests.scannersTests.XmlEndTagScanningTest", "XmlEndTagScanningTest");
    }

    public XmlEndTagScanningTest(String name) {
        super(name);
    }

    public void testSingleTagParsing() throws ParserException {
        createParser("<div style=\"page-break-before: always; \" />");
        parseAndAssertNodeCount(1);
        assertType("div tag",Div.class,node[0]);
        Div div = (Div)node[0];
        assertStringEquals(
            "style",
            "page-break-before: always; ",
            div.getAttribute("style")
        );
    }

}
