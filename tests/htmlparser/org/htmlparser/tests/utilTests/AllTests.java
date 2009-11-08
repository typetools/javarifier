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

package org.htmlparser.tests.utilTests;


import junit.framework.TestSuite;

import org.htmlparser.tests.ParserTestCase;

/**
 * Insert the type's description here.
 * Creation date: (6/17/2001 6:07:04 PM)
 */
public class AllTests extends ParserTestCase
{
    static
    {
        System.setProperty ("org.htmlparser.tests.utilTests.AllTests", "AllTests");
    }

    /**
     * AllTests constructor comment.
     * @param name java.lang.String
     */
    public AllTests(String name) {
        super(name);
    }

    /**
     * Insert the method's description here.
     * Creation date: (6/17/2001 6:07:15 PM)
     * @return junit.framework.TestSuite
     */
    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite("Utility Tests");

        suite.addTestSuite(BeanTest.class);
        suite.addTestSuite(CharacterTranslationTest.class);
        suite.addTestSuite(HTMLParserUtilsTest.class);
        suite.addTestSuite(NodeListTest.class);
        suite.addTestSuite(NonEnglishTest.class);
        suite.addTestSuite(SortTest.class);

        return suite;
    }
}
