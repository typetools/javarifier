// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/scannersTests/TagScannerTest.java,v $
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

import org.htmlparser.Tag;
import org.htmlparser.tests.ParserTestCase;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserUtils;

public class TagScannerTest extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.scannersTests.TagScannerTest", "TagScannerTest");
    }

    public TagScannerTest(String name) {
        super(name);
    }

    public void testTagExtraction() throws ParserException
    {
        String testHTML = "<AREA \n coords=0,0,52,52 href=\"http://www.yahoo.com/r/c1\" shape=RECT>";
        createParser(testHTML);
        Tag tag = (Tag)parser.elements ().nextNode ();
        assertNotNull(tag);
    }

    public void testRemoveChars() {
        String test = "hello\nworld\n\tqsdsds";
        String result = ParserUtils.removeChars(test,'\n');
        assertEquals("Removing Chars","helloworld\tqsdsds",result);
    }
}
