// HTMLParser Library $Name:  $ - A java-based parser for HTML
// http://sourceforge.org/projects/htmlparser
// Copyright (C) 2004 Somik Raha
//
// Revision Control Information
//
// $Source: /afs/csail/group/pag/projects/javari/.CVS/javarifier/tests/htmlparser/org/htmlparser/tests/AllTests.java,v $
// $Author: jaimeq $
// $Date: 2008-05-25 04:57:11 $
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

package org.htmlparser.tests;

import junit.framework.TestSuite;

import org.htmlparser.tests.ParserTestCase;

public class AllTests extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.AllTests", "AllTests");
    }

    public AllTests(String name) {
        super(name);
    }

    public static TestSuite suite()
    {
        TestSuite suite;
        TestSuite sub;
        
        suite = new TestSuite ("HTMLParser Tests");
        sub = new TestSuite ("Basic Tests");
        sub.addTestSuite (ParserTest.class);
        sub.addTestSuite (AssertXmlEqualsTest.class);
        sub.addTestSuite (FunctionalTests.class);
        sub.addTestSuite (LineNumberAssignedByNodeReaderTest.class);
        sub.addTestSuite (MemoryTest.class);
        suite.addTest (sub);
        suite.addTest (org.htmlparser.tests.lexerTests.AllTests.suite ());
        suite.addTest (org.htmlparser.tests.scannersTests.AllTests.suite ());
        suite.addTest (org.htmlparser.tests.utilTests.AllTests.suite ());
        suite.addTest (org.htmlparser.tests.tagTests.AllTests.suite ());
        suite.addTest (org.htmlparser.tests.visitorsTests.AllTests.suite ());
        suite.addTest (org.htmlparser.tests.parserHelperTests.AllTests.suite ());
        suite.addTest (org.htmlparser.tests.nodeDecoratorTests.AllTests.suite ());
        suite.addTestSuite (org.htmlparser.tests.filterTests.FilterTest.class);

        return (suite);
    }
}

