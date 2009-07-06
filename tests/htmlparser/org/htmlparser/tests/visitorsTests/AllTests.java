// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/visitorsTests/AllTests.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:33 $
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

package org.htmlparser.tests.visitorsTests;

import junit.framework.TestSuite;
import org.htmlparser.tests.ParserTestCase;

public class AllTests extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.visitorsTests.AllTests", "AllTests");
    }

    public AllTests(String name) {
        super(name);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Visitor Tests");

        suite.addTestSuite(CompositeTagFindingVisitorTest.class);
        suite.addTestSuite(HtmlPageTest.class);
        suite.addTestSuite(LinkFindingVisitorTest.class);
        suite.addTestSuite(NodeVisitorTest.class);
        suite.addTestSuite(StringFindingVisitorTest.class);
        suite.addTestSuite(TagFindingVisitorTest.class);
        suite.addTestSuite(TextExtractingVisitorTest.class);
        suite.addTestSuite(UrlModifyingVisitorTest.class);
        suite.addTestSuite(ScriptCommentTest.class);

        return suite;
    }
}
